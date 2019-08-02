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

/**
 * A branch node type defines a single complex node as child node of parent node. This node type can defines new child nodes again.
 * 
 * @author Sebastian Palarus
 *
 * @param <P> type of parent node
 * @param <T> type of branch node
 */
public class BranchNodeType<P extends BranchNodeMetaModel, T extends BranchNodeMetaModel> implements INodeType<P, T>
{
	private Class<T> typeClass = null;
	private Class<P> parentNodeClass = null;
	private String name = null;
	/**
	 * Constructor for branch node type.
	 * 
	 * @param parentNodeClass class of parent node (should be a class of {@link BranchNodeMetaModel})
	 * @param typeClass type of child node (should be a class of {@link BranchNodeMetaModel})
	 * @param name name of node type
	 */
	public BranchNodeType(Class<P> parentNodeClass, Class<T> typeClass, String name)
	{
		this.parentNodeClass = parentNodeClass;
		this.typeClass = typeClass;
		this.name = name;
	}
	
	@Override
	public Class<T> getTypeClass()
	{
		return typeClass;
	}

	@Override
	public Class<P> getParentNodeClass()
	{
		return parentNodeClass;
	}
	
	@Override
	public String getNodeName()
	{
		return this.name;
	}
	
	@Override
	public T getTypeMetaInstance()
	{
		return ModelingProcessor.DEFAULT_INSTANCE.getCachedBranchNodeMetaModel(getTypeClass());
	}
}
