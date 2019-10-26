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

public class SpooledChannelWorker
{
	protected SpooledChannelWorker(ChannelImpl queue,long wakeupTime)
	{
		super();
		this.queue = queue;
		this.wakeupTime = wakeupTime;
	}
	
	private ChannelImpl queue;
	private long wakeupTime;
	private volatile boolean valid = true;
	
	public ChannelImpl getQueue()
	{
		return queue;
	}
	public long getWakeupTime()
	{
		return wakeupTime;
	}
	public boolean isValid()
	{
		return valid;
	}
	public void setValid(boolean valid)
	{
		this.valid = valid;
	}
}