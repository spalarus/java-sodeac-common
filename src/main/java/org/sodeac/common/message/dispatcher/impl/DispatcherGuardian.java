/*******************************************************************************
 * Copyright (c) 2017, 2020 Sebastian Palarus
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Sebastian Palarus - initial API and implementation
 *******************************************************************************/
package org.sodeac.common.message.dispatcher.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DispatcherGuardian extends Thread
{
	public static final long DEFAULT_WAIT_TIME = 108 * 108 * 13;
	
	protected DispatcherGuardian(MessageDispatcherImpl dispatcher)
	{
		super();
		this.eventDispatcher = eventDispatcher;
		this.taskTimeOutIndex = new HashMap<ChannelImpl<?>,TaskObservable>();
		
		this.taskTimeOutIndexLock = new ReentrantReadWriteLock(true);
		this.taskTimeOutIndexReadLock = this.taskTimeOutIndexLock.readLock();
		this.taskTimeOutIndexWriteLock = this.taskTimeOutIndexLock.writeLock();
		super.setDaemon(true);
		super.setName(DispatcherGuardian.class.getSimpleName() + " " + dispatcher.getId());
	}
	
	private volatile MessageDispatcherImpl eventDispatcher = null;
	private volatile boolean go = true;
	private volatile boolean isUpdateNotified = false;
	private volatile Object waitMonitor = new Object();
	private volatile Map<ChannelImpl<?>,TaskObservable> taskTimeOutIndex = null;
	private volatile ReentrantReadWriteLock taskTimeOutIndexLock;
	private volatile ReadLock taskTimeOutIndexReadLock;
	private volatile WriteLock taskTimeOutIndexWriteLock;
	
	private volatile long currentWait = -1;
	
	private Logger logger = LoggerFactory.getLogger(DispatcherGuardian.class);
	
	@Override
	public void run()
	{
		long nextTimeOutTimeStamp = -1;
		List<ChannelImpl<?>> timeOutList = null;
		List<TaskObservable> removeTaskObservableList = null;
		boolean inTimeOut = false;
		
		while(go)
		{
			nextTimeOutTimeStamp = -1;
			if(timeOutList != null)
			{
				timeOutList.clear();
			}
			if(removeTaskObservableList != null)
			{
				removeTaskObservableList.clear();
			}
			
			taskTimeOutIndexReadLock.lock();
			
			try
			{
				long currentTimeStamp = System.currentTimeMillis();
				long heartBeatTimeOut = -1;
				long lastHeartBeat = -1;
				long heartBeatTimeOutStamp = -1;
				TaskContainer task = null;
				
				
				// Task TimeOut
				
				for(Entry<ChannelImpl<?>,TaskObservable> entry : this.taskTimeOutIndex.entrySet())
				{
					inTimeOut = false;
					task = entry.getKey().getCurrentRunningTask();
					
					if((task == null) || (task != entry.getValue().task))
					{
						if(removeTaskObservableList == null)
						{
							removeTaskObservableList = new ArrayList<TaskObservable>();
						}
						removeTaskObservableList.add(entry.getValue());
					}
					
					// Task Timeout
					
					Long taskTimeOut = entry.getValue().taskTimeOut;
					
					if((taskTimeOut != null) && (taskTimeOut.longValue() > 0))
					{
						if(taskTimeOut.longValue() <= currentTimeStamp)
						{
							if(timeOutList == null)
							{
								timeOutList = new ArrayList<ChannelImpl<?>>();
							}
							timeOutList.add(entry.getKey());
							inTimeOut = true;
						}
						else
						{
							if(nextTimeOutTimeStamp < 0)
							{
								nextTimeOutTimeStamp = taskTimeOut.longValue();
							}
							else if(nextTimeOutTimeStamp > taskTimeOut.longValue())
							{
								nextTimeOutTimeStamp = taskTimeOut.longValue();
							}
						}
					}
					
					if(inTimeOut)
					{
						continue;
					}
					
					// HeartBeat Timeout
					
					heartBeatTimeOutStamp = -1;
					
					if((task !=  null) && (task.getTaskControl() != null))
					{
						heartBeatTimeOut = task.getTaskControl().getHeartbeatTimeout();
						
						
						if(heartBeatTimeOut > 0)
						{
							try
							{
								lastHeartBeat = task.getLastHeartbeat();
								heartBeatTimeOutStamp = lastHeartBeat + heartBeatTimeOut;
								if(lastHeartBeat > 0)
								{
									if(heartBeatTimeOutStamp <= currentTimeStamp)
									{
										if(timeOutList == null)
										{
											timeOutList = new ArrayList<ChannelImpl<?>>();
										}
										timeOutList.add(entry.getKey());
										inTimeOut = true;
									}
									else
									{
										if((nextTimeOutTimeStamp < 0) || nextTimeOutTimeStamp > heartBeatTimeOutStamp)
										{
											nextTimeOutTimeStamp = heartBeatTimeOutStamp;
										}
									}
								}
							}
							catch (Exception e) 
							{
								logger.error("Error while check heartbeat timeout",e);
							}
						}
					}
				}
				
			}
			catch (Exception e) 
			{
				logger.error("Exception while run DispatcherGuardian",e);
			}
			catch (Error e) 
			{
				logger.error("Error while run DispatcherGuardian",e);
			}
			
			taskTimeOutIndexReadLock.unlock();
			
			
			if(timeOutList != null)
			{
				for(ChannelImpl<?> queue : timeOutList)
				{
					queue.checkTimeOut();
				}
			}
			
			if((removeTaskObservableList != null) && (! removeTaskObservableList.isEmpty()))
			{
				taskTimeOutIndexWriteLock.lock();
				
				try
				{
					for(TaskObservable taskObservable : removeTaskObservableList)
					{
						TaskObservable toRemoveObservable = this.taskTimeOutIndex.get(taskObservable.queue);
						if(toRemoveObservable == null)
						{
							continue;
						}
						if(toRemoveObservable != taskObservable)
						{
							continue;
						}
						
						TaskContainer task = taskObservable.queue.getCurrentRunningTask();
						if((task == null) || (task != taskObservable.task))
						{
							this.taskTimeOutIndex.remove(taskObservable.queue);
						}
					}
				}
				catch (Exception e) 
				{
					logger.error("Exception while run DispatcherGuardian",e);
				}
				catch (Error e) 
				{
					logger.error("Error while run DispatcherGuardian",e);
				}
			
				taskTimeOutIndexWriteLock.unlock();
			}
			
			try
			{
				synchronized (this.waitMonitor)
				{
					if(go)
					{
						if(isUpdateNotified)
						{
							isUpdateNotified = false;
						}
						else
						{
							long waitTime = nextTimeOutTimeStamp - System.currentTimeMillis();
							if(waitTime > DEFAULT_WAIT_TIME)
							{
								waitTime = DEFAULT_WAIT_TIME;
							}
							if(nextTimeOutTimeStamp < 0)
							{
								waitTime = DEFAULT_WAIT_TIME;
							}
							if(waitTime > 0)
							{
								this.currentWait = System.currentTimeMillis() + waitTime;
								waitMonitor.wait(waitTime);
								this.currentWait = -1;
							}
						}
					}
				}
			}
			catch (InterruptedException e) {}
			catch (Exception e) 
			{
				logger.error("Exception while run Dispatcher DispatcherGuardian",e);
			}
			catch (Error e) 
			{
				logger.error("Error while run Dispatcher DispatcherGuardian",e);
			}
		}
	}
	
	public void registerTimeOut(ChannelImpl<?> channel, TaskContainer taskContainer)
	{
		TaskControlImpl taskControl = taskContainer.getTaskControl();
		if(taskControl == null)
		{
			return;
		}
		
		
		long timeOutTimeStamp = taskControl.getTimeout() < 0L ? -1 : taskControl.getTimeout() + System.currentTimeMillis();
		
		taskTimeOutIndexWriteLock.lock();
		try
		{
			TaskObservable taskObservable = this.taskTimeOutIndex.get(channel);
			if(taskObservable ==  null)
			{
				taskObservable =  new TaskObservable();
				taskObservable.queue = channel;
				this.taskTimeOutIndex.put(channel,taskObservable);
			}
			if(taskControl.getTimeout() > 0)
			{
				taskObservable.task = taskContainer;
				taskObservable.taskTimeOut = timeOutTimeStamp;
			}
			else if(taskControl.getHeartbeatTimeout() > 0)
			{
				taskObservable.task = taskContainer;
				taskObservable.taskTimeOut =  null;
			}
			else
			{
				return;
			}
		}
		finally 
		{
			taskTimeOutIndexWriteLock.unlock();
		}
		
		boolean notify = false;
		synchronized (this.waitMonitor)
		{
			this.isUpdateNotified = true;
			if(this.currentWait > 0)
			{
				if((timeOutTimeStamp > 0) && (timeOutTimeStamp < this.currentWait))
				{
					notify = true;
				}
				else
				{
					long heartBeatTimeOut = taskContainer.getTaskControl().getHeartbeatTimeout();
					if(heartBeatTimeOut > 0)
					{
						try
						{
							long lastHeartBeat = taskContainer.getLastHeartbeat();
							if(lastHeartBeat > 0)
							{
								long heartBeatTimeOutStamp = lastHeartBeat + heartBeatTimeOut;
								if(heartBeatTimeOutStamp < this.currentWait)
								{
									notify = true;
								}
								
							}
						}
						catch (Exception e) 
						{
							logger.error("Error while check heartbeat timeout",e);
						}
					}
				}
			}
			
			if(notify)
			{
				try
				{
					waitMonitor.notify();
				}
				catch (Exception e) {}
				catch (Error e) {}
			}
		}
		
	}
	
	public void unregisterTimeOut(ChannelImpl<?> queue, TaskContainer task)
	{
		taskTimeOutIndexWriteLock.lock();
		try
		{
			TaskObservable taskObservable = this.taskTimeOutIndex.get(queue);
			if(taskObservable !=  null)
			{
				if((taskObservable.task != null) && (taskObservable.task == task))
				{
					this.taskTimeOutIndex.remove(queue);
				}
			}
		}
		finally 
		{
			taskTimeOutIndexWriteLock.unlock();
		}
	}
	
	public void stopGuardian()
	{
		this.go = false;
		synchronized (this.waitMonitor)
		{
			try
			{
				this.waitMonitor.notify();
			}
			catch (Exception e) 
			{
				logger.error("Exception while stop DispatcherGuardian",e);
			}
		}
	}
	
	private class TaskObservable
	{
		public Long taskTimeOut = null;
		public TaskContainer task = null;
		public ChannelImpl<?> queue = null;
	}
	
}
