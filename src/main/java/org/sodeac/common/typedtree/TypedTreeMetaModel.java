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

public class TypedTreeMetaModel<T extends BranchNodeMetaModel> extends BranchNodeMetaModel
{
	/**
	 * 
	 * @param field
	 * @return RootNode of generic model tree
	 */
	public <F extends BranchNodeMetaModel> RootBranchNode<T,F> createRootNode(BranchNodeType<T,F> node)
	{
		return new RootBranchNode(node.getTypeClass());
	}
	
	public static class RootBranchNode<P extends BranchNodeMetaModel,R  extends BranchNodeMetaModel> extends BranchNode<P,R>
	{
		protected RootBranchNode(Class<R> modelType)
		{
			super(modelType);
		}
		public void dispose()
		{
			// TODO
		}
	}
}
