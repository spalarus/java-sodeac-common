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
 * An extension interface for {@link IChannelManager} to consume a notification if {@link IChannel} has queued an {@link IMessage}
 * 
 * @author Sebastian Palarus
 *
 */
public interface IOnMessageStore extends IChannelManager
{
	/**
	 * This is fired, if {@link IChannel} has queued an {@link IMessage}
	 * <br>
	 * invoked and synchronized by queue worker
	 * 
	 * @param message new message
	 */
	public <T> void onMessageStore(IMessage<T> message);
}
