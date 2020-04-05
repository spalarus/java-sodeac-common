package org.sodeac.common.message.service.impl;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.sodeac.common.message.service.api.IServiceConnection;
import org.sodeac.common.message.service.api.IServiceConnector;
import org.sodeac.common.misc.Driver;
import org.sodeac.common.misc.Driver.IDriver;
import org.sodeac.common.xuri.URI;

@Component
public class LocalServiceConnectorImpl implements IServiceConnector
{
	//private volatile IServiceRegistry serviceRegistry = null;
	

	@Override
	public int driverIsApplicableFor(Map<String, Object> properties)
	{
		if((properties == null) || properties.isEmpty())
		{
			return IDriver.APPLICABLE_NONE;
		}
		if(! IServiceConnector.TYPE_LOCAL.equals(properties.get(IDriver.TYPE)))
		{
			return IDriver.APPLICABLE_NONE;
		}
		return IDriver.APPLICABLE_DEFAULT;
	}

	@Override
	public IServiceConnection lookup(URI serviceURI)
	{
		/*IServiceRegistry serviceRegistry = this.serviceRegistry;
		if(serviceRegistry == null)
		{
			serviceRegistry = Driver.getSingleDriver(IServiceRegistry.class, null);
			if(serviceRegistry != null)
			{
				this.serviceRegistry = serviceRegistry;
			}
			else
			{
				return null;
			}
		}*/
		return null;
	}

}
