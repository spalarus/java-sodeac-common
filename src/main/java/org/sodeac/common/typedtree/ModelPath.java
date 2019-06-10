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

public class ModelPath<S,T>
{
	private ModelPath<?,?> previousNode = null;
	private ModelPath<?,?> nextNode = null;
	private Predicate<?> nextPredicate = null;
	private IField<?, ?> type = null;
	private BranchNodeType root = null;
	
	protected ModelPath(BranchNodeType root)
	{
		super();
		this.root = root;
	}
	
	protected ModelPath(ModelPath<?,?> previousNode, IField<?,?> type )
	{
		super();
		this.previousNode = previousNode;
		this.type = type;
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
	protected Predicate<?> getNextPredicate()
	{
		return nextPredicate;
	}
	protected ModelPath<?, ?> setNextPredicate(Predicate<?> childPredicate)
	{
		this.nextPredicate = childPredicate;
		return this;
	}
	
	
}
