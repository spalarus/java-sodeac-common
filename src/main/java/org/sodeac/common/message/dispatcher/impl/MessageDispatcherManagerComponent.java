package org.sodeac.common.message.dispatcher.impl;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component(immediate=true)
public class MessageDispatcherManagerComponent
{
	@Activate
	public void activate()
	{
		MessageDispatcherManagerImpl.get();
	}
	
	@Deactivate
	public void deactivate()
	{
		((MessageDispatcherManagerImpl)MessageDispatcherManagerImpl.get()).shutdownAllDispatcher(); 
	}
}
