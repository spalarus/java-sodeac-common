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
 * An eventcontroller reacts to a wide variety of queue happenings, if it implements appropriate extension interfaces.
 * 
 * @author Sebastian Palarus
 *
 */
public interface IChannelManager extends IChannelComponent
{
	/**
	 * Configure controller behavior.
	 * 
	 * @param configurationPolicy
	 */
	public default void configure(IChannelControllerPolicy configurationPolicy)
	{
	}
	
	/**
	 * Policy container defines runtime configuration of controller.
	 * 
	 * @author "Sebastian Palarus"
	 *
	 */
	public static interface IChannelControllerPolicy
	{
		/**
		 * 
		 * @param configuration
		 */
		public void addConfigurationDetail(DispatcherChannelSetup configuration);
	}
}
