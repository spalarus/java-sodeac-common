package org.sodeac.common.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.sodeac.common.IService.IServiceProvider;
import org.sodeac.common.impl.LocalServiceRegistryImpl.RegisteredService;
import org.sodeac.common.impl.LocalServiceRegistryImpl.ServiceController;
import org.sodeac.common.xuri.ldapfilter.IFilterItem;

public class LocalServiceProviderImpl<S> implements IServiceProvider<S>
{
	private ServiceController serviceController = null;
	private List<IFilterItem> filterList = null;
	private Map<Long,List<IFilterItem>> preferencesList = null;
	private Lock lock = null;
	private volatile RegisteredService<S> registeredService = null;
	
	protected LocalServiceProviderImpl(ServiceController serviceController, List<IFilterItem> filterList, Map<Long,List<IFilterItem>> preferencesList)
	{
		super();
		this.serviceController = serviceController;
		this.filterList = filterList;
		this.preferencesList = preferencesList;
		this.lock = new ReentrantLock();
	}
	@Override
	public S get()
	{
		RegisteredService<S> registeredService = this.registeredService;
		if(registeredService == null)
		{
			this.lock.lock();
			try
			{
				registeredService = this.registeredService;
				if(registeredService == null)
				{
					registeredService = serviceController.getRegisteredService(filterList, preferencesList);
				}
			}
			finally 
			{
				this.lock.unlock();
			}
			
		}
		return registeredService.supply();
	}

	@Override
	public Optional<S> getOptional()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IServiceProvider<S> setAutoDisconnectTime(long ms)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IServiceProvider<S> disconnect()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
