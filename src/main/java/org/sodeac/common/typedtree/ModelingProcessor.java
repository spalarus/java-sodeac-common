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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Intern helper class
 * 
 * @author Sebastian Palarus
 *
 */
public class ModelingProcessor
{
	protected static final ModelingProcessor DEFAULT_INSTANCE = new ModelingProcessor();
	
	protected ModelingProcessor()
	{
		ReadWriteLock lock = new ReentrantReadWriteLock(true);
		this.readLock = lock.readLock();
		this.writeLock = lock.writeLock();
		this.metaModelIndex = new HashMap<Object,BranchNodeMetaModel>();
		this.typedTreeModelIndex = new HashMap<Class<?>,TypedTreeMetaModel>();
		this.branchNodeModelIndex = new HashMap<Class<?>,BranchNodeMetaModel>();
	}
	
	private Lock readLock = null;
	private Lock writeLock = null;
	private Map<Object,BranchNodeMetaModel> metaModelIndex = null;
	private Map<Class<?>,TypedTreeMetaModel> typedTreeModelIndex = null;
	private Map<Class<?>,BranchNodeMetaModel> branchNodeModelIndex = null;
	
	protected <T> BranchNodeMetaModel getModel(Class<T> modelClass) throws InstantiationException, IllegalAccessException
	{
		this.readLock.lock();
		try
		{
			BranchNodeMetaModel model = (BranchNodeMetaModel)this.metaModelIndex.get(modelClass);
			if(model != null)
			{
				return model;
			}
		}
		finally 
		{
			this.readLock.unlock();
		}
		
		this.writeLock.lock();
		try
		{
			BranchNodeMetaModel model = (BranchNodeMetaModel)this.metaModelIndex.get(modelClass);
			if(model != null)
			{
				return model;
			}
			model = (BranchNodeMetaModel)modelClass.newInstance();
			this.metaModelIndex.put(modelClass, model);
			return model;
		}
		finally 
		{
			this.writeLock.unlock();
		}
	}
		
	protected <M extends TypedTreeMetaModel> M getCachedTypedTreeMetaModel(Class<M> clazz)
	{
		this.readLock.lock();
		try
		{
			M modelObject = (M)this.typedTreeModelIndex.get(clazz);
			if(modelObject != null)
			{
				return modelObject;
			}
		}
		finally 
		{
			this.readLock.unlock();
		}
		
		this.writeLock.lock();
		try
		{
			M modelObject = (M)this.typedTreeModelIndex.get(clazz);
			if(modelObject != null)
			{
				return modelObject;
			}
			
			modelObject = (M) clazz.newInstance();
			this.typedTreeModelIndex.put(clazz, modelObject);
			
			return modelObject;
		}
		catch (RuntimeException e) 
		{
			throw e;
		}
		catch (Exception e) 
		{
			throw new RuntimeException(e);
		}
		finally 
		{
			this.writeLock.unlock();
		}
	}
	
	protected <M extends BranchNodeMetaModel> M getCachedBranchNodeMetaModel(Class<M> clazz)
	{
		this.readLock.lock();
		try
		{
			M modelObject = (M)this.branchNodeModelIndex.get(clazz);
			if(modelObject != null)
			{
				return modelObject;
			}
		}
		finally 
		{
			this.readLock.unlock();
		}
		
		this.writeLock.lock();
		try
		{
			M modelObject = (M)this.branchNodeModelIndex.get(clazz);
			if(modelObject != null)
			{
				return modelObject;
			}
			
			modelObject = (M) clazz.newInstance();
			this.branchNodeModelIndex.put(clazz, modelObject);
			
			return modelObject;
		}
		catch (RuntimeException e) 
		{
			throw e;
		}
		catch (Exception e) 
		{
			throw new RuntimeException(e);
		}
		finally 
		{
			this.writeLock.unlock();
		}
	}
}
