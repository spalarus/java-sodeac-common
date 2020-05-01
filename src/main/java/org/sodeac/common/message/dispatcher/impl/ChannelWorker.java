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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sodeac.common.message.dispatcher.api.IDispatcherChannel;
import org.sodeac.common.message.dispatcher.api.IDispatcherChannelService;
import org.sodeac.common.message.dispatcher.api.IDispatcherChannelTask;
import org.sodeac.common.message.dispatcher.api.IDispatcherChannelWorker;
import org.sodeac.common.message.dispatcher.api.IMessage;
import org.sodeac.common.message.dispatcher.api.IOnChannelAttach;
import org.sodeac.common.message.dispatcher.api.IOnChannelSignal;
import org.sodeac.common.message.dispatcher.api.IOnMessageRemove;
import org.sodeac.common.message.dispatcher.api.IOnMessageRemoveSnapshot;
import org.sodeac.common.message.dispatcher.api.IOnMessageStore;
import org.sodeac.common.message.dispatcher.api.IOnMessageStoreResult;
import org.sodeac.common.message.dispatcher.api.IOnMessageStoreSnapshot;
import org.sodeac.common.message.dispatcher.api.IOnTaskDone;
import org.sodeac.common.message.dispatcher.api.IOnTaskError;
import org.sodeac.common.message.dispatcher.api.IOnTaskTimeout;
import org.sodeac.common.message.dispatcher.api.IPeriodicChannelTask;
import org.sodeac.common.message.dispatcher.api.ITaskControl.ExecutionTimestampSource;
import org.sodeac.common.message.dispatcher.impl.TaskControlImpl.PeriodicServiceTimestampPredicate;
import org.sodeac.common.snapdeque.DequeSnapshot;

public class ChannelWorker extends Thread
{
	public static final long DEFAULT_WAIT_TIME = 108 * 108 * 108 * 7;
	public static final long FREE_TIME = 108 + 27;
	public static final long RESCHEDULE_BUFFER_TIME = 27;
	public static final long DEFAULT_SHUTDOWN_TIME = 1080 * 54;
	
	private long spoolTimeStamp = 0;
	
	private ChannelImpl<?> channel = null;
	private IDispatcherChannelWorker workerWrapper = null;
	private volatile boolean go = true;
	protected volatile boolean isUpdateNotified = false;
	protected volatile boolean isSoftUpdated = false;
	private volatile Object waitMonitor = new Object();
	
	private List<TaskContainer> dueTaskList = null;
	
	private volatile Long currentTimeOutTimeStamp = null;
	private volatile TaskContainer currentRunningTask = null;
	private volatile long wakeUpTimeStamp = -1;
	private volatile boolean inFreeingArea = false;
	
	private ChannelTaskContextImpl context = null;
	
	private Logger logger = LoggerFactory.getLogger(ChannelWorker.class);
	
	protected ChannelWorker(ChannelImpl<?> impl)
	{
		super();
		this.channel = impl;
		this.workerWrapper = new ChannelWorkerWrapper(this);
		this.dueTaskList = new ArrayList<TaskContainer>();
		this.context = new ChannelTaskContextImpl(this.dueTaskList);
		this.context.setChannel(this.channel);
		super.setDaemon(true);
		super.setName(ChannelWorker.class.getSimpleName() + " " + this.channel.getId());
	}

