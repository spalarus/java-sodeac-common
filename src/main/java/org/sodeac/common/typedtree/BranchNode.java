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
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.sodeac.common.typedtree.ModelingProcessor.PreparedNodeType;
import org.sodeac.common.typedtree.TypedTreeMetaModel.RootBranchNode;
import org.sodeac.common.typedtree.ModelingProcessor.PreparedMetaModel;

public class BranchNode<P extends BranchNodeMetaModel, T extends BranchNodeMetaModel> extends Node<P,T>
{
	private PreparedMetaModel preparedMetaModel = null;
	private List<NodeContainer<T,?>> nodeContainerList = null;
	protected RootBranchNode<?,?> rootNode = null;
	protected BranchNode<?,P> parentNode = null;
	private long OID = -1;
	private long positionInList = -1;
	
	protected BranchNode(RootBranchNode<?,?> rootNode, BranchNode<?,P> parentNode, Class<T> modelType)
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
					nodeContainer.node = new LeafNode<>(this,preparedNodeType);
				}
				else if (preparedNodeType.getNodeType() == PreparedNodeType.NodeType.BranchNodeList)
				{
					nodeContainer.nodeList = new ArrayList<BranchNode>();
					nodeContainer.unmodifiableNodeList = Collections.unmodifiableList(nodeContainer.nodeList);
				}
				nodeContainer.preparedNodeType = preparedNodeType;
				nodeContainerList.add(nodeContainer);
			}
			this.nodeContainerList = Collections.unmodifiableList(this.nodeContainerList);
			if(rootNode == null)
			{
				if(this instanceof RootBranchNode)
				{
					this.rootNode = (RootBranchNode<?,?>)this;
					this.OID = 0L;
				}
				else
				{
					throw new RuntimeException("missing root node");
				}
			}
			else
			{
				this.rootNode = rootNode;
				this.OID = rootNode.nextOID();
			}
			this.parentNode = parentNode;
			
			
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
	
	protected void disposeNode()
	{
		List<NodeContainer<T,?>> nodeContainerList = this.nodeContainerList;
		if(nodeContainerList != null)
		{
			for(NodeContainer<T,?> container : nodeContainerList)
			{
				if(container.node != null)
				{
					container.node.disposeNode();
				}
				if(container.nodeList != null)
				{
					for(BranchNode<?,?> item : container.nodeList)
					{
						item.disposeNode();
					}
					container.nodeList.clear();
				}
				container.node = null;
				container.nodeList = null;
				container.unmodifiableNodeList = null;
				container.preparedNodeType = null;
			}
		}
		this.preparedMetaModel = null;
		this.nodeContainerList = null;
		this.rootNode = null;
		this.parentNode = null;
	}
	
	// global
	
	protected RootBranchNode<?, ?> getRootNode()
	{
		return rootNode;
	}

	protected BranchNode<?, P> getParentNode()
	{
		return parentNode;
	}

	public BranchNode<P, T> compute(Consumer<BranchNode<P, T>> computer)
	{
		if(computer == null)
		{
			return this;
		}
		computer.accept(this);
		return this;
	}
	
	public BranchNode<P, T> computeWithReadLock(Consumer<BranchNode<P, T>> computer)
	{
		if(computer == null)
		{
			return this;
		}
		Lock lock = this.rootNode.getReadLock();
		lock.lock();
		try
		{
			computer.accept(this);
		}
		finally 
		{
			lock.unlock();
		}
		return this;
	}
	
	public BranchNode<P, T> computeWithWriteLock(Consumer<BranchNode<P, T>> computer)
	{
		if(computer == null)
		{
			return this;
		}
		Lock lock = this.rootNode.getWriteLock();
		lock.lock();
		try
		{
			computer.accept(this);
		}
		finally 
		{
			lock.unlock();
		}
		return this;
	}
	
	/*
	 * LeafNode methods
	 */
	
	public <X> LeafNode<T,X> get(LeafNodeType<T,X> nodeType)
	{
		return (LeafNode<T,X>) this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType)).node;
	}
	
	public <X> BranchNode<P,T> compute(LeafNodeType<T,X> nodeType, BiConsumer<BranchNode<P, T>, LeafNode<T,X>> computer)
	{
		LeafNode<T,X> node = (LeafNode<T,X>) this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType)).node;
		
		Lock lock = this.rootNode.isSychronized() ? this.rootNode.getWriteLock() : null;
		if(lock != null)
		{
			lock.lock();
		}
		try
		{
			computer.accept(this, node);
		}
		finally 
		{
			if(lock != null)
			{
				lock.unlock();
			}
		}
		return this;
	}
	
	public <X> BranchNode<P,T> setValue(LeafNodeType<T,X> nodeType, X value)
	{
		LeafNode<T,X> node = (LeafNode<T,X>) this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType)).node;
		Lock lock = this.rootNode.isSychronized() ? this.rootNode.getWriteLock() : null;
		if(lock != null)
		{
			lock.lock();
		}
		try
		{
			node.setValue(value);
		}
		finally 
		{
			if(lock != null)
			{
				lock.unlock();
			}
		}
		return this;
	}
	
	public <X> X getValue(LeafNodeType<T,X> nodeType)
	{
		return ((LeafNode<T,X>) this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType)).node).getValue();
	}
	
	/*
	 *  BranchNode methods
	 */
	
	public <X extends BranchNodeMetaModel> BranchNode<P, T> compute(BranchNodeType<T,X> nodeType, BiConsumer<BranchNode<P, T>, BranchNode<T,X>> computer)
	{
		if(computer == null)
		{
			return this;
		}
		
		Lock lock = this.rootNode.isSychronized() ? this.rootNode.getWriteLock() : null;
		if(lock != null)
		{
			lock.lock();
		}
		try
		{
			BranchNode<T,X> node = get(nodeType);
			computer.accept(this, node);
			return this;
		}
		finally 
		{
			if(lock != null)
			{
				lock.unlock();
			}
		}
	}
	
	public <X extends BranchNodeMetaModel> BranchNode<P, T> compute(BranchNodeType<T,X> nodeType,BiConsumer<BranchNode<P, T>, BranchNode<T,X>> ifAbsent,BiConsumer<BranchNode<P, T>, BranchNode<T,X>> ifPresent)
	{
		NodeContainer<T,?> nodeContainer = this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType));
		
		Lock lock = this.rootNode.isSychronized() ? this.rootNode.getWriteLock() : null;
		if(lock != null)
		{
			lock.lock();
		}
		try
		{
			BranchNode<T,X> node = (BranchNode)nodeContainer.node;
			if(node == null)
			{
				if(rootNode.isBranchNodeComputeAutoCreate() && (! rootNode.isImmutable()))
				{
					boolean created = false;
					node = new BranchNode(this.rootNode,this,nodeType.getTypeClass());
					try
					{
						if(ifAbsent != null)
						{
							ifAbsent.accept(this, node);
						}
						
						if(this.rootNode.publishModify(this, nodeContainer.preparedNodeType.getNodeTypeName(), nodeContainer.preparedNodeType.getStaticNodeTypeInstance(), BranchNodeType.class, null, node))
						{
							nodeContainer.node = node;
							created = true;
						}
					}
					finally 
					{
						if(! created)
						{
							node.disposeNode();
							node = null;
						}
					}
				}
				
				return this;
			}
			
			if(ifPresent == null)
			{
				return this;
			}
			ifPresent.accept(this, node);
			return this;
		}
		finally 
		{
			if(lock != null)
			{
				lock.unlock();
			}
		}
	}
	
	public <X extends BranchNodeMetaModel> BranchNode<P, T> remove(BranchNodeType<T,X> nodeType)
	{
		if(rootNode.isImmutable())
		{
			return this;
		}
		NodeContainer<T,?> nodeContainer = this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType));
		Lock lock = this.rootNode.isSychronized() ? this.rootNode.getWriteLock() : null;
		if(lock != null)
		{
			lock.lock();
		}
		try
		{
			if(nodeContainer.node != null)
			{
				if(this.rootNode.publishModify(this, nodeContainer.preparedNodeType.getNodeTypeName(), nodeContainer.preparedNodeType.getStaticNodeTypeInstance(), BranchNodeType.class, nodeContainer.node, null))
				{
					nodeContainer.node.disposeNode();
					nodeContainer.node = null;
				}
			}
			return this;
		}
		finally 
		{
			if(lock != null)
			{
				lock.unlock();
			}
		}
	}
	public <X extends BranchNodeMetaModel> BranchNode<T,X> create(BranchNodeType<T,X> nodeType)
	{
		return create(nodeType, null);
	}
	public <X extends BranchNodeMetaModel> BranchNode<T,X> create(BranchNodeType<T,X> nodeType, BiConsumer<BranchNode<P, T>, BranchNode<T,X>> computer)
	{
		if(rootNode.isImmutable())
		{
			return null;
		}
		
		NodeContainer<T,?> nodeContainer = this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType));
		
		Lock lock = this.rootNode.isSychronized() ? this.rootNode.getWriteLock() : null;
		if(lock != null)
		{
			lock.lock();
		}
		try
		{
			boolean created = false;
			BranchNode<T,X> oldNode = (BranchNode<T,X>)nodeContainer.node;
			BranchNode<T,X> newNode = new BranchNode(this.rootNode,this,nodeType.getTypeClass());
			
			try
			{
				if(computer != null)
				{
					computer.accept(this, newNode);
				}
				if(this.rootNode.publishModify(this, nodeContainer.preparedNodeType.getNodeTypeName(), nodeContainer.preparedNodeType.getStaticNodeTypeInstance(), BranchNodeType.class, oldNode, newNode))
				{
					if(oldNode != null)
					{
						nodeContainer.node.disposeNode();
					}
					nodeContainer.node = newNode;
					created = true;
					return newNode;
				}
			}
			finally 
			{
				if(! created)
				{
					newNode.disposeNode();
				}
			}
			return null;
		}
		finally 
		{
			if(lock != null)
			{
				lock.unlock();
			}
		}
	}
	public <X extends BranchNodeMetaModel> BranchNode<T,X> get(BranchNodeType<T,X> nodeType)
	{
		NodeContainer<T,?> nodeContainer = this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType));
		
		if(! this.rootNode.isBranchNodeGetterAutoCreate())
		{
			return (BranchNode<T,X>) nodeContainer.node;
		}
		
		BranchNode<T,X> node = (BranchNode<T,X>)nodeContainer.node;
		if(node != null)
		{
			return node;
		}
		
		Lock lock = this.rootNode.isSychronized() ? this.rootNode.getWriteLock() : null;
		if(lock != null)
		{
			lock.lock();
		}
		try
		{
			node = (BranchNode<T,X>)nodeContainer.node;
			if(node != null)
			{
				return node;
			}
			boolean created = false;
			
			node = new BranchNode(this.rootNode,this,nodeType.getTypeClass());
			
			try
			{
				if(this.rootNode.publishModify(this, nodeContainer.preparedNodeType.getNodeTypeName(), nodeContainer.preparedNodeType.getStaticNodeTypeInstance(), BranchNodeType.class, null, node))
				{
					nodeContainer.node = node;
					created = true;
				}
			}
			finally 
			{
				if(! created)
				{
					node.disposeNode();
				}
			}
			return node;
		}
		finally 
		{
			if(lock != null)
			{
				lock.unlock();
			}
		}
	}
	
	/*
	 * BranchNode List
	 */
	
	public <X extends BranchNodeMetaModel> List<BranchNode<T,X>> getUnmodifiableNodeList(BranchNodeListType<T,X> nodeType)
	{
		return this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType)).unmodifiableNodeList;
	}
	
	public <X extends BranchNodeMetaModel> List<BranchNode<T,X>> getUnmodifiableNodeList(BranchNodeListType<T,X> nodeType, Predicate<BranchNode<T,X>> predicate)
	{
		List<BranchNode<T,X>> originalList = this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType)).unmodifiableNodeList;
		if(predicate == null)
		{
			return originalList;
		}
		Lock lock = this.rootNode.isSychronized() ? this.rootNode.getReadLock() : null;
		if(lock != null)
		{
			lock.lock();
		}
		try
		{
			List<BranchNode<T,X>> filteredList = new ArrayList<BranchNode<T,X>>();
			originalList.forEach(n -> { if(predicate.test(n)) {filteredList.add(n);} });
			return Collections.unmodifiableList(originalList);
		}
		finally 
		{
			if(lock != null)
			{
				lock.unlock();
			}
		}
	}
	
	public <X extends BranchNodeMetaModel> List<BranchNode<T,X>> getUnmodifiableNodeListSnapshot(BranchNodeListType<T,X> nodeType)
	{
		NodeContainer<T,?> nodeContainer = this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType));
		
		List<BranchNode<T,X>> unmodifiableNodeListSnapshot = nodeContainer.unmodifiableNodeListSnapshot;
		if(unmodifiableNodeListSnapshot != null)
		{
			return unmodifiableNodeListSnapshot;
		}
			
		Lock lock = this.rootNode.isSychronized() ? this.rootNode.getWriteLock() : null;
		if(lock != null)
		{
			lock.lock();
		}
		try
		{
			unmodifiableNodeListSnapshot = nodeContainer.unmodifiableNodeListSnapshot;
			if(unmodifiableNodeListSnapshot != null)
			{
				return unmodifiableNodeListSnapshot;
			}
			
			List<BranchNode<T,X>> snapshot = new ArrayList<BranchNode<T,X>>();
			nodeContainer.nodeList.forEach(n -> snapshot.add(n));
			nodeContainer.unmodifiableNodeListSnapshot = Collections.unmodifiableList(snapshot);
			return snapshot;
		}
		finally 
		{
			if(lock != null)
			{
				lock.unlock();
			}
		}
	}
	
	public <X extends BranchNodeMetaModel> BranchNode<P, T> setComperator(BranchNodeListType<T,X> nodeType, Comparator<BranchNode<T,X>> comparator)
	{
		NodeContainer<T,?> nodeContainer = this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType));
		
		
		Lock lock = this.rootNode.isSychronized() ? this.rootNode.getWriteLock() : null;
		if(lock != null)
		{
			lock.lock();
		}
		try
		{
			if((nodeContainer.listComparator == null) && (comparator == null))
			{
				return this;
			}
			
			nodeContainer.listComparator = comparator;
			
			if(this.rootNode.isImmutable())
			{
				return this;
			}
			
			Collections.sort(nodeContainer.nodeList, nodeContainer.listComparator);
			nodeContainer.unmodifiableNodeListSnapshot = null;
		}
		finally 
		{
			if(lock != null)
			{
				lock.unlock();
			}
		}
		
		return this;
	}
	
	public <X extends BranchNodeMetaModel> BranchNode<T,X> get(BranchNodeListType<T,X> nodeType, Predicate<BranchNode<T,X>> predicate)
	{
		NodeContainer<T,?> nodeContainer = this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType));
		Lock lock = this.rootNode.isSychronized() ? this.rootNode.getWriteLock() : null;
		if(lock != null)
		{
			lock.lock();
		}
		try
		{
			for(BranchNode<T,X> node : nodeContainer.nodeList)
			{
				if(predicate.test(node))
				{
					return node;
				}
			}
		}
		finally 
		{
			if(lock != null)
			{
				lock.unlock();
			}
		}
		return null;
	}

	public <X extends BranchNodeMetaModel> BranchNode<T,X> create(BranchNodeListType<T,X> nodeType)
	{
		if(this.rootNode.isImmutable())
		{
			return null;
		}
		
		NodeContainer<T,?> nodeContainer = this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType));
		Lock lock = this.rootNode.isSychronized() ? this.rootNode.getWriteLock() : null;
		if(lock != null)
		{
			lock.lock();
		}
		try
		{
			boolean created = false;
			BranchNode<T,X> node = new BranchNode(this.rootNode,this,nodeContainer.preparedNodeType.getNodeTypeClass());
			try
			{
				if(this.rootNode.publishModify(this, nodeContainer.preparedNodeType.getNodeTypeName(), nodeContainer.preparedNodeType.getStaticNodeTypeInstance(), BranchNodeType.class, null, node))
				{
					nodeContainer.nodeList.add(node);
					node.positionInList = nodeContainer.nodeList.size() -1;
					
					nodeContainer.unmodifiableNodeListSnapshot = null;
					created = true;
					return node;
				}
			}
			finally
			{
				if(! created)
				{
					node.disposeNode();
				}
			}
		}
		finally 
		{
			if(lock != null)
			{
				lock.unlock();
			}
		}
		return null;
	}
	
	public <X extends BranchNodeMetaModel> BranchNode<P, T> create(BranchNodeListType<T,X> nodeType, BiConsumer<BranchNode<P, T>, BranchNode<T,X>> consumer)
	{
		if(this.rootNode.isImmutable())
		{
			return this;
		}
		
		NodeContainer<T,?> nodeContainer = this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType));
		Lock lock = this.rootNode.isSychronized() ? this.rootNode.getWriteLock() : null;
		if(lock != null)
		{
			lock.lock();
		}
		try
		{
			boolean created = false;
			BranchNode<T,X> node = new BranchNode(this.rootNode,this,nodeContainer.preparedNodeType.getNodeTypeClass());
			try
			{
				if(consumer != null)
				{
					consumer.accept(this, node);
				}
				
				if(this.rootNode.publishModify(this, nodeContainer.preparedNodeType.getNodeTypeName(), nodeContainer.preparedNodeType.getStaticNodeTypeInstance(), BranchNodeType.class, null, node))
				{
					if((nodeContainer.listComparator == null) || nodeContainer.nodeList.isEmpty())
					{
						nodeContainer.nodeList.add(node);
						node.positionInList = nodeContainer.nodeList.size() -1;
					}
					else
					{
						if(nodeContainer.nodeList.isEmpty() || nodeContainer.listComparator.compare(node, nodeContainer.nodeList.get(nodeContainer.nodeList.size() -1)) > 0)
						{
							nodeContainer.nodeList.add(node);
							node.positionInList = nodeContainer.nodeList.size() -1;
						}
						else if(nodeContainer.listComparator.compare(node, nodeContainer.nodeList.get(0)) < 0)
						{
							nodeContainer.nodeList.add(0,node);
							
							int index = 0;
							for(BranchNode nodeItem : nodeContainer.nodeList)
							{
								nodeItem.positionInList = index++;
							}
						}
						else
						{
							int beginIndex = 0;
							int rangeSize = nodeContainer.nodeList.size();
							int endIndex = rangeSize -1;
							int testIndex = endIndex / 2;
							int testResult = nodeContainer.listComparator.compare(node, nodeContainer.nodeList.get(testIndex));
							
							while(rangeSize > 1)
							{
								
								if(testResult < 0)
								{
									if(endIndex == testIndex)
									{
										testIndex = beginIndex;
										rangeSize = endIndex - beginIndex;
										testResult = nodeContainer.listComparator.compare(node, nodeContainer.nodeList.get(testIndex));
										break;
									}
									endIndex = testIndex;
								}
								else
								{
									beginIndex = testIndex;
								}
								
								testIndex = (beginIndex + endIndex) / 2;
								rangeSize = endIndex - beginIndex;
								testResult = nodeContainer.listComparator.compare(node, nodeContainer.nodeList.get(testIndex));
							}
							
							if(testResult < 0)
							{
								nodeContainer.nodeList.add(testIndex,node);
								
								for(int i = testIndex; i < nodeContainer.nodeList.size(); i++)
								{
									BranchNode nodeItem = nodeContainer.nodeList.get(i);
									nodeItem.positionInList = i;
								}
							}
							else if(nodeContainer.nodeList.size() -1 > testIndex)
							{
								nodeContainer.nodeList.add(testIndex + 1,node);
								
								for(int i = testIndex + 1; i < nodeContainer.nodeList.size(); i++)
								{
									BranchNode nodeItem = nodeContainer.nodeList.get(i);
									nodeItem.positionInList = i;
								}
							}
							else
							{
								nodeContainer.nodeList.add(node);
								node.positionInList = nodeContainer.nodeList.size() -1;
							}
						}
					}
					nodeContainer.unmodifiableNodeListSnapshot = null;
					created = true;
				}
			}
			finally 
			{
				if(!created)
				{
					node.disposeNode();
				}
			}
		}
		finally 
		{
			if(lock != null)
			{
				lock.unlock();
			}
		}
		return this;
	}
	
	public <X extends BranchNodeMetaModel> BranchNode<P, T> createIfAbsent(BranchNodeListType<T,X> nodeType, Predicate<BranchNode<T,X>> predicate,  BiConsumer<BranchNode<P, T>, BranchNode<T,X>> consumer)
	{
		if(this.rootNode.isImmutable())
		{
			return this;
		}
		
		NodeContainer<T,?> nodeContainer = this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType));
		Lock lock = this.rootNode.isSychronized() ? this.rootNode.getWriteLock() : null;
		if(lock != null)
		{
			lock.lock();
		}
		try
		{
			for(BranchNode<T,X> node : nodeContainer.nodeList)
			{
				if(predicate.test(node))
				{
					if(consumer != null)
					{
						consumer.accept(this, node);
					}
					return this;
				}
			}
			boolean created = false;
			BranchNode<T,X> node = new BranchNode(this.rootNode,this,nodeContainer.preparedNodeType.getNodeTypeClass());
			try
			{
				if(consumer != null)
				{
					consumer.accept(this, node);
				}
				
				if(this.rootNode.publishModify(this, nodeContainer.preparedNodeType.getNodeTypeName(), nodeContainer.preparedNodeType.getStaticNodeTypeInstance(), BranchNodeType.class, null, node))
				{
					if((nodeContainer.listComparator == null) || nodeContainer.nodeList.isEmpty())
					{
						nodeContainer.nodeList.add(node);
						node.positionInList = nodeContainer.nodeList.size() -1;
					}
					else
					{
						if(nodeContainer.nodeList.isEmpty() || nodeContainer.listComparator.compare(node, nodeContainer.nodeList.get(nodeContainer.nodeList.size() -1)) > 0)
						{
							nodeContainer.nodeList.add(node);
							node.positionInList = nodeContainer.nodeList.size() -1;
						}
						else if(nodeContainer.listComparator.compare(node, nodeContainer.nodeList.get(0)) < 0)
						{
							nodeContainer.nodeList.add(0,node);
							
							int index = 0;
							for(BranchNode nodeItem : nodeContainer.nodeList)
							{
								nodeItem.positionInList = index++;
							}
						}
						else
						{
							int beginIndex = 0;
							int rangeSize = nodeContainer.nodeList.size();
							int endIndex = rangeSize -1;
							int testIndex = endIndex / 2;
							int testResult = nodeContainer.listComparator.compare(node, nodeContainer.nodeList.get(testIndex));
							
							while(rangeSize > 1)
							{
								
								if(testResult < 0)
								{
									if(endIndex == testIndex)
									{
										testIndex = beginIndex;
										rangeSize = endIndex - beginIndex;
										testResult = nodeContainer.listComparator.compare(node, nodeContainer.nodeList.get(testIndex));
										break;
									}
									endIndex = testIndex;
								}
								else
								{
									beginIndex = testIndex;
								}
								
								testIndex = (beginIndex + endIndex) / 2;
								rangeSize = endIndex - beginIndex;
								testResult = nodeContainer.listComparator.compare(node, nodeContainer.nodeList.get(testIndex));
							}
							
							if(testResult < 0)
							{
								nodeContainer.nodeList.add(testIndex,node);
								
								for(int i = testIndex; i < nodeContainer.nodeList.size(); i++)
								{
									BranchNode nodeItem = nodeContainer.nodeList.get(i);
									nodeItem.positionInList = i;
								}
							}
							else if(nodeContainer.nodeList.size() -1 > testIndex)
							{
								nodeContainer.nodeList.add(testIndex + 1,node);
								
								for(int i = testIndex + 1; i < nodeContainer.nodeList.size(); i++)
								{
									BranchNode nodeItem = nodeContainer.nodeList.get(i);
									nodeItem.positionInList = i;
								}
							}
							else
							{
								nodeContainer.nodeList.add(node);
								node.positionInList = nodeContainer.nodeList.size() -1;
							}
						}
					}
					nodeContainer.unmodifiableNodeListSnapshot = null;
					created = true;
				}
			}
			finally 
			{
				if(!created)
				{
					node.disposeNode();
				}
			}
		}
		finally 
		{
			if(lock != null)
			{
				lock.unlock();
			}
		}
		return this;
	}
	

	public <X extends BranchNodeMetaModel> boolean remove(BranchNodeListType<T,X> nodeType, BranchNode<P, T> node)
	{
		if(this.rootNode.isImmutable())
		{
			return false;
		}
		
		NodeContainer<T,?> nodeContainer = this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType));
		
		Lock lock = this.rootNode.isSychronized() ? this.rootNode.getWriteLock() : null;
		if(lock != null)
		{
			lock.lock();
		}
		try
		{
			if(nodeContainer.nodeList.contains(node))
			{
				if(this.rootNode.publishModify(this, nodeContainer.preparedNodeType.getNodeTypeName(), nodeContainer.preparedNodeType.getStaticNodeTypeInstance(), BranchNodeType.class, node, null))
				{
					nodeContainer.nodeList.remove(node);
					node.disposeNode();
					nodeContainer.unmodifiableNodeListSnapshot = null;
					return true;
				}
			}
		}
		finally 
		{
			if(lock != null)
			{
				lock.unlock();
			}
		}
		return false;
	}

	public <X extends BranchNodeMetaModel> void clear(BranchNodeListType<T,X> nodeType)
	{
		if(this.rootNode.isImmutable())
		{
			return;
		}
		
		NodeContainer<T,?> nodeContainer = this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType));
		
		Lock lock = this.rootNode.isSychronized() ? this.rootNode.getWriteLock() : null;
		if(lock != null)
		{
			lock.lock();
		}
		try
		{
			List<BranchNode<P, T>> copy = (List<BranchNode<P, T>>)new ArrayList(nodeContainer.nodeList);
			
			for(BranchNode<P,T> node : copy)
			{
				if(this.rootNode.publishModify(this, nodeContainer.preparedNodeType.getNodeTypeName(), nodeContainer.preparedNodeType.getStaticNodeTypeInstance(), BranchNodeType.class, node, null))
				{
					nodeContainer.nodeList.remove(node);
					node.disposeNode();
				}
			}
			
			nodeContainer.unmodifiableNodeListSnapshot = null;
		}
		finally 
		{
			if(lock != null)
			{
				lock.unlock();
			}
		}
		
	}
	public <X extends BranchNodeMetaModel> boolean remove(BranchNodeListType<T,X> nodeType, int index)
	{
		if(this.rootNode.isImmutable())
		{
			return false;
		}
		
		NodeContainer<T,?> nodeContainer = this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType));
		
		Lock lock = this.rootNode.isSychronized() ? this.rootNode.getWriteLock() : null;
		if(lock != null)
		{
			lock.lock();
		}
		try
		{
			BranchNode<T,X> node = (BranchNode<T,X>)nodeContainer.nodeList.get(index);
			
			if(node != null)
			{
				if(this.rootNode.publishModify(this, nodeContainer.preparedNodeType.getNodeTypeName(), nodeContainer.preparedNodeType.getStaticNodeTypeInstance(), BranchNodeType.class, node, null))
				{
					nodeContainer.nodeList.remove(index);
					node.disposeNode();
					nodeContainer.unmodifiableNodeListSnapshot = null;
					return true;
				}
			}
		}
		finally 
		{
			if(lock != null)
			{
				lock.unlock();
			}
		}
		return false;
	}

	
	/*public <X extends BranchNodeMetaModel> BranchNodeList<T,X> get(BranchNodeListType<T,X> nodeType)
	{
		return (BranchNodeList<T,X>) this.nodeContainerList.get(this.preparedMetaModel.getNodeTypeIndexByClass().get(nodeType)).getNode();
	}*/
	
	// Helper Node
	
	private static class NodeContainer<P,T>
	{
		private PreparedNodeType preparedNodeType = null;
		private volatile Node node = null;
		private ArrayList<BranchNode> nodeList = null; 
		private List unmodifiableNodeList = null;
		private volatile Comparator listComparator = null; 
		private volatile List unmodifiableNodeListSnapshot = null;
		
	}
}
