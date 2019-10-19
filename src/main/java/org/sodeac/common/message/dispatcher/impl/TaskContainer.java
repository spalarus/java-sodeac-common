/*******************************************************************************
 * Copyright (c) 2017, 2019 Sebastian Palarus
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Sebastian Palarus - initial API and implementation
 *******************************************************************************/
package org.sodeac.common.message.dispatcher.impl;

import org.sodeac.common.message.dispatcher.api.IChannelTask;
import org.sodeac.common.message.dispatcher.api.IPropertyBlock;

public class TaskContainer
{
	protected TaskContainer()
	{
		super();
	}
	
	private IChannelTask task;
	private String id;
	private IPropertyBlock properties;
	private TaskControlImpl taskControl;
	private boolean namedTask = false;
	private long lastHeartbeat = System.currentTimeMillis();
	
	public IChannelTask getTask()
	{
		return task;
	}
	public void setTask(IChannelTask task)
	{
		this.task = task;
	}
	public String getId()
	{
		return id;
	}
	public void setId(String id)
	{
		this.id = id;
	}
	public IPropertyBlock getPropertyBlock()
	{
		return properties;
	}
	public void setPropertyBlock(IPropertyBlock properties)
	{
		this.properties = properties;
	}
	public TaskControlImpl getTaskControl()
	{
		return taskControl;
	}
	public void setTaskControl(TaskControlImpl taskControl)
	{
		this.taskControl = taskControl;
	}
	public boolean isNamedTask()
	{
		return namedTask;
	}
	public void setNamedTask(boolean namedTask)
	{
		this.namedTask = namedTask;
	}
	
	public void heartbeat()
	{
		this.lastHeartbeat = System.currentTimeMillis();
	}
	public long getLastHeartbeat() 
	{
		return lastHeartbeat;
	}
}
