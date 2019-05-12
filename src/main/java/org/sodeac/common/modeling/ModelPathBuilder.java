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
package org.sodeac.common.modeling;

import java.util.function.Predicate;

public class ModelPathBuilder<R extends ComplexType<R>,S extends ComplexType<S>,T extends IType<?>>
{
	private ComplexType<R> root = null;
	private ComplexType<S> self = null;
	private ModelPath<?, ?> type = null;
	
	public static <R extends ComplexType<R>, T extends IType<?>>  RootModelPathBuilder<R,T> newBuilder(ComplexType<R> root, Class<T> clazz)
	{
		return new RootModelPathBuilder<>(root);
	}
	
	private ModelPathBuilder(ComplexType<R> root,ComplexType<S> self)
	{
		super();
		this.root = root;
		this.self = self;
		this.type = new ModelPath<>(self);
	}
	
	public <N extends ComplexType<N>> ModelPathBuilder<R,N,T> with(IField<S, N> field) // field,on,sel(select),to,go,with
	{
		this.type = new ModelPath<>(this.type, field);
		return new ModelPathBuilder<R,N,T>(this.root,field.getType());
	}
	
	public <N extends ComplexType<N>> ModelPathBuilder<R,N,T> with(IField<S, N> field, Predicate<PredicateContainer<N>> predicate)
	{
		this.type = new ModelPath<>(this.type, field).setNextPredicate(predicate);
		return new ModelPathBuilder<R,N,T>(this.root,field.getType());
	}
	
	public ModelPath<R, T> buildFor(IField<S, T> field) 
	{
		return new ModelPath<R,T>(type,field);
	}
	
	protected ComplexType<S> getSelf()
	{
		return this.self;
	}
	
	protected ComplexType<R> getRoot()
	{
		return this.root;
	}
	
	public static class RootModelPathBuilder<R extends ComplexType<R>, T extends IType<?>> extends ModelPathBuilder<R, R, T>
	{
		private RootModelPathBuilder(ComplexType<R> root)
		{
			super(root,root);
		}
	}
}
