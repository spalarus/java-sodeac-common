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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.sodeac.common.message.dispatcher.api.ComponentBindingSetup;
import org.sodeac.common.message.dispatcher.api.IDispatcherChannel;
import org.sodeac.common.message.dispatcher.api.IMessage;
import org.sodeac.common.message.dispatcher.api.IOnChannelAttach;
import org.sodeac.common.message.dispatcher.api.IOnChannelSignal;
import org.sodeac.common.message.dispatcher.api.IOnMessageRemoveSnapshot;
import org.sodeac.common.message.dispatcher.api.IOnMessageStoreSnapshot;
import org.sodeac.common.message.dispatcher.api.IOnTaskTimeout;
import org.sodeac.common.message.dispatcher.components.ConsumeMessagesPlannerManager.ConsumeMessagesPlannerManagerAdapter.ConsumableState;
import org.sodeac.common.message.dispatcher.setup.MessageConsumerFeature.ConsumerRule;
import org.sodeac.common.message.dispatcher.setup.MessageConsumerFeature.SpecialErrorHandlerDefinition;
import org.sodeac.common.message.dispatcher.setup.MessageDispatcherChannelSetup.MessageConsumeHelper;
import org.sodeac.common.misc.OSGiDriverRegistry;
import org.sodeac.common.misc.RuntimeWrappedException;
import org.sodeac.common.snapdeque.DequeSnapshot;
import org.sodeac.common.message.dispatcher.api.IDispatcherChannelSystemManager;
import org.sodeac.common.message.dispatcher.api.IDispatcherChannelSystemService;
import org.sodeac.common.message.dispatcher.api.IDispatcherChannelTask;
import org.sodeac.common.message.dispatcher.api.IDispatcherChannelTaskContext;
import org.sodeac.common.xuri.ldapfilter.IFilterItem;
import org.sodeac.common.xuri.ldapfilter.FilterBuilder;

@Component(service={IDispatcherChannelSystemManager.class,IDispatcherChannelSystemService.class}, property= {"type=consume-messages","role=consumer"})
public class ConsumeMessagesConsumerManager implements IDispatcherChannelSystemManager, IOnChannelAttach<Object>, IOnTaskTimeout<Object>, IOnMessageStoreSnapshot<Object>, IOnMessageRemoveSnapshot<Object>, IOnChannelSignal<Object>, IDispatcherChannelSystemService<Object>
{
	@Reference(cardinality=ReferenceCardinality.MANDATORY,policy=ReferencePolicy.STATIC)
	protected volatile OSGiDriverRegistry internalBootstrapDep;
	
	public static final IFilterItem MATCH_FILTER = FilterBuilder.andLinker().criteriaWithName(ConsumeMessagesConsumerManager.class.getCanonicalName()).eq(Boolean.TRUE.toString()).build();
	public static final String MATCH_NAME = "Consume Messages Consumer Manager";
	public static final String SERVICE_NAME = "Consume Messages Consumer Service";
	public static final String SERVICE_ID = ConsumeMessagesConsumerManager.class.getCanonicalName() + ".Service";
	public static final String SIGNAL_CONSUME = ConsumeMessagesConsumerManager.class.getCanonicalName() + ".Signal.Consume";
			
	@Override
	public void configureChannelManagerPolicy(IChannelManagerPolicy componentBindingPolicy)
	{
		componentBindingPolicy.addConfigurationDetail(new ComponentBindingSetup.BoundedByChannelConfiguration(MATCH_FILTER).setName(MATCH_NAME));
	}
	