	private void checkQueueAttach()
	{
		try
		{
			DequeSnapshot<IOnChannelAttach> onQueueAttachSnapshot = channel.getOnQueueAttachList();
			if(onQueueAttachSnapshot == null)
			{
				return;
			}
			try
			{
				
				for(IOnChannelAttach onQueueAttach : onQueueAttachSnapshot)
				{
					try
					{
						onQueueAttach.onChannelAttach(this.channel);
					}
					catch (Exception e) 
					{
						logger.error("Exception on on-create() event controller", e);
					}
					catch (Error e) 
					{
						logger.error("Exception on on-create() event controller", e);
					}
				}
			}
			finally 
			{
				onQueueAttachSnapshot.close();
			}
		}
		catch (Exception e) 
		{
			logger.error("Exception while check queueAttach",e);
		}
		catch (Error e) 
		{
			logger.error("Exception while check queueAttach",e);
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public void run()
	{
		Set<IOnMessageStoreResult> scheduledResultSet = new HashSet<IOnMessageStoreResult>();
		Set<String> signalProcessed = new HashSet<String>();
		DequeSnapshot<? extends IMessage> newMessagesSnapshot;
		DequeSnapshot<? extends IMessage> removedMessagesSnapshot;
		while(go)
		{
			
			try
			{
				checkQueueAttach();
			}
			catch(Exception ex) {}
			catch(Error ex) {}
			
			try
			{
				synchronized (this.waitMonitor)
				{
					this.isUpdateNotified = false;
					this.isSoftUpdated = false;
				}
				
				channel.closeWorkerSnapshots();
			}
			catch (Exception e) 
			{
				logger.error("Exception while worker go init",e);
			}
			catch (Error e) 
			{
				logger.error("Exception while worker go init",e);
			}
			
			try
			{
				removedMessagesSnapshot = channel.getRemovedMessagesSnapshot();
				try
				{
					if((removedMessagesSnapshot != null) && (! removedMessagesSnapshot.isEmpty()))
					{
	
						try
						{
							checkQueueAttach();
						}
						catch(Exception ex) {}
						catch(Error ex) {}
						
						boolean onMessageRemoveSingle = false;
						boolean onMessageRemoveSnapshot = false;
						
						for(ChannelManagerContainer conf : channel.getManagerContainerList())
						{
							try
							{
								if(go && conf.isImplementingIOnMessageRemoveSnapshot())
								{
									((IOnMessageRemoveSnapshot)conf.getChannelManager()).onMessageRemoveSnapshot(removedMessagesSnapshot);
								}
							}
							catch (Exception e) {}
							catch (Error e) {}
						}
						
						for(MessageImpl message : (DequeSnapshot<MessageImpl>)removedMessagesSnapshot)
						{
							channel.touchLastWorkerAction();
							for(ChannelManagerContainer conf : channel.getManagerContainerList())
							{
								try
								{
									if(go && conf.isImplementingIOnMessageRemove())
									{
										((IOnMessageRemove)conf.getChannelManager()).onMessageRemove(message);
									}
								}
								catch (Exception e) {}
								catch (Error e) {}
							}
						}
						
						for(MessageImpl message : (DequeSnapshot<MessageImpl>)removedMessagesSnapshot)
						{
							try
							{
								message.dispose();
							}
							catch (Exception e) {}
							catch (Error e) {}
						}
					}
				}
				finally 
				{
					if(removedMessagesSnapshot != null)
					{
						try
						{
							removedMessagesSnapshot.close();
						}
						finally 
						{
							removedMessagesSnapshot = null;
						}
					}
				}
			}
			catch (Exception e) 
			{
				logger.error("Exception while process removedEventList",e);
			}
			catch (Error e) 
			{
				logger.error("Error while process removedEventList",e);
			}
			
			try
			{
				newMessagesSnapshot = channel.getNewScheduledEventsSnaphot();
				try
				{
					if((newMessagesSnapshot != null) && (! newMessagesSnapshot.isEmpty()))
					{
						try
						{
							checkQueueAttach();
						}
						catch(Exception ex) {}
						catch(Error ex) {}
						
						boolean onMessageStoredSingle = false;
						boolean onMessageStoredSnapshot = false;
						
						for(ChannelManagerContainer conf : channel.getManagerContainerList())
						{
							if(conf.isImplementingIOnMessageStore())
							{
								onMessageStoredSingle = true;
							}
							if(conf.isImplementingIOnMessageStoreSnapshot())
							{
								onMessageStoredSnapshot = true;
							}
						}
						
						if(onMessageStoredSingle || onMessageStoredSnapshot)
						{
							scheduledResultSet.clear();
							for(IMessage<?> event : newMessagesSnapshot)
							{
								try
								{
									scheduledResultSet.add(event.getScheduleResultObject());
								}
								catch (Exception ie) {}
							}
							
							if(onMessageStoredSnapshot)
							{
								channel.touchLastWorkerAction();
								for(ChannelManagerContainer conf : channel.getManagerContainerList())
								{
									if(go && conf.isImplementingIOnMessageStoreSnapshot())
									{
										try
										{
											((IOnMessageStoreSnapshot)conf.getChannelManager()).onMessageStoreSnapshot(newMessagesSnapshot);
										
										}
										catch (Exception e) {}
										catch (Error e) {}
									}
									
								}
							}
							
							if(onMessageStoredSingle)
							{
								for(MessageImpl<?> message : (DequeSnapshot<MessageImpl<?>>)newMessagesSnapshot)
								{
									channel.touchLastWorkerAction();
									for(ChannelManagerContainer conf : channel.getManagerContainerList())
									{
										try
										{
											if(go && conf.isImplementingIOnMessageStore())
											{
												((IOnMessageStore)conf.getChannelManager()).onMessageStore(message);
											}
										}
										catch (Exception e) 
										{
											try
											{
												message.getScheduleResultObject().addError(e);
											}
											catch (Exception ie) {}		
										}
										catch (Error e) 
										{
											try
											{
												message.getScheduleResultObject().addError(e);
											}
											catch (Exception ie) {}		
										}
									}
								}
							}
							
							for(IOnMessageStoreResult scheduleResult : scheduledResultSet)
							{
								try
								{
									((PublishMessageResultImpl)scheduleResult).processPhaseIsFinished();
								}
								catch (Exception ie) {}										
							}
							for(MessageImpl<?> event : (DequeSnapshot<MessageImpl<?>>)newMessagesSnapshot)
							{
								try
								{
									event.setScheduleResultObject(null);
								}
								catch(Exception e) {}
							}
							
							scheduledResultSet.clear();
						}
					}
				}
				finally
				{
					if(newMessagesSnapshot != null)
					{
						try
						{
							newMessagesSnapshot.close();
						}
						finally 
						{
							newMessagesSnapshot = null;
						}
						scheduledResultSet.clear();
					}
				}
			}
			catch (Exception e) 
			{
				logger.error("Exception while process newScheduledList",e);
			}
			catch (Error e) 
			{
				logger.error("Error while process newScheduledList",e);
			}
			
			try
			{
				DequeSnapshot<String> signalSnapshot = channel.getSignalsSnapshot();
				try
				{
					if((signalSnapshot != null) && (! signalSnapshot.isEmpty()))
					{
	
						try
						{
							checkQueueAttach();
						}
						catch(Exception ex) {}
						catch(Error ex) {}
						
						signalProcessed.clear();
						for(String signal : signalSnapshot)
						{
							if(signalProcessed.contains(signal))
							{
								continue;
							}
							channel.touchLastWorkerAction();
							for(ChannelManagerContainer conf : channel.getManagerContainerList())
							{
								try
								{
									
									if(go && conf.isImplementingIOnChannelSignal())
									{
										((IOnChannelSignal)conf.getChannelManager()).onChannelSignal(channel, signal);
									}
								}
								catch (Exception e) 
								{
									logger.error("Exception while process signal",e);
								}
								catch (Error e) 
								{
									logger.error("Error while process signal",e);
								}
							}
							
							signalProcessed.add(signal);
						}
						signalProcessed.clear();
					}
				}
				finally 
				{
					if(signalSnapshot != null)
					{
						try
						{
							signalSnapshot.close();
						}
						catch (Exception e) {}
					}
				}
			}
			catch (Exception e) 
			{
				logger.error("Exception while process signalList",e);
			}
			catch (Error e) 
			{
				logger.error("Error while process signalList",e);
			}
			
			this.dueTaskList.clear();
			channel.getDueTasks(this.dueTaskList);
			
			if(! dueTaskList.isEmpty())
			{

				try
				{
					checkQueueAttach();
				}
				catch(Exception ex) {}
				catch(Error ex) {}
				
				channel.touchLastWorkerAction();
				boolean taskTimeOut  = false;
				for(TaskContainer dueTask : this.dueTaskList)
				{
					try
					{
						if(dueTask.getTaskControl().isDone())
						{
							continue;
						}
						if(go)
						{
							this.context.resetCurrentProcessedTaskList();
							
							try
							{
								taskTimeOut = ((dueTask.getTaskControl().getTimeout() > 0) || (dueTask.getTaskControl().getHeartbeatTimeout() > 0));
								this.currentRunningTask = dueTask;
								
								if(dueTask.getTask() instanceof IPeriodicChannelTask)
								{
									Long periodicRepetitionInterval = ((IPeriodicChannelTask) dueTask.getTask()).getPeriodicRepetitionInterval();
									if((periodicRepetitionInterval ==  null) || (periodicRepetitionInterval.longValue() < 1))
									{
										periodicRepetitionInterval = 1000L * 60L * 60L * 24L * 365L * 108L;
									}
									dueTask.getTaskControl().setExecutionTimeStamp
									(
										System.currentTimeMillis() + periodicRepetitionInterval, 
										ExecutionTimestampSource.PERODIC, 
										PeriodicServiceTimestampPredicate.getInstance()
									);
									dueTask.getTaskControl().preRunPeriodicTask();
								}
								else if(dueTask.getTask() instanceof IDispatcherChannelService)
								{
									long periodicRepetitionInterval = -1L;
									
									try
									{
										if(dueTask.getPropertyBlock().getProperty(ChannelImpl.PROPERTY_PERIODIC_REPETITION_INTERVAL) != null)
										{
											Object pri = dueTask.getPropertyBlock().getProperty(ChannelImpl.PROPERTY_PERIODIC_REPETITION_INTERVAL);
											if(pri instanceof String)
											{
												periodicRepetitionInterval = Long.parseLong(((String)pri).trim());
											}
											else if(pri instanceof Integer)
											{
												periodicRepetitionInterval = ((Integer)pri);
											}
											else
											{
												periodicRepetitionInterval = ((Long)pri);
											}
										}		
									}
									catch (Exception e) {}
									
									if(periodicRepetitionInterval < 1)
									{
										periodicRepetitionInterval = 1000L * 60L * 60L * 24L * 365L * 108L;
									}
									dueTask.getTaskControl().setExecutionTimeStamp
									(
										System.currentTimeMillis() + periodicRepetitionInterval, 
										ExecutionTimestampSource.PERODIC, 
										PeriodicServiceTimestampPredicate.getInstance()
									);
									dueTask.getTaskControl().preRunPeriodicTask();
								}
								else
								{
									dueTask.getTaskControl().preRun();
								}
								
								if(taskTimeOut)
								{
									if(dueTask.getTaskControl().getTimeout() > 0)
									{
										this.currentTimeOutTimeStamp = System.currentTimeMillis() + dueTask.getTaskControl().getTimeout();
									}
									this.channel.getMessageDispatcher().registerTimeOut(this.channel,dueTask);
								}
								this.context.setDueTask(dueTask);	
								dueTask.heartbeat();
								
								//
								//	run task or service
								//
								
								dueTask.getTask().run(this.context);
								
								
								if(go)
								{
									dueTask.getPropertyBlock().setProperty(ChannelImpl.PROPERTY_KEY_THROWED_EXCEPTION, null);
									
									dueTask.getTaskControl().postRun();
									
									this.currentTimeOutTimeStamp = null;
									this.currentRunningTask = null;
									if(taskTimeOut)
									{
										try
										{
											this.channel.getMessageDispatcher().unregisterTimeOut(this.channel,dueTask);
										}
										catch (Exception e) 
										{
											this.logger.error( "eventQueue.getEventDispatcher().unregisterTimeOut(this.eventQueue,dueTask)", e);
										}
									}
								}
								else
								{
									channel.closeWorkerSnapshots();
									return;
								}
							}
							catch (Exception e) 
							{
								TaskContainer runningTask = this.currentRunningTask;
								this.currentTimeOutTimeStamp = null;
								this.currentRunningTask = null;
								
								runningTask.getPropertyBlock().setProperty(ChannelImpl.PROPERTY_KEY_THROWED_EXCEPTION, e);
								logger.error("Exception while process task " + dueTask.getTask(),e);
								
								dueTask.getTaskControl().postRun();
								if(taskTimeOut)
								{
									this.channel.getMessageDispatcher().unregisterTimeOut(this.channel,dueTask);
								}
								
								if(! (dueTask.getTask() instanceof IDispatcherChannelService))
								{
									dueTask.getTaskControl().setDone();
								}
								
								if(! go)
								{
									this.channel.closeWorkerSnapshots();
									return;
								}
								
								try
								{
									for(ChannelManagerContainer conf : channel.getManagerContainerList())
									{
										if(conf.isImplementingIOnTaskError())
										{
											try
											{
												((IOnTaskError)conf.getChannelManager()).onTaskError(this.channel, dueTask.getTask(),  e);
											}
											catch (Exception ie) 
											{
												logger.error("Error while process onTaskError " + dueTask,ie);
											}
										}
									}
								}
								catch (Exception ie) 
								{
									logger.error("Error while process onTaskError " + dueTask,ie);
								}
				
							}
							catch (Error e) 
							{
								TaskContainer runningTask = this.currentRunningTask;
								this.currentTimeOutTimeStamp = null;
								this.currentRunningTask = null;
								
								Exception exc = new Exception(e.getMessage(),e);
								runningTask.getPropertyBlock().setProperty(ChannelImpl.PROPERTY_KEY_THROWED_EXCEPTION, exc);
								logger.error("Error while process task " + dueTask.getTask(),e);
								
								dueTask.getTaskControl().postRun();
								if(taskTimeOut)
								{
									this.channel.getMessageDispatcher().unregisterTimeOut(this.channel,dueTask);
								}
								
								if(! (dueTask.getTask() instanceof IDispatcherChannelService))
								{
									dueTask.getTaskControl().setDone();
								}
								
								if(e instanceof ThreadDeath)
								{
									go = false;
								}
								
								if(! go)
								{
									this.channel.closeWorkerSnapshots();
									return;
								}
								
								try
								{
									for(ChannelManagerContainer conf : channel.getManagerContainerList())
									{
										if(conf.isImplementingIOnTaskError())
										{
											try
											{
												((IOnTaskError)conf.getChannelManager()).onTaskError(this.channel, dueTask.getTask(), exc);
											}
											catch (Exception ie) 
											{
												logger.error("Error while process onTaskError " + dueTask,ie);
											}
										}
									}
								}
								catch (Exception ie) 
								{
									logger.error("Error while process onTaskError " + dueTask,ie);
								}
								
							}
							
							this.currentTimeOutTimeStamp = null;
							this.currentRunningTask = null;
							
							if(! go)
							{
								this.channel.closeWorkerSnapshots();
								return;
							}
							
							if(dueTask.getTaskControl().isDone())
							{
								for(ChannelManagerContainer conf : channel.getManagerContainerList())
								{
									try
									{
										if(go)
										{
											if(conf.isImplementingIOnTaskDone())
											{
												((IOnTaskDone)conf.getChannelManager()).onTaskDone(this.channel, dueTask.getTask());
											}
										}
									}
									catch (Exception e) {}
								}
							}
						}
					}
					catch (Exception e) 
					{
						try
						{
							if(! (dueTask.getTask() instanceof IDispatcherChannelService))
							{
								dueTask.getTaskControl().setDone();
							}
						}
						catch (Exception ie) {}
						logger.error("Error while process currentProcessedTaskList",e);
					}
					
				}
			}
			
			channel.cleanDoneTasks();
			this.channel.closeWorkerSnapshots();
			
			try
			{
				boolean shutdownWorker = false;
				if(System.currentTimeMillis() > (channel.getLastWorkerAction() + DEFAULT_SHUTDOWN_TIME))
				{
					this.inFreeingArea = true;
					shutdownWorker = this.channel.checkWorkerShutdown(this);
					if(! shutdownWorker)
					{
						this.inFreeingArea = false;
					}
				}
				
				if(shutdownWorker)
				{
					synchronized (this.waitMonitor)
					{
						while((this.channel == null) && (this.go))
						{
							try
							{
								this.wakeUpTimeStamp = System.currentTimeMillis() + DEFAULT_WAIT_TIME;
								waitMonitor.wait(DEFAULT_WAIT_TIME);
								this.wakeUpTimeStamp = -1;
							}
							catch (Exception e) {}
							catch (ThreadDeath e) {this.go = false;}
							catch (Error e) {}
						}
						
						this.inFreeingArea = false;
						
						if(! go)
						{
							return;
						}
						
						channel.touchLastWorkerAction();
						
						continue;
					}
				}
				
				try
				{
					checkQueueAttach();
				}
				catch(Exception ex) {}
				catch(Error ex) {}
				
				synchronized (this.waitMonitor)
				{
					if(go)
					{
						this.wakeUpTimeStamp = -1;
						
						if(this.isUpdateNotified)
						{
							this.isUpdateNotified = false;
							continue;
						}
							
						long nextRunTimeStamp = System.currentTimeMillis() + DEFAULT_WAIT_TIME;
						try
						{
							nextRunTimeStamp = channel.getNextRun();
						}
						catch (Exception e) 
						{
							logger.error("Error while recalc next runtime again",e);
						}
						long waitTime = nextRunTimeStamp - System.currentTimeMillis();
						if(waitTime > DEFAULT_WAIT_TIME)
						{
							waitTime = DEFAULT_WAIT_TIME;
						}
						if(waitTime > 0)
						{
							boolean freeWorker = false;
							if(! isSoftUpdated)
							{
								this.inFreeingArea = true;
								if(waitTime >= FREE_TIME)
								{
									freeWorker = this.channel.checkFreeWorker(this, nextRunTimeStamp);
								}
							}
							if(freeWorker)
							{
								while((this.channel == null) && (this.go))
								{
									try
									{
										this.wakeUpTimeStamp = System.currentTimeMillis() + DEFAULT_WAIT_TIME ;
										waitMonitor.wait(DEFAULT_WAIT_TIME);
										this.wakeUpTimeStamp = -1;
									}
									catch (Exception e) {}
									catch (ThreadDeath e) {this.go = false;}
									catch (Error e) {}
								}
								
								this.inFreeingArea = false;
								
							}
							else
							{
								this.inFreeingArea = false;
								this.wakeUpTimeStamp = System.currentTimeMillis() + waitTime ;
								waitMonitor.wait(waitTime);
								this.wakeUpTimeStamp = -1;
							}
						}
					}
				}
			}
			
			catch (InterruptedException e) {}
			catch (Exception e) 
			{
				logger.error("Exception while run QueueWorker",e);
			}
			catch (ThreadDeath e) 
			{
				this.go = false;
			}
			catch (Error e) 
			{
				logger.error("Error while run QueueWorker",e);
			}
		}		
	}
	
	public boolean checkTimeOut(AtomicBoolean stop)
	{
		TaskContainer timeOutTaskContainer = this.currentRunningTask;
		if(timeOutTaskContainer == null)
		{
			return false;
		}
		
		// HeartBeat TimeOut
		
		boolean heartBeatTimeout = false;
		if(timeOutTaskContainer.getTaskControl().getHeartbeatTimeout() > 0)
		{
			try
			{
				long lastHeartBeat = timeOutTaskContainer.getLastHeartbeat();
				if(lastHeartBeat > 0)
				{
					if((lastHeartBeat + timeOutTaskContainer.getTaskControl().getHeartbeatTimeout() ) <= System.currentTimeMillis())
					{
						heartBeatTimeout = true;
					}
				}
			}
			catch (Exception e) 
			{
				logger.error("Error while check heartbeat timeout",e);
			}
		}
		
		if(! heartBeatTimeout)
		{
			// Task TimeOut
			
			Long timeOut = this.currentTimeOutTimeStamp;
			if(timeOut == null)
			{
				return false;
			}
			
			// check timeOut and timeOutTask again to prevent working with values don't match
			
			if(timeOutTaskContainer != this.currentRunningTask)
			{
				return false;
			}
			
			if(timeOut != this.currentTimeOutTimeStamp)
			{
				return false;
			}
			
			if(timeOut.longValue() > System.currentTimeMillis())
			{
				return false;
			}
		}
		
		ChannelImpl channel = this.channel;
		boolean stopFlag = timeOutTaskContainer.getTaskControl().getStopOnTimeoutFlag();
		IDispatcherChannelTask<Object> task = timeOutTaskContainer.getTask();
		TaskControlImpl taskControl = timeOutTaskContainer.getTaskControl();
		Object taskState = taskControl.getTaskState();
		timeOutTaskContainer.setTaskControl(timeOutTaskContainer.getTaskControl().copyForTimeout());
		
		this.go = false;
		
		try
		{
			if(task instanceof IDispatcherChannelService)
			{
				taskControl.timeOutService();
			}
			else
			{
				taskControl.timeout();
			}
		}
		catch (Exception e) {}
		
		for(ChannelManagerContainer conf : (List<ChannelManagerContainer>)channel.getManagerContainerList())
		{
			try
			{
				if(conf.getChannelManager() instanceof IOnTaskTimeout)
				{
					try
					{
						((MessageDispatcherImpl)channel.getDispatcher()).executeOnTaskTimeOut((IOnTaskTimeout)conf.getChannelManager(), channel, task, taskState, this);
					}
					catch (Exception e) {}
					catch (Error e) {}
				}
			}
			catch (Exception e) {}
			catch (Error e) {}
		}
		
		try
		{
			this.context.onTimeout();
		}
		catch (Exception e) {}
		
		if(stopFlag)
		{
			if(Thread.currentThread() != this)
			{
				try
				{
					stop.set(true);
					((MessageDispatcherImpl)channel.getDispatcher()).executeOnTaskStopExecuter(this, task);
				}
				catch (Exception e) {}
				
			}
			else
			{
				logger.warn("worker not stopped: checkTimeout invoke by self");
			}
		}
		
		return true;
	}
	
	public void notifySoftUpdate()
	{
		this.isUpdateNotified = true;
		this.isSoftUpdated = true;
	}
	
	public void notifyUpdate(long newRuntimeStamp)
	{
		try
		{
			synchronized (this.waitMonitor)
			{
				this.isUpdateNotified = true;
				this.isSoftUpdated = false;
				if(this.wakeUpTimeStamp > 0) // waits for new run
				{
					if(newRuntimeStamp <= System.currentTimeMillis())
					{
						waitMonitor.notify();
					}
					else if(this.wakeUpTimeStamp >= newRuntimeStamp)
					{
						waitMonitor.notify();
					}
				}
			}	
		}
		catch (Exception e) {}
		catch (Error e) {}
	}
	
	public void notifyUpdate()
	{
		try
		{
			synchronized (this.waitMonitor)
			{
				this.isUpdateNotified = true;
				this.isSoftUpdated = false;
				
				waitMonitor.notify();
			}	
		}
		catch (Exception e) {}
		catch (Error e) {}
	}
	
	public void softStopWorker()
	{
		this.go = false;
	}
	
	public void stopWorker()
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
				logger.error("Exception while stop QueueWorker",e);
			}
			catch (Error e) 
			{
				logger.error("Error while stop QueueWorker",e);
			}
		}
	}
	
	public ChannelImpl getMessageChannel()
	{
		return channel;
	}

	protected boolean setMessageChannel(ChannelImpl eventQueue)
	{
		if(! this.go)
		{
			return false;
		}
		
		if((eventQueue != null) && (this.channel != null))
		{
			return false;
		}
		
		if(! inFreeingArea)
		{
			return false;
		}
		
		this.channel = eventQueue;
		this.context.setChannel(this.channel);
		if(this.channel == null)
		{
			super.setName(ChannelWorker.class.getSimpleName() + " IDLE");
		}
		else
		{
			super.setName(ChannelWorker.class.getSimpleName() + " " + this.channel.getId());
		}
		
		return true;
	}

	public TaskContainer getCurrentRunningTask()
	{
		return currentRunningTask;
	}

	public boolean isGo()
	{
		return go;
	}

	public long getSpoolTimeStamp()
	{
		return spoolTimeStamp;
	}

	public void setSpoolTimeStamp(long spoolTimeStamp)
	{
		this.spoolTimeStamp = spoolTimeStamp;
	}

	public IDispatcherChannelWorker getWorkerWrapper()
	{
		return workerWrapper;
	}
}
