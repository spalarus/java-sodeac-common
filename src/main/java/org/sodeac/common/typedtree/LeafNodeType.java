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
 * A leaf node type defines a simple node. This node contains single java objects and can not include other child nodes. Theoretical the leaf node type can be any java type, 
 * but it's highly recommend to restrict the type to the following list
 * 
 * <p>{@link java.lang.Character}
 * <p>{@link java.lang.String}
 * <p>{@link java.lang.Boolean}
 * <p>{@link java.lang.Byte}
 * <p>{@link java.lang.Short}
 * <p>{@link java.lang.Integer}
 * <p>{@link java.lang.Long}
 * <p>{@link java.lang.Float}
 * <p>{@link java.lang.Double}
 * <p>{@link java.util.UUID}
 * <p>{@link java.util.Date}
 * <p>{@link java.nio.ByteBuffer}
 * 
 * @author Sebastian Palarus
 *
 * @param <P> type of parent node
 * @param <T> type of node
 */
public class LeafNodeType<P extends BranchNodeMetaModel, T> implements INodeType<P, T>
{
	private Class<T> typeClass = null;
	private Class<P> parentNodeClass = null;
	
	public LeafNodeType(Class<P> parentNodeClass, Class<T> typeClass)
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
