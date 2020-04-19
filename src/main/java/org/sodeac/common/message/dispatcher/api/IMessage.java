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

import java.util.Map;
import java.util.Set;

import org.sodeac.common.message.MessageHeader;

/**
 * The Message interface provides access to message payload. 
 * 
 * @author Sebastian Palarus
 *
 */
public interface IMessage<T>
{
	/**
	 * 
	 * @return message payload
	 */
	public T getPayload();
	
	
	/**
	 * 
	 * @return message header of message
	 */
	public MessageHeader getMessageHeader();
	
	/**
	 * 
	 * @return parent channel
	 */
	public IDispatcherChannel<?> getChannel();
	
	/**
	 * insert or update property for {@link IMessage}
	 * 
	 * @param key property key
	 * @param value property value
	 * @return overwritten property or null
	 */
	public Object setProperty(String key,Object value);
	
	/**
	 * get property for {@link IMessage} registered with {@code key}
	 * 
	 * @param key property key
	 * 
	 * @return property for {@link IMessage} registered with {@code key} or null, if absent
	 */
	public Object getProperty(String key);
	
	/**
	 * get set of all property-keys for {@link IMessage}
	 * 
	 * @return set of all property-keys for {@link IMessage}
	 */
	public Set<String> getPropertyKeySet();
	
	/**
	 * get immutable deep copy of property-keys {@link IMessage}
	 * 
	 * @return immutable deep copy of property-keys {@link IMessage}
	 */
	public Map<String,Object> getProperties();
	
	/**
	 * getter for {@link IOnMessageStoreResult} to inform schedule invoker about result
	 * 
	 * @return schedule result object
	 */
	public IOnMessageStoreResult getScheduleResultObject();
	
	/**
	 * get registered adapter
	 * 
	 * @param adapterClass type of adapter
	 * 
	 * @return registered adapter with specified adapterClass
	 */
	@SuppressWarnings("unchecked")
	public default <A> A getAdapter(Class<A> adapterClass)
	{
		return (A)getProperty(adapterClass.getCanonicalName());
	}
	
	/**
	 * remove event from parent queue
	 */
	public void removeFromChannel();
	
}
