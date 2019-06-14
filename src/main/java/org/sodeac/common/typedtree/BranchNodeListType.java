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

public class BranchNodeListType<A extends BranchNodeMetaModel, T extends BranchNodeMetaModel> implements INodeType<A, T>
{
	private Class<T> typeClass = null;
	private Class<A> anchorClass = null;
	
	public BranchNodeListType(Class<A> anchorClass, Class<T> typeClass)
	{
		this.anchorClass = anchorClass;
		this.typeClass = typeClass;
	}

	public Class<T> getTypeClass()
	{
		return typeClass;
	}

	public Class<A> getAnchorClass()
	{
		return anchorClass;
	}
}
