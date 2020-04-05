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
import org.sodeac.common.xuri.URI;
import org.sodeac.common.xuri.URISyntaxException;
import org.sodeac.common.xuri.json.JsonExtension;
import org.sodeac.common.xuri.ldapfilter.DefaultMatchableWrapper;
import org.sodeac.common.xuri.ldapfilter.IMatchable;

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
					ServiceRegistrationAddress.newBuilder().forDomain("sodeac.org").withServiceName("localserviceregistry").andVersion(1, 0, 0).addOption("systemservice", 1.0).build(), 
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
		service.serviceRegistration = new ServiceRegistration();
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
		
		if(serviceAddress.getPath().getSubComponentList().size() != 3 )
		{
			throw new URISyntaxException(serviceAddress.toString(), "wrong path size");
		}
		
		String serviceName = serviceAddress.getPath().getSubComponentList().get(0).getValue();
				
		if(serviceName.isEmpty())
		{
			throw new URISyntaxException(serviceAddress.toString(), "name of service not defined");
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
		
		return null;
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
		
		private void addService(RegisteredService registeredService)
		{
			this.lock.lock();
			try
			{
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
