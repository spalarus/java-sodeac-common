/*******************************************************************************
 * Copyright (c) 2017, 2019 Sebastian Palarus
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

import org.sodeac.common.message.dispatcher.api.DispatcherChannelSetup;
import org.sodeac.common.message.dispatcher.api.IChannelManager;
import org.sodeac.common.message.dispatcher.api.IFeatureConfigurableController;
import org.sodeac.common.message.dispatcher.api.IOnChannelAttach;
import org.sodeac.common.message.dispatcher.api.IOnChannelDetach;
import org.sodeac.common.message.dispatcher.api.IOnChannelSignal;
import org.sodeac.common.message.dispatcher.api.IOnMessageRemove;
import org.sodeac.common.message.dispatcher.api.IOnMessageStore;
import org.sodeac.common.message.dispatcher.api.IOnTaskDone;
import org.sodeac.common.message.dispatcher.api.IOnTaskError;
import org.sodeac.common.message.dispatcher.api.IOnTaskTimeout;
import org.sodeac.common.xuri.ldapfilter.Attribute;
import org.sodeac.common.xuri.ldapfilter.AttributeLinker;
import org.sodeac.common.xuri.ldapfilter.IFilterItem;
import org.sodeac.common.xuri.ldapfilter.LDAPFilterDecodingHandler;

public class ChannelManagerContainer
{
	protected ChannelManagerContainer
	(
		MessageDispatcherImpl dispatcher,
		IChannelManager queueController, 
		List<DispatcherChannelSetup.BoundedByChannelId> boundByIdList, 
		List<DispatcherChannelSetup.BoundedByChannelConfiguration> boundedByQueueConfigurationList
	)
	{
		super();
		this.boundedByQueueConfigurationList = boundedByQueueConfigurationList;
		this.boundByIdList = boundByIdList;
		this.dispatcher = dispatcher;
		this.channelController = queueController;
		this.createFilterObjectList();
		this.detectControllerImplementions();
	}
	
	private MessageDispatcherImpl dispatcher = null;
	private volatile IChannelManager channelController = null;
	private List<DispatcherChannelSetup.BoundedByChannelId> boundByIdList = null;
	private List<DispatcherChannelSetup.BoundedByChannelConfiguration> boundedByQueueConfigurationList = null;
	
	private volatile boolean registered = false;
	
	private volatile List<ControllerFilterObjects> filterObjectList;
	private volatile Set<String> filterAttributes;
	
	private volatile boolean implementsIOnTaskDone = false;
	private volatile boolean implementsIOnTaskError = false;
	private volatile boolean implementsIOnTaskTimeout = false;
	private volatile boolean implementsIOnQueueAttach = false;
	private volatile boolean implementsIOnQueueDetach = false;
	private volatile boolean implementsIOnQueueSignal = false;
	private volatile boolean implementsIOnScheduleEvent = false;
	private volatile boolean implementsIOnRemoveEvent = false;
	
	public void detectControllerImplementions()
	{
		if(this.channelController == null)
		{
			implementsIOnTaskDone = false;
			implementsIOnTaskError = false;
			implementsIOnTaskTimeout = false;
			implementsIOnQueueAttach = false;
			implementsIOnQueueDetach = false;
			implementsIOnQueueSignal = false;
			implementsIOnScheduleEvent = false;
			implementsIOnRemoveEvent = false;
			implementsIOnTaskTimeout = false;
			return;
		}
		
		
		if(this.channelController instanceof IFeatureConfigurableController)
		{
			IFeatureConfigurableController featureConfigurableController = (IFeatureConfigurableController)this.channelController;
			implementsIOnTaskDone = featureConfigurableController.implementsOnTaskDone();
			implementsIOnTaskError = featureConfigurableController.implementsOnTaskError();
			implementsIOnTaskTimeout = featureConfigurableController.implementsOnTaskTimeout();
			implementsIOnQueueAttach = featureConfigurableController.implementsOnChannelAttach();
			implementsIOnQueueDetach = featureConfigurableController.implementsOnChannelDetach();
			implementsIOnQueueSignal = featureConfigurableController.implementsOnChannelSignal();
			implementsIOnScheduleEvent = featureConfigurableController.implementsOnMessageStore();
			implementsIOnRemoveEvent = featureConfigurableController.implementsOnMessageRemove();
		}
		else
		{
			implementsIOnTaskDone = this.channelController instanceof IOnTaskDone;
			implementsIOnTaskError = this.channelController instanceof IOnTaskError;
			implementsIOnTaskTimeout = this.channelController instanceof IOnTaskTimeout;
			implementsIOnQueueAttach = this.channelController instanceof IOnChannelAttach;
			implementsIOnQueueDetach = this.channelController instanceof IOnChannelDetach;
			implementsIOnQueueSignal = this.channelController instanceof IOnChannelSignal;
			implementsIOnScheduleEvent = this.channelController instanceof IOnMessageStore;
			implementsIOnRemoveEvent = this.channelController instanceof IOnMessageRemove;
		}
	}
	
	private void createFilterObjectList()
	{
		List<ControllerFilterObjects> list = new ArrayList<ControllerFilterObjects>();
		if(this.boundedByQueueConfigurationList != null)
		{
			for(DispatcherChannelSetup.BoundedByChannelConfiguration boundedByQueueConfiguration : boundedByQueueConfigurationList)
			{
				if(boundedByQueueConfiguration.getLdapFilter() == null)
				{
					continue;
				}
				if(boundedByQueueConfiguration.getLdapFilter().isEmpty())
				{
					continue;
				}
				ControllerFilterObjects controllerFilterObjects = new ControllerFilterObjects();
				controllerFilterObjects.bound = boundedByQueueConfiguration;
				controllerFilterObjects.filterExpression = boundedByQueueConfiguration.getLdapFilter();
				
				try
				{
					controllerFilterObjects.filter = LDAPFilterDecodingHandler.getInstance().decodeFromString(controllerFilterObjects.filterExpression);
					
					LinkedList<IFilterItem> discoverLDAPItem = new LinkedList<IFilterItem>();
					IFilterItem filter = controllerFilterObjects.filter;
					
					discoverLDAPItem.addLast(filter);
					
					while(! discoverLDAPItem.isEmpty())
					{
						filter = discoverLDAPItem.removeFirst();
						
						if(filter instanceof Attribute) 
						{
							controllerFilterObjects.attributes.add(((Attribute)filter).getName());
						}
						else if(filter instanceof AttributeLinker)
						{
							discoverLDAPItem.addAll(((AttributeLinker)filter).getLinkedItemList());
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
		for(ControllerFilterObjects controllerFilterObjects : this.filterObjectList)
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
	
	public IChannelManager getChannelManager()
	{
		return channelController;
	}
	public boolean isRegistered()
	{
		return registered;
	}
	public void setRegistered(boolean registered)
	{
		this.registered = registered;
	}
	public List<DispatcherChannelSetup.BoundedByChannelConfiguration> getBoundedByChannelConfigurationList()
	{
		return boundedByQueueConfigurationList;
	}
	public List<DispatcherChannelSetup.BoundedByChannelId> getBoundByIdList()
	{
		return boundByIdList;
	}
	
	public List<ControllerFilterObjects> getFilterObjectList()
	{
		return filterObjectList;
	}

	public Set<String> getFilterAttributeSet()
	{
		return filterAttributes;
	}
	
	public void clean()
	{
		this.dispatcher = null;
		this.channelController = null;
		this.boundByIdList = null;
		this.boundedByQueueConfigurationList = null;
		this.filterObjectList = null;
		this.filterAttributes = null;
	}
	
	public class ControllerFilterObjects
	{
		DispatcherChannelSetup.BoundedByChannelConfiguration bound = null;
		String filterExpression = null;
		IFilterItem filter = null;
		Set<String> attributes = new HashSet<String>();
	}

	public boolean isImplementingIOnTaskDone()
	{
		return implementsIOnTaskDone;
	}

	public boolean isImplementingIOnTaskError()
	{
		return implementsIOnTaskError;
	}

	public boolean isImplementingIOnQueueAttach()
	{
		return implementsIOnQueueAttach;
	}

	public boolean isImplementingIOnQueueDetach()
	{
		return implementsIOnQueueDetach;
	}

	public boolean isImplementingIOnQueueSignal()
	{
		return implementsIOnQueueSignal;
	}

	public boolean isImplementingIOnScheduleEvent()
	{
		return implementsIOnScheduleEvent;
	}

	public boolean isImplementingIOnRemoveEvent()
	{
		return implementsIOnRemoveEvent;
	}

	public boolean isImplementingIOnTaskTimeout()
	{
		return implementsIOnTaskTimeout;
	}
	
	public List<DispatcherChannelSetup> getComponentConfigurationList()
	{
		List<DispatcherChannelSetup> list = new ArrayList<DispatcherChannelSetup>();
		
		if(boundByIdList != null)
		{
			list.addAll(boundByIdList);
		}
		if(boundedByQueueConfigurationList != null)
		{
			list.addAll(boundedByQueueConfigurationList);
		}
		
		return list;
	}
	
}
