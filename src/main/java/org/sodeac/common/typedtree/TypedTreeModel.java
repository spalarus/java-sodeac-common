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

public class TypedTreeModel<T extends BranchNodeType> extends BranchNodeType
{
	/**
	 * 
	 * @param field
	 * @return RootNode of generic model tree
	 */
	public <F extends BranchNodeType> ComplexRootObject<T,F> newInstance(BranchNodeField<T,F> field)
	{
		return new ComplexRootObject(field.getTypeClass());
	}
	
	public <F> BasicRootObject<T,F> newInstance(LeafNodeField<T,F> field)
	{
		return null;
	}
	
	public <F extends BranchNodeType> BranchNodeList<T,F> newInstance(BranchNodeListField<T,F> field)
	{
		return null;
	}
	
	public static class ComplexRootObject<P extends BranchNodeType,R  extends BranchNodeType> extends BranchNode<P,R>
	{
		protected ComplexRootObject(Class<R> modelType)
		{
			super(modelType);
		}
		public void dispose()
		{
			// TODO
		}
	}
	
	public static class BasicRootObject<P extends BranchNodeType,R> extends LeafNode<P,R>
	{
		public void dispose()
		{
			// TODO
		}
	}
	
	public static class ComplexRootList<P extends BranchNodeType, R  extends BranchNodeType> extends BranchNodeList<P,R>
	{
		public void dispose()
		{
			// TODO
		}
	}
}
