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
 * A node is part of a tree.
 * 
 * @author Sebastian Palarus
 *
 * @param <P> type of parent branch node
 * @param <T> type of node
 */
public abstract class Node<P extends BranchNodeMetaModel, T>
{
	protected boolean rootLinked = false;
	protected volatile boolean disposed = false;

	/**
	 * 
	 * @return true if, this node is connected with root node
	 */
	public boolean isRootLinked()
	{
		return rootLinked;
	}

	protected void setRootLinked(boolean rootLinked)
	{
		this.rootLinked = rootLinked;
	}

	/**
	 * Dispose this node. 
	 */
	protected abstract void disposeNode();
	protected boolean isDisposed()
	{
		return this.disposed;
	}
	
	public abstract INodeType<P,T> getNodeType();
}
