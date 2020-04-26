/*******************************************************************************
 * Copyright (c) 2020 Sebastian Palarus
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Sebastian Palarus - initial API and implementation
 *******************************************************************************/
package org.sodeac.common.message.dispatcher.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.osgi.service.component.annotations.Component;
import org.sodeac.common.message.dispatcher.api.ComponentBindingSetup;
import org.sodeac.common.message.dispatcher.api.IDispatcherChannel;
import org.sodeac.common.message.dispatcher.api.IDispatcherChannelComponent;
import org.sodeac.common.message.dispatcher.api.IMessage;
import org.sodeac.common.message.dispatcher.api.IOnChannelAttach;
import org.sodeac.common.message.dispatcher.api.IOnChannelDetach;
import org.sodeac.common.message.dispatcher.components.ConsumeMessagesExecuteManager.ConsumeMessagesExecuteManagerAdapter;
import org.sodeac.common.message.dispatcher.setup.MessageConsumerFeature;
import org.sodeac.common.message.dispatcher.setup.MessageConsumerFeature.ConsumerRule;
import org.sodeac.common.message.dispatcher.setup.MessageConsumerFeature.ConsumerRule.TriggerByMessageAgeMode;
import org.sodeac.common.snapdeque.DequeSnapshot;
import org.sodeac.common.message.dispatcher.api.IDispatcherChannelSystemManager;
import org.sodeac.common.message.dispatcher.api.IDispatcherChannelSystemService;
import org.sodeac.common.message.dispatcher.api.IDispatcherChannelTaskContext;
import org.sodeac.common.xuri.ldapfilter.IFilterItem;

@Component(service= {IDispatcherChannelSystemManager.class,IDispatcherChannelSystemService.class})
public class ConsumeMessagesPlannerManager implements IDispatcherChannelSystemManager,IOnChannelAttach<Object>, IOnChannelDetach<Object>, IDispatcherChannelSystemService<Object>
{
	public static final IFilterItem MATCH_FILTER = IDispatcherChannelComponent.getAdapterMatchFilter(MessageConsumerFeature.MessageConsumerFeatureConfiguration.class);
	
	public static final String MANAGER_NAME = "Consume Messages Planner Manager";
	public static final String SERVICE_NAME = "Consume Messages Planner Service";
	public static final String SERVICE_ID = ConsumeMessagesPlannerManager.class.getCanonicalName() + ".Service";
			
	@Override
	public void configure(IChannelManagerPolicy componentBindingPolicy)
	{
		componentBindingPolicy
			.addConfigurationDetail(new ComponentBindingSetup.BoundedByChannelConfiguration(MATCH_FILTER).setName(MANAGER_NAME));
	}
	
	@Override
	public void configure(IChannelServicePolicy componentBindingPolicy)
	{
		componentBindingPolicy
			.addConfigurationDetail(new ComponentBindingSetup.BoundedByChannelConfiguration(MATCH_FILTER).setName(SERVICE_NAME))
			.addConfigurationDetail(new ComponentBindingSetup.ChannelServiceConfiguration(SERVICE_ID).setName(SERVICE_NAME)
				.setPeriodicRepetitionIntervalMS(77 * 1080)
				.setStartDelayInMS(77));
	}

	@Override
	public void onChannelAttach(IDispatcherChannel<Object> channel)
	{
		// activate Consume-Message-Execute manager in parent channel
		channel.getParentChannel().getConfigurationPropertyBlock().setProperty(ConsumeMessagesExecuteManager.class.getCanonicalName(), Boolean.TRUE.toString());
		
		// planner state adapter in current channel
		ConsumeMessagesPlannerManagerAdapter plannerAdapter = new ConsumeMessagesPlannerManagerAdapter
		(
			channel.getConfigurationAdapter(MessageConsumerFeature.MessageConsumerFeatureConfiguration.class),
			channel.getParentChannel(Object.class)
		);
		channel.setStateAdapter(ConsumeMessagesPlannerManagerAdapter.class, plannerAdapter);
		
		// execute state adapter in parent channel
		ConsumeMessagesExecuteManagerAdapter executeAdapter = channel.getParentChannel(Object.class).getStateAdapter
		(
			ConsumeMessagesExecuteManagerAdapter.class,
			() -> new ConsumeMessagesExecuteManagerAdapter()
		);
		executeAdapter.addPlanner(plannerAdapter);
		
		// monitor already existing messages
		
		try(DequeSnapshot<IMessage<Object>> messages = channel.getParentChannel(Object.class).getMessageSnapshot())
		{
			if(! messages.isEmpty())
			{
				plannerAdapter.addAllToMonitoring(messages);
			}
		}
	}

