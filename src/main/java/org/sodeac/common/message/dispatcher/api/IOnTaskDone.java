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
package org.sodeac.common.message.dispatcher.api;

/**
 * 
 * An extension interface for {@link IDispatcherChannelManager} to consume notifications of finishing a task
 * 
 * @author Sebastian Palarus
 *
 */
public interface IOnTaskDone<T> extends IDispatcherChannelManager
{
	/**
	 * This is fired, if {@link IDispatcherChannelTask} remove a scheduled {@link IMessage}
	 * <br>
	 * invoked and synchronized by queue worker
	 * 
	 * @param channel  queue of task finished {@link IDispatcherChannelTask}
	 * @param task finished {@link IDispatcherChannelTask}
	 */
	public void onTaskDone(IDispatcherChannel<T> channel,IDispatcherChannelTask<T> task);
}
