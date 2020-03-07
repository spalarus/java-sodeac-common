package org.sodeac.common.impl;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.sodeac.common.message.service.api.IServiceConnection;
import org.sodeac.common.message.service.api.IServiceRegistry;
import org.sodeac.common.message.service.api.ISessionDrivenConnectionFactory;
import org.sodeac.common.misc.Driver;
import org.sodeac.common.xuri.URI;

@Component(service=ISessionDrivenConnectionFactory.class)
public class LogMessageServiceConnectionFactory implements ISessionDrivenConnectionFactory
{
	
	@Activate
	protected void activate()
	{
		//Driver.getSingleDriver(IServiceRegistry.class, null).registerLocalService(getServiceURI(), this::setup);
		
		// TODO : Driver mit serviceURI???? => ISessionDrivenConnectionFactory implementiert driverIsApplicableFor mit Hilfe der URI 
		// Dann ist serviceRegistry aber absolete
		// Bewertungssystem für PropertyMatch
	}
	
	@Deactivate
	protected void deactivate()
	{
		
	}

	@Override
	public URI getServiceURI()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setup(IServiceConnection connection)
	{
		// TODO Auto-generated method stub

	}

}
