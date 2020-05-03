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
package org.sodeac.common;

import java.util.Optional;
import java.util.function.Supplier;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.sodeac.common.misc.Version;
import org.sodeac.common.xuri.URI;
import org.sodeac.common.xuri.ldapfilter.IFilterItem;
import org.sodeac.common.xuri.ldapfilter.FilterBuilder;

public interface IService
{
	public static URI URI_SERVICE_LOCATOR_SERVICE_REGISTRY = ServiceSelectorAddress.newBuilder() 
			.forDomain("sodeac.org")
			.withServiceName("localserviceregistry")
			.setFilter
			(
				FilterBuilder.andLinker()
					.criteriaWithName("version").gte(new Version(1).toString())
					.criteriaWithName("version").notGte(new Version(2).toString())
				.build()
			)
			.build();
	
	/**
	 * Get service provider 
	 * 
	 * @param clazz type of service
	 * @param address address of registration encoded in URI
	 * 
	 * @return service provider
	 */
	public <S> IServiceProvider<S> getServiceProvider(Class<S> clazz, URI address);
	
	public IInjector getInjector();
	
	public interface IInjector
	{
		public void injectMembers(Object instance);
	}
	
	public interface IServiceReference<S> extends Supplier<S>,AutoCloseable{}
	
	public interface IServiceProvider<S>
	{
		public IServiceReference<S> getService(); // TODO getReference ??? get ServiceReference ???? 
		
		public Optional<IServiceReference<S>> getOptionalService();
		
		public IServiceProvider<S> setAutoDisconnectTime(long ms);
		
		public IServiceProvider<S> disconnect();
	}
	
	public interface IServiceRegistry
	{
		/**
		 * Register a Service.
		 * 
		 * @param type type of service
		 * @param address address of registration encoded in URI
		 * @param serviceReference supplier for service instance
		 * 
		 * @return registration object
		 */
		public <S> IServiceRegistration registerService(Class<S> type, URI address, Supplier<? extends S> serviceReference);
		
		
		
		/**
		 * 
		 * 
		 * @author Sebastian Palarus
		 *
		 */
		public interface IServiceRegistration
		{
			// TODO pause / resume / close
			// 
			
			public void close(); // no IServiceProvider.get() ... anymore / providers update to another Service
			public void dispose(); // hard unregister
		}
	}
	
	public class ServiceSelectorAddress
	{
		private ServiceSelectorAddress()
		{
			super();
			this.uriBuilder = new StringBuilder("sdc://serviceselector:"); 
		}
		
		private StringBuilder uriBuilder = null; 
		
		public static ServiceSelectorAddress newBuilder()
		{
			return new ServiceSelectorAddress();
		}
		
		public SelectorAddressName forDomain(String domain)
		{
			uriBuilder.append(domain + "/");
			return new SelectorAddressName();
		}
		
		public class SelectorAddressName
		{
			public SelectorAddressFilter withServiceName(String serviceName)
			{
				uriBuilder.append(serviceName);
				return new SelectorAddressFilter();
			}
			
			public class SelectorAddressFilter
			{
				private boolean preferencesPathItem = false;
				
				public URI build()
				{
					return new URI(uriBuilder.toString());
				}
				
				public PreferenceAddressFilter setFilter(IFilterItem filter)
				{
					if(filter != null)
					{
						uriBuilder.append(filter.toString());
					}
					return new PreferenceAddressFilter();
				}
				
				public PreferenceAddressFilter.PreferenceAddressFilterScore scoreThePreferenceFilter(IFilterItem filter)
				{
					return new PreferenceAddressFilter().new PreferenceAddressFilterScore(filter);
				}
				
				public class PreferenceAddressFilter
				{ 
					public URI build()
					{
						return SelectorAddressFilter.this.build();
					}
					
					public PreferenceAddressFilterScore scoreThePreferenceFilter(IFilterItem filter)
					{
						return new PreferenceAddressFilterScore(filter);
					}
					
					
					public class PreferenceAddressFilterScore
					{
						private IFilterItem filter = null;
						
						private PreferenceAddressFilterScore(IFilterItem filter)
						{
							super();
							this.filter = filter;
						}
						
						public PreferenceAddressFilterScoreSatisfied with(int counts)
						{
							return new PreferenceAddressFilterScoreSatisfied(counts);
						}
						
						public class PreferenceAddressFilterScoreSatisfied
						{
							private int counts;
							private PreferenceAddressFilterScoreSatisfied(int counts)
							{
								super();
								this.counts = counts;
							}
							
							public PreferenceAddressFilter points()
							{
								if(! preferencesPathItem)
								{
									preferencesPathItem = true;
									uriBuilder.append("/preferences");
								}
								uriBuilder.append(Json.createObjectBuilder().add("score", counts).add("filter", filter.toString()).build().toString());
								return PreferenceAddressFilter.this;
							}
						}
					}
				}
			}
		}
	}
	
	public class ServiceRegistrationAddress
	{
		private ServiceRegistrationAddress()
		{
			super();
			this.uriBuilder = new StringBuilder("sdc://serviceaddress:"); 
		}
		
		private StringBuilder uriBuilder = null; 
		
		public static ServiceRegistrationAddress newBuilder()
		{
			return new ServiceRegistrationAddress();
		}
		
		public RegistrationAddressName forDomain(String domain)
		{
			uriBuilder.append(domain + "/");
			return new RegistrationAddressName();
		}
		
		public class RegistrationAddressName
		{
			public RegistrationAddressVersion withServiceName(String serviceName)
			{
				uriBuilder.append(serviceName + "/");
				return new RegistrationAddressVersion();
			}
			
			
			public class RegistrationAddressVersion
			{
				
				public RegistrationAddressOptions andVersion(int major, int minor, int service)
				{
					uriBuilder.append(major + "." + minor + "." + service +"/options");
					return new RegistrationAddressOptions();
				}
				
				public class RegistrationAddressOptions
				{
					private JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
					
					public RegistrationAddressOptions addOption(String name, String value)
					{
						this.jsonBuilder.add(name, value);
						return this;
					}
					
					public RegistrationAddressOptions addOption(String name, boolean value)
					{
						this.jsonBuilder.add(name, value);
						return this;
					}
					
					public RegistrationAddressOptions addOption(String name, long value)
					{
						this.jsonBuilder.add(name, value);
						return this;
					}
					
					public RegistrationAddressOptions addOption(String name, double value)
					{
						this.jsonBuilder.add(name, value);
						return this;
					}
					
					public URI build()
					{
						return new URI(uriBuilder.toString() + jsonBuilder.build());
					}
				}
			}
		}
	}
	
}
