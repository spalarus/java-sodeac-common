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
 * Helper class to build model paths.
 * 
 * @author Sebastian Palarus
 *
 * @param <R> type off root node in path
 * @param <S> type of self node in path
 */
public class ModelPathBuilder<R extends BranchNodeMetaModel,S extends BranchNodeMetaModel>
{
	private BranchNodeMetaModel root = null;
	private BranchNodeMetaModel self = null;
	private ModelPath<?, ?> type = null;
	
	/**
	 * Create new model path builder.
	 * 
	 * @param rootClass type off root node in path (start node)
	 * @return builder
	 */
	public static <R extends BranchNodeMetaModel>  RootModelPathBuilder<R> newBuilder(Class<R> rootClass)
	{
		try
		{
			return new RootModelPathBuilder<>(ModelingProcessor.DEFAULT_INSTANCE.getModel(rootClass));
		}
		catch (Exception e) 
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Create new model path builder.
	 * 
	 * @param root root node of path (start node)
	 * @return builder
	 */
	public static <R extends BranchNodeMetaModel>  RootModelPathBuilder<R> newBuilder(R root)
	{
		return new RootModelPathBuilder<>(root);
	}
	
	private ModelPathBuilder(BranchNodeMetaModel root)
	{
		super();
		this.root = root;
		this.self = root;
		this.type = new ModelPath<>(root);
	}
	
	private ModelPathBuilder(BranchNodeMetaModel root,BranchNodeType field, ModelPath<?,?> previews)
	{
		super();
		this.root = root;
		this.self = field.getTypeMetaInstance();
		this.type = new ModelPath<>(this.root,previews,field);
	}
	
	/**
	 * Definition to navigate to next child node.
	 * 
	 * @param field static child node type instance from meta model
	 * @return builder
	 */
	public <N extends BranchNodeMetaModel> ModelPathBuilder<R,N> to(BranchNodeType<S, N> field)
	{
		return new ModelPathBuilder<R,N>(this.root,field,this.type);
	}
	
	/**
	 * Definition to navigate to next child node.
	 * 
	 * @param field static child node type instance from meta model
	 * @param predicate filter to restrict child nodes
	 * @return builder
	 */
	public <N extends BranchNodeMetaModel> ModelPathBuilder<R,N> to(BranchNodeType<S, N> field, Predicate<ModelPathCursor<S,N>> predicate)
	{
		ModelPathBuilder<R,N> builder = new ModelPathBuilder<R,N>(this.root,field,type);
		builder.type.setPredicate(predicate);
		return builder;
	}
	
	public <T> ModelPath<R, T> buildForValue(LeafNodeType<S, T> field) 
	{
		return new ModelPath<R,T>(this.root,this.type,field);
	}
	
	public <T> ModelPath<R, LeafNode<?, T>> buildForNode(LeafNodeType<S, T> field) 
	{
		return new ModelPath<R,LeafNode<?, T>>(this.root,this.type, (INodeType)field);
	}
	
	public <T extends BranchNodeMetaModel> ModelPath<R, BranchNode<S, T>> buildForNode(BranchNodeType<S, T> field) 
	{
		return new ModelPath<R,BranchNode<S, T>>(this.root,this.type, (INodeType)field);
	}
	
	protected BranchNodeMetaModel getSelf()
	{
		return this.self;
	}
	
	protected BranchNodeMetaModel getRoot()
	{
		return this.root;
	}
	
	/**
	 * Helper class to build model paths.
	 * 
	 * @author Sebastian Palarus
	 *
	 * @param <R> type off root node in path
	 */
	public static class RootModelPathBuilder<R extends BranchNodeMetaModel> extends ModelPathBuilder<R, R>
	{
		private RootModelPathBuilder(BranchNodeMetaModel root)
		{
			super(root);
		}
	}
}
