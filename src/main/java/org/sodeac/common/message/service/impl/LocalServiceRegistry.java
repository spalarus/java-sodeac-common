/*******************************************************************************
 * Copyright (c) 2020 Sebastian Palarus
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Sebastian Palarus - initial API and implementation
 *******************************************************************************/
package org.sodeac.common.message.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.osgi.service.component.annotations.Component;
import org.sodeac.common.message.service.api.IServiceConnection;
import org.sodeac.common.message.service.api.IServiceConnector;
import org.sodeac.common.message.service.api.IServiceRegistry;
import org.sodeac.common.misc.Driver;
import org.sodeac.common.misc.Driver.IDriver;
import org.sodeac.common.xuri.URI;

@Component(service=IServiceRegistry.class)
public class LocalServiceRegistry implements IServiceRegistry
{
	
	@Override
	public int driverIsApplicableFor(Map<String, Object> properties)
	{
		return IDriver.APPLICABLE_DEFAULT;
	}

	@Override
	public void registerLocalService(URI serviceURI, Consumer<IServiceConnection> setup)
	{
		
		
	}

	@Override
	public IServiceConnection lookupLocalService(URI serviceURI)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void unegisterLocalService(Consumer<IServiceConnection> setup)
	{
		// TODO Auto-generated method stub
		
	}
}
