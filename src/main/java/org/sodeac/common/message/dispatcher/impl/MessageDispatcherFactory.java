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
package org.sodeac.common.message.dispatcher.impl;

import java.util.UUID;

import org.sodeac.common.message.dispatcher.api.IMessageDispatcher;
import org.sodeac.common.message.dispatcher.api.IMessageDispatcherFactory;

public class MessageDispatcherFactory  implements IMessageDispatcherFactory
{
	public static IMessageDispatcher newInstance()
	{
		return new MessageDispatcherImpl(UUID.randomUUID().toString());
	}
	
	public static IMessageDispatcher newInstance(String id)
	{
		return new MessageDispatcherImpl(id);
	}

	@Override
	public IMessageDispatcher dispatcher(String id) 
	{
		return new MessageDispatcherImpl(id);
	}
}
