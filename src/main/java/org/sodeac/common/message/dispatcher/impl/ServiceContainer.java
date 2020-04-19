/*******************************************************************************
 * Copyright (c) 2017, 2020 Sebastian Palarus
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Sebastian Palarus - initial API and implementation
 *******************************************************************************/
package org.sodeac.common.message.dispatcher.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.sodeac.common.message.dispatcher.api.DispatcherChannelSetup;
import org.sodeac.common.message.dispatcher.api.IDispatcherChannelService;
import org.sodeac.common.xuri.ldapfilter.Criteria;
import org.sodeac.common.xuri.ldapfilter.CriteriaLinker;
import org.sodeac.common.xuri.ldapfilter.IFilterItem;
import org.sodeac.common.xuri.ldapfilter.LDAPFilterDecodingHandler;

public class ServiceContainer
{
	protected ServiceContainer
	(
		MessageDispatcherImpl dispatcher, 
		List<DispatcherChannelSetup.BoundedByChannelId> boundByIdList, 
		List<DispatcherChannelSetup.BoundedByChannelConfiguration> boundedByQueueConfigurationList,
		List<DispatcherChannelSetup.ChannelServiceConfiguration> serviceConfigurationList
	)
	{
		super();
		this.dispatcher = dispatcher;
		this.boundByIdList = boundByIdList;
		this.boundedByQueueConfigurationList = boundedByQueueConfigurationList;
		if((serviceConfigurationList != null) && (! serviceConfigurationList.isEmpty()))
		{
			this.serviceConfiguration = serviceConfigurationList.get(0);
		}
		else
		{
			this.serviceConfiguration = new DispatcherChannelSetup.ChannelServiceConfiguration(UUID.randomUUID().toString());
		}
		this.createFilterObjectList();
	}
	
	private MessageDispatcherImpl dispatcher;
	private List<DispatcherChannelSetup.BoundedByChannelId> boundByIdList = null;
	private List<DispatcherChannelSetup.BoundedByChannelConfiguration> boundedByQueueConfigurationList = null;
	private DispatcherChannelSetup.ChannelServiceConfiguration serviceConfiguration = null;
	
	private volatile IDispatcherChannelService queueService = null;
	
	private volatile boolean registered = false;
	
	private volatile List<ServiceFilterObjects> filterObjectList;
	private volatile Set<String> filterAttributes;
	
	private void createFilterObjectList()
	{
		List<ServiceFilterObjects> list = new ArrayList<ServiceFilterObjects>();
		if(this.boundedByQueueConfigurationList != null)
		{
			for(DispatcherChannelSetup.BoundedByChannelConfiguration boundedByQueueConfiguration : boundedByQueueConfigurationList)
			{
				if(boundedByQueueConfiguration.getLdapFilter() == null)
				{
					continue;
				}
				ServiceFilterObjects controllerFilterObjects = new ServiceFilterObjects();
				controllerFilterObjects.bound = boundedByQueueConfiguration;
				controllerFilterObjects.filter = boundedByQueueConfiguration.getLdapFilter();
				
				try
				{
					LinkedList<IFilterItem> discoverLDAPItem = new LinkedList<IFilterItem>();
					IFilterItem filter = LDAPFilterDecodingHandler.getInstance().decodeFromString(controllerFilterObjects.filterExpression);
					
					discoverLDAPItem.addLast(filter);
					
					while(! discoverLDAPItem.isEmpty())
					{
						filter = discoverLDAPItem.removeFirst();
						
						if(filter instanceof Criteria) 
						{
							controllerFilterObjects.attributes.add(((Criteria)filter).getName());
						}
						else if(filter instanceof CriteriaLinker)
						{
							discoverLDAPItem.addAll(((CriteriaLinker)filter).getLinkedItemList());
						}
					}
					
					list.add(controllerFilterObjects);
				}
				catch (Exception e) 
				{
					dispatcher.logError("parse bounded queue configuration " + boundedByQueueConfiguration.getLdapFilter(),e);
				}
			}
		}
		this.filterObjectList = list;
		this.filterAttributes = new HashSet<String>();
		for(ServiceFilterObjects controllerFilterObjects : this.filterObjectList)
		{
			if(controllerFilterObjects.attributes != null)
			{
				for(String attribute : controllerFilterObjects.attributes)
				{
					this.filterAttributes.add(attribute);
				}
			}
		}
	}

	public IDispatcherChannelService getChannelService()
	{
		return queueService;
	}
	public void setChannelService(IDispatcherChannelService queueService)
	{
		this.queueService = queueService;
	}
	public boolean isRegistered()
	{
		return registered;
	}
	public void setRegistered(boolean registered)
	{
		this.registered = registered;
	}
	
	public void clean()
	{
		this.dispatcher = null;
		this.queueService = null;
		this.boundByIdList = null;
		this.boundedByQueueConfigurationList = null;
		this.serviceConfiguration = null;
		this.filterObjectList = null;
		this.filterAttributes = null;
	}
	
	public List<ServiceFilterObjects> getFilterObjectList()
	{
		return filterObjectList;
	}

	public List<DispatcherChannelSetup.BoundedByChannelId> getBoundByIdList()
	{
		return boundByIdList;
	}


	public List<DispatcherChannelSetup.BoundedByChannelConfiguration> getBoundedByChannelConfigurationList()
	{
		return boundedByQueueConfigurationList;
	}


	public DispatcherChannelSetup.ChannelServiceConfiguration getServiceConfiguration()
	{
		return serviceConfiguration;
	}

	public Set<String> getFilterAttributeSet()
	{
		return filterAttributes;
	}



	public class ServiceFilterObjects
	{
		DispatcherChannelSetup.BoundedByChannelConfiguration bound = null;
		String filterExpression = null;
		IFilterItem filter = null;
		Set<String> attributes = new HashSet<String>();
	}
}
