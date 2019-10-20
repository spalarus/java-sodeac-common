package org.sodeac.common.message.service.impl;

import java.util.Hashtable;
import java.util.Map.Entry;

import org.sodeac.common.message.service.api.IServiceLocator;
import org.sodeac.common.message.service.api.IServiceSession;
import org.sodeac.common.message.service.api.ServiceContext;

public class DefaultServiceLocatorImpl implements IServiceLocator
{
	/*@Override
	public IServiceSession createSession(String name, Entry<Object, Object>... contextEntries)
	{
		
		Hashtable<Object, Object> environment = null;
		if((contextEntries != null) && (contextEntries.length != 0))
		{
			environment = new Hashtable<>();
			for(Entry<Object,Object> entry : contextEntries)
			{
				environment.put(entry.getKey(), entry.getValue());
			}
		}
		try
		{
			ServiceContext serviceContext = new ServiceContext(environment);
			return (IServiceSession)serviceContext.lookup(name);
		}
		catch (Exception e) 
		{
			// TODO 
			e.printStackTrace();
			return null;
		}
		
	}*/
	
	

}
