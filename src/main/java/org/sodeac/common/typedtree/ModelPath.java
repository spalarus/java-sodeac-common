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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import org.sodeac.common.typedtree.ModelPath.NodeSelector.Axis;
import org.sodeac.common.typedtree.ModelPath.NodeSelector.PathPredicate;

/**
 * A model path selects nodes. It starts from source node and navigate to tree by path definition to select nodes or value of nodes.
 * 
 * @author Sebastian Palarus
 *
 * @param <R> type of start node
 * @param <T> type of nodes or node's value to select
 */
public class ModelPath<R extends BranchNodeMetaModel,T>
{
	private LinkedList<NodeSelector<?,?>> selectorList = new LinkedList<>();
	private Class<T> clazz = null;
	
	private ModelPath()
	{
		super();
	}
	
	protected ModelPath(NodeSelector<R,T> lastSelector)
	{
		super();
		
		if(lastSelector.getType() == null)
		{
			this.clazz = (Class<T>) lastSelector.getRootType().getClass();
		}
		else
		{
			if(lastSelector.getAxis() == Axis.VALUE)
			{
				this.clazz = (Class<T>) lastSelector.getType().getTypeClass();
			}
			else
			{
				this.clazz = (Class<T>) lastSelector.getType().getClass();
			}
		}
		
		NodeSelector<?,?> current = lastSelector;
		while(current != null)
		{
			selectorList.addFirst(current);
			current = current.getParentSelector();
		}
	}
	
	protected Class<T> getClazz()
	{
		return clazz;
	}

	protected LinkedList<NodeSelector<?, ?>> getNodeSelectorList()
	{
		return selectorList;
	}

	public void dispose()
	{
		for(NodeSelector<?,?> selector : selectorList)
		{
			selector.dispose();
		}
		selectorList.clear();
		selectorList = null;
		clazz = null;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((selectorList == null) ? 0 : selectorList.hashCode());
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModelPath other = (ModelPath) obj;
		if (selectorList == null)
		{
			if (other.selectorList != null)
				return false;
		} else if (!selectorList.equals(other.selectorList))
			return false;
		if (clazz == null)
		{
			if (other.clazz != null)
				return false;
		} else if (!clazz.equals(other.clazz))
			return false;
		return true;
	}
	
	public ModelPath<R,T> clone()
	{
		ModelPath<R, T> clonedModelPath = new ModelPath<R,T>();
		if(this.selectorList != null)
		{
			clonedModelPath.selectorList = new LinkedList<NodeSelector<?,?>>();
			
			NodeSelector previewsNodeSelector = null;
			for(NodeSelector nodeSelector :  this.selectorList)
			{
				NodeSelector<R,T> clonedNodeSelector = new NodeSelector<>();
				clonedNodeSelector.root = nodeSelector.root;
				clonedNodeSelector.parentSelector = previewsNodeSelector;
				clonedNodeSelector.type = nodeSelector.type;
				clonedNodeSelector.axis = nodeSelector.axis;
				clonedNodeSelector.predicate = nodeSelector.predicate;
				
				if(nodeSelector.childSelectorList != null)
				{
					clonedNodeSelector.childSelectorList = new ArrayList<NodeSelector<?,?>>();
				}
				
				if((previewsNodeSelector != null) && (previewsNodeSelector.childSelectorList != null))
				{
					previewsNodeSelector.childSelectorList.add(clonedNodeSelector);
				}
				
				previewsNodeSelector = clonedNodeSelector;
				clonedModelPath.selectorList.add(clonedNodeSelector);
			}
		}
		clonedModelPath.clazz = this.clazz;
		return clonedModelPath;
	}

	public static class ModelPathBuilder<R extends BranchNodeMetaModel,S extends BranchNodeMetaModel>
	{
		private BranchNodeMetaModel root = null;
		private BranchNodeMetaModel self = null;
		private NodeSelector<?, ?> selector = null;
		
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
			this.selector = new NodeSelector<>(root);
		}
		
