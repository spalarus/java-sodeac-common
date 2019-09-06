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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.sodeac.common.typedtree.ModelPath.NodeSelector;

public class ModifyListenerRegistration<R extends BranchNodeMetaModel>
{
	private List<NodeSelector<R,?>> rootNodeSelectorList = null;
	
	protected ModifyListenerRegistration()
	{
		super();
		this.rootNodeSelectorList = new ArrayList<ModelPath.NodeSelector<R,?>>();
	}
	
	protected List<NodeSelector<R, ?>> getRootNodeSelectorList()
	{
		return rootNodeSelectorList;
	}
	
	public void dispose()
	{
		this.internDispose(this.rootNodeSelectorList);
	}
	
	private void internDispose(List nodeSelectorList)
	{
		if(nodeSelectorList == null)
		{
			return;
		}
		
		if(nodeSelectorList.isEmpty())
		{
			return;
		}
		
		for(NodeSelector<?,?> nodeSelector : (List<NodeSelector<?, ?>>)nodeSelectorList)
		{
			
			//recursive
			if((nodeSelector.getChildSelectorList() != null) && (!nodeSelector.getChildSelectorList().isEmpty()))
			{
				internDispose(nodeSelector.getChildSelectorList());
				nodeSelector.getChildSelectorList().clear();
			}
			
			for(Set<IModifyListener<?>>  set : nodeSelector.getRegistrationObjects().values())
			{
				set.clear();
			}
			nodeSelector.getRegistrationObjects().clear();
			
			if(nodeSelector.getModifyListenerList() != null)
			{
				nodeSelector.getModifyListenerList().clear();
			}
			
			nodeSelector.dispose();
		}
		
		nodeSelectorList.clear();
	}
	
	protected <T> ModifyListenerRegistration<R> registerListener(ModelPath<R, T> path, IModifyListener<T> listener)
	{
		return registerListeners(path, listener);
	}

	protected <T> ModifyListenerRegistration<R> registerListeners(ModelPath<R, T> path, IModifyListener<T>... listener)
	{
		// deep copy
		ModelPath<R, T> originPath = path;
		path = path.clone();
		
		// add listener to deep copy of path
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
		List<NodeSelector<?,?>> otherNodeSelectorList = new ArrayList<NodeSelector<?,?>>();
		otherNodeSelectorList.add(path.getNodeSelectorList().get(0));
		
		internRegister(null, this.rootNodeSelectorList,otherNodeSelectorList, originPath);
		
		return this;
	}
	
	protected <T> ModifyListenerRegistration<R> unregister(ModelPath<R, T> path)
	{
		internUnregister(this.rootNodeSelectorList, path);
		return this;
	}
	
	protected <T> ModifyListenerRegistration<R> registerListener(ModifyListenerRegistration<?> registration, IModifyListener<T> listener)
	{
		return registerListeners(registration, listener);
	}
	
	protected <T> ModifyListenerRegistration<R> registerListeners(ModifyListenerRegistration<?> registration, IModifyListener<T>... listener)
	{
		internRegister(null, this.rootNodeSelectorList,registration.rootNodeSelectorList, registration);
		return this;
	}
	
	protected <T> ModifyListenerRegistration<R> unregister(ModifyListenerRegistration<?> registration)
	{
		internUnregister(this.rootNodeSelectorList, registration);
		return this;
	}
	
