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

public class TypedTreeMetaModel<T extends BranchNodeMetaModel> extends BranchNodeMetaModel
{
	public <F extends BranchNodeMetaModel> RootBranchNode<T,F> createRootNode(BranchNodeType<T,F> node)
	{
		return new RootBranchNode(node.getTypeClass());
	}
	
	public static class RootBranchNode<P extends BranchNodeMetaModel,R  extends BranchNodeMetaModel> extends BranchNode<P,R>
	{
		private boolean sychronized;
		private boolean immutable;
		private boolean branchNodeGetterAutoCreate;
		private boolean branchNodeComputeAutoCreate;
		
		private CopyOnWriteArrayList<IModifyListener> modifyListeners;
		private ReadLock readLock;
		private WriteLock writeLock;
		private ConplierBean<Boolean> sharedDoit;
		private volatile long sequnceOID = 0L;
		
		protected RootBranchNode(Class<R> modelType)
		{
			super(null,null,modelType);
			
			this.sychronized = false;
			this.immutable = false;
			this.branchNodeGetterAutoCreate = false;
			this.branchNodeComputeAutoCreate = false;
			
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
		public boolean isSychronized()
		{
			return sychronized;
		}
		public RootBranchNode<P,R> setSychronized(boolean sychronized)
		{
			this.sychronized = sychronized;
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
			return branchNodeComputeAutoCreate;
		}
		public RootBranchNode<P,R> setBranchNodeComputeAutoCreate(boolean branchNodeComputeAutoCreate)
		{
			this.branchNodeComputeAutoCreate = branchNodeComputeAutoCreate;
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
						if(! isSychronized())
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
