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
	}
	
	private Lock readLock = null;
	private Lock writeLock = null;
	private Map<Object,BranchNodeMetaModel> metaModelIndex = null;
	private Map<BranchNodeMetaModel,PreparedMetaModel> compiledEntityMetaIndex = null;
	
	public <T> BranchNodeMetaModel getModel(Class<T> modelClass) throws InstantiationException, IllegalAccessException
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
	public <T extends BranchNodeMetaModel> PreparedMetaModel getPreparedMetaModel(T model) throws IllegalArgumentException, IllegalAccessException
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
							prepatedNodeType.nodeTypeName = field.getName();
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
							prepatedNodeType.staticNodeTypeInstance = field.get(model);
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
				preparedMetaModel.nodeTypeNames[i] = compiledEntityFieldMeta.getNodeTypeName();
				nodeTypeIndexByName.put(compiledEntityFieldMeta.getNodeTypeName(), i);
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
		private String nodeTypeName = null;
		private NodeType nodeType = null;
		private Object staticNodeTypeInstance = null; // static field in metamodel
		
		protected Class<?> getNodeTypeClass()
		{
			return clazz;
		}
		protected String getNodeTypeName()
		{
			return nodeTypeName;
		}
		protected NodeType getNodeType()
		{
			return nodeType;
		}
		public Object getStaticNodeTypeInstance()
		{
			return staticNodeTypeInstance;
		}
	}
	
}
