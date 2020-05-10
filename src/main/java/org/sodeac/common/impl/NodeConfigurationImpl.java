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
package org.sodeac.common.impl;

import java.util.function.Function;

import org.sodeac.common.INodeConfiguration;
import org.sodeac.common.IService.IFactoryEnvironment;
import org.sodeac.common.IService.IServiceProvider;
import org.sodeac.common.annotation.ServiceFactory;
import org.sodeac.common.annotation.ServiceRegistration;
import org.sodeac.common.annotation.ServiceSatisfiedCheck;
import org.sodeac.common.annotation.Version;
import org.sodeac.common.jdbc.TypedTreeJDBCCruder;

@ServiceFactory(factoryClass=NodeConfigurationImpl.LocalServiceFactory.class)
@ServiceRegistration(serviceType=INodeConfiguration.class)
@Version(major=0,minor=6)
public class NodeConfigurationImpl implements INodeConfiguration
{
	private NodeConfigurationImpl()
	{
		super();
	}
	
	@ServiceSatisfiedCheck(trigger=ServiceSatisfiedCheck.MatchRequired.class)
	protected volatile IServiceProvider<TypedTreeJDBCCruder> cruderProvider;
	
	protected static class LocalServiceFactory implements Function<IFactoryEnvironment<?>,INodeConfiguration>
	{
		@Override
		public INodeConfiguration apply(IFactoryEnvironment<?> t)
		{
			NodeConfigurationImpl nodeConfigurationImpl = new NodeConfigurationImpl();
			return nodeConfigurationImpl;
		}	
	}
}
