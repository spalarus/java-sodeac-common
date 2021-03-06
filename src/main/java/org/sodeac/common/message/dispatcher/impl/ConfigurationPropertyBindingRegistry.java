/*******************************************************************************
 * Copyright (c) 2019, 2020 Sebastian Palarus
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Sebastian Palarus - initial API and implementation
 *******************************************************************************/
package org.sodeac.common.message.dispatcher.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.sodeac.common.message.dispatcher.impl.ChannelManagerContainer.ControllerFilterObjects;
import org.sodeac.common.message.dispatcher.impl.ServiceContainer.ServiceFilterObjects;

public class ConfigurationPropertyBindingRegistry
{
	protected ConfigurationPropertyBindingRegistry()
	{
		super();
		this.controllerContainerIndex = new HashMap<String,Set<ChannelManagerContainer>>();
		this.serviceContainerIndex = new HashMap<String,Set<ServiceContainer>>();
		this.lock = new ReentrantLock();
	}
	
	private Map<String,Set<ChannelManagerContainer>> controllerContainerIndex = null;
	private Map<String,Set<ServiceContainer>> serviceContainerIndex = null;
	private Lock lock = null;
	
	public void register(ChannelManagerContainer controllerContainer)
	{
		if(controllerContainer == null)
		{
			return;
		}
		
		List<ControllerFilterObjects>  controllerFilterObjectsList = controllerContainer.getFilterObjectList();
		if(controllerFilterObjectsList == null)
		{
			return;
		}
		
		lock.lock();
		try
		{
			for(ControllerFilterObjects controllerFilterObjects : controllerFilterObjectsList)
			{
				if((controllerFilterObjects.attributes != null) && (! controllerFilterObjects.attributes.isEmpty()))
				{
					for(String attributeName : controllerFilterObjects.attributes)
					{
						Set<ChannelManagerContainer> controllerContainerSet = controllerContainerIndex.get(attributeName);
						if(controllerContainerSet == null)
						{
							controllerContainerSet = new HashSet<ChannelManagerContainer>();
							controllerContainerIndex.put(attributeName,controllerContainerSet);
						}
						controllerContainerSet.add(controllerContainer);
					}
				}
			}
		}
		finally 
		{
			lock.unlock();
		}
	}
	
	public Set<ChannelManagerContainer> getManagerContainer(String... attributes)
	{
		if(attributes == null)
		{
			return null;
		}
		
		if(attributes.length == 0)
		{
			return null;
		}
		
		lock.lock();
		try
		{
			Set<ChannelManagerContainer> set = null;
			for(String attribute : attributes)
			{
				Set<ChannelManagerContainer> controllerContainerSet = controllerContainerIndex.get(attribute);
				if(controllerContainerSet == null)
				{
					continue;
				}
				if(set == null)
				{
					set = new HashSet<ChannelManagerContainer>();
				}
				set.addAll(controllerContainerSet);
			}
			return set;
		}
		finally 
		{
			lock.unlock();
		}
	}
	
	public void unregister(ChannelManagerContainer controllerContainer)
	{
		if(controllerContainer == null)
		{
			return;
		}
		
		List<ControllerFilterObjects>  controllerFilterObjectsList = controllerContainer.getFilterObjectList();
		if(controllerFilterObjectsList == null)
		{
			return;
		}
		
		LinkedList<String> removeList = null;
		
		lock.lock();
		try
		{
			if(controllerContainerIndex != null)
			{
				for(Entry<String,Set<ChannelManagerContainer>> controllerContainerSetEntry : controllerContainerIndex.entrySet())
				{
					if(controllerContainerSetEntry.getValue().remove(controllerContainer))
					{
		
						if(controllerContainerSetEntry.getValue().isEmpty())
						{
							if(removeList == null)
							{
								removeList = new LinkedList<String>();
							}
							removeList.add(controllerContainerSetEntry.getKey());
						}
					}
				}
				
				if(removeList != null)
				{
					for(String attribute : removeList)
					{
						controllerContainerIndex.remove(attribute);
					}
				}
			}
		}
		finally 
		{
			lock.unlock();
		}
	}
	