	private <T> void internUnregister(List nodeSelectorList, Object pathObject)
	{
		if(nodeSelectorList == null)
		{
			return;
		}
		
		if(nodeSelectorList.isEmpty())
		{
			return;
		}
		
		List<NodeSelector<?,?>> toRemoveSelector = new ArrayList<NodeSelector<?,?>>();
		for(NodeSelector<?,?> nodeSelector : (List<NodeSelector<?, ?>>)nodeSelectorList)
		{
			if(nodeSelector.getRegistrationObjects().get(pathObject) == null)
			{
				// nothing todo
				continue;
			}
			
			//recursive
			if((nodeSelector.getChildSelectorList() != null) && (!nodeSelector.getChildSelectorList().isEmpty()))
			{
				internUnregister(nodeSelector.getChildSelectorList(), pathObject);
			}
			
			// remove registration
			nodeSelector.getRegistrationObjects().get(pathObject).clear();
			nodeSelector.getRegistrationObjects().remove(pathObject);
			
			// merge modify listener list by still existing registrations
			if((nodeSelector.getModifyListenerList() != null) && (! nodeSelector.getModifyListenerList().isEmpty()))
			{
				Set<IModifyListener<?>> listenerIndex = new HashSet<IModifyListener<?>>();
				for(Set<IModifyListener<?>> listenerList : nodeSelector.getRegistrationObjects().values())
				{
					listenerIndex.addAll(listenerList);
				}
				
				List<IModifyListener<?>> toRemoveListener = new ArrayList<IModifyListener<?>>();
				for(IModifyListener<?> modifyListener : nodeSelector.getModifyListenerList())
				{
					if(! listenerIndex.contains(modifyListener))
					{
						toRemoveListener.add(modifyListener);
					}
				}
				
				listenerIndex.clear();
				listenerIndex = null;
				
				for(IModifyListener<?> toRemove : toRemoveListener)
				{
					nodeSelector.getModifyListenerList().remove(toRemove);
				}
				toRemoveListener.clear();
			}
			
			// remove registration, if no registration object exists anymore
			if(nodeSelector.getRegistrationObjects().isEmpty())
			{
				nodeSelector.dispose();
				toRemoveSelector.add(nodeSelector);
			}
		}
		
		for(NodeSelector<?, ?> toRemove : toRemoveSelector)
		{
			nodeSelectorList.remove(toRemove);
		}
	}
	
	private <T> void internRegister(NodeSelector<?,?> parent, List thisNodeSelectorList, List otherNodeSelectorList, Object pathObject)
	{
		Objects.requireNonNull(thisNodeSelectorList, "this node selector is null");
		Objects.requireNonNull(pathObject, "pathObject is null");
		
		if(otherNodeSelectorList == null)
		{
			return;
		}
		
		if(otherNodeSelectorList.isEmpty())
		{
			return;
		}
	
		
		for(NodeSelector<?,?> otherNodeSelector : (List<NodeSelector<?, ?>>)otherNodeSelectorList)
		{
			// current this nodeSelector
			
			NodeSelector<?,?> nodeSelector = null;
			for(NodeSelector<?,?> thisNodeSelector : (List<NodeSelector<?, ?>>)thisNodeSelectorList)
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
			
			// registration object
			
			if(nodeSelector.getRegistrationObjects() == null)
			{
				nodeSelector.setRegistrationObjects(new HashMap<Object,Set<IModifyListener<?>>>());
			}
			if(nodeSelector.getRegistrationObjects().get(pathObject) == null)
			{
				nodeSelector.getRegistrationObjects().put(pathObject,new HashSet<IModifyListener<?>>());
			}
			// modify listener
			
			if((otherNodeSelector.getModifyListenerList() != null) && (!otherNodeSelector.getModifyListenerList().isEmpty()))
			{
				Set<IModifyListener<?>> registrationSet = nodeSelector.getRegistrationObjects().get(pathObject);
				if(nodeSelector.getModifyListenerList() == null)
				{
					nodeSelector.setModifyListenerList(new ArrayList<IModifyListener<?>>());
				}
				for(IModifyListener<?> modifyListener : otherNodeSelector.getModifyListenerList())
				{
					if(! nodeSelector.getModifyListenerList().contains(modifyListener))
					{
						nodeSelector.getModifyListenerList().add(modifyListener);
					}
					if(! registrationSet.contains(modifyListener))
					{
						registrationSet.add(modifyListener);
					}
				}
			}
			
			// recursive 
			
			if((otherNodeSelector.getChildSelectorList() != null) && (!otherNodeSelector.getChildSelectorList().isEmpty()))
			{
				if(nodeSelector.getChildSelectorList() == null)
				{
					nodeSelector.setChildSelectorList(new ArrayList<NodeSelector<?,?>>());
				}
				internRegister(nodeSelector, nodeSelector.getChildSelectorList(), otherNodeSelector.getChildSelectorList(), pathObject);
			}
			
		}
	}

}
