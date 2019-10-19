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
package org.sodeac.common.message.dispatcher.api;

/**
 * 
 * @author Sebastian Palarus
 *
 */
public class ChannelComponentUnconfiguredException extends RuntimeException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4059017650955484511L;
	
	private IChannelComponent component = null;
	public ChannelComponentUnconfiguredException(IChannelComponent component)
	{
		super();
		this.component = component;
	}

	public ChannelComponentUnconfiguredException(IChannelComponent component,String message, Throwable cause, boolean enableSuppression,boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.component = component;
	}

	public ChannelComponentUnconfiguredException(IChannelComponent component, String message, Throwable cause)
	{
		super(message, cause);
		this.component = component;
	}

	public ChannelComponentUnconfiguredException(IChannelComponent component,String message)
	{
		super(message);
		this.component = component;
	}

	public ChannelComponentUnconfiguredException(IChannelComponent component,Throwable cause)
	{
		super(cause);
		this.component = component;
	}

	public IChannelComponent getChannelComponent()
	{
		return this.component;
	}
	
}
