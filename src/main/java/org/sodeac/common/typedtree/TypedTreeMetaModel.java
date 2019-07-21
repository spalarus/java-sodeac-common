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
		return ModelingProcessor.DEFAULT_INSTANCE.getModelObject(modelClass);
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
	 * 
	 * @author Sebastian Palarus
	 *
	 * @param <P>
	 * @param <R>
	 */
	public static class RootBranchNode<P extends TypedTreeMetaModel,R  extends BranchNodeMetaModel> extends BranchNode<P,R>
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
		
		/**
		 * getter for synchronized option 
		 * 
		 * @return true, if access to tree is synchronized, otherwise false
		 */
		public boolean isSynchronized()
		{
			return nodeSynchronized;
		}
		
		/**
		 * setter for synchronized option that determines whether access to tree is synchronized or not
		 * 
		 * @param nodeSynchronized synchronized option to set
		 * @return root node
		 */
		public RootBranchNode<P,R> setSynchronized(boolean nodeSynchronized)
		{
			this.nodeSynchronized = nodeSynchronized;
			return this;
		}
		public boolean isImmutable()
		{
			return immutable;
		}
		public RootBranchNode<P,R> setImmutable()
		{
			this.immutable = true;
			return this;
		}
		public boolean isBranchNodeGetterAutoCreate()
		{
			return branchNodeGetterAutoCreate;
		}
		public RootBranchNode<P,R> setBranchNodeGetterAutoCreate(boolean branchNodeGetterAutoCreate)
		{
			this.branchNodeGetterAutoCreate = branchNodeGetterAutoCreate;
			return this;
		}
		
		public boolean isBranchNodeComputeAutoCreate()
		{
			return branchNodeConsumeAutoCreate;
		}
		public RootBranchNode<P,R> setBranchNodeConsumeAutoCreate(boolean branchNodeConsumeAutoCreate)
		{
			this.branchNodeConsumeAutoCreate = branchNodeConsumeAutoCreate;
			return this;
		}
		
		protected long nextOID()
		{
			return ++this.sequnceOID;
		}

		public void addModifyListener(IModifyListener modifyListener)
		{
			if(modifyListener == null)
			{
				return;
			}
			if(this.modifyListeners == null)
			{
				this.modifyListeners = new CopyOnWriteArrayList<IModifyListener>();
			}
			this.modifyListeners.addIfAbsent(modifyListener);
		}
		public void addModifyListeners(IModifyListener... modifyListeners)
		{
			if(modifyListeners == null)
			{
				return;
			}
			if(modifyListeners.length == 0)
			{
				return;
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
					return;
				}
				this.modifyListeners.addAllAbsent(modifyListenerList);
			}
			else
			{
				this.modifyListeners.addAllAbsent(Arrays.asList(modifyListeners));
			}
		}
		public void removeModifyListener(IModifyListener modifyListener)
		{
			if(modifyListener == null)
			{
				return;
			}
			if(this.modifyListeners == null)
			{
				return;
			}
			
			this.modifyListeners.remove(modifyListener);
		}
		
		protected <C extends INodeType<?,?>, T> boolean publishModify(BranchNode<?, ?> parentNode, String nodeTypeName, Object staticNodeTypeInstance, Class<C> type, T oldValue, T newValue)
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
					modifyListener.onModify(parentNode, nodeTypeName, staticNodeTypeInstance, type, oldValue, newValue, doit);
					
					if((doit.get() != null) && (! doit.get().booleanValue()))
					{
						return false;
					}		
				}
			}
			
			return doit == null ? true : ( doit.get() == null ? true : doit.get().booleanValue());
		}
	}
}
