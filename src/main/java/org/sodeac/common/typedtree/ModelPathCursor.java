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

public class ModelPathCursor<P extends BranchNodeMetaModel,T>
{
	public boolean isSingleList()
	{
		return false;
	}
	public boolean isEmptyList()
	{
		return false;
	}
	public boolean isMultipleList()
	{
		return false;
	}
	public int getListSite()
	{
		return 0;
	}
	public LeafNode<P,T> getCurrent()
	{
		return null;
	}
	
	public LeafNode<P,T> getPrevious()
	{
		return null;
	}
	public LeafNode<P,T> getNext()
	{
		return null;
	}
	public LeafNode<P,T> getFirst()
	{
		return null;
	}
	public LeafNode<P,T> getLast()
	{
		return null;
	}
	public boolean isFirst()
	{
		return getCurrent() != null && ( getCurrent() == getFirst() );
	}
	public boolean isLast()
	{
		return getCurrent() != null && ( getCurrent() == getLast() );
	}
	public INodeType<?,?> getFieldObject()
	{
		return null;
	}
	public ModelPathCursor<?,P> getParentCursor()
	{
		return null;
	}
	
	protected void dispose()
	{
		
	}
	
}
