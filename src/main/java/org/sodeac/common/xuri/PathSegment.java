/*******************************************************************************
 * Copyright (c) 2016, 2019 Sebastian Palarus
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Sebastian Palarus - initial API and implementation
 *******************************************************************************/
package org.sodeac.common.xuri;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Single path segment of URI path.
 * 
 * @author Sebastian Palarus
 * @since 1.0
 * @version 1.0
 *
 */
public class PathSegment implements Serializable, IExtensible
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -603368963433292990L;

	/**
	 * constructor of path segment
	 * 
	 * @param expression string expression
	 * @param value segment value
	 */
	public PathSegment(String expression, String value)
	{
		super();
		this.expression = expression;
		this.value = value;
		this.axis = Axis.CHILD;
		
		this.extensionsLock = new ReentrantLock();
	}
	
	/**
	 * 
	 * constructor of path segment
	 * 
	 * @param expression string expression
	 * @param value segment value
	 * @param axis axistype
	 */
	public PathSegment(String expression, String value, Axis axis)
	{
		super();
		this.expression = expression;
		this.value = value;
		this.axis = axis;
	}

	private List<IExtension<?>> extensions = null;
	private volatile List<IExtension<?>> extensionsImmutable = null;
	
	private Lock extensionsLock = null;
	private String expression = null;
	private String value = null;
	private Axis axis = null;
	
	/**
	 * setter for expression string
	 * 
	 * @param expression
	 */
	protected void setExpression(String expression)
	{
		this.expression = expression;
	}

	/**
	 * getter for axistype
	 * 
	 * @return axistype
	 */
	public Axis getAxis() 
	{
		return axis;
	}

	/**
	 * Append an extension for this pathsegment
	 * 
	 * @param extension
	 */
	protected void addExtension(IExtension<?> extension)
	{
		this.extensionsLock.lock();
		try
		{
			if(this.extensions == null)
			{
				this.extensions = new ArrayList<IExtension<?>>();
			}
			this.extensions.add(extension);
			this.extensionsImmutable = null;
		}
		finally 
		{
			this.extensionsLock.unlock();
		}
	}

	@Override
	public IExtension<?> getExtension(String type)
	{
		List<IExtension<?>> extensionList = getExtensionList();
		
		if((type == null) && (! extensionList.isEmpty()))
		{
			return extensionList.get(0);
		}
		for(IExtension<?> extension : extensionList)
		{
			if(type.equals(extension.getType()))
			{
				return extension;
			}
		}
		return null;
	}

	@Override
	public List<IExtension<?>> getExtensionList()
	{
		List<IExtension<?>> extensionList = extensionsImmutable;
		if(extensionList == null)
		{
			this.extensionsLock.lock();
			try
			{
				extensionList = this.extensionsImmutable;
				if(extensionList != null)
				{
					return extensionList;
				}
				this.extensionsImmutable = Collections.unmodifiableList(this.extensions == null ? new ArrayList<IExtension<?>>() : new ArrayList<IExtension<?>>(this.extensions));
				extensionList = this.extensionsImmutable;
			}
			finally 
			{
				this.extensionsLock.unlock();
			}
		}
		return extensionList;
	}

	@Override
	public List<IExtension<?>> getExtensionList(String type)
	{
		List<IExtension<?>> extensionList = new ArrayList<IExtension<?>>();
		for(IExtension<?> extension : getExtensionList())
		{
			if(type.equals(extension.getType()))
			{
				extensionList.add(extension);
			}
		}
		return extensionList;
	}

	/**
	 * getter for representative string for pathsegment (value and extensions)
	 * 
	 * @return representative string for pathsegment
	 */
	public String getExpression()
	{
		return expression;
	}

	/**
	 * getter for pathsegment value
	 * 
	 * @return value of pathsegment
	 */
	public String getValue()
	{
		return value;
	}

}
