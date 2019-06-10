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
package org.sodeac.common.typedtree;

public interface IField<A extends BranchNodeType, T>
{
	public Class<T> getTypeClass();
	public Class<A> getAnchorClass();
	
	public default T getType()
	{
		try
		{
			return getTypeClass().newInstance(); // TODO Registry, context of this field
		} 
		catch (InstantiationException | IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public default A getAnchor()
	{
		try
		{
			return getAnchorClass().newInstance(); // TODO Registry, context of this field
		} 
		catch (InstantiationException | IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}
}
