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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.sodeac.common.function.ConplierBean;

/**
 * A typed tree meta model defines all root nodes (trees) of this model.
 * 
 * @author Sebastian Palarus
 *
 * @param <T>
 */
public class TypedTreeMetaModel<T extends TypedTreeMetaModel> extends BranchNodeMetaModel
{
	/**
	 * get cached model instanced by <code>modelClass</code> 
	 * 
	 * @param modelClass class of model instance
	 * @return  model instance
	 */
	public static <M extends TypedTreeMetaModel> M getInstance(Class<M> modelClass)
	{
		return ModelingProcessor.DEFAULT_INSTANCE.getCachedTypedTreeMetaModel(modelClass);
	}
	
	/**
	 * Create new root node instance provided by model
	 * 
	 * @param type type of root node
	 * @return new root node instance
	 */
	public <F extends BranchNodeMetaModel> RootBranchNode<T,F> createRootNode(BranchNodeType<T,F> type)
	{
		return new RootBranchNode(type.getTypeClass());
	}
	
	/**
	 * A root node represents a typed tree
	 * 
	 * @author Sebastian Palarus
	 *
	 * @param <P> type of parent model
	 * @param <R> type of root node
	 */
	public static class RootBranchNode<P extends TypedTreeMetaModel,R  extends BranchNodeMetaModel> extends BranchNode<P,R> implements ITree<P,R>
	{
		private boolean nodeSynchronized;
		private boolean immutable;
		private boolean branchNodeGetterAutoCreate;
		private boolean branchNodeConsumeAutoCreate;
		
		private CopyOnWriteArrayList<IModifyListener> modifyListeners;
		private ReadLock readLock;
		private WriteLock writeLock;
		private ConplierBean<Boolean> sharedDoit;
		private volatile long sequnceOID = 0L;
		
		/**
		 * Constructor of root node.
		 * 
		 * @param modelType type of root node
		 */
		protected RootBranchNode(Class<R> modelType)
		{
			super(null,null,modelType);
			
			this.nodeSynchronized = false;
			this.immutable = false;
			this.branchNodeGetterAutoCreate = false;
			this.branchNodeConsumeAutoCreate = false;
			
			this.modifyListeners = null;
			this.sharedDoit = new ConplierBean<Boolean>(true);
			
			ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
			this.readLock = lock.readLock();
			this.writeLock = lock.writeLock();
		}
		
		protected ReadLock getReadLock()
		{
			return readLock;
		}
		protected WriteLock getWriteLock()
		{
			return writeLock;
		}

		@Override
		public void dispose()
		{
			if(this.modifyListeners != null)
			{
				this.modifyListeners.clear();
			}
			super.disposeNode();
			this.readLock = null;
			this.writeLock = null;
			this.modifyListeners = null;
			this.sharedDoit = null;
		}
		
		@Override
		public boolean isSynchronized()
		{
			return nodeSynchronized;
		}
		
		@Override
		public RootBranchNode<P,R> setSynchronized(boolean nodeSynchronized)
		{
			this.nodeSynchronized = nodeSynchronized;
			return this;
		}
		
		@Override
		public boolean isImmutable()
		{
			return immutable;
		}
		
		@Override
		public RootBranchNode<P,R> setImmutable()
		{
			this.immutable = true;
			return this;
		}
		
		@Override
		public boolean isBranchNodeGetterAutoCreate()
		{
			return branchNodeGetterAutoCreate;
		}
		
		@Override
		public RootBranchNode<P,R> setBranchNodeGetterAutoCreate(boolean branchNodeGetterAutoCreate)
		{
			this.branchNodeGetterAutoCreate = branchNodeGetterAutoCreate;
			return this;
		}
		
		@Override
		public boolean isBranchNodeApplyToConsumerAutoCreate()
		{
			return branchNodeConsumeAutoCreate;
		}
		
		@Override
		public RootBranchNode<P,R> setBranchNodeApplyToConsumerAutoCreate(boolean branchNodeConsumeAutoCreate)
		{
			this.branchNodeConsumeAutoCreate = branchNodeConsumeAutoCreate;
			return this;
		}
		
		/**
		 * Sequencer for tree's object-ID
		 * 
		 * @return next objectId
		 */
		protected long nextOID()
		{
			return ++this.sequnceOID;
		}

		@Override
		public RootBranchNode<P,R> addModifyListener(IModifyListener modifyListener)
		{
			if(modifyListener == null)
			{
				return this;
			}
			if(this.modifyListeners == null)
			{
				this.modifyListeners = new CopyOnWriteArrayList<IModifyListener>();
			}
			this.modifyListeners.addIfAbsent(modifyListener);
			return this;
		}
		
		@Override
		public RootBranchNode<P,R> addModifyListeners(IModifyListener... modifyListeners)
		{
			if(modifyListeners == null)
			{
				return this;
			}
			if(modifyListeners.length == 0)
			{
				return this;
			}
			if(this.modifyListeners == null)
			{
				this.modifyListeners = new CopyOnWriteArrayList<IModifyListener>();
			}
			boolean hasNullItem = false;
			for(IModifyListener modifyListener : modifyListeners)
			{
				if(modifyListener == null)
				{
					hasNullItem = true;
					break;
				}
			}
			if(hasNullItem)
			{
				List<IModifyListener> modifyListenerList = new ArrayList<IModifyListener>();
				for(IModifyListener modifyListener : modifyListeners)
				{
					if(modifyListener != null)
					{
						modifyListenerList.add(modifyListener);
					}
				}
				if(modifyListenerList.isEmpty())
				{
					return this;
				}
				this.modifyListeners.addAllAbsent(modifyListenerList);
			}
			else
			{
				this.modifyListeners.addAllAbsent(Arrays.asList(modifyListeners));
			}
			return this;
		}
		
		@Override
		public RootBranchNode<P,R> removeModifyListener(IModifyListener modifyListener)
		{
			if(modifyListener == null)
			{
				return this;
			}
			if(this.modifyListeners == null)
			{
				return this;
			}
			
			this.modifyListeners.remove(modifyListener);
			return this;
		}
		
		protected <C extends INodeType<?,?>, T> boolean notifyBeforeModify(BranchNode<?, ?> parentNode, Object staticNodeTypeInstance, Class<C> type, T oldValue, T newValue)
		{
			ConplierBean<Boolean> doit = null;
			
			if((modifyListeners != null) && (! modifyListeners.isEmpty()))
			{
				for(IModifyListener modifyListener : this.modifyListeners)
				{
					if(doit == null)
					{
						doit = this.sharedDoit;
						if(! isSynchronized())
						{
							doit = new ConplierBean<Boolean>(true); // TODO recycle
						}
					}
					modifyListener.beforeModify(parentNode, staticNodeTypeInstance, type, oldValue, newValue, doit);
					
					if((doit.get() != null) && (! doit.get().booleanValue()))
					{
						return false;
					}		
				}
			}
			
			return true;
		}
		
		protected <C extends INodeType<?,?>, T> void notifyAfterModify(BranchNode<?, ?> parentNode, Object staticNodeTypeInstance, Class<C> type, T oldValue, T newValue)
		{
			if((modifyListeners != null) && (! modifyListeners.isEmpty()))
			{
				for(IModifyListener modifyListener : this.modifyListeners)
				{
					modifyListener.afterModify(parentNode, staticNodeTypeInstance, type, oldValue, newValue);		
				}
			}
		}
	}
}
