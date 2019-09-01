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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.sodeac.common.typedtree.ModelPath.NodeSelector;

public class ModifyListenerRegistration<R extends BranchNodeMetaModel>
{
	private List<NodeSelector<?,?>> rootNodeSelectorList = null;
	
	protected ModifyListenerRegistration()
	{
		super();
		this.rootNodeSelectorList = new ArrayList<ModelPath.NodeSelector<?,?>>();
	}
	
	public <T> ModifyListenerRegistration<R> merge(ModelPath<R, T> path, IModifyListener<T>... listener)
	{
		path = path.clone();
		NodeSelector targetNodeSelector = path.getNodeSelectorList().get(path.getNodeSelectorList().size() -1);
		if(listener != null)
		{
			for(IModifyListener modifyListener : listener)
			{
				if(targetNodeSelector.getModifyListenerList() == null)
				{
					targetNodeSelector.setModifyListenerList(new ArrayList<>());
				}
				if(!targetNodeSelector.getModifyListenerList().contains(modifyListener))
				{
					targetNodeSelector.getModifyListenerList().add(modifyListener);
				}
			}
		}
		List<NodeSelector<?,?>> otherNodeSelectorList = new ArrayList<NodeSelector<?,?>>();otherNodeSelectorList.add(path.getNodeSelectorList().get(0));
		
		internMerge(null, this.rootNodeSelectorList,otherNodeSelectorList);
		
		return this;
	}
	
	private <T> void internMerge(NodeSelector<?,?> parent, List<NodeSelector<?,?>> thisNodeSelectorList, List<NodeSelector<?,?>> otherNodeSelectorList)
	{
		Objects.requireNonNull(thisNodeSelectorList, " this node selector is null");
		
		if(otherNodeSelectorList == null)
		{
			return;
		}
		
		if(otherNodeSelectorList.isEmpty())
		{
			return;
		}
	
		
		for(NodeSelector<?,?> otherNodeSelector : otherNodeSelectorList)
		{
			NodeSelector<?,?> nodeSelector = null;
			for(NodeSelector<?,?> thisNodeSelector : thisNodeSelectorList)
			{
				if(thisNodeSelector.equals(otherNodeSelector))
				{
					nodeSelector = thisNodeSelector;
					break;
				}
			}
			if(nodeSelector == null)
			{
				nodeSelector = otherNodeSelector.copy(parent, false);
				thisNodeSelectorList.add(nodeSelector);
			}
			
			if((otherNodeSelector.getModifyListenerList() != null) && (!otherNodeSelector.getModifyListenerList().isEmpty()))
			{
				if(nodeSelector.getModifyListenerList() == null)
				{
					nodeSelector.setModifyListenerList(new ArrayList<IModifyListener<?>>());
				}
				for(IModifyListener<?> otherModifyListener : otherNodeSelector.getModifyListenerList())
				{
					if(! nodeSelector.getModifyListenerList().contains(otherModifyListener))
					{
						nodeSelector.getModifyListenerList().add(otherModifyListener);
					}
				}
			}
			
			if((otherNodeSelector.getChildSelectorList() != null) && (!otherNodeSelector.getChildSelectorList().isEmpty()))
			{
				if(nodeSelector.getChildSelectorList() == null)
				{
					nodeSelector.setChildSelectorList(new ArrayList<NodeSelector<?,?>>());
				}
				internMerge(nodeSelector, nodeSelector.getChildSelectorList(), otherNodeSelector.getChildSelectorList());
			}
			
		}
	}

}
