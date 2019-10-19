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
package org.sodeac.common.message.dispatcher.api;

/**
 * 
 * An extension interface for {@link IChannelManager} to consume notifications of finishing a task
 * 
 * @author Sebastian Palarus
 *
 */
public interface IOnTaskDone extends IChannelManager
{
	/**
	 * This is fired, if {@link IChannelTask} remove a scheduled {@link IMessage}
	 * <br>
	 * invoked and synchronized by queue worker
	 * 
	 * @param channel  queue of task finished {@link IChannelTask}
	 * @param task finished {@link IChannelTask}
	 */
	public void onTaskDone(IChannel channel,IChannelTask task);
}
