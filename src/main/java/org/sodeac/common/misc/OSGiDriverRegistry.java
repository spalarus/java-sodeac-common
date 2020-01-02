/*******************************************************************************
 * Copyright (c) 2019 Sebastian Palarus
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Sebastian Palarus - initial API and implementation
 *******************************************************************************/
package org.sodeac.common.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.sodeac.common.jdbc.IDBSchemaUtilsDriver;
import org.sodeac.common.misc.Driver.IDriver;

@Component(immediate=true,service=OSGiDriverRegistry.class)
public class OSGiDriverRegistry
{
	private ComponentContext componentContext;
	protected static OSGiDriverRegistry INSTANCE;
	private Lock lock;
	private Map<Class,DriverServiceTracker> trackerIndex = new HashMap<Class, OSGiDriverRegistry.DriverServiceTracker>();
	
	public OSGiDriverRegistry()
	{
		super();
		this.lock = new ReentrantLock();
	}
	
	@Activate
	public void activate(ComponentContext componentContext)
	{
		this.componentContext = componentContext;
		OSGiDriverRegistry.INSTANCE = this;
		
		observe(IDBSchemaUtilsDriver.class);
	}
	
	@Deactivate
	public void deactivate(ComponentContext componentContext)
	{
		List<DriverServiceTracker> values = null;
		lock.lock();
		try
		{
			values = new ArrayList<OSGiDriverRegistry.DriverServiceTracker>(trackerIndex.values());
			trackerIndex.clear();
			trackerIndex = null;
		}
		finally 
		{
			lock.unlock();
		}
		
		for(DriverServiceTracker driverServiceTracker : values)
		{
			try
			{
				driverServiceTracker.close();
			}
			catch (Exception e) {}
		}
		this.componentContext = null;
		OSGiDriverRegistry.INSTANCE = null;
	}
	
	public <T extends IDriver> void observe(Class<T> driverClass)
	{
		lock.lock();
		try
		{
			if(trackerIndex.containsKey(driverClass))
			{
				return;
			}
			
			DriverServiceTracker driverServiceTracker = new DriverServiceTracker(this.componentContext.getBundleContext(), driverClass, new Customizer());
			this.trackerIndex.put(driverClass, driverServiceTracker);
			driverServiceTracker.open(true);
		}
		finally 
		{
			lock.unlock();
		}
	}
	
	public <T extends IDriver> T getSingleDriver(Class<T> driverClass, Map<String,Object> properties)
	{
		lock.lock();
		try
		{
			DriverServiceTracker tracker = trackerIndex.get(driverClass);
			if(tracker == null)
			{
				observe(driverClass);
				tracker = trackerIndex.get(driverClass);
			}
			return (T)tracker.getSingleDriver(properties);
		}
		finally 
		{
			lock.unlock();
		}
	}
	
	protected static class DriverServiceTracker extends ServiceTracker
	{
		private Class clazz = null;
		private Lock lock = null;
		
		public DriverServiceTracker(BundleContext context, Class clazz, Customizer customizer)
		{
			super(context, clazz,customizer);
			this.clazz = clazz;
			this.lock = new ReentrantLock();
			customizer.setTracker(this);
			this.listsByClassName = new HashMap<String,List<ServiceReference>>();
		}
		
		private Map<String,List<ServiceReference>> listsByClassName = null;
		
		@Override
		public void close()
		{
			super.close();
			if(lock == null)
			{
				return;
			}
			lock.lock();
			try
			{
				listsByClassName.values().forEach(i -> i.clear());
				listsByClassName.clear();
			}
			finally 
			{
				lock.unlock();
			}
			lock = null;
			clazz = null;
			listsByClassName = null;
		}
		