	@Override
	public void onChannelDetach(IDispatcherChannel<Object> channel)
	{
		ConsumeMessagesPlannerManagerAdapter plannerAdapter = channel.getStateAdapter(ConsumeMessagesPlannerManagerAdapter.class);
		if(plannerAdapter == null)
		{
			return;
		}
		ConsumeMessagesExecuteManagerAdapter executeAdapter = channel.getParentChannel(Object.class).getStateAdapter(ConsumeMessagesExecuteManagerAdapter.class);
		if(executeAdapter == null)
		{
			return;
		}
		executeAdapter.removePlanner(plannerAdapter);
		plannerAdapter.dispose();
		
	}
	

	@Override
	public void run(IDispatcherChannelTaskContext<Object> taskContext) throws Exception
	{
		
	}

	protected static class ConsumeMessagesPlannerManagerAdapter 
	{
		public ConsumeMessagesPlannerManagerAdapter(MessageConsumerFeature.MessageConsumerFeatureConfiguration configuration, IDispatcherChannel<Object> channel)
		{
			super();
			Objects.requireNonNull(configuration, "no configuratrion for message consumer feature");
			Objects.requireNonNull(channel, "no consumer channel");
			
			this.lock = new ReentrantReadWriteLock(true);
			this.readLock = this.lock.readLock();
			this.writeLock = this.lock.writeLock();
			this.monitoringPoolList = new ArrayList<>(configuration.getConsumerRuleList().size());
			
			for(ConsumerRule consumerRule : configuration.getConsumerRuleList())
			{
				this.monitoringPoolList.add(new MessageMonitoringPool(consumerRule, channel));
			}
		}
		
		private ReentrantReadWriteLock lock = null;
		private ReentrantReadWriteLock.ReadLock readLock = null;
		private ReentrantReadWriteLock.WriteLock writeLock = null;
		private List<MessageMonitoringPool> monitoringPoolList = null;
		
		protected void addToMonitoring(IMessage<Object> message)
		{
			Lock lock = this.writeLock;
			lock.lock();
			try
			{
				for(MessageMonitoringPool messageMonitoringPool : monitoringPoolList)
				{
					messageMonitoringPool.addToMonitoring(message);
				}
			}
			finally 
			{
				lock.unlock();
			}
		}
		
		protected void addAllToMonitoring(Collection<IMessage<Object>> messageList)
		{
			Lock lock = this.writeLock;
			lock.lock();
			try
			{
				for(MessageMonitoringPool messageMonitoringPool : monitoringPoolList)
				{
					messageList.forEach(m -> messageMonitoringPool.addToMonitoring(m));
				}
			}
			finally 
			{
				lock.unlock();
			}
		}
		
		protected void removeFromMonitoring(IMessage<Object> message)
		{
			Lock lock = this.writeLock;
			lock.lock();
			try
			{
				for(MessageMonitoringPool messageMonitoringPool : monitoringPoolList)
				{
					messageMonitoringPool.removeFromMonitoring(message);
				}
			}
			finally 
			{
				lock.unlock();
			}
		}
		
		protected void removeRemovedMessages()
		{
			Lock lock = this.writeLock;
			lock.lock();
			try
			{
				for(MessageMonitoringPool messageMonitoringPool : monitoringPoolList)
				{
					messageMonitoringPool.removeRemovedMessages();
				}
			}
			finally 
			{
				lock.unlock();
			}
		}
		
		protected class ConsumableState
		{
			private boolean consumable = false;
			private Long fatefulTime = null;
		}
		
		private class MessageMonitoringPool
		{
			public MessageMonitoringPool(ConsumerRule consumerRule, IDispatcherChannel<Object> channel)
			{
				super();
				this.messageBufferList = new LinkedList<>();
				this.messageMonitoringList = new LinkedList<>();
				this.consumerRule = consumerRule;
				this.channel = channel;
				
				if(this.consumerRule.getConsumeEventAgeTriggerAge() > -1)
				{
					this.consumeAgeSensible = true;
				}
				if(this.consumerRule.getMessageAgeTriggerMode() != TriggerByMessageAgeMode.NONE)
				{
					messageAgeSensible = true;
				}
				
			}
			
			private LinkedList<IMessage<Object>> messageBufferList = null;
			private LinkedList<IMessage<Object>> messageMonitoringList = null;
			private ConsumerRule consumerRule = null;
			private IDispatcherChannel<Object> channel = null;
			
			private boolean consumeAgeSensible = false;
			private boolean messageAgeSensible = false;
			private int validMessageSize = 0;
			
			private volatile long lastConsumeEvent = System.currentTimeMillis();
			
