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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.sodeac.common.IService.IFactoryEnvironment;
import org.sodeac.common.annotation.BooleanProperty;
import org.sodeac.common.annotation.DecimalProperty;
import org.sodeac.common.annotation.IntegerProperty;
import org.sodeac.common.annotation.ServiceRegistration;
import org.sodeac.common.annotation.StringProperty;
import org.sodeac.common.annotation.ServiceFactory.NoRequiredConfiguration;
import org.sodeac.common.impl.LocalServiceRegistryImpl;
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
	
	public static final String REPLACED_BY_CLASS_NAME = "<REPLACED__BY__CLASS__NAME>";
	public static final String REPLACED_BY_PACKAGE_NAME = "<REPLACED__BY__PACKAGE__NAME>";
	
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
	
	public interface IServiceReference<S> extends Supplier<S>,AutoCloseable
	{
		public IServiceReference<S> getServiceProvider();
	}
	
	public interface IServiceProvider<S>
	{
		public IServiceReference<S> getService(); // TODO getReference ??? get ServiceReference ???? 
		
		// TODO public Optional<IServiceReference<S>> getOptionalService();
		
		public IServiceProvider<S> setAutoDisconnectTime(long ms);
		
		public IServiceProvider<S> disconnect();
		
		public Object getClient();
		
		public boolean isMatched();
	}
	
	public interface IOnServiceReferenceAttach<S>
	{
		public void onServiceReferenceAttach(IServiceReference<S> serviceReference);
	}
	
	public interface IOnServiceReferenceDetach<S>
	{
		public void onServiceReferenceDetach(IServiceReference<S> serviceReference);
	}
	
	public interface IServiceRegistry
	{
		/**
		 * 
		 * register a service
		 * 
		 * @param serviceFactoryPolicy policy to manage service instance
		 * @param serviceRegistrationAddresses addresses at which the service is registered
		 * @return registration object
		 */
		public IServiceRegistration registerService(ServiceFactoryPolicy serviceFactoryPolicy, ServiceRegistrationAddress... serviceRegistrationAddresses);		
		
		
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
	
	public interface IFactoryEnvironment<S,C>
	{
		public Class<?> getReferenceClass();
		public Class<S> getServiceClass();
		public C getConfiguration();
		public IServiceProvider<S> getInitialServiceProvider();
		public boolean isRequireConfiguration();
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
	
	public class ServiceFactoryPolicy
	{
		private ServiceFactoryPolicy()
		{
			super();
		}
		
		public static ServiceFactoryPolicy.FactoryPolicyBuilder newBuilder()
		{
			return new FactoryPolicyBuilder();
		}
		
		private int lowerScalingLimit = 1;
		private int upperScalingLimit = 1;
		private int initialScaling = 0; 
		private boolean shared = true;
		
		private Class<?> requiredConfigurationClass = null;
		private Function<IFactoryEnvironment<?,?>,?> factory = null;
		
		private Map<String,Object> options = null;
		
		public int getLowerScalingLimit()
		{
			return lowerScalingLimit;
		}

		public int getUpperScalingLimit()
		{
			return upperScalingLimit;
		}

		public int getInitialScaling()
		{
			return initialScaling;
		}

		public boolean isShared()
		{
			return shared;
		}

		public Class<?> getRequiredConfigurationClass()
		{
			return requiredConfigurationClass;
		}

		public Function<IFactoryEnvironment<?, ?>, ?> getFactory()
		{
			return factory;
		}

		public Map<String, Object> getOptions()
		{
			return options;
		}



		public static class FactoryPolicyBuilder
		{
			private FactoryPolicyBuilder()
			{
				super();
				this.options = new HashMap<String, Object>();
			}
			
			// Scaling
			
			private int lowerScalingLimit = 1;
			private int upperScalingLimit = 1;
			private int initialScaling = 0; 
			
			// Share
			
			private boolean shared = true;
			
			// Factory
			
			private Class<?> requiredConfigurationClass = null;
			private Function<IFactoryEnvironment<?,?>,?> factory = null;
			
			// Properties
			private Map<String,Object> options = null;
			
			public BuilderScalingLowerLimit1 defineServiceScalingLimits()
			{
				return new BuilderScalingLowerLimit1();
			}
			
			public BuilderShared defineServiceAsLazyLoadingSingleton()
			{
				this.lowerScalingLimit = 1;
				this.upperScalingLimit = 1;
				this.initialScaling = 0; 
				
				return new BuilderShared();
			}
			
			public BuilderShared defineServiceAsSingletonWithAutoCreation()
			{
				this.lowerScalingLimit = 1;
				this.upperScalingLimit = 1;
				this.initialScaling = 1; 
				
				return new BuilderShared();
			}
			
			public class BuilderScalingLowerLimit1
			{
				public BuilderScalingLowerLimit2 forTheLowerLimitDefine(int lowerLimit)
				{
					FactoryPolicyBuilder.this.lowerScalingLimit = lowerLimit;
					return new BuilderScalingLowerLimit2();
				}
				
				public BuilderScalingLowerLimit2.BuilderScalingUpperLimit1 forTheLowerLimitDefineOneInstance()
				{
					FactoryPolicyBuilder.this.lowerScalingLimit = 1;
					return new BuilderScalingLowerLimit2().new BuilderScalingUpperLimit1();
				}
				
				
				public class BuilderScalingLowerLimit2
				{
					public BuilderScalingUpperLimit1 instances()
					{
						return new BuilderScalingUpperLimit1();
					}
				
					public class BuilderScalingUpperLimit1
					{
						public BuilderScalingUpperLimit2 forTheUpperLimitDefine(int upperLimit)
						{
							FactoryPolicyBuilder.this.upperScalingLimit = upperLimit;
							return new BuilderScalingUpperLimit2();
						}
						
						public BuilderScalingUpperLimit2.BuilderScalingInitialSize1 forTheUpperLimitDefineOneInstance()
						{
							FactoryPolicyBuilder.this.upperScalingLimit = 1;
							return new BuilderScalingUpperLimit2().new BuilderScalingInitialSize1();
						}
						
						public class BuilderScalingUpperLimit2
						{
							public BuilderScalingInitialSize1 instances()
							{
								return new BuilderScalingInitialSize1();
							}
							
							public class BuilderScalingInitialSize1
							{
								public BuilderScalingInitialSize2 initializeServiceWith(int initialScaling)
								{
									FactoryPolicyBuilder.this.initialScaling = initialScaling;
									return new BuilderScalingInitialSize2();
								}
								
								public BuilderShared initializeServiceWithOneInstance()
								{
									FactoryPolicyBuilder.this.initialScaling = 1;
									return new BuilderShared();
								}
								
								public class BuilderScalingInitialSize2
								{
									public BuilderShared instances()
									{
										return new BuilderShared();
									}
								}
							}
						}
					}
				}
			}
			
			public class BuilderShared
			{
				public BuilderFactory1 supplyServiceInstanceToMultipleServiceClients()
				{
					FactoryPolicyBuilder.this.shared = true;
					return new BuilderFactory1();
				}
				public BuilderFactory1 supplyServiceInstanceToOneSingleServiceClientOnly()
				{
					FactoryPolicyBuilder.this.shared = false;
					return new BuilderFactory1();
				}
			}
			
			public class BuilderFactory1
			{
				public BuilderFactory2 applyConstructorForNewServiceInstance()
				{
					FactoryPolicyBuilder.this.factory = e -> new LocalServiceRegistryImpl.DefaultFactory().apply(e);
					return new BuilderFactory2();
				}
				
				public  BuilderFactory1b applyFactory(Function<IFactoryEnvironment<?,?>,?> factory)
				{
					FactoryPolicyBuilder.this.factory = factory;
					return new BuilderFactory1b();
				}
				
				public class BuilderFactory1b
				{
					public BuilderFactory2 forNewServiceInstance()
					{
						return new BuilderFactory2();
					}
				}
				
				public class BuilderFactory2
				{
					public BuilderOptions newServiceInstanceRequiresConfiguration(Class<?> configurationClass)
					{
						FactoryPolicyBuilder.this.requiredConfigurationClass = configurationClass;
						return new BuilderOptions();
					}
					
					public BuilderOptions addOption(String name, String value)
					{
						FactoryPolicyBuilder.this.options.put(name, value);
						return new BuilderOptions();
					}
					
					public BuilderOptions addOption(String name, boolean value)
					{
						return new BuilderOptions().addOption(name, value);
					}
					
					public BuilderOptions addOption(String name, long value)
					{
						return new BuilderOptions().addOption(name, value);
					}
					
					public BuilderOptions addOption(String name, double value)
					{
						return new BuilderOptions().addOption(name, value);
					}
					
					public ServiceFactoryPolicy build()
					{
						return FactoryPolicyBuilder.this.build();
					}
				}
			}
			
			public class BuilderOptions
			{
				public BuilderOptions addOption(String name, String value)
				{
					FactoryPolicyBuilder.this.options.put(name, value);
					return this;
				}
				
				public BuilderOptions addOption(String name, boolean value)
				{
					FactoryPolicyBuilder.this.options.put(name, value);
					return this;
				}
				
				public BuilderOptions addOption(String name, long value)
				{
					FactoryPolicyBuilder.this.options.put(name, value);
					return this;
				}
				
				public BuilderOptions addOption(String name, double value)
				{
					FactoryPolicyBuilder.this.options.put(name, value);
					return this;
				}
				
				public ServiceFactoryPolicy build()
				{
					return FactoryPolicyBuilder.this.build();
				}
			}
			
			private ServiceFactoryPolicy build()
			{
				ServiceFactoryPolicy serviceFactoryPolicy = new ServiceFactoryPolicy();
				serviceFactoryPolicy.lowerScalingLimit = this.lowerScalingLimit;
				serviceFactoryPolicy.upperScalingLimit = this.upperScalingLimit;
				serviceFactoryPolicy.initialScaling = this.initialScaling;
				serviceFactoryPolicy.shared = this.shared;
				
				serviceFactoryPolicy.requiredConfigurationClass = this.requiredConfigurationClass;
				serviceFactoryPolicy.factory = this.factory;
				
				serviceFactoryPolicy.options = Collections.unmodifiableMap(new HashMap<String,Object>(this.options));
				return serviceFactoryPolicy;
			}
		}
	}
	
	public class ServiceRegistrationAddress
	{
		private ServiceRegistrationAddress()
		{
			super();
		}
		
		public static ServiceRegistrationAddressBuilder newBuilder()
		{
			return new ServiceRegistrationAddressBuilder();
		}
		
		private String domain = null;
		private String name = null;
		private Version version = null;
		private List<Class<?>> types = null;
		
		public String getDomain()
		{
			return domain;
		}

		public String getName()
		{
			return name;
		}

		public Version getVersion()
		{
			return version;
		}

		public List<Class<?>> getTypes()
		{
			return types;
		}

		public static class ServiceRegistrationAddressBuilder
		{
			private ServiceRegistrationAddressBuilder()
			{
				super();
				this.types = new ArrayList<Class<?>>();
			}
			
			private String domain = null;
			private String name = null;
			private Version version = null;
			private List<Class<?>> types = null;
			
			public RegistrationAddressName forDomain(String domain)
			{
				ServiceRegistrationAddressBuilder.this.domain = domain;
				return new RegistrationAddressName();
			}
			
			public class RegistrationAddressName
			{
				public RegistrationAddressVersion withServiceName(String serviceName)
				{
					ServiceRegistrationAddressBuilder.this.name = serviceName;
					return new RegistrationAddressVersion();
				}
				
				public class RegistrationAddressVersion
				{
					
					public RegistrationAddressTypes andVersion(int major, int minor, int service)
					{
						ServiceRegistrationAddressBuilder.this.version = new Version(major,major,service);
						return new RegistrationAddressTypes();
					}
					
					public class RegistrationAddressTypes
					{
						public RegistrationAddressTypes addType(Class<?> type)
						{
							ServiceRegistrationAddressBuilder.this.types.add(type);
							return this;
						}
						
						public ServiceRegistrationAddress build()
						{
							ServiceRegistrationAddress address = new ServiceRegistrationAddress();
							address.domain = ServiceRegistrationAddressBuilder.this.domain;
							address.name = ServiceRegistrationAddressBuilder.this.name;
							address.version = ServiceRegistrationAddressBuilder.this.version;
							address.types = Collections.unmodifiableList(new ArrayList<Class<?>>(ServiceRegistrationAddressBuilder.this.types));
							return address;
						}
					}
					
					/*public class RegistrationAddressOptions
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
					}*/
				}
			}
		}
	}
	
}
