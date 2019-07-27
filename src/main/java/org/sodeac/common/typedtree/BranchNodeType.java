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

public class BranchNodeType<P extends BranchNodeMetaModel, T extends BranchNodeMetaModel> implements INodeType<P, T>
{
	private Class<T> typeClass = null;
	private Class<P> parentNodeClass = null;
	
	public BranchNodeType(Class<P> parentNodeClass, Class<T> typeClass)
	{
		this.parentNodeClass = parentNodeClass;
		this.typeClass = typeClass;
	}

	public Class<T> getTypeClass()
	{
		return typeClass;
	}

	public Class<P> getParentNodeClass()
	{
		return parentNodeClass;
	}
	

}
