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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.sodeac.common.IService.IServiceProvider;
import org.sodeac.common.IService.IServiceRegistry;
import org.sodeac.common.IService.ServiceRegistrationAddress;
import org.sodeac.common.function.ConplierBean;
import org.sodeac.common.misc.Version;
import org.sodeac.common.xuri.IExtension;
import org.sodeac.common.xuri.PathSegment;
import org.sodeac.common.xuri.URI;
import org.sodeac.common.xuri.URISyntaxException;
import org.sodeac.common.xuri.json.JsonExtension;
import org.sodeac.common.xuri.ldapfilter.DefaultMatchableWrapper;
import org.sodeac.common.xuri.ldapfilter.IFilterItem;
import org.sodeac.common.xuri.ldapfilter.IMatchable;
import org.sodeac.common.xuri.ldapfilter.LDAPFilterDecodingHandler;
import org.sodeac.common.xuri.ldapfilter.LDAPFilterEncodingHandler;
import org.sodeac.common.xuri.ldapfilter.LDAPFilterExtension;

public class LocalServiceRegistryImpl implements IServiceRegistry
{
	private static LocalServiceRegistryImpl INSTANCE = null;
	private Lock lock;
	
	private Map<String,Map<String,Map<Class,ServiceController>>> registeredServices = null;
	private LocalServiceRegistryImpl()
	{
		super();
		this.lock = new ReentrantLock();
		this.registeredServices = new HashMap<>();
	}
	
	protected static LocalServiceRegistryImpl get()
	{
		LocalServiceRegistryImpl instance = INSTANCE;
		if(instance != null)
		{
			return instance;
		}
		
		synchronized (LocalServiceRegistryImpl.class)
		{
			instance = INSTANCE;
			if(instance == null)
			{
				instance = new LocalServiceRegistryImpl();
				instance.registerService
				(
					IServiceRegistry.class,
					ServiceRegistrationAddress.newBuilder().forDomain("sodeac.org").withServiceName("localserviceregistry").andVersion(1, 0, 0).addOption("systemservice", true).build(), 
					new ConplierBean<>(instance)
				);
				INSTANCE = instance;
			}
			return instance;
		}
	}
	
	@Override
	public <S> IServiceRegistration registerService(Class<S> type, URI address, Supplier<? extends S> serviceReference)
	{
		Objects.requireNonNull(type, "Missing type");
		Objects.requireNonNull(address, "Missing address");
		Objects.requireNonNull(serviceReference, "Missing service reference");
		
		RegisteredService service = new RegisteredService<>(type, address, serviceReference);
		ServiceController serviceController = null;
		lock.lock();
		try
		{
			Map<String,Map<Class,ServiceController>> byDomain = this.registeredServices.get(service.domain);
			if(byDomain == null)
			{
				byDomain = new HashMap<>();
				this.registeredServices.put(service.domain, byDomain);
			}
			Map<Class,ServiceController> byServiceName = byDomain.get(service.serviceName);
			if(byServiceName == null)
			{
				byServiceName = new HashMap<>();
				byDomain.put(service.serviceName,byServiceName);
			}
			serviceController = byServiceName.get(service.type);
			if(serviceController == null)
			{
				serviceController = new ServiceController(type);
				byServiceName.put(service.type, serviceController);
			}
		}
		finally 
		{
			lock.unlock();
		}
		
		service.serviceController = serviceController;
		service.serviceRegistration = service.new ServiceRegistration();
		serviceController.addService(service);
		
		return service.serviceRegistration;
	}
	
