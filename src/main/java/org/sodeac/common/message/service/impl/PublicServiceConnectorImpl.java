package org.sodeac.common.message.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.sodeac.common.message.service.api.IServiceConnection;
import org.sodeac.common.message.service.api.IServiceConnector;
import org.sodeac.common.misc.Driver;
import org.sodeac.common.misc.Driver.IDriver;
import org.sodeac.common.xuri.URI;

@Component(service=IServiceConnector.class,immediate=true)
public class PublicServiceConnectorImpl implements IServiceConnector
{
	private volatile IServiceConnector localConnector = null;
	
	@Override
	public int driverIsApplicableFor(Map<String, Object> properties)
	{
		if((properties == null) || properties.isEmpty())
		{
			return IDriver.APPLICABLE_DEFAULT;
		}
		return IDriver.APPLICABLE_NONE;
	}

	@Override
	public IServiceConnection lookup(URI serviceURI)
	{
		IServiceConnector localConnector = this.localConnector;
		if(localConnector == null)
		{
			Map<String,Object> properties = new HashMap<String, Object>();
			properties.put(IDriver.TYPE, IServiceConnector.TYPE_LOCAL);
			localConnector = Driver.getSingleDriver(IServiceConnector.class, properties);
			if(localConnector != null)
			{
				this.localConnector = localConnector;
			}
			else
			{
				return null;
			}
		}
		IServiceConnection serviceConnection = localConnector.lookup(serviceURI);
		return serviceConnection;
	}

}
