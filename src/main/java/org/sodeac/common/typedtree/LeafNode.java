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

import java.util.concurrent.locks.Lock;

import org.sodeac.common.typedtree.ModelingProcessor.PreparedNodeType;

/**
 * A leaf node is an instance of simple tree node with a value, but without child nodes.
 * 
 * @author Sebastian Palarus
 *
 * @param <P> type of parent branch node
 * @param <T> type of node's value
 */
public class LeafNode<P extends BranchNodeMetaModel,T> extends Node<P,T>
{
	
	/**
	 * Constructor for leaf node
	 * 
	 * @param parentNode parent node instance
	 * @param preparedNodeType container for leaf node
	 */
	protected LeafNode(BranchNode<?,P> parentNode, PreparedNodeType preparedNodeType)
	{
		super();
		this.parentNode = parentNode;
		this.preparedNodeType = preparedNodeType;
	}
	
	private PreparedNodeType preparedNodeType = null;
	private BranchNode<?,P> parentNode = null;
	private T value = null;
	
	/**
	 * Dispose this node.
	 */
	protected void disposeNode()
	{
		this.value = null;
		this.parentNode = null;
	}
	
	/**
	 * Getter for value of leaf node.
	 * 
	 * @return value of leaf node.
	 */
	public T getValue()
	{
		return this.value;
	}
	
	/**
	 * Setter for value of leaf node.
	 * 
	 * @param value leaf node's value to set.
	 * 
	 * @return this leaf node
	 */
	public LeafNode<P,T> setValue(T value)
	{
		if(this.parentNode.getRootNode().isImmutable())
		{
			return this;
		}
		Lock lock = this.parentNode.getRootNode().isSynchronized() ? this.parentNode.getRootNode().getWriteLock() : null;
		if(lock != null)
		{
			lock.lock();
		}
		try
		{
			if
			(
				parentNode.getRootNode().publishModify
				(
					this.parentNode,
					this.preparedNodeType.getStaticNodeTypeInstance(), 
					LeafNodeType.class, 
					this.value, 
					value
				)
			)
			{
				this.value = value;
			}
		}
		finally 
		{
			if(lock != null)
			{
				lock.unlock();
			}
		}
		return this;
	}

	/**
	 * Getter for parent node.
	 * 
	 * @return parent node.
	 */
	protected BranchNode<?, P> getParentNode()
	{
		return parentNode;
	}
	
}
