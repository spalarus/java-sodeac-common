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

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
		this.compiledEntityMetaIndex = new HashMap<BranchNodeMetaModel,PreparedMetaModel>();
		this.typedTreeModelIndex = new HashMap<Class<?>,TypedTreeMetaModel>();
		this.branchNodeModelIndex = new HashMap<Class<?>,BranchNodeMetaModel>();
	}
	
	private Lock readLock = null;
	private Lock writeLock = null;
	private Map<Object,BranchNodeMetaModel> metaModelIndex = null;
	private Map<BranchNodeMetaModel,PreparedMetaModel> compiledEntityMetaIndex = null;
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
	
	@SuppressWarnings("unchecked")
	protected <T extends BranchNodeMetaModel> PreparedMetaModel getPreparedMetaModel(T model) throws IllegalArgumentException, IllegalAccessException
	{
		this.readLock.lock();
		try
		{
			PreparedMetaModel compiledEntityMeta = this.compiledEntityMetaIndex.get(model);
			if(compiledEntityMeta != null)
			{
				return compiledEntityMeta;
			}
		}
		finally 
		{
			this.readLock.unlock();
		}
		
		this.writeLock.lock();
		try
		{
			PreparedMetaModel preparedMetaModel = this.compiledEntityMetaIndex.get(model);
			if(preparedMetaModel != null)
			{
				return preparedMetaModel;
			}
			
			Class<T> modelClass = (Class<T>)model.getClass();
			
			List<PreparedNodeType> list = new ArrayList<PreparedNodeType>();
			for(Field field : modelClass.getDeclaredFields())
			{
				boolean isField = false;
				for(Class<?> clazz : field.getType().getInterfaces())
				{
					if(clazz == INodeType.class)
					{
						isField = true;
						break;
					}
				}
				if(! isField)
				{
					continue;
				}
				Type type = field.getGenericType();
				
				if(type instanceof ParameterizedType)
				{
					ParameterizedType pType = (ParameterizedType)type;
					if((pType.getActualTypeArguments() != null) && (pType.getActualTypeArguments().length == 2))
					{
						if(pType.getActualTypeArguments()[0] == modelClass)
						{
							Type type2 = pType.getActualTypeArguments()[1];
							PreparedNodeType prepatedNodeType = new PreparedNodeType();
							prepatedNodeType.clazz = ((Class<?>)type2);
							if(pType.getRawType() == LeafNodeType.class)
							{
								prepatedNodeType.nodeType = PreparedNodeType.NodeType.LeafNode;
							}
							else if(pType.getRawType() == BranchNodeType.class)
							{
								prepatedNodeType.nodeType = PreparedNodeType.NodeType.BranchNode;
							}
							else if(pType.getRawType() == BranchNodeListType.class)
							{
								prepatedNodeType.nodeType = PreparedNodeType.NodeType.BranchNodeList;
							}
							prepatedNodeType.staticNodeTypeInstance = (INodeType)field.get(model);
							list.add(prepatedNodeType);
						}
					}
				}
			}
			
			preparedMetaModel = new PreparedMetaModel();
			preparedMetaModel.model = model;
			preparedMetaModel.preparedNodeTypeList = Collections.unmodifiableList(list);
			preparedMetaModel.nodeTypeNames = new String[preparedMetaModel.preparedNodeTypeList.size()];
			Map<String,Integer> nodeTypeIndexByName = new HashMap<String,Integer>();
			Map<Object,Integer> nodeTypeIndexByClass = new HashMap<Object,Integer>();
			for(int i = 0; i < preparedMetaModel.preparedNodeTypeList.size(); i++)
			{
				PreparedNodeType compiledEntityFieldMeta = preparedMetaModel.preparedNodeTypeList.get(i);
				preparedMetaModel.nodeTypeNames[i] = compiledEntityFieldMeta.staticNodeTypeInstance.getNodeName();
				nodeTypeIndexByName.put(compiledEntityFieldMeta.staticNodeTypeInstance.getNodeName(), i);
				nodeTypeIndexByClass.put(compiledEntityFieldMeta.staticNodeTypeInstance, i);
			}
			preparedMetaModel.nodeTypeIndexByName = Collections.unmodifiableMap(nodeTypeIndexByName);
			preparedMetaModel.nodeTypeIndexByClass = Collections.unmodifiableMap(nodeTypeIndexByClass);
			
			this.compiledEntityMetaIndex.put(model,preparedMetaModel);
			return preparedMetaModel;
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
	
	protected static class PreparedMetaModel
	{
		private BranchNodeMetaModel model = null;
		private String[] nodeTypeNames = null;
		private List<PreparedNodeType> preparedNodeTypeList = null;
		private Map<String, Integer> nodeTypeIndexByName = null;
		private Map<Object, Integer> nodeTypeIndexByClass = null;

		protected List<PreparedNodeType> getPreparedNodeTypeList()
		{
			return preparedNodeTypeList;
		}
		protected String[] getNodeTypeNames()
		{
			return nodeTypeNames;
		}
		protected Map<String, Integer> getNodeTypeIndexByName()
		{
			return nodeTypeIndexByName;
		}
		protected Map<Object, Integer> getNodeTypeIndexByClass()
		{
			return nodeTypeIndexByClass;
		}
		protected BranchNodeMetaModel getModel()
		{
			return model;
		}
	}
	
	protected static class PreparedNodeType
	{
		protected enum NodeType {LeafNode,BranchNode,BranchNodeList};
		
		private Class<?> clazz = null;
		private NodeType nodeType = null;
		private INodeType staticNodeTypeInstance = null; // static field in metamodel
		
		protected Class<?> getNodeTypeClass()
		{
			return clazz;
		}
		protected NodeType getNodeType()
		{
			return nodeType;
		}
		public INodeType getStaticNodeTypeInstance()
		{
			return staticNodeTypeInstance;
		}
	}
	
}