	@Override
	public void configureChannelServicePolicy(IChannelServicePolicy componentBindingPolicy)
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
		channel.getStateAdapter(ConsumeMessagesConsumerManagerAdapter.class, () -> new ConsumeMessagesConsumerManagerAdapter(channel));
	}
	
	@Override
	public void run(IDispatcherChannelTaskContext<Object> taskContext) throws Exception
	{
		ConsumableState consumableState = taskContext.getChannel().getStateAdapter(ConsumableState.class);
		if(consumableState == null)
		{
			return;
		}
		
		taskContext.setTaskState(consumableState);
		
		long now = System.currentTimeMillis();
		
		try
		{
			
			MessageConsumeHelperImpl messageConsumeHelper = new MessageConsumeHelperImpl();
			messageConsumeHelper.firstMessage = false;
			messageConsumeHelper.lastMessage = false;
			messageConsumeHelper.messageList = consumableState.getConsumableList();
			messageConsumeHelper.message = null;
			messageConsumeHelper.channel = taskContext.getChannel();
			messageConsumeHelper.helper = null;
			messageConsumeHelper.taskContext = taskContext;
			consumableState.setMessageConsumeHelperImpl(messageConsumeHelper);
			
			if(messageConsumeHelper.messageList.isEmpty())
			{
				try
				{
					taskContext.heartbeat();
					consumableState.getConsumerRule().getMessageConsumer().accept(null, messageConsumeHelper);
				}
				catch (Exception e) 
				{
					try
					{
						handleError(e, consumableState.getConsumerRule(), messageConsumeHelper);
					}
					catch (Exception e2) {}
					catch (Error e2) {}
				}
				catch (Error e) 
				{
					try
					{
						handleError(e, consumableState.getConsumerRule(), messageConsumeHelper);
					}
					catch (Exception e2) {}
					catch (Error e2) {}
				}
				
				if(taskContext.getTaskControl().isInTimeout())
				{
					return;
				}
			}
			else
			{
				int index = 0;
				int lastIndex = messageConsumeHelper.messageList.size() -1;
				
				for(IMessage<Object> message : messageConsumeHelper.messageList)
				{
					
					messageConsumeHelper.firstMessage = index == 0;
					messageConsumeHelper.lastMessage = index == lastIndex;
					messageConsumeHelper.message = message;
					
					try
					{
						taskContext.heartbeat();
						
						try
						{
							consumableState.getConsumerRule().getMessageConsumer().accept(message, messageConsumeHelper);
						}
						finally 
						{
							if(! consumableState.getConsumerRule().isKeepMessages())
							{
								message.removeFromChannel();
							}
						}
					}
					catch (Exception e) 
					{
						try
						{
							handleError(e, consumableState.getConsumerRule(), messageConsumeHelper);
						}
						catch (Exception e2) {}
						catch (Error e2) {}
					}
					catch (Error e) 
					{
						try
						{
							handleError(e, consumableState.getConsumerRule(), messageConsumeHelper);
						}
						catch (Exception e2) {}
						catch (Error e2) {}
					}
					
					index++;
					
					if(taskContext.getTaskControl().isInTimeout())
					{
						return;
					}
				}
			}
		}
		finally 
		{
			if(! taskContext.getTaskControl().isInTimeout())
			{
				ConsumeMessagesConsumerManagerAdapter consumerAdapter = taskContext.getChannel().getStateAdapter(ConsumeMessagesConsumerManagerAdapter.class);
				if(consumerAdapter != null)
				{
					consumerAdapter.setConsumeTimestamp(now, consumableState.getConsumerRule().getGroupMembers());
					consumerAdapter.removeRemovedMessagesFromMonitoringPools(); // reschedule planner && signal if necessary 
				}
				
				taskContext.getChannel().removeStateAdapter(ConsumableState.class);
				
				consumableState.dispose();
				
			}
		}
		
	}
	
	private static void handleError(Throwable throwable,ConsumerRule consumerRule, MessageConsumeHelperImpl messageConsumeHelper)
	{
		if(throwable instanceof RuntimeWrappedException)
		{
			throwable = ((RuntimeWrappedException)throwable).getCause();
		}
		
		for(SpecialErrorHandlerDefinition specialErrorHandlerDefinition : consumerRule.getSpecialErrorHandler())
		{
			if(throwable.getClass() == specialErrorHandlerDefinition.getType() && (specialErrorHandlerDefinition.getHandler() != null))
			{
				specialErrorHandlerDefinition.getHandler().accept(throwable, messageConsumeHelper);
				return;
			}
		}
		if(consumerRule.getDefaultErrorHandler() != null)
		{
			consumerRule.getDefaultErrorHandler().accept(throwable, messageConsumeHelper);
		}
	}
	
	@Override
	public void onTaskTimeout(IDispatcherChannel<Object> channel, IDispatcherChannelTask<Object> task, Object taskState, Runnable interrupter)
	{
		ConsumableState consumableState = (ConsumableState)taskState;
		if(consumableState == null)
		{
			return;
		}
		
		if(consumableState.getConsumerRule().getTimeOutHandler() != null)
		{
			try
			{
				consumableState.getConsumerRule().getTimeOutHandler().accept(consumableState
						.getMessageConsumeHelperImpl()
						.getMessage(), consumableState
						.getMessageConsumeHelperImpl());
			}
			catch (Exception e) {e.printStackTrace();}
			catch (Error e) {}
		}
		
		ConsumeMessagesConsumerManagerAdapter consumerAdapter = channel.getStateAdapter(ConsumeMessagesConsumerManagerAdapter.class);
		if(consumerAdapter != null)
		{
			consumerAdapter.setConsumeTimestamp(System.currentTimeMillis(), consumableState.getConsumerRule().getGroupMembers());
			consumerAdapter.removeRemovedMessagesFromMonitoringPools(); // reschedule planner && signal if necessary 
		}
		consumableState.dispose();
	}
	
	@Override
	public void onMessageRemoveSnapshot(DequeSnapshot<IMessage<Object>> messageRemoveSnapshot)
	{
		ConsumeMessagesConsumerManagerAdapter consumerAdapter = messageRemoveSnapshot.getFirstElement().getChannel().getStateAdapter(ConsumeMessagesConsumerManagerAdapter.class);
		if(consumerAdapter == null)
		{
			return;
		}
		consumerAdapter.removeRemovedMessagesFromMonitoringPools();
	}

	@Override
	public void onMessageStoreSnapshot(DequeSnapshot<IMessage<Object>> messageStoreSnapshot)
	{
		ConsumeMessagesConsumerManagerAdapter consumerAdapter = messageStoreSnapshot.getFirstElement().getChannel().getStateAdapter(ConsumeMessagesConsumerManagerAdapter.class);
		if(consumerAdapter == null)
		{
			return;
		}
		consumerAdapter.addMessagesToMonitoringPools(messageStoreSnapshot);
	}

	@Override
	public void onChannelSignal(IDispatcherChannel<Object> channel, String signal)
	{
		if(SIGNAL_CONSUME.equals(signal))
		{
			channel.removeStateAdapter(ConsumableState.class);
			
			ConsumeMessagesConsumerManagerAdapter consumerAdapter = channel.getStateAdapter(ConsumeMessagesConsumerManagerAdapter.class);
			if(consumerAdapter == null)
			{
				return;
			}
			
			ConsumableState consumableState = consumerAdapter.getConsumableState();
			if(consumableState == null)
			{
				return;
			}
			channel.setStateAdapter(ConsumableState.class,consumableState);
			if(consumableState.getConsumerRule().getTimeOut() < 1)
			{
				channel.rescheduleTask(SERVICE_ID, System.currentTimeMillis(), -1L, -1L);
			}
			else
			{
				
				long heartbeatTimeout = consumableState.getConsumerRule().getTimeOutUnit().toMillis(consumableState.getConsumerRule().getTimeOut());
				channel.rescheduleTask(SERVICE_ID, System.currentTimeMillis(), -1L, heartbeatTimeout);
			}
		}
	}

	protected static class ConsumeMessagesConsumerManagerAdapter
	{
		protected ConsumeMessagesConsumerManagerAdapter(IDispatcherChannel<Object> channel)
		{
			super();
			
			lock = new ReentrantReadWriteLock(true);
			this.readLock = this.lock.readLock();
			this.writeLock = this.lock.writeLock();
			this.plannerList = new LinkedList<ConsumeMessagesPlannerManager.ConsumeMessagesPlannerManagerAdapter>();
			this.channel = channel;
		}
		
		private ReentrantReadWriteLock lock = null;
		private ReentrantReadWriteLock.ReadLock readLock = null;
		private ReentrantReadWriteLock.WriteLock writeLock = null;
		private LinkedList<ConsumeMessagesPlannerManager.ConsumeMessagesPlannerManagerAdapter> plannerList = null;
		private IDispatcherChannel<Object> channel = null;
		
		protected ConsumableState getConsumableState()
		{
			Lock lock = this.readLock;
			lock.lock();
			try
			{
				for(ConsumeMessagesPlannerManager.ConsumeMessagesPlannerManagerAdapter planner : plannerList)
				{
					ConsumableState consumableState = planner.getConsumableState(true);
					if(consumableState == null)
					{
						continue;
					}
					if(! consumableState.isConsumable())
					{
						continue;
					}
					return consumableState;
				}
			}
			finally 
			{
				lock.unlock();
			}
			return null;
		}
		
		protected void reschedulePlanner()
		{
			Lock lock = this.readLock;
			lock.lock();
			try
			{
				for(ConsumeMessagesPlannerManager.ConsumeMessagesPlannerManagerAdapter planner : plannerList)
				{
					planner.checkConsumeOrReschedule();
				}
			}
			finally 
			{
				lock.unlock();
			}
		}
		
		protected void setConsumeTimestamp(long timestamp, Set<String> members)
		{
			if(members == null)
			{
				return;
			}
			Lock lock = this.readLock;
			lock.lock();
			try
			{
				for(ConsumeMessagesPlannerManager.ConsumeMessagesPlannerManagerAdapter planner : plannerList)
				{
					planner.setConsumeTimestamp(timestamp,members);
				}
			}
			finally 
			{
				lock.unlock();
			}
		}
		
		protected void addMessagesToMonitoringPools(DequeSnapshot<IMessage<Object>> snapshot)
		{
			boolean signal = false;
			Lock lock = this.readLock;
			lock.lock();
			try
			{
				for(ConsumeMessagesPlannerManager.ConsumeMessagesPlannerManagerAdapter planner : plannerList)
				{
					planner.addMessagesToMonitoring(snapshot);
					
					if(planner.checkConsumeOrReschedule())
					{
						if(! signal)
						{
							ConsumableState  consumableState = planner.getConsumableState(false);
							if((consumableState != null) && consumableState.isConsumable())
							{
								signal = true;
							}
						}
					}
				}
			}
			finally 
			{
				lock.unlock();
			}
			
			if(signal)
			{
				channel.signal(SIGNAL_CONSUME);
			}
		}
		
		protected void removeRemovedMessagesFromMonitoringPools()
		{
			boolean signal = false;
			Lock lock = this.readLock;
			lock.lock();
			try
			{
				for(ConsumeMessagesPlannerManager.ConsumeMessagesPlannerManagerAdapter planner : plannerList)
				{
					planner.removeRemovedMessages();
					
					if(planner.checkConsumeOrReschedule())
					{
						if(! signal)
						{
							ConsumableState  consumableState = planner.getConsumableState(false);
							if((consumableState != null) && consumableState.isConsumable())
							{
								signal = true;
							}
						}
					}
				}
			}
			finally 
			{
				lock.unlock();
			}
			
			if(signal)
			{
				channel.signal(SIGNAL_CONSUME);
			}
		}
		
		protected void addPlanner(ConsumeMessagesPlannerManager.ConsumeMessagesPlannerManagerAdapter planner)
		{
			Lock lock = this.writeLock;
			lock.lock();
			try
			{
				for(ConsumeMessagesPlannerManager.ConsumeMessagesPlannerManagerAdapter check : plannerList)
				{
					if(planner == check)
					{
						return;
					}
				}
				plannerList.add(planner);
			}
			finally 
			{
				lock.unlock();
			}
		}
		
		protected void removePlanner(ConsumeMessagesPlannerManager.ConsumeMessagesPlannerManagerAdapter planner)
		{
			Lock lock = this.writeLock;
			lock.lock();
			try
			{
				ListIterator<ConsumeMessagesPlannerManager.ConsumeMessagesPlannerManagerAdapter> itr = plannerList.listIterator();
				while(itr.hasNext())
				{
					if(planner == itr.next())
					{
						itr.remove();
					}
				}
			}
			finally 
			{
				lock.unlock();
			}
		}
	}
	
	public class MessageConsumeHelperImpl implements MessageConsumeHelper<Object, Object>
	{
		private boolean firstMessage = false;
		private boolean lastMessage = false;
		private List<IMessage<Object>> messageList = null;
		private IMessage<Object> message = null;
		private IDispatcherChannel<Object> channel = null;
		private Object helper = null;
		private IDispatcherChannelTaskContext<Object> taskContext = null;
		
		@Override
		public boolean isFirstMessage()
		{
			return this.firstMessage;
		}

		@Override
		public boolean isLastMessage()
		{
			return this.lastMessage;
		}

		@Override
		public int messageSize()
		{
			return this.messageList.size();
		}

		@Override
		public Collection<IMessage<Object>> getAllMessages()
		{
			return this.messageList;
		}

		@Override
		public IMessage<Object> getMessage()
		{
			return this.message;
		}

		@Override
		public IDispatcherChannel<Object> getChannel()
		{
			return this.channel;
		}

		@Override
		public Object getHelper(Supplier<Object> supplierIfNotExist)
		{
			if(this.helper == null)
			{
				if(supplierIfNotExist != null)
				{
					this.helper = supplierIfNotExist.get();
				}
			}
			return this.helper;
		}

		@Override
		public void heartbeat()
		{
			try
			{
				this.taskContext.heartbeat();
			}
			catch (Exception e) {}
			catch (Error e) {}
		}

		@Override
		public boolean isInTimeout()
		{
			return taskContext.getTaskControl().isInTimeout();
		}
		
	}
}