	protected <S> IServiceProvider<S> getServiceProvider(Class<S> type, URI serviceAddress)
	{
		if(! serviceAddress.getScheme().getValue().equalsIgnoreCase("sdc"))
		{
			throw new URISyntaxException(serviceAddress.toString(), "expected schema is 'sdc', and not " + serviceAddress.getScheme().getValue());
		}
		
		if((serviceAddress.getAuthority().getSubComponentList().size() > 2) || (serviceAddress.getAuthority().getSubComponentList().size() < 1))
		{
			throw new URISyntaxException(serviceAddress.toString(), "wrong authority size");
		}
		
		String domain = null;
		
		if(serviceAddress.getAuthority().getSubComponentList().size() == 2)
		{
			if(! serviceAddress.getAuthority().getSubComponentList().get(0).getValue().equalsIgnoreCase("serviceselector"))
			{
				throw new URISyntaxException(serviceAddress.toString(), "expected authority type is 'serviceselector', and not '" + serviceAddress.getAuthority().getSubComponentList().get(0).getValue() + "'");
			}
			domain = serviceAddress.getAuthority().getSubComponentList().get(1).getValue();
		}
		else
		{
			domain = serviceAddress.getAuthority().getSubComponentList().get(0).getValue();
		}
		
		if((domain == null) || domain.isEmpty())
		{
			throw new URISyntaxException(serviceAddress.toString(), "domain of service not defined");
		}
		
		if(serviceAddress.getPath().getSubComponentList().size() == 0 )
		{
			throw new URISyntaxException(serviceAddress.toString(), "wrong path size");
		}
		
		String serviceName = serviceAddress.getPath().getSubComponentList().get(0).getValue();
				
		if(serviceName.isEmpty())
		{
			throw new URISyntaxException(serviceAddress.toString(), "name of service not defined");
		}
		
		// Collect HardConstraint
		
		List<IFilterItem> filterList = new ArrayList<>();
		for(IExtension extension  : serviceAddress.getPath().getSubComponentList().get(0).getExtensionList(LDAPFilterExtension.TYPE))
		{
			IFilterItem filterItem = (IFilterItem)extension.getDecoder().decodeFromString(extension.getExpression());
			filterList.add(filterItem);
		}
		
		// Collect SoftConstraint
		
		Map<Long,List<IFilterItem>> preferencesList = new HashMap<Long,List<IFilterItem>>();
		
		for(int i = 1; i < serviceAddress.getPath().getSubComponentList().size(); i++)
		{
			PathSegment pathSegment = serviceAddress.getPath().getSubComponentList().get(i);
			if(! "preferences".equalsIgnoreCase(pathSegment.getValue()))
			{
				continue;
			}
			
			for(IExtension extension  : pathSegment.getExtensionList(JsonExtension.TYPE))
			{
				JsonObject jsonObject = (JsonObject)extension.getDecoder().decodeFromString(extension.getExpression());
				
				long score = jsonObject.getJsonNumber("score").longValue();
				IFilterItem filterItem = LDAPFilterDecodingHandler.getInstance().decodeFromString(jsonObject.getString("filter"));
				
				List<IFilterItem> preferences = preferencesList.get(score);
				if(preferences == null)
				{
					preferences = new ArrayList<>();
					preferencesList.put(score,preferences);
				}
				
				preferences.add(filterItem);
			}
		}
		
		ServiceController serviceController = null;
		lock.lock();
		try
		{
			Map<String,Map<Class,ServiceController>> byDomain = this.registeredServices.get(domain);
			if(byDomain == null)
			{
				byDomain = new HashMap<>();
				this.registeredServices.put(domain, byDomain);
			}
			Map<Class,ServiceController> byServiceName = byDomain.get(serviceName);
			if(byServiceName == null)
			{
				byServiceName = new HashMap<>();
				byDomain.put(serviceName,byServiceName);
			}
			serviceController = byServiceName.get(type);
			if(serviceController == null)
			{
				serviceController = new ServiceController(type);
				byServiceName.put(type, serviceController);
			}
		}
		finally 
		{
			lock.unlock();
		}
		
		//RegisteredService registeredService = serviceController.getRegisteredService(filterList, preferencesList);
		
		LocalServiceProviderImpl<S> serviceProvider = new LocalServiceProviderImpl<>(serviceController, filterList, preferencesList);
		serviceController.localServiceProvider.add(serviceProvider);
		
		return serviceProvider;
	}
	
	protected class ServiceController
	{
		public ServiceController(Class type)
		{
			super();
			this.type = type;
			this.lock = new ReentrantLock();
		}
		
		private Lock lock = null;
		private Class type = null;
		private List<RegisteredService> serviceList = new ArrayList<>();
		private List<LocalServiceProviderImpl> localServiceProvider = new ArrayList<>();
		
		protected RegisteredService getRegisteredService(List<IFilterItem> filterList, Map<Long,List<IFilterItem>> preferencesList)
		{
			List<RegisteredService> copy = null;
			this.lock.lock();
			try
			{
				
				if(this.serviceList.isEmpty())
				{
					return null;
				}
				
				copy = new ArrayList<>(this.serviceList);
			}
			finally 
			{
				this.lock.unlock();
			}
			
			long bestScore = Long.MIN_VALUE;
			RegisteredService bestService = null;
			
			for(RegisteredService registeredService : copy)
			{
				if(filterList != null)
				{
					for(IFilterItem filterItem : filterList)
					{
						if(filterItem ==  null)
						{
							continue;
						}
						
						if(! filterItem.matches(registeredService.options))
						{
							break;
						}
					}
				}
				
				long currentScore = 0;
				
				if(preferencesList != null)
				{
					for(Entry<Long,List<IFilterItem>> preferencesEntry : preferencesList.entrySet())
					{
						if(preferencesEntry.getKey() == null)
						{
							continue;
						}
						if(preferencesEntry.getValue() == null)
						{
							continue;
						}
						if(preferencesEntry.getValue().isEmpty())
						{
							continue;
						}
						
						for(IFilterItem filter : preferencesEntry.getValue())
						{
							if(filter.matches(registeredService.options))
							{
								currentScore += preferencesEntry.getKey();
							}
						}
					}
				}
				
				if(currentScore > bestScore)
				{
					bestService = registeredService;
					bestScore = currentScore;
				}
			}
			
			copy.clear();
			copy = null;
			
			return bestService;
			
		}
		
