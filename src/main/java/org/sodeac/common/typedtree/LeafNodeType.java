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
 * A leaf node type defines a simple child node. This kind of child type can not include other child nodes. It is recommend to restrict the type of node to one of the following list:
 * 
 * <br>{@link java.lang.Character}
 * <br>{@link java.lang.String}
 * <br>{@link java.lang.Boolean}
 * <br>{@link java.lang.Byte}
 * <br>{@link java.lang.Short}
 * <br>{@link java.lang.Integer}
 * <br>{@link java.lang.Long}
 * <br>{@link java.lang.Float}
 * <br>{@link java.lang.Double}
 * <br>{@link java.util.UUID}
 * <br>{@link java.util.Date}
 * <br>{@link java.nio.ByteBuffer}
 * <br>array of bytes
 * 
 * @author Sebastian Palarus
 *
 * @param <P> type of parent node
 * @param <T> type of leaf node
 */
public class LeafNodeType<P extends BranchNodeMetaModel, T> implements INodeType<P, T>
{
	private Class<T> typeClass = null;
	private Class<P> parentNodeClass = null;
	private String name = null;
	
	/**
	 * Constructor for leaf node type.
	 * 
	 * @param parentNodeClass class of parent node (should be a class of {@link BranchNodeMetaModel})
	 * @param typeClass type of child node's value
	 * @param name name of node type
	 */
	public LeafNodeType(Class<P> parentNodeClass, Class<T> typeClass, String name)
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
}
