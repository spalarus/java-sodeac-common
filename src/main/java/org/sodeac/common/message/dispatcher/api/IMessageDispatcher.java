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

import java.util.List;

/**
 * 
 * API for message dispatcher
 * 
 *  @author Sebastian Palarus
 *
 */
public interface IMessageDispatcher
{
	/**
	 * queue a message to addressed queue
	 * 
	 * @param channelId id of {@link IChannel} 
	 * @param message message to queue
	 * 
	 * 
	 * @throws ChannelNotFoundException
	 * @throws ChannelIsFullException
	 */
	public <T> void sendMessage(String channelId,T message) throws ChannelNotFoundException, ChannelIsFullException;
	
	/**
	 * factory-methode creating instance of {@link IPropertyBlock} 
	 * 
	 * @return instance of {@link IPropertyBlock} 
	 */
	public IPropertyBlock createPropertyBlock();
	
	/**
	 * request for all {@link IChannel}-IDs
	 * 
	 * @return {@link java.util.List} with queueIds
	 */
	public List<String> getChannelIdList();
	
	/**
	 * getter to request for {@link IChannel} with given id
	 * 
	 * @param channelId  id for queue
	 * @return instance of {@link IChannel} registered with {@code queueId}
	 */
	public IChannel<?> getChannel(String channelId);
	
	public <T> IChannel<T> getTypedChannel(String channelId, Class<T> messageType);
	
	/**
	 * getter for propertyblock of dispatcher
	 * 
	 * @return {@link IPropertyBlock} of dispatcher
	 */
	public IPropertyBlock getPropertyBlock();
	
	/**
	 * getter for id of dispatcher.
	 * 
	 * @return id of dispatcher
	 */
	public String getId();
	
	/**
	 * Remove all workers and clean resources. After disposing the dispatcher is not usable anymore. 
	 */
	public void dispose();
	
	public void registerChannelManager(IChannelManager queueController);
	public void registerChannelService(IChannelService queueService);
	
	public void unregisterChannelManager(IChannelManager eventController);
	public void unregisterChannelService(IChannelService queueService);
}