		private void addService(RegisteredService registeredService)
		{
			this.lock.lock();
			try
			{
				for(RegisteredService check : this.serviceList)
				{
					if(check == this.serviceList)
					{
						return;
					}
					
					if(check.serviceReference == registeredService.serviceReference)
					{
						return;
					}
				}
				
				this.serviceList.add(registeredService);
			}
			finally 
			{
				this.lock.unlock();
			}
		}
	}
	
	protected class RegisteredService<S>
	{
		protected RegisteredService(Class<S> type, URI address, Supplier<? extends S> serviceReference)
		{
			super();
			this.registrationAddress = address;
			this.type = type;
			this.serviceReference = serviceReference;
			this.parse();
		}
		
		private void parse()
		{
			if(! registrationAddress.getScheme().getValue().equalsIgnoreCase("sdc"))
			{
				throw new URISyntaxException(registrationAddress.toString(), "expected schema is 'sdc', and not " + registrationAddress.getScheme().getValue());
			}
			
			if((registrationAddress.getAuthority().getSubComponentList().size() > 2) || (registrationAddress.getAuthority().getSubComponentList().size() < 1))
			{
				throw new URISyntaxException(registrationAddress.toString(), "wrong authority size");
			}
			
			if(registrationAddress.getAuthority().getSubComponentList().size() == 2)
			{
				if(! registrationAddress.getAuthority().getSubComponentList().get(0).getValue().equalsIgnoreCase("serviceaddress"))
				{
					throw new URISyntaxException(registrationAddress.toString(), "expected authority type is 'serviceaddress', and not '" + registrationAddress.getAuthority().getSubComponentList().get(0).getValue() + "'");
				}
				domain = registrationAddress.getAuthority().getSubComponentList().get(1).getValue();
			}
			else
			{
				domain = registrationAddress.getAuthority().getSubComponentList().get(0).getValue();
			}
			
			if((domain == null) || domain.isEmpty())
			{
				throw new URISyntaxException(registrationAddress.toString(), "domain of service not defined");
			}
			
			if(registrationAddress.getPath().getSubComponentList().size() != 3 )
			{
				throw new URISyntaxException(registrationAddress.toString(), "wrong path size");
			}
			
			serviceName = registrationAddress.getPath().getSubComponentList().get(0).getValue();
					
			if(serviceName.isEmpty())
			{
				throw new URISyntaxException(registrationAddress.toString(), "name of service not defined");
			}
			
			options = new HashMap<String, IMatchable>();
			
			version = Version.fromString(registrationAddress.getPath().getSubComponentList().get(1).getValue());
			
			options.put("version", new DefaultMatchableWrapper(version));
			
			if(! registrationAddress.getPath().getSubComponentList().get(2).getValue().equalsIgnoreCase("options"))
			{
				throw new URISyntaxException(registrationAddress.toString(), "service options not defined");
			}
			
			IExtension<JsonObject> jsonExtension = (IExtension<JsonObject>)registrationAddress.getPath().getSubComponentList().get(2).getExtension(JsonExtension.TYPE);
			if(jsonExtension != null)
			{
				jsonObject = jsonExtension.getDecoder().decodeFromString(jsonExtension.getExpression());
				for(Entry<String,JsonValue> jsonItem : jsonObject.entrySet())
				{
					if(jsonItem.getValue().getValueType() == ValueType.NULL)
					{
						options.put(jsonItem.getKey(), new DefaultMatchableWrapper(null));
					}
					else if(jsonItem.getValue().getValueType() == ValueType.TRUE)
					{
						options.put(jsonItem.getKey(), new DefaultMatchableWrapper(true));
					}
					else if(jsonItem.getValue().getValueType() == ValueType.FALSE)
					{
						options.put(jsonItem.getKey(), new DefaultMatchableWrapper(false));
					}
					else if(jsonItem.getValue().getValueType() == ValueType.NUMBER)
					{
						if(((JsonNumber)jsonItem.getValue()).isIntegral())
						{
							options.put(jsonItem.getKey(), new DefaultMatchableWrapper(((JsonNumber)jsonItem.getValue()).longValue()));
						}
						else
						{
							options.put(jsonItem.getKey(), new DefaultMatchableWrapper(((JsonNumber)jsonItem.getValue()).doubleValue()));
						}
					}
					else if(jsonItem.getValue().getValueType() == ValueType.STRING)
					{
						options.put(jsonItem.getKey(), new DefaultMatchableWrapper(((JsonString)jsonItem.getValue()).getString()));
					}
				}
			}
			
		}
		
		private URI registrationAddress = null;
		private Supplier<? extends S> serviceReference = null;
		private Class<S> type = null;
		
		private String domain = null;
		private String serviceName = null;
		private Version version = null;
		private Map<String,IMatchable> options = null;
		private JsonObject jsonObject = null;
		private ServiceController serviceController = null;
		
		private ServiceRegistration serviceRegistration = null;
		
		protected <S> S supply()
		{
			return (S)this.serviceReference.get();
		}
		
		protected class ServiceRegistration implements IServiceRegistration
		{

			@Override
			public void close()
			{
				// TODO Auto-generated method stub
			}

			@Override
			public void dispose()
			{
				// TODO Auto-generated method stub		
			}
		}
		
	}
}
