/*******************************************************************************
 * Copyright (c) 2020 Sebastian Palarus
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Sebastian Palarus - initial API and implementation
 *******************************************************************************/
package org.sodeac.common.typedtree;

import org.sodeac.common.typedtree.annotation.SQLPrimaryKey;

public class TypedTreeHelper
{
	public static <M  extends BranchNodeMetaModel,T> LeafNodeType<M, T> getPrimaryKeyNode(Class<M> clazz)
	{
		try
		{
			BranchNodeMetaModel model = ModelRegistry.getBranchNodeMetaModel(clazz);
			for(LeafNodeType ln : model.getLeafNodeTypeList())
			{
				if(ln.referencedByField().getAnnotation(SQLPrimaryKey.class) != null)
				{
					return ln;
				}
			}
		}
		catch (RuntimeException e)
		{
			throw e;
		}
		catch (Exception e) 
		{
			throw new RuntimeException(e);
		}
		return null;
	}
}
