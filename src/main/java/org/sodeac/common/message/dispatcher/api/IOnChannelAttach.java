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
 * An extension interface for {@link IChannelManager} to consume notifications if instance of {@link IChannelManager} attach to a {@link IChannel}
 * 
 * @author Sebastian Palarus
 *
 */
public interface IOnChannelAttach extends IChannelManager
{
	/**
	 * This is fired, if {@link IChannelManager} attach to a {@link IChannel}
	 * <br>
	 * invoked and synchronized by queue worker
	 * 
	 * @param channel is attached with {@link IChannelManager}
	 */
	public void onChannelAttach(IChannel channel);
}
