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
 * A {@link IChannelTask} acts as processor for queued {@link IMessage}s or as service.
 * 
 * @author Sebastian Palarus
 *
 */
@FunctionalInterface
public interface IChannelTask
{
	
	/**
	 * invoked one time at initialization of this task
	 * 
	 * @param queue parent-{@link IChannel} 
	 * @param id registration-id of this task
	 * @param propertyBlock properties for this task
	 * @param taskControl state-handler for this task
	 */
	public default void configure(IChannel queue, String id, IPropertyBlock propertyBlock, ITaskControl taskControl) {};
	
	/**
	 * run this task, invoked by channel-worker.
	 * 
	 * @param context of task running
	 * @throws Exception
	 */
	public void run(IChannelTaskContext taskContext) throws Exception;
}