			private Boolean consumable = null;
			private Long fatefulTime = null;
			
			private void resetCache()
			{
				this.consumable = null;
				this.fatefulTime = null;
			}
			
			private void setConsumeTimestamp(long timestamp, Set<String> members)
			{
				if(! consumeAgeSensible)
				{
					return;
				}
				Objects.requireNonNull(members, "group members is null");
				if(! members.contains(this.consumerRule.getConsumeEventAgeTriggerGroup()))
				{
					return;
				}
				this.lastConsumeEvent = timestamp;
				resetCache();
			}
			
			private void addToMonitoring(IMessage<Object> message)
			{
				if(message == null)
				{
					return;
				}
				if(consumerRule.getPoolMaxSize() < 1)
				{
					return;
				}
				if(message.isRemoved())
				{
					return;
				}
				Long messageSequence = message.getSequence();
				if(messageSequence == null)
				{
					return;
				}
				if(this.channel != message.getChannel())
				{
					return;
				}
				Long newestSequence = null;
				if(! messageBufferList.isEmpty())
				{
					newestSequence =  messageBufferList.getFirst().getSequence();
				}
				else if(! messageMonitoringList.isEmpty())
				{
					newestSequence = messageMonitoringList.getFirst().getSequence();
				}
				
				if((newestSequence != null) && (newestSequence.longValue() < messageSequence.longValue()))
				{
					messageBufferList.addFirst(message);
				}
				else if(messageBufferList.isEmpty() && messageMonitoringList.isEmpty())
				{
					messageBufferList.addFirst(message);
				}
				else
				{
					int pos = -1;
					if(! messageBufferList.isEmpty())
					{
						int index = 0;
						for(IMessage<Object> check : messageBufferList)
						{
							if(check.getSequence().longValue() < message.getSequence())
							{
								pos = index;
							}
							index ++;
						}
					}
					
					if(pos > -1)
					{
						messageBufferList.add(pos, message);
					}
					else
					{
						int index = 0;
						for(IMessage<Object> check : messageMonitoringList)
						{
							if(check.getSequence().longValue() < message.getSequence())
							{
								pos = index;
							}
							index ++;
						}
						
						if(pos > -1)
						{
							messageMonitoringList.add(pos, message);
						}
						else
						{
							messageMonitoringList.add(message);
						}
					}
				}
				while(messageMonitoringList.size() > consumerRule.getPoolMaxSize())
				{
					messageBufferList.addLast(messageMonitoringList.removeFirst());
					this.resetCache();
				}
				while((messageBufferList.size() > 0) && (messageMonitoringList.size() < consumerRule.getPoolMaxSize()))
				{
					messageMonitoringList.addFirst(messageBufferList.removeLast());
				}
			}
			
			private void removeFromMonitoring(IMessage<Object> message)
			{
				if(message == null)
				{
					return;
				}
				
				if(consumerRule.getPoolMaxSize() < 1)
				{
					return;
				}
				
				LinkedList<Integer> toRemovePositions = new LinkedList<>();
				int index = 0;
				for(IMessage<Object> check : messageBufferList)
				{
					if(check == message)
					{
						toRemovePositions.addFirst(index);
					}
					index++;
				}
				for(int pos : toRemovePositions)
				{
					messageBufferList.remove(pos);
				}
				toRemovePositions.clear();
				
				index = 0;
				for(IMessage<Object> check : messageMonitoringList)
				{
					if(check == message)
					{
						toRemovePositions.addFirst(index);
					}
					index++;
				}
				for(int pos : toRemovePositions)
				{
					messageMonitoringList.remove(pos);
					this.resetCache();
				}
				toRemovePositions.clear();
			}
			
			private void removeRemovedMessages()
			{
				LinkedList<Integer> toRemovePositions = new LinkedList<>();
				int index = 0;
				for(IMessage<Object> check : messageBufferList)
				{
					if(check.isRemoved())
					{
						toRemovePositions.addFirst(index);
					}
					index++;
				}
				for(int pos : toRemovePositions)
				{
					messageBufferList.remove(pos);
				}
				toRemovePositions.clear();
				
				index = 0;
				for(IMessage<Object> check : messageMonitoringList)
				{
					if(check.isRemoved())
					{
						toRemovePositions.addFirst(index);
					}
					index++;
				}
				if(! toRemovePositions.isEmpty())
				{
					for(int pos : toRemovePositions)
					{
						messageMonitoringList.remove(pos);
					}
					toRemovePositions.clear();
					this.resetCache();
				}
			}
		}
		
		private void dispose()
		{
			// TODO
		}
	}
}
