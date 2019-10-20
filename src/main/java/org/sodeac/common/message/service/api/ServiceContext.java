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
package org.sodeac.common.message.service.api;

import java.util.Hashtable;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public class ServiceContext extends InitialContext
{
	public ServiceContext() throws NamingException
	{
		super();
	}

	public ServiceContext(Hashtable<?, ?> environment) throws NamingException
	{
		super(environment);
	}

	@Override
	public Object lookup(String name) throws NamingException
	{
		return super.lookup(name);
	}
}
