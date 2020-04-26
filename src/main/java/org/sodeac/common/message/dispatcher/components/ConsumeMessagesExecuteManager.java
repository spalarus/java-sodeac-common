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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.osgi.service.component.annotations.Component;
import org.sodeac.common.message.dispatcher.api.ComponentBindingSetup;
import org.sodeac.common.message.dispatcher.api.IDispatcherChannel;
import org.sodeac.common.message.dispatcher.api.IMessage;
import org.sodeac.common.message.dispatcher.api.IOnChannelAttach;
import org.sodeac.common.message.dispatcher.api.IOnChannelSignal;
import org.sodeac.common.message.dispatcher.api.IOnMessageRemove;
import org.sodeac.common.message.dispatcher.api.IOnMessageStore;
import org.sodeac.common.message.dispatcher.api.IDispatcherChannelSystemManager;
import org.sodeac.common.xuri.ldapfilter.IFilterItem;
import org.sodeac.common.xuri.ldapfilter.LDAPFilterBuilder;

@Component(service=IDispatcherChannelSystemManager.class)
public class ConsumeMessagesExecuteManager implements IDispatcherChannelSystemManager, IOnChannelAttach<Object>, IOnMessageStore<Object>, IOnMessageRemove<Object>, IOnChannelSignal<Object>
{
	public static final IFilterItem MATCH_FILTER = LDAPFilterBuilder.andLinker().criteriaWithName(ConsumeMessagesExecuteManager.class.getCanonicalName()).eq(Boolean.TRUE.toString()).build();
	public static final String MATCH_NAME = "Consume Messages Execute Manager";
			
	@Override
	public void configure(IChannelManagerPolicy componentBindingPolicy)
	{
		componentBindingPolicy.addConfigurationDetail(new ComponentBindingSetup.BoundedByChannelConfiguration(MATCH_FILTER).setName(MATCH_NAME));
	}
	
	@Override
	public void onChannelAttach(IDispatcherChannel<Object> channel)
	{
		channel.getStateAdapter(ConsumeMessagesExecuteManagerAdapter.class, () -> new ConsumeMessagesExecuteManagerAdapter());
	}
	
	@Override
	public void onMessageRemove(IMessage<Object> message)
	{
		message.getChannel().getStateAdapter(ConsumeMessagesExecuteManagerAdapter.class, () -> new ConsumeMessagesExecuteManagerAdapter()).removeFromMonitoringPools(message);
	}

	@Override
	public void onMessageStore(IMessage<Object> message)
	{
		message.getChannel().getStateAdapter(ConsumeMessagesExecuteManagerAdapter.class, () -> new ConsumeMessagesExecuteManagerAdapter()).addToMonitoringPools(message);
	}
	
	@Override
	public void onChannelSignal(IDispatcherChannel<Object> channel, String signal)
	{
		// TODO Auto-generated method stub
	}

	protected static class ConsumeMessagesExecuteManagerAdapter
	{
		protected ConsumeMessagesExecuteManagerAdapter()
		{
			super();
			
			lock = new ReentrantReadWriteLock(true);
			this.readLock = this.lock.readLock();
			this.writeLock = this.lock.writeLock();
			this.plannerList = new ArrayList<ConsumeMessagesPlannerManager.ConsumeMessagesPlannerManagerAdapter>();
		}
		
		private ReentrantReadWriteLock lock = null;
		private ReentrantReadWriteLock.ReadLock readLock = null;
		private ReentrantReadWriteLock.WriteLock writeLock = null;
		private List<ConsumeMessagesPlannerManager.ConsumeMessagesPlannerManagerAdapter> plannerList = null;
		
		protected void addToMonitoringPools(IMessage<Object> message)
		{
			Lock lock = this.readLock;
			lock.lock();
			try
			{
				for(ConsumeMessagesPlannerManager.ConsumeMessagesPlannerManagerAdapter planner : plannerList)
				{
					planner.addToMonitoring(message);
				}
			}
			finally 
			{
				lock.unlock();
			}
		}
		
		protected void removeFromMonitoringPools(IMessage<Object> message)
		{
			Lock lock = this.readLock;
			lock.lock();
			try
			{
				for(ConsumeMessagesPlannerManager.ConsumeMessagesPlannerManagerAdapter planner : plannerList)
				{
					planner.removeFromMonitoring(message);
				}
			}
			finally 
			{
				lock.unlock();
			}
		}
		
		protected void removeRemovedMessagesFromMonitoringPools()
		{
			Lock lock = this.readLock;
			lock.lock();
			try
			{
				for(ConsumeMessagesPlannerManager.ConsumeMessagesPlannerManagerAdapter planner : plannerList)
				{
					planner.removeRemovedMessages();
				}
			}
			finally 
			{
				lock.unlock();
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
				LinkedList<Integer> removeIndex = new LinkedList<>();
				int index = 0;
				for(ConsumeMessagesPlannerManager.ConsumeMessagesPlannerManagerAdapter check : plannerList)
				{
					if(planner == check)
					{
						removeIndex.addFirst(index);
					}
					index++;
				}
				for(int i : removeIndex)
				{
					plannerList.remove(i);
				}
				removeIndex.clear();
			}
			finally 
			{
				lock.unlock();
			}
		}
	}
}
