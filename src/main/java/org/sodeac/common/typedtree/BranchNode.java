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
				Node<?,?> node = null;
				if(preparedNodeType.getNodeType() == PreparedNodeType.NodeType.LeafNode)
				{
					node = new LeafNode<>();
				}
				else if (preparedNodeType.getNodeType() == PreparedNodeType.NodeType.BranchNodeList)
				{
					node = new BranchNodeList<>(preparedNodeType.getNodeTypeClass());
				}
				NodeContainer<T, ?> nodeContainer = new NodeContainer<>();
				nodeContainer.node = node;
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
	
	public BranchNodeMetaModel getModel()
	{
		return (BranchNodeMetaModel)this.preparedMetaModel.getModel();
	}
	
	public BranchNode<P, T> build(Consumer<BranchNode<P, T>> consumer) // TODO better Name -> for, consume, invoke, run, doit
	{
		if(consumer == null)
		{
			return this;
		}
		consumer.accept(this);
		return this;
	}
	
	public <X extends BranchNodeMetaModel> BranchNode<P, T> forNode(BranchNodeType<T,X> nodeType, BranchNodeGetterPolicy policy, BiConsumer<BranchNode<P, T>, BranchNode<T,X>> consumer)
	{
		if(consumer == null)
		{
			return this;
		}
		BranchNode<T,X> node = get(nodeType, policy);
		consumer.accept(this, node);
		return this;
	}
	
	public <X> LeafNode<T,X> get(LeafNodeType<T,X> nodeType)
	{
		return (LeafNode<T,X>) this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType)).getNode();
	}
	public <X> BranchNode<P,T> setValue(LeafNodeType<T,X> nodeType, X value)
	{
		LeafNode<T,X> node = (LeafNode<T,X>) this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType)).getNode();
		node.setValue(value);
		return this;
	}
	public <X extends BranchNodeMetaModel> BranchNode<T,X> get(BranchNodeType<T,X> nodeType)
	{
		return get(nodeType,BranchNodeGetterPolicy.CreateNeverPolicy);
	}
	public <X extends BranchNodeMetaModel> BranchNode<T,X> get(BranchNodeType<T,X> nodeType,BranchNodeGetterPolicy policy)
	{
		if(policy == null)
		{
			policy = BranchNodeGetterPolicy.CreateNeverPolicy;
		}
		
		NodeContainer<T,?> nodeContainer = this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType));
		
		if(policy.getCreateCase() == BranchNodeGetterPolicy.CreateCase.Never)
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
	
	public <X> LeafNode<T,X> getSingleValue(ModelPath<T,X> path)
	{
		return null;
	}
	
	private static class NodeContainer<P,T>
	{
		private PreparedNodeType preparedNodeType = null;
		private volatile Node node = null;
		
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
	}
	
	public static class BranchNodeGetterPolicy
	{
		public enum CreateCase {Never,CreateIfNull,ForceCreate};
		
		private CreateCase createCase = CreateCase.Never;
		private boolean synchronize = false;
		
		public static final BranchNodeGetterPolicy CreateNeverPolicy = new BranchNodeGetterPolicy(CreateCase.Never, false);
		public static final BranchNodeGetterPolicy CreateIfNullPolicy = new BranchNodeGetterPolicy(CreateCase.CreateIfNull, false);
		public static final BranchNodeGetterPolicy CreatePolicy = new BranchNodeGetterPolicy(CreateCase.ForceCreate, false);
		public static final BranchNodeGetterPolicy CreateIfNullSynchronizedPolicy = new BranchNodeGetterPolicy(CreateCase.CreateIfNull, true);
		public static final BranchNodeGetterPolicy CreateSynchronizedPolicy = new BranchNodeGetterPolicy(CreateCase.ForceCreate, true);
		
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
