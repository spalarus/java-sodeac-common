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
import java.util.Map;

/**
 * Represents an ldap attribute.
 * 
 * @author Sebastian Palarus
 * @since 1.0
 * @version 1.0
 *
 */
public class Attribute implements IFilterItem, Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5791677902733009628L;

	/**
	 * constructor of ldap attribute
	 */
	public Attribute()
	{
		super();
	}
	
	private boolean invert = false;
	private String name = null;
	private ComparativeOperator operator = null;
	private String value = null;
	private AttributeLinker parent;
	
	/**
	 * getter for name of ldap attribute
	 * 
	 * @return name of ldap attribute
	 */
	public String getName() 
	{
		return name;
	}
	
	/**
	 * setter for name of ldap attribute
	 * 
	 * @param name ldap attribute name
	 * 
	 * @return attribute
	 */
	public Attribute setName(String name) 
	{
		this.name = name;
		return this;
	}
	
	/**
	 * getter for ldap operator
	 * 
	 * @return operator
	 */
	public ComparativeOperator getOperator() 
	{
		return operator;
	}
	
	/**
	 * setter for ldap operator
	 * 
	 * @param operator ldap attribute operator
	 * @return
	 */
	public Attribute setOperator(ComparativeOperator operator) 
	{
		this.operator = operator;
		return this;
	}
	
	/**
	 * getter for value of ldap attribute
	 * 
	 * @return value of ldap attribute
	 */
	public String getValue() 
	{
		return value;
	}
	
	/**
	 * setter for value of ldap attribute
	 * 
	 * @param value ldap attribute value
	 * @return attribute
	 */
	public Attribute setValue(String value) 
	{
		this.value = value;
		return this;
	}
	
	@Override
	public boolean isInvert() 
	{
		return invert;
	}
	
	@Override
	public Attribute setInvert(boolean invert) 
	{
		this.invert = invert;
		return this;
	}
	
	@Override
	public AttributeLinker getParent()
	{
		return this.parent;
	}
	
	/**
	 * setter for parent
	 * 
	 * @param parent parent attribute linker
	 */
	protected void setParent(AttributeLinker parent)
	{
		this.parent = parent;
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
		
		stringBuilder.append(name);
		
		if(operator != null)
		{
			if(operator == ComparativeOperator.EQUAL)
			{
				stringBuilder.append("=");
			}
			else if(operator == ComparativeOperator.GREATER)
			{
				stringBuilder.append(">=");
			}
			else if(operator == ComparativeOperator.LESS)
			{
				stringBuilder.append("<=");
			}
			else if(operator == ComparativeOperator.APPROX)
			{
				stringBuilder.append("~=");
			}
		}
		
		if(value != null)
		{
			stringBuilder.append(value);
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
		IMatchable matchable = properties.get(this.name);
		if((matchable != null) && matchable.matches(operator, this.name, this.value ))
		{
			return ! invert;
		}
		return invert;
	}
}
