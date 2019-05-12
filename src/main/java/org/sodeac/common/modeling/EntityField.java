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
package org.sodeac.common.modeling;

public class EntityField<T>
{
	private T object  = null;
	private Class clazz = null;
	private String fieldName = null;
	
	public T getObject()
	{
		return object;
	}
	protected void setObject(T object)
	{
		this.object = object;
	}
	public Class getClazz()
	{
		return clazz;
	}
	protected void setClazz(Class clazz)
	{
		this.clazz = clazz;
	}
	public String getFieldName()
	{
		return fieldName;
	}
	protected void setFieldName(String fieldName)
	{
		this.fieldName = fieldName;
	}
	
	
	
}
