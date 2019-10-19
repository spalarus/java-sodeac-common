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
package org.sodeac.common.xuri.ldapfilter;

import java.util.Objects;

/**
 * 
 * Matchable wrapper for strings, booleans and numbers
 * 
 * @author Sebastian Palarus
 *
 */
public class DefaultMatchableWrapper implements IMatchable
{
	private Object value = null;
	
	/**
	 * Constructor to create a matchable wrapper for strings, booleans and numbers
	 * 
	 * @param value value to wrap
	 */
	public DefaultMatchableWrapper(Object value)
	{
		super();
		this.value = value;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public boolean matches(ComparativeOperator operator, String name, String valueExpression)
	{
		Objects.requireNonNull(valueExpression, "value expression must not be null");
		valueExpression = valueExpression.trim();
		
		if((operator == ComparativeOperator.EQUAL) && valueExpression.equals("*")) // Present
		{
			return true;
		}
		
		if(value == null)
		{
			return false;
		}
		
		if((this.value instanceof String) && (operator == ComparativeOperator.APPROX))
		{
			return ((String)this.value).trim().equalsIgnoreCase(valueExpression);
		}
		
		if((this.value instanceof String) && (operator == ComparativeOperator.EQUAL))
		{
			// TODO SubstringFilter
			return ((String)this.value).equals(valueExpression);
		}
		
		if(this.value instanceof Comparable)
		{
			int compareValue = ((Comparable)this.value).compareTo(convertValueExpression(valueExpression));
			
			if((operator == ComparativeOperator.APPROX) || (operator == ComparativeOperator.EQUAL))
			{
				return compareValue == 0;
			}
			if(operator == ComparativeOperator.LESS)
			{
				return compareValue <= 0;
			}
			if(operator == ComparativeOperator.GREATER)
			{
				return compareValue >= 0;
			}
			throw new RuntimeException("unkown operator " + operator);
		}
		
		if((operator == ComparativeOperator.GREATER) || (operator == ComparativeOperator.LESS))
		{
			throw new RuntimeException("gt and lt requires complarable object! current: " + this.value.getClass());
		}
		
		Object convertedRightHandSide = convertValueExpression(valueExpression);
		if((convertedRightHandSide instanceof String) && (!(this.value instanceof String)))
		{
			return this.value.toString().equals(convertedRightHandSide);
		}
		
		return this.value.equals(convertedRightHandSide);
	}
	
	private Object convertValueExpression(String valueExpression)
	{
		if(this.value instanceof String)
		{
			return valueExpression;
		}
		
		if(this.value instanceof Boolean)
		{
			return Boolean.TRUE.toString().equals(valueExpression.toLowerCase());
		}
		
		try
		{
			if(this.value instanceof Byte)
			{
				return Byte.parseByte(valueExpression);
			}
			
			if(this.value instanceof Short)
			{
				return Short.parseShort(valueExpression);
			}
			
			if(this.value instanceof Integer)
			{
				return Integer.parseInt(valueExpression);
			}
			
			if(this.value instanceof Long)
			{
				return Long.parseLong(valueExpression);
			}
			
			if(this.value instanceof Float)
			{
				return Float.parseFloat(valueExpression);
			}
			
			if(this.value instanceof Double)
			{
				return Double.parseDouble(valueExpression);
			}
		}
		catch(Exception e) {}
		
		return valueExpression;
	}
}