		private ModelPathBuilder(BranchNodeMetaModel root,BranchNodeType field, NodeSelector<?,?> previews)
		{
			super();
			this.root = root;
			this.self = field.getValueDefaultInstance();
			this.selector = new NodeSelector<>(this.root,previews,field,NodeSelector.Axis.CHILD);
		}
		
		/**
		 * Definition to navigate to next child node.
		 * 
		 * @param field static child node type instance from meta model
		 * @return builder
		 */
		public <N extends BranchNodeMetaModel> ModelPathBuilder<R,N> child(BranchNodeType<S, N> field)
		{
			if((this.selector.childSelectorList != null) && (!this.selector.childSelectorList.isEmpty()))
			{
				throw new RuntimeException(getClass() + ": child already exists ");
			}
			return new ModelPathBuilder<R,N>(this.root,field,this.selector);
		}
		
		public <N extends BranchNodeMetaModel> BranchNodePredicateBuilder<R,N> childWithPredicates(BranchNodeType<S, N> field)
		{
			if((this.selector.childSelectorList != null) && (!this.selector.childSelectorList.isEmpty()))
			{
				throw new RuntimeException(getClass() + ": child already exists ");
			}
			ModelPathBuilder<R,N> builder = new ModelPathBuilder<R,N>(this.root,field,selector);
			return new BranchNodePredicateBuilder(builder);
		}
		
		public <T> ModelPath<R, T> buildForValue(LeafNodeType<S, T> field) 
		{
			if((this.selector.childSelectorList != null) && (!this.selector.childSelectorList.isEmpty()))
			{
				throw new RuntimeException(getClass() + ": child already exists ");
			}
			return new ModelPath(new NodeSelector<R,T>(this.root,this.selector,field, NodeSelector.Axis.VALUE));
		}
		
		public <T> ModelPath<R, T> build() 
		{
			return new ModelPath(this.selector);
		}
		
		public <T> ModelPath<R, LeafNode<?, T>> buildForNode(LeafNodeType<S, T> field) 
		{
			if((this.selector.childSelectorList != null) && (!this.selector.childSelectorList.isEmpty()))
			{
				throw new RuntimeException(getClass() + ": child already exists ");
			}
			return new ModelPath(new NodeSelector<R,LeafNode<?, T>>(this.root,this.selector, (INodeType)field, NodeSelector.Axis.CHILD));
		}
		
		public <T extends BranchNodeMetaModel> ModelPath<R, BranchNode<S, T>> buildForNode(BranchNodeType<S, T> field) 
		{
			if((this.selector.childSelectorList != null) && (!this.selector.childSelectorList.isEmpty()))
			{
				throw new RuntimeException(getClass() + ": child already exists ");
			}
			return new ModelPath(new NodeSelector<R,BranchNode<S, T>>(this.root,this.selector, (INodeType)field , NodeSelector.Axis.CHILD));
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
			
			public BranchNodePredicateBuilder<R,R> childWithPredicates()
			{
				return new BranchNodePredicateBuilder(this);
			}
		}
		
		public static class BranchNodePredicateBuilder<R extends BranchNodeMetaModel,N extends BranchNodeMetaModel>
		{
			private ModelPathBuilder<R,N> builder = null;
			private PathPredicate rootPedicate = null;
			private PathPredicate currentPredicate = null;
			
			private BranchNodePredicateBuilder(ModelPathBuilder<R,N> builder)
			{
				super();
				this.builder = builder;
				this.rootPedicate = new PathPredicate(null, PathPredicate.LogicalOperator.AND, false);
				this.currentPredicate = this.rootPedicate;
			}
			
			public <T> BranchNodePredicateBuilder<R,N> addLeafNodePredicate(LeafNodeType<N, T> field, Predicate<LeafNode<N,T>> predicate)
			{
				this.currentPredicate.addLeafNodePredicate((LeafNodeType<?, ?>)field, (Predicate)predicate );
				return this;
			}
			