		public Object getSingleDriver(Map<String,Object> properties)
		{
			if(lock == null)
			{
				return null;
			}
			lock.lock();
			try
			{
				Object bestDriver = null;
				int bestIndex = -1;
				for(List<ServiceReference> serviceReferenceList : this.listsByClassName.values())
				{
					if(serviceReferenceList.isEmpty())
					{
						continue;
					}
					Object driver = null;
					for(ServiceReference serviceReference : serviceReferenceList)
					{
						driver = serviceReference.getBundle().getBundleContext().getService(serviceReference);
						if(driver != null)
						{
							break;
						}
					}
					if(driver == null)
					{
						continue;
					}
					
					int applicableIndex = ((IDriver)driver).driverIsApplicableFor(properties);
					if(applicableIndex > bestIndex)
					{
						bestDriver = driver;
						bestIndex = applicableIndex;
					}
				}
				return bestDriver;
			}
			finally 
			{
				lock.unlock();
			}
		}
		
		public void addDriver(ServiceReference reference, Object driver)
		{
			if(driver == null)
			{
				return;
			}
			if(!(driver instanceof IDriver))
			{
				return;
			}
			if(!this.clazz.isInstance(driver))
			{
				return;
			}
			if(lock == null)
			{
				return;
			}
			lock.lock();
			try
			{
				List<ServiceReference> list = listsByClassName.get(driver.getClass().getCanonicalName());
				if(list == null)
				{
					list = new ArrayList<>();
					listsByClassName.put(driver.getClass().getCanonicalName(),list);
				}
				for(ServiceReference sr : list)
				{
					if(sr == reference)
					{
						return;
					}
				}
				list.add(reference);
				
				Collections.sort(list, Collections.reverseOrder(new Comparator<ServiceReference>()
				{

					@Override
					public int compare(ServiceReference o1, ServiceReference o2)
					{
						if((o1 == null) || (o2 == null))
						{
							return 0;
						}
						Bundle bundle1 = o1.getBundle();
						Bundle bundle2 = o2.getBundle();
						if((bundle1 == null) || (bundle2 == null))
						{
							return 0;
						}
						Version version1 = bundle1.getVersion();
						Version version2 = bundle2.getVersion();
						if((version1 == null) || (version2 == null))
						{
							return 0;
						}
						return version1.compareTo(version2);
					}
				}));
				
			}
			finally 
			{
				lock.unlock();
			}
		}
		
		public void removeDriver(ServiceReference reference, Object driver)
		{
			if(lock == null)
			{
				return;
			}
			lock.lock();
			try
			{
				Set<String> toRemoveLists = new HashSet<>();
				for(Entry<String,List<ServiceReference>> entry  : listsByClassName.entrySet())
				{
					List<ServiceReference> list = entry.getValue();
					LinkedList<Integer> toRemovePositions = new LinkedList<>();
					int index = 0;
					for(ServiceReference serviceReference : list)
					{
						if(serviceReference == reference)
						{
							toRemovePositions.addFirst(index);
						}
						index++;
					}
					for(Integer toRemove : toRemovePositions)
					{
						list.remove((int)toRemove);
					}
					
					if(list.isEmpty())
					{
						toRemoveLists.add(entry.getKey());
					}
				}
				for(String toRemove : toRemoveLists)
				{
					listsByClassName.remove(toRemove);
				}
			}
			finally 
			{
				lock.unlock();
			}
		}
		
		protected BundleContext getContext()
		{
			return super.context;
		}
	}
	
	protected static class Customizer implements ServiceTrackerCustomizer
	{
		DriverServiceTracker tracker = null;
		public Customizer()
		{
			super();
		}
		
		public void setTracker(DriverServiceTracker tracker)
		{
			this.tracker = tracker;
		}
		
		@Override
		public Object addingService(ServiceReference reference)
		{
			Object driver = tracker.getContext().getService(reference);
			tracker.addDriver(reference,driver);
			return driver;
		}

		@Override
		public void modifiedService(ServiceReference reference, Object service){}

		@Override
		public void removedService(ServiceReference reference, Object service)
		{
			tracker.removeDriver(reference,service);
		}
		
	}
}
