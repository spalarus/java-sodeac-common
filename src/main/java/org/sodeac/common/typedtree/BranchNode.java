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
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.sodeac.common.typedtree.ModelingProcessor.PreparedNodeType;
import org.sodeac.common.typedtree.ModelingProcessor.PreparedMetaModel;

public class BranchNode<P extends BranchNodeMetaModel, T extends BranchNodeMetaModel> extends Node<P,T>
{
	private PreparedMetaModel preparedMetaModel = null;
	private List<NodeContainer<T,?>> nodeContainerList = null;
	
	protected BranchNode(Class<T> modelType)
	{
		try
		{
			BranchNodeMetaModel model = ModelingProcessor.DEFAULT_INSTANCE.getModel(modelType);
			this.preparedMetaModel = ModelingProcessor.DEFAULT_INSTANCE.getPreparedMetaModel(model);
			
			this.nodeContainerList = new ArrayList<>();
			for(int i = 0; i < this.preparedMetaModel.getNodeTypeNames().length; i++)
			{
				PreparedNodeType preparedNodeType = preparedMetaModel.getPreparedNodeTypeList().get(i);
				
				NodeContainer<T, ?> nodeContainer = new NodeContainer<>();
				if(preparedNodeType.getNodeType() == PreparedNodeType.NodeType.LeafNode)
				{
					nodeContainer.node = new LeafNode<>();
				}
				else if (preparedNodeType.getNodeType() == PreparedNodeType.NodeType.BranchNodeList)
				{
					nodeContainer.nodeList = new ArrayList<BranchNode<P,T>>();
					nodeContainer.unmodifiableNodeList = Collections.unmodifiableList(nodeContainer.nodeList);
				}
				nodeContainer.preparedNodeType = preparedNodeType;
				nodeContainerList.add(nodeContainer);
			}
			this.nodeContainerList = Collections.unmodifiableList(this.nodeContainerList);
			
		}
		catch (Exception e) 
		{
			if(e instanceof RuntimeException)
			{
				throw (RuntimeException)e;
			}
			throw new RuntimeException(e);
		}
	}
	
	/*
	 * LeafNode methods
	 */
	
