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

public class ModelPathBuilder<R extends ComplexType,S extends ComplexType,T>
{
	private ComplexType root = null;
	private ComplexType self = null;
	private ModelPath<?, ?> type = null;
	
	public static <R extends ComplexType, T>  RootModelPathBuilder<R,T> newBuilder(R root, Class<T> clazz)
	{
		return new RootModelPathBuilder<>(root);
	}
	
	private ModelPathBuilder(ComplexType root,ComplexType self)
	{
		super();
		this.root = root;
		this.self = self;
		this.type = new ModelPath<>(self);
	}
	
	public void x()
	{
		
	}
	public <N extends ComplexType> ModelPathBuilder<R,N,T> with(IField<S, N> field) // field,on,sel(select),to,go,with,connect   x
	{
		this.type = new ModelPath<>(this.type, field);
		return new ModelPathBuilder<R,N,T>(this.root,field.getType());
	}
	
	public <N extends ComplexType> ModelPathBuilder<R,N,T> with(IField<S, N> field, Predicate<ModelPathCursor<N>> predicate)
	{
		this.type = new ModelPath<>(this.type, field).setNextPredicate(predicate);
		return new ModelPathBuilder<R,N,T>(this.root,field.getType());
	}
	
	public ModelPath<R, T> buildFor(IField<S, T> field) 
	{
		return new ModelPath<R,T>(type,field);
	}
	
	protected ComplexType getSelf()
	{
		return this.self;
	}
	
	protected ComplexType getRoot()
	{
		return this.root;
	}
	
	public static class RootModelPathBuilder<R extends ComplexType, T> extends ModelPathBuilder<R, R, T>
	{
		private RootModelPathBuilder(ComplexType root)
		{
			super(root,root);
		}
	}
}