	public void register(ServiceContainer serviceContainer)
	{
		if(serviceContainer == null)
		{
			return;
		}
		
		List<ServiceFilterObjects>  serviceFilterObjectsList = serviceContainer.getFilterObjectList();
		if(serviceFilterObjectsList == null)
		{
			return;
		}
		
		lock.lock();
		try
		{
			for(ServiceFilterObjects serviceFilterObjects : serviceFilterObjectsList)
			{
				if((serviceFilterObjects.attributes != null) && (! serviceFilterObjects.attributes.isEmpty()))
				{
					for(String attributeName : serviceFilterObjects.attributes)
					{
						Set<ServiceContainer> serviceContainerSet = serviceContainerIndex.get(attributeName);
						if(serviceContainerSet == null)
						{
							serviceContainerSet = new HashSet<ServiceContainer>();
							serviceContainerIndex.put(attributeName,serviceContainerSet);
						}
						serviceContainerSet.add(serviceContainer);
					}
				}
			}
		}
		finally 
		{
			lock.unlock();
		}
	}
	
	public Set<ServiceContainer> getServiceContainer(String... attributes)
	{
		if(attributes == null)
		{
			return null;
		}
		
		if(attributes.length == 0)
		{
			return null;
		}
		
		lock.lock();
		try
		{
			Set<ServiceContainer> set = null;
			for(String attribute : attributes)
			{
				Set<ServiceContainer> serviceContainerSet = serviceContainerIndex.get(attribute);
				if(serviceContainerSet == null)
				{
					continue;
				}
				if(set == null)
				{
					set = new HashSet<ServiceContainer>();
				}
				set.addAll(serviceContainerSet);
			}
			return set;
		}
		finally 
		{
			lock.unlock();
		}
	}
	
	public void unregister(ServiceContainer serviceContainer)
	{
		if(serviceContainer == null)
		{
			return;
		}
		
		List<ServiceFilterObjects>  serviceFilterObjectsList = serviceContainer.getFilterObjectList();
		if(serviceFilterObjectsList == null)
		{
			return;
		}
		
		LinkedList<String> removeList = null;
		
		lock.lock();
		try
		{
			if(serviceContainerIndex != null)
			{
				for(Entry<String,Set<ServiceContainer>> serviceContainerSetEntry : serviceContainerIndex.entrySet())
				{
					if(serviceContainerSetEntry.getValue().remove(serviceContainer))
					{
		
						if(serviceContainerSetEntry.getValue().isEmpty())
						{
							if(removeList == null)
							{
								removeList = new LinkedList<String>();
							}
							removeList.add(serviceContainerSetEntry.getKey());
						}
					}
				}
				
				if(removeList != null)
				{
					for(String attribute : removeList)
					{
						serviceContainerIndex.remove(attribute);
					}
				}
			}
		}
		finally 
		{
			lock.unlock();
		}
	}
	
	public void clear()
	{
		lock.lock();
		try
		{
			for(Entry<String,Set<ChannelManagerContainer>> controllerContainerSetEntry : controllerContainerIndex.entrySet())
			{
				if(controllerContainerSetEntry.getValue() == null)
				{
					continue;
				}
				controllerContainerSetEntry.getValue().clear();
			}
			controllerContainerIndex.clear();
			controllerContainerIndex = null;
			
			for(Entry<String,Set<ServiceContainer>> serviceContainerSetEntry : serviceContainerIndex.entrySet())
			{
				if(serviceContainerSetEntry.getValue() == null)
				{
					continue;
				}
				serviceContainerSetEntry.getValue().clear();
			}
			serviceContainerIndex.clear();
			serviceContainerIndex = null;
		}
		finally 
		{
			lock.unlock();
		}
	}
}
