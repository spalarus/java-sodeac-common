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

import java.util.function.Predicate;

/**
 * A model path selects nodes. It starts from source node and navigate to tree by path definition to select nodes or value of nodes.
 * 
 * @author Sebastian Palarus
 *
 * @param <S> type of start node
 * @param <T> type of nodes or node's value to select
 */
public class ModelPath<S extends BranchNodeMetaModel,T>
{
	private ModelPath<?,?> previousNode = null;
	private ModelPath<?,?> nextNode = null;
	private Predicate<?> predicate = null;
	private INodeType<?, ?> type = null;
	private BranchNodeMetaModel root = null;
	
	protected ModelPath(BranchNodeMetaModel root)
	{
		super();
		this.root = root;
	}
	
	protected ModelPath(BranchNodeMetaModel root, ModelPath<?,?> previousNode, INodeType<?,?> type )
	{
		super();
		this.root = root;
		this.previousNode = previousNode;
		this.type = type;
		previousNode.nextNode = this;
	}
	
	protected ModelPath<?, ?> getPreviousNode()
	{
		return previousNode;
	}
	protected ModelPath<?, ?> setPreviousNode(ModelPath<?, ?> previousNode)
	{
		this.previousNode = previousNode;
		return this;
	}
	protected ModelPath<?, ?> getNextNode()
	{
		return nextNode;
	}
	protected ModelPath<?, ?> setNextNode(ModelPath<?, ?> nextNode)
	{
		this.nextNode = nextNode;
		return this;
	}
	protected Predicate<?> getPredicate()
	{
		return predicate;
	}
	protected ModelPath<?, ?> setPredicate(Predicate<?> predicate)
	{
		this.predicate = predicate;
		return this;
	}
	
	
}