	public <X> LeafNode<T,X> get(LeafNodeType<T,X> nodeType)
	{
		return (LeafNode<T,X>) this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType)).getNode();
	}
	
	public <X> LeafNode<T,X> getLeafNode(ModelPath<T,X> path)
	{
		// TODO
		return null;
	}
	
	/*
	 *  BranchNode methods
	 */
	
	public BranchNode<P, T> compute(Consumer<BranchNode<P, T>> consumer)
	{
		if(consumer == null)
		{
			return this;
		}
		consumer.accept(this);
		return this;
	}
	
	public <X extends BranchNodeMetaModel> BranchNode<P, T> computeChildNode(BranchNodeType<T,X> nodeType, BranchNodeGetterPolicy policy, BiConsumer<BranchNode<P, T>, BranchNode<T,X>> consumer)
	{
		if(consumer == null)
		{
			return this;
		}
		BranchNode<T,X> node = get(nodeType, policy);
		consumer.accept(this, node);
		return this;
	}
	
	public <X extends BranchNodeMetaModel> BranchNode<P, T> computeChildNode(BranchNodeType<T,X> nodeType,BiConsumer<BranchNode<P, T>, BranchNode<T,X>> ifAbsent,BiConsumer<BranchNode<P, T>, BranchNode<T,X>> ifPresent)
	{
		NodeContainer<T,?> nodeContainer = this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType));
		BranchNode<T,X> node = (BranchNode)nodeContainer.getNode();
		if(node == null)
		{
			if(ifAbsent == null)
			{
				return this;
			}
			ifAbsent.accept(this, node);
			return this;
		}
		
		if(ifPresent == null)
		{
			return this;
		}
		ifPresent.accept(this, node);
		return this;
	}
	
	public <X> BranchNode<P,T> setValue(LeafNodeType<T,X> nodeType, X value)
	{
		LeafNode<T,X> node = (LeafNode<T,X>) this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType)).getNode();
		node.setValue(value);
		return this;
	}
	
	public <X extends BranchNodeMetaModel> BranchNode<P, T> remove(BranchNodeType<T,X> nodeType)
	{
		NodeContainer<T,?> nodeContainer = this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType));
		if(nodeContainer.getNode() != null)
		{
			nodeContainer.node = null;
		}
		return this;
	}
	public <X extends BranchNodeMetaModel> BranchNode<T,X> get(BranchNodeType<T,X> nodeType)
	{
		return get(nodeType,BranchNodeGetterPolicy.CreateNever);
	}
	public <X extends BranchNodeMetaModel> BranchNode<T,X> create(BranchNodeType<T,X> nodeType)
	{
		return get(nodeType,BranchNodeGetterPolicy.CreateAlways);
	}
	public <X extends BranchNodeMetaModel> BranchNode<T,X> get(BranchNodeType<T,X> nodeType,BranchNodeGetterPolicy policy)
	{
		if(policy == null)
		{
			policy = BranchNodeGetterPolicy.CreateNever;
		}
		
		NodeContainer<T,?> nodeContainer = this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType));
		
		if(policy.getCreateCase() == BranchNodeGetterPolicy.CreateCase.CreateNever)
		{
			return (BranchNode<T,X>) nodeContainer.getNode();
		}
		
		if( policy.isSynchronized())
		{
			synchronized (nodeContainer)
			{
				if(policy.getCreateCase() == BranchNodeGetterPolicy.CreateCase.CreateIfNull)
				{
					if(nodeContainer.getNode() == null)
					{
						nodeContainer.setNode(new BranchNode(nodeType.getTypeClass()));
					}
					return (BranchNode<T,X>) nodeContainer.getNode();
				}
				
				// if(policy.getCreateCase() == GetBranchNodePolicy.CreateCase.ForceCreate)
				
				if(nodeContainer.getNode() != null)
				{
					nodeContainer.getNode().disposeNode();
				}
				nodeContainer.setNode(new BranchNode(nodeType.getTypeClass()));
				return (BranchNode<T,X>) nodeContainer.getNode();
			}
		}
		else
		{
			if(policy.getCreateCase() == BranchNodeGetterPolicy.CreateCase.CreateIfNull)
			{
				if(nodeContainer.getNode() == null)
				{
					nodeContainer.setNode(new BranchNode(nodeType.getTypeClass()));
				}
				return (BranchNode<T,X>) nodeContainer.getNode();
			}
			
			// if(policy.getCreateCase() == GetBranchNodePolicy.CreateCase.ForceCreate)
			
			if(nodeContainer.getNode() != null)
			{
				nodeContainer.getNode().disposeNode();
			}
			nodeContainer.setNode(new BranchNode(nodeType.getTypeClass()));
			return (BranchNode<T,X>) nodeContainer.getNode();
		}
	}
	
	/*
	 * BranchNode List
	 */
	
	public <X extends BranchNodeMetaModel> List<BranchNode<P,T>> getUnmodifiableNodeList(BranchNodeListType<T,X> nodeType)
	{
		return this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType)).getUnmodifiableNodeList();
	}

	public <X extends BranchNodeMetaModel> BranchNode<T,X> createItem(BranchNodeListType<T,X> nodeType)
	{
		return this.createItem(nodeType, null);
	}
	public <X extends BranchNodeMetaModel> BranchNode<T,X> createItem(BranchNodeListType<T,X> nodeType, BiConsumer<BranchNode<P, T>, BranchNode<T,X>> consumer)
	{
		NodeContainer<T,?> nodeContainer = this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType));
		
		BranchNode<T,X> node = new BranchNode(nodeContainer.preparedNodeType.getNodeTypeClass());
		if(consumer != null)
		{
			consumer.accept(this, node);
		}
		nodeContainer.nodeList.add(node);
		return node;
	}
	
	public <X extends BranchNodeMetaModel> BranchNode<T,X> createItem(BranchNodeListType<T,X> nodeType,int index)
	{
		NodeContainer<T,?> nodeContainer = this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType));
		
		BranchNode<T,X> node = new BranchNode(nodeContainer.preparedNodeType.getNodeTypeClass());
		nodeContainer.nodeList.add(index,node);
		return node;
	}

	public <X extends BranchNodeMetaModel> boolean remove(BranchNodeListType<T,X> nodeType, BranchNode<P, T> node)
	{
		NodeContainer<T,?> nodeContainer = this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType));
		
		if(nodeContainer.nodeList.remove(node))
		{
			node.disposeNode();
			return true;
		}
		return false;
	}

	public <X extends BranchNodeMetaModel> void clear(BranchNodeListType<T,X> nodeType)
	{
		NodeContainer<T,?> nodeContainer = this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType));
		
		List<BranchNode<P, T>> copy = new ArrayList<>(nodeContainer.nodeList);
		nodeContainer.nodeList.clear();
		for(BranchNode<P, T> node : copy)
		{
			node.disposeNode();
		}
		
	}
	public <X extends BranchNodeMetaModel> BranchNode<T,X> remove(BranchNodeListType<T,X> nodeType, int index)
	{
		NodeContainer<T,?> nodeContainer = this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType));
		
		BranchNode<T,X> node = (BranchNode<T,X>)nodeContainer.nodeList.remove(index);
		
		if(node != null)
		{
			node.disposeNode();
		}
		return null; // TODO return disposed node ????
	}

	
	/*public <X extends BranchNodeMetaModel> BranchNodeList<T,X> get(BranchNodeListType<T,X> nodeType)
	{
		return (BranchNodeList<T,X>) this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType)).getNode();
	}*/
	
	// Helper
	
	private static class NodeContainer<P,T>
	{
		private PreparedNodeType preparedNodeType = null;
		private volatile Node node = null;
		private ArrayList nodeList = null; 
		private List unmodifiableNodeList = null;
		
		private PreparedNodeType getPreparedNodeType()
		{
			return preparedNodeType;
		}
		private void setPreparedNodeType(PreparedNodeType preparedNodeType)
		{
			this.preparedNodeType = preparedNodeType;
		}
		private Node getNode()
		{
			return node;
		}
		private void setNode(Node node)
		{
			this.node = node;
		}
		private ArrayList getNodeList()
		{
			return nodeList;
		}
		private void setNodeList(ArrayList nodeList)
		{
			this.nodeList = nodeList;
		}
		private List getUnmodifiableNodeList()
		{
			return unmodifiableNodeList;
		}
		private void setUnmodifiableNodeList(List unmodifiableNodeList)
		{
			this.unmodifiableNodeList = unmodifiableNodeList;
		}
	}
	
	public static class BranchNodeGetterPolicy
	{
		public enum CreateCase {CreateNever,CreateIfNull,CreateAlways};
		
		private CreateCase createCase = CreateCase.CreateNever;
		private boolean synchronize = false;
		
		public static final BranchNodeGetterPolicy CreateNever = new BranchNodeGetterPolicy(CreateCase.CreateNever, false);
		public static final BranchNodeGetterPolicy CreateIfNull = new BranchNodeGetterPolicy(CreateCase.CreateIfNull, false);
		public static final BranchNodeGetterPolicy CreateAlways = new BranchNodeGetterPolicy(CreateCase.CreateAlways, false);
		public static final BranchNodeGetterPolicy CreateIfNullSynchronized = new BranchNodeGetterPolicy(CreateCase.CreateIfNull, true);
		public static final BranchNodeGetterPolicy CreateAlwaysSynchronized = new BranchNodeGetterPolicy(CreateCase.CreateAlways, true);
		
		private BranchNodeGetterPolicy(BranchNodeGetterPolicy.CreateCase createCase, boolean synchronize)
		{
			super();
			this.createCase = createCase;
			this.synchronize = synchronize;
		}
		private CreateCase getCreateCase()
		{
			return createCase;
		}

		private boolean isSynchronized()
		{
			return synchronize;
		}
	}
}
