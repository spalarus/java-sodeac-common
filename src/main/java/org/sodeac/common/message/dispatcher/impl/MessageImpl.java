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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.sodeac.common.message.MessageHeader;
import org.sodeac.common.message.dispatcher.api.IChannel;
import org.sodeac.common.message.dispatcher.api.IMessage;
import org.sodeac.common.message.dispatcher.api.IOnMessageStoreResult;
import org.sodeac.common.message.dispatcher.api.IPropertyBlock;
import org.sodeac.common.snapdeque.DequeNode;

public class MessageImpl<T> implements IMessage<T>
{
	private PublishMessageResultImpl scheduleResult = null;
	private ChannelImpl channel = null;
	private T payload = null;
	private MessageHeader messageHeader = null;
	
	private volatile PropertyBlockImpl propertyBlock = null;
	private DequeNode<MessageImpl<T>> node = null;
	
	protected MessageImpl(T payload,ChannelImpl channel, MessageHeader messageHeader)
	{
		super();
		this.payload = payload;
		this.channel = channel;
		this.messageHeader = messageHeader;
	}

	public DequeNode<MessageImpl<T>> getNode()
	{
		return node;
	}
	public void setNode(DequeNode<MessageImpl<T>> node)
	{
		this.node = node;
	}

	@Override
	public T getPayload()
	{
		return this.payload;
	}
	
	@Override
	public IOnMessageStoreResult getScheduleResultObject()
	{
		return this.scheduleResult;
	}

	public void setScheduleResultObject(PublishMessageResultImpl scheduleResult)
	{
		this.scheduleResult = scheduleResult;
	}

	@Override
	public Object setProperty(String key, Object value)
	{
		if(this.propertyBlock == null)
		{
			ReentrantLock lock = this.channel.getMessageEventLock();
			lock.lock();
			try
			{
				if(this.propertyBlock == null)
				{
					this.propertyBlock =  (PropertyBlockImpl)channel.getDispatcher().createPropertyBlock();
				}
			}
			finally 
			{
				lock.unlock();
			}
		}
		
		return this.propertyBlock.setProperty(key, value);
	}

	@Override
	public Object getProperty(String key)
	{
		if(this.propertyBlock == null)
		{
			return null;
		}
		
		return this.propertyBlock.getProperty(key);
	}

	public MessageHeader getMessageHeader()
	{
		return this.messageHeader;
	}

	@Override
	public Set<String> getPropertyKeySet()
	{
		if(this.propertyBlock == null)
		{
			return PropertyBlockImpl.EMPTY_KEYSET;
		}
		return this.propertyBlock.getPropertyKeySet();
	}

	@Override
	public Map<String, Object> getProperties()
	{
		if(this.propertyBlock == null)
		{
			return PropertyBlockImpl.EMPTY_PROPERTIES;
		}
		return this.propertyBlock.getProperties();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <A> A getAdapter(Class<A> adapterClass)
	{
		if(adapterClass == IPropertyBlock.class)
		{
			if(this.propertyBlock == null)
			{
				ReentrantLock lock = this.channel.getMessageEventLock();
				lock.lock();
				try
				{
					if(this.propertyBlock == null)
					{
						this.propertyBlock =  (PropertyBlockImpl)channel.getDispatcher().createPropertyBlock();
					}
				}
				finally 
				{
					lock.unlock();
				}
			}
			return (A)this.propertyBlock;
		}
		return IMessage.super.getAdapter(adapterClass);
	}

	@Override
	public IChannel getChannel()
	{
		return this.channel;
	}

	@Override
	public void removeFromChannel()
	{
		if(channel != null)
		{
			channel.removeMessage(this);
		}
	}
	
	protected void dispose()
	{
		if(this.scheduleResult != null)
		{
			try
			{
				this.scheduleResult.dispose();
			}
			catch (Exception e) {}
		}
		this.scheduleResult = null;
		this.channel = null;
		this.payload = null;
		if(this.messageHeader != null)
		{
			try
			{
				this.messageHeader.dispose();
			}
			catch (Exception e) {}
		}
		if(this.propertyBlock != null)
		{
			try
			{
				this.propertyBlock.dispose();
			}
			catch (Exception e) {}
		}
		this.propertyBlock = null;
		this.node = null;
	}
}