			public BranchNodePredicateBuilder<R,N> and()
			{
				this.currentPredicate = new PathPredicate(this.currentPredicate, PathPredicate.LogicalOperator.AND, false);
				return this;
			}
			
			public BranchNodePredicateBuilder<R,N> andNot()
			{
				this.currentPredicate = new PathPredicate(this.currentPredicate, PathPredicate.LogicalOperator.AND, true);
				return this;
			}
			
			public BranchNodePredicateBuilder<R,N> or()
			{
				this.currentPredicate = new PathPredicate(this.currentPredicate, PathPredicate.LogicalOperator.OR, false);
				return this;
			}
			
			public BranchNodePredicateBuilder<R,N> orNot()
			{
				this.currentPredicate = new PathPredicate(this.currentPredicate, PathPredicate.LogicalOperator.OR, true);
				return this;
			}
			
			public BranchNodePredicateBuilder<R,N> close()
			{
				this.currentPredicate = currentPredicate.getParent();
				Objects.requireNonNull(this.currentPredicate, "No child predicate to close");
				return this;
			}
			
			public ModelPathBuilder<R,N> build()
			{
				this.builder.selector.setPredicate(this.rootPedicate);
				return this.builder;
			}
		}
	}

	protected static class NodeSelector<R extends BranchNodeMetaModel,T>
	{
		protected static enum Axis {SELF,CHILD,VALUE}
		
		private NodeSelector<?,?> parentSelector = null;
		private List<NodeSelector<?,?>> childSelectorList = null; // Set?
		private List<IModifyListener<?>> modifyListenerList = null;
		private Map<Object,Set<IModifyListener<?>>> registrationObjects = null;
		private PathPredicate predicate = null;
		private INodeType<?, ?> type = null;
		private BranchNodeMetaModel root = null;
		private Axis axis = null;
		
		protected NodeSelector(BranchNodeMetaModel root)
		{
			super();
			this.root = root;
			this.axis = Axis.SELF;
		}
		
		protected NodeSelector<R,T> copy(NodeSelector<?,?> clonedParentSelector, boolean deep)
		{
			NodeSelector<R,T> clonedNodeSelector = new NodeSelector<>();
			clonedNodeSelector.root = this.root;
			clonedNodeSelector.parentSelector = clonedParentSelector;
			clonedNodeSelector.type = this.type;
			clonedNodeSelector.axis = this.axis;
			if((childSelectorList != null) && deep)
			{
				clonedNodeSelector.childSelectorList = new ArrayList<NodeSelector<?,?>>(); 
				for(NodeSelector<?, ?> child : this.childSelectorList)
				{
					clonedNodeSelector.childSelectorList.add(child.copy(clonedNodeSelector,true));
				}
			}
			clonedNodeSelector.predicate = this.predicate;
			
			return clonedNodeSelector;
		}
		
		private NodeSelector()
		{
			super();
		}
		
		protected NodeSelector(BranchNodeMetaModel root, NodeSelector<?,?> previousSelector, INodeType<?,?> type, Axis axis)
		{
			super();
			this.root = root;
			this.parentSelector = previousSelector;
			this.type = type;
			if(previousSelector.childSelectorList == null)
			{
				previousSelector.childSelectorList = new ArrayList<NodeSelector<?,?>>();
			}
			previousSelector.childSelectorList.add(this);
			if(axis == null)
			{
				axis = Axis.CHILD;
			}
			this.axis = axis;
		}
		
		protected NodeSelector<?, ?> getParentSelector()
		{
			return parentSelector;
		}
		protected NodeSelector<?, ?> setParentSelector(NodeSelector<?, ?> parentSelector)
		{
			this.parentSelector = parentSelector;
			return this;
		}
		protected List<NodeSelector<?, ?>> getChildSelectorList()
		{
			return this.childSelectorList;
		}
		protected void setChildSelectorList(List<NodeSelector<?, ?>> childSelectorList)
		{
			this.childSelectorList = childSelectorList;
		}

		protected NodeSelector<?, ?> setPredicate(PathPredicate predicate)
		{
			this.predicate = predicate;
			return this;
		}

		protected PathPredicate getPredicate()
		{
			return predicate;
		}
		
		protected INodeType<?, ?> getType()
		{
			return type;
		}

		protected BranchNodeMetaModel getRootType()
		{
			return root;
		}

		protected void setRoot(BranchNodeMetaModel root)
		{
			this.root = root;
		}

		protected Axis getAxis()
		{
			return axis;
		}

		protected List<IModifyListener<?>> getModifyListenerList()
		{
			return modifyListenerList;
		}

		protected void setModifyListenerList(List<IModifyListener<?>> modifyListenerList)
		{
			this.modifyListenerList = modifyListenerList;
		}

		protected Map<Object, Set<IModifyListener<?>>> getRegistrationObjects()
		{
			return registrationObjects;
		}

		protected void setRegistrationObjects(Map<Object, Set<IModifyListener<?>>> registrationObjects)
		{
			this.registrationObjects = registrationObjects;
		}

		protected void dispose()
		{
			if(childSelectorList != null)
			{
				for(NodeSelector<?, ?> child : this.childSelectorList)
				{
					child.dispose();
				}
				this.childSelectorList.clear();
			}
			
			if(registrationObjects != null)
			{
				registrationObjects.clear();
			}
			
			if(predicate != null)
			{
				predicate.dispose();
			}
			
			parentSelector = null;
			childSelectorList = null;
			predicate = null;
			registrationObjects = null;
			type = null;
			root = null;
			axis = null;
		}
		
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((axis == null) ? 0 : axis.hashCode());
			result = prime * result + ((predicate == null) ? 0 : predicate.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NodeSelector other = (NodeSelector) obj;
			if (axis != other.axis)
				return false;
			if (predicate == null)
			{
				if (other.predicate != null)
					return false;
			} else if (!predicate.equals(other.predicate))
				return false;
			if (type == null)
			{
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}



		protected static class PathPredicate
		{
			protected static enum LogicalOperator {AND,OR};
			
			protected PathPredicate(PathPredicate parent, LogicalOperator logicalOperator,boolean invert)
			{
				super();
				this.parent = parent;
				this.logicalOperator = logicalOperator;
			}
			
			private PathPredicate parent = null;
			private LogicalOperator logicalOperator = null;
			private boolean invert = false;
			
			private List<LeafNodePredicate<?, ?>> leafNodePredicateList = null;
			private List<PathPredicate> childPredicateList = null;
			
			protected LogicalOperator getLogicalOperator()
			{
				return logicalOperator;
			}
			protected void setLogicalOperator(LogicalOperator logicalOperator)
			{
				this.logicalOperator = logicalOperator;
			}

			protected boolean isInvert()
			{
				return invert;
			}
			protected void setInvert(boolean invert)
			{
				this.invert = invert;
			}

			protected PathPredicate getParent()
			{
				return parent;
			}

			protected PathPredicate addLeafNodePredicate(LeafNodeType<?, ?> field, Predicate<LeafNode<?,?>> predicate)
			{
				if(this.leafNodePredicateList == null)
				{
					this.leafNodePredicateList = new ArrayList<NodeSelector.LeafNodePredicate<?,?>>();
				}
				this.leafNodePredicateList.add(new LeafNodePredicate(field, predicate) );
				return this;
			}
			
			protected PathPredicate addChildPredicate(PathPredicate childPredicate)
			{
				if(this.childPredicateList == null)
				{
					this.childPredicateList = new ArrayList<PathPredicate>();
				}
				this.childPredicateList.add(childPredicate);
				return this;
			}
			
			protected void dispose()
			{
				if(this.childPredicateList != null)
				{
					for(PathPredicate childPredicate : this.childPredicateList)
					{
						childPredicate.dispose();
					}
					this.childPredicateList.clear();
				}
				if(this.leafNodePredicateList != null)
				{
					for(LeafNodePredicate<?,?> leafNodePredicate : this.leafNodePredicateList)
					{
						leafNodePredicate.dispose();
					}
					this.leafNodePredicateList.clear();
				}
				
				this.parent = null;
				this.logicalOperator = null;
				this.childPredicateList = null;
				this.leafNodePredicateList = null;
			}
			@Override
			public int hashCode()
			{
				final int prime = 31;
				int result = 1;
				result = prime * result + ((childPredicateList == null) ? 0 : childPredicateList.hashCode());
				result = prime * result + (invert ? 1231 : 1237);
				result = prime * result + ((leafNodePredicateList == null) ? 0 : leafNodePredicateList.hashCode());
				result = prime * result + ((logicalOperator == null) ? 0 : logicalOperator.hashCode());
				return result;
			}
			
			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				PathPredicate other = (PathPredicate) obj;
				if (childPredicateList == null)
				{
					if (other.childPredicateList != null)
						return false;
				} else if (!childPredicateList.equals(other.childPredicateList))
					return false;
				if (invert != other.invert)
					return false;
				if (leafNodePredicateList == null)
				{
					if (other.leafNodePredicateList != null)
						return false;
				} else if (!leafNodePredicateList.equals(other.leafNodePredicateList))
					return false;
				if (logicalOperator != other.logicalOperator)
					return false;
				return true;
			}
			
			public PathPredicate clone(PathPredicate clonedParent)
			{
				PathPredicate clonedPathPredicate = new PathPredicate(clonedParent, this.logicalOperator, this.invert);
				
				if(this.leafNodePredicateList != null)
				{
					clonedPathPredicate.leafNodePredicateList = new ArrayList<LeafNodePredicate<?, ?>>();
					for(LeafNodePredicate<?, ?> leafNodePredicate : this.leafNodePredicateList)
					{
						clonedPathPredicate.leafNodePredicateList.add(leafNodePredicate.clone());
					}
				}
				
				if(this.childPredicateList != null)
				{
					clonedPathPredicate.childPredicateList = new ArrayList<PathPredicate>();
					for(PathPredicate pathPredicate : this.childPredicateList)
					{
						clonedPathPredicate.childPredicateList.add(pathPredicate.clone(clonedPathPredicate));
					}
				}
				
				return clonedPathPredicate;
			}
			
		}
		
		protected static class LeafNodePredicate<N extends BranchNodeMetaModel,T>
		{
			protected LeafNodePredicate(LeafNodeType<N, T> field, Predicate<LeafNode<N,T>> predicate)
			{
				super();
				this.field = field;
				this.predicate = predicate;
			}
			private LeafNodeType<N, T> field = null;
			private Predicate<LeafNode<N,T>> predicate = null;
			
			protected LeafNodeType<N, T> getField()
			{
				return field;
			}
			protected Predicate<LeafNode<N, T>> getPredicate()
			{
				return predicate;
			}
			
			protected void dispose()
			{
				this.field = null;
				this.predicate = null;
			}
			
			@Override
			public int hashCode()
			{
				final int prime = 31;
				int result = 1;
				result = prime * result + ((field == null) ? 0 : field.hashCode());
				result = prime * result + ((predicate == null) ? 0 : predicate.hashCode());
				return result;
			}
			
			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				LeafNodePredicate other = (LeafNodePredicate) obj;
				if (field == null)
				{
					if (other.field != null)
						return false;
				} else if (!field.equals(other.field))
					return false;
				if (predicate == null)
				{
					if (other.predicate != null)
						return false;
				} else if (!predicate.equals(other.predicate))
					return false;
				return true;
			}
			
			@Override
			protected LeafNodePredicate<N,T> clone()
			{
				return new LeafNodePredicate(this.field, this.predicate);
			}
		}
		
	}
	
}
