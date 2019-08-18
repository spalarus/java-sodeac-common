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

import java.lang.reflect.Field;

/**
 * A branch node list type defines a multiple complex node. This node type can defines new child nodes of type {@link LeafNodeType}, {@link BranchNodeType} and {@link BranchNodeListType} again.
 * 
 * @author Sebastian Palarus
 *
 *  @param <P> type of parent node
 * @param <T> type of branch node
 */
public class BranchNodeListType<P extends BranchNodeMetaModel, T extends BranchNodeMetaModel> implements INodeType<P, T>
{
	private Class<T> typeClass = null;
	private Class<P> parentNodeClass = null;
	private String name = null;
	private Field field = null;
	private int hashCode = 1;
	
	/**
	 * Constructor for branch node list type.
	 * 
	 * @param parentNodeClass class of parent node (should be a class of {@link BranchNodeMetaModel})
	 * @param typeClass type of child node (should be a class of {@link BranchNodeMetaModel})
	 * @param field referenced by field
	 */
	public BranchNodeListType(Class<P> parentNodeClass, Class<T> typeClass, Field field)
	{
		this.parentNodeClass = parentNodeClass;
		this.typeClass = typeClass;
		this.name = field.getName();
		this.field = field;
		
		// generate hashcode
		
		final int prime = 31;
		hashCode = prime * hashCode + ((field == null) ? 0 : field.hashCode());
		hashCode = prime * hashCode + ((name == null) ? 0 : name.hashCode());
		hashCode = prime * hashCode + ((parentNodeClass == null) ? 0 : parentNodeClass.hashCode());
		hashCode = prime * hashCode + ((typeClass == null) ? 0 : typeClass.hashCode());
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
	public T getValueDefaultInstance()
	{
		return ModelingProcessor.DEFAULT_INSTANCE.getCachedBranchNodeMetaModel(getTypeClass());
	}
	
	@Override
	public Field referencedByField()
	{
		return this.field;
	}
	
	@Override
	public int hashCode()
	{
		return this.hashCode;
	}

	@Override
	public boolean equals(Object obj)
	{
		return this == obj;
	}
}
