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

package org.sodeac.common.xuri.ldapfilter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Attribute linker represents an ldap query and consists of multiple ldap attributes or other attribute linker.
 * 
 * @author Sebastian Palarus
 * @since 1.0
 * @version 1.0
 *
 */
public class AttributeLinker implements IFilterItem, Serializable 
{	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3252972156160851293L;

	/**
	 * constructor of attribute linker
	 */
	public AttributeLinker()
	{
		super();
		this.lock = new ReentrantLock();
	}
	
	private volatile boolean invert = false;
	private volatile LogicalOperator operator = LogicalOperator.AND;
	private volatile AttributeLinker parent = null;
	private Lock lock = null;
	
	private List<IFilterItem> linkedItemList = new ArrayList<IFilterItem>();
	private List<IFilterItem> linkedItemListCopy = null;

	/**
	 * getter for linked filter item list
	 * 
	 * @return linked filter item list
	 */
	public List<IFilterItem> getLinkedItemList() 
	{
		List<IFilterItem> itemList = this.linkedItemListCopy ;
		if(itemList != null)
		{
			return itemList;
		}
		lock.lock();
		try
		{
			itemList = this.linkedItemListCopy ;
			if(itemList != null)
			{
				return itemList;
			}
			
			this.linkedItemListCopy = Collections.unmodifiableList(new ArrayList<IFilterItem>(this.linkedItemList));
			return this.linkedItemListCopy;
		}
		finally 
		{
			lock.unlock();
		}
	}
	
	/**
	 * adds new filter item to list
	 * 
	 * @param item filter item to add
	 * @return attribute linker
	 */
	public AttributeLinker addItem(IFilterItem item)
	{
		if(item instanceof Attribute)
		{
			((Attribute)item).setParent(this);
		}
		if(item instanceof AttributeLinker)
		{
			((AttributeLinker)item).setParent(this);
		}
		lock.lock();
		try
		{
			this.linkedItemList.add(item);
			this.linkedItemListCopy = null;
		}
		finally 
		{
			lock.unlock();
		}
		
		return this;
	}

	@Override
	public boolean isInvert() 
	{
		return invert;
	}

	@Override
	public AttributeLinker setInvert(boolean invert) 
	{
		this.invert = invert;
		return this;
	}
	
	/**
	 * setter for parent attribute linker
	 * 
	 * @param parent parent attribute linker
	 */
	protected void setParent(AttributeLinker parent)
	{
		this.parent = parent;
	}
	
	@Override
	public AttributeLinker getParent()
	{
		return this.parent;
	}

	/** 
	 * getter for link operator
	 * @return link operator
	 */
	public LogicalOperator getOperator() 
	{
		return operator;
	}

	/**
	 * setter for link operator
	 * 
	 * @param operator link operator
	 * @return this
	 */
	public AttributeLinker setOperator(LogicalOperator operator) 
	{
		this.operator = operator;
		return this;
	}
	
	@Override
	public String toString() 
	{
		StringBuilder stringBuilder = new StringBuilder();
		
		stringBuilder.append("(");
		
		if(invert)
		{
			stringBuilder.append("!(");
		}
		
		stringBuilder.append(operator.getAbbreviation());
		
		if(this.linkedItemList != null)
		{
			for(IFilterItem filterItem : this.linkedItemList)
			{
				stringBuilder.append(filterItem.toString());
			}
		}
		
		if(invert)
		{
			stringBuilder.append(")");
		}
		
		stringBuilder.append(")");
		return stringBuilder.toString();
	}
	
	@Override
	public boolean matches(Map<String,IMatchable> properties)
	{
		if(operator == LogicalOperator.OR)
		{
			for(IFilterItem filterItem : getLinkedItemList())
			{
				if(filterItem.matches(properties))
				{
					return ! invert;
				}
			}
			return invert;
		}
		
		for(IFilterItem filterItem : getLinkedItemList())
		{
			if(!filterItem.matches(properties))
			{
				return invert;
			}
		}
		return ! invert;
	}
}