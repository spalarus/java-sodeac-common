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

public class ModelPathBuilder<R extends BranchNodeMetaModel,S extends BranchNodeMetaModel,T>
{
	private BranchNodeMetaModel root = null;
	private BranchNodeMetaModel self = null;
	private ModelPath<?, ?> type = null;
	
	public static <R extends BranchNodeMetaModel, T>  RootModelPathBuilder<R,T> newBuilder(R root, Class<T> clazz)
	{
		return new RootModelPathBuilder<>(root);
	}
	
	private ModelPathBuilder(BranchNodeMetaModel root,BranchNodeMetaModel self)
	{
		super();
		this.root = root;
		this.self = self;
		this.type = new ModelPath<>(self);
	}
	
	public void x()
	{
		
	}
	public <N extends BranchNodeMetaModel> ModelPathBuilder<R,N,T> with(INodeType<S, N> field) // field,on,sel(select),to,go,with,connect   x
	{
		this.type = new ModelPath<>(this.type, field);
		return new ModelPathBuilder<R,N,T>(this.root,field.getType());
	}
	
	public <N extends BranchNodeMetaModel> ModelPathBuilder<R,N,T> with(INodeType<S, N> field, Predicate<ModelPathCursor<S,N>> predicate)
	{
		this.type = new ModelPath<>(this.type, field).setNextPredicate(predicate);
		return new ModelPathBuilder<R,N,T>(this.root,field.getType());
	}
	
	public ModelPath<R, T> buildFor(INodeType<S, T> field) 
	{
		return new ModelPath<R,T>(type,field);
	}
	
	protected BranchNodeMetaModel getSelf()
	{
		return this.self;
	}
	
	protected BranchNodeMetaModel getRoot()
	{
		return this.root;
	}
	
	public static class RootModelPathBuilder<R extends BranchNodeMetaModel, T> extends ModelPathBuilder<R, R, T>
	{
		private RootModelPathBuilder(BranchNodeMetaModel root)
		{
			super(root,root);
		}
	}
}
