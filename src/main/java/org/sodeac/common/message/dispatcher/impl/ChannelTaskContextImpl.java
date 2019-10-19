/*******************************************************************************
 * Copyright (c) 2019 Sebastian Palarus
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

import org.sodeac.common.message.dispatcher.api.IChannel;
import org.sodeac.common.message.dispatcher.api.IChannelTask;
import org.sodeac.common.message.dispatcher.api.IChannelTaskContext;
import org.sodeac.common.message.dispatcher.api.IPropertyBlock;
import org.sodeac.common.message.dispatcher.api.ITaskControl;

public class ChannelTaskContextImpl implements IChannelTaskContext
{
	private IChannel queue;
	private TaskContainer dueTask;
	private List<IChannelTask> currentProcessedTaskList;
	private List<IChannelTask> currentProcessedTaskListWritable;
	private List<IChannelTask> currentProcessedTaskListReadOnly;
	private List<TaskContainer> dueTaskList;
	
	protected ChannelTaskContextImpl(List<TaskContainer> dueTaskList)
	{
		super();
		this.dueTaskList = dueTaskList;
		currentProcessedTaskListWritable = new ArrayList<IChannelTask>();
		currentProcessedTaskListReadOnly = Collections.unmodifiableList(currentProcessedTaskListWritable);
	}
	
	@Override
	public IChannel getQueue()
	{
		return this.queue;
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
	public List<IChannelTask> currentProcessedTaskList()
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

	public void setQueue(IChannel queue)
	{
		this.queue = queue;
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
