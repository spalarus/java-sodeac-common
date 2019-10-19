/*******************************************************************************
 * Copyright (c) 2019 Sebastian Palarus
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Sebastian Palarus - initial API and implementation
 *******************************************************************************/
package org.sodeac.common.message.dispatcher.api;

import java.util.Iterator;
import java.util.ServiceLoader;


public interface IMessageDispatcherFactory 
{
	public static IMessageDispatcherFactory newInstance()
	{
		ServiceLoader<IMessageDispatcherFactory> serviceLoader = ServiceLoader.load(IMessageDispatcherFactory.class);
		Iterator<IMessageDispatcherFactory> iterator = serviceLoader.iterator();
		if(iterator.hasNext())
		{
			return iterator.next();
		}
		return null;
	}
	
	public IMessageDispatcher dispatcher(String id);
}
