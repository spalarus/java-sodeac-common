/*******************************************************************************
 * Copyright (c) 2019, 2020 Sebastian Palarus
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
import java.util.Collections;
import java.util.List;

import org.sodeac.common.message.dispatcher.api.IDispatcherChannel;
import org.sodeac.common.message.dispatcher.api.IDispatcherChannelTask;
import org.sodeac.common.message.dispatcher.api.IDispatcherChannelTaskContext;
import org.sodeac.common.message.dispatcher.api.IPropertyBlock;
import org.sodeac.common.message.dispatcher.api.ITaskControl;

public class ChannelTaskContextImpl implements IDispatcherChannelTaskContext
{
	private IDispatcherChannel channel;
	private TaskContainer dueTask;
	private List<IDispatcherChannelTask> currentProcessedTaskList;
	private List<IDispatcherChannelTask> currentProcessedTaskListWritable;
	private List<IDispatcherChannelTask> currentProcessedTaskListReadOnly;
	private List<TaskContainer> dueTaskList;
	
	protected ChannelTaskContextImpl(List<TaskContainer> dueTaskList)
	{
		super();
		this.dueTaskList = dueTaskList;
		currentProcessedTaskListWritable = new ArrayList<IDispatcherChannelTask>();
		currentProcessedTaskListReadOnly = Collections.unmodifiableList(currentProcessedTaskListWritable);
	}
	
	@Override
	public IDispatcherChannel getChannel()
	{
		return this.channel;
	}

	@Override
	public IPropertyBlock getTaskPropertyBlock()
	{
		return this.dueTask.getPropertyBlock();
	}

	@Override
	public ITaskControl getTaskControl()
	{
		return this.dueTask.getTaskControl();
	}

	@Override
	public List<IDispatcherChannelTask> currentProcessedTaskList()
	{
		if(currentProcessedTaskList == null)
		{
			currentProcessedTaskListWritable.clear();
			for(TaskContainer taskContainer : this.dueTaskList)
			{
				currentProcessedTaskListWritable.add(taskContainer.getTask());
			}
		}
		this.currentProcessedTaskList = this.currentProcessedTaskListReadOnly;
		return this.currentProcessedTaskList;
	}

	public void setChannel(IDispatcherChannel channel)
	{
		this.channel = channel;
	}

	public void resetCurrentProcessedTaskList()
	{
		this.currentProcessedTaskList = null;
		if(! this.currentProcessedTaskListWritable.isEmpty())
		{
			this.currentProcessedTaskListWritable.clear();
		}
	}

	public void setDueTask(TaskContainer dueTask)
	{
		this.dueTask = dueTask;
	}
}
