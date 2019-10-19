/*******************************************************************************
 * Copyright (c) 2018, 2019 Sebastian Palarus
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.sodeac.common.message.dispatcher.api.IChannel;
import org.sodeac.common.message.dispatcher.api.ISubChannel;

public class SubChannelImpl extends ChannelImpl implements ISubChannel
{
	private UUID scopeId;
	private UUID parentScopeId;
	private String scopeName = null;
	private boolean adoptContoller = false;
	private boolean adoptServices = false;
	
	protected SubChannelImpl(UUID scopeId,UUID parentScopeId,ChannelImpl parent, String scopeName, boolean adoptContoller, boolean adoptServices, Map<String, Object> configurationProperties, Map<String, Object> stateProperties)
	{
		super(parent.getId() + "." + scopeId.toString(),(MessageDispatcherImpl)parent.getDispatcher(), null, configurationProperties,stateProperties);
		
		super.parent = parent;
		this.scopeName = scopeName;
		this.adoptContoller = adoptContoller;
		this.adoptServices = adoptServices;
		this.scopeId = scopeId;
		this.parentScopeId = parentScopeId;
		super.channelId = parent.getId() + "." + this.scopeId.toString();
	}

	@Override
	public IChannel getRootChannel()
	{
		return parent;
	}

	@Override
	public UUID getScopeId()
	{
		return scopeId;
	}
	
	protected UUID getParentScopeId()
	{
		return this.parentScopeId;
	}
	
	protected void unlinkFromParent()
	{
		this.parentScopeId = null;
	}
	
	@Override
	public ISubChannel getParentChannel()
	{
		return this.parentScopeId == null ? null : this.parent.getChildScope(this.parentScopeId);
	}

	@Override
	public List<ISubChannel> getChildScopes()
	{
		if(this.parentScopeId == null)
		{
			return Collections.unmodifiableList(new ArrayList<ISubChannel>());
		}
		return parent.getChildSessionScopes(this.parentScopeId);
	}

	@Override
	public String getChannelName()
	{
		return this.scopeName;
	}
	
	public boolean isAdoptContoller()
	{
		return adoptContoller;
	}

	public boolean isAdoptServices()
	{
		return adoptServices;
	}

	@Override
	public void dispose()
	{
		super.dispose();
	}

	public ISubChannel<?> createScope(String scopeName, Map<String, Object> configurationProperties, Map<String, Object> stateProperties)
	{
		return null; // TODO
	}


	// TODO
	/*
	public List<ISubChannel> getChildScopes(Filter filter)
	{
		return null; // TODO
	}*/

}
