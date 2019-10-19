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
 * Interface to register {@link IChannelTask}
 * 
 * @author Sebastian Palarus
 *
 */
public interface IChannelService extends IChannelTask,IChannelComponent
{ 
	// TODO configuration => dispatcher configuration
	// TODO ChannelComponentConfiguration => ChannelServiceConfiguration
	/**
	 * 
	 * @param componentConfiguration
	 */
	public default void configure(IChannelServicePolicy policy)
	{
	}
	
	public static interface IChannelServicePolicy
	{
		/**
		 * 
		 * @param configuration
		 */
		public void addConfigurationDetail(DispatcherChannelSetup configuration);
	}
}
