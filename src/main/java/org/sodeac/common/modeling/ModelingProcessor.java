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
package org.sodeac.common.modeling;

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
		this.complexTypeIndex = new HashMap<Object,ComplexType>();
		this.compiledEntityMetaIndex = new HashMap<ComplexType,CompiledEntityMeta>();
	}
	
	private Lock readLock = null;
	private Lock writeLock = null;
	private Map<Object,ComplexType> complexTypeIndex = null;
	private Map<ComplexType,CompiledEntityMeta> compiledEntityMetaIndex = null;
	
	@SuppressWarnings("unchecked")
	public <T> ComplexType getModel(Class<T> type) throws InstantiationException, IllegalAccessException
	{
		this.readLock.lock();
		try
		{
			ComplexType model = (ComplexType)this.complexTypeIndex.get(type);
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
			ComplexType model = (ComplexType)this.complexTypeIndex.get(type);
			if(model != null)
			{
				return model;
			}
			model = (ComplexType)type.newInstance();
			this.complexTypeIndex.put(type, model);
			return model;
		}
		finally 
		{
			this.writeLock.unlock();
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends ComplexType> CompiledEntityMeta getModelCache(T complexType) throws IllegalArgumentException, IllegalAccessException
	{
		this.readLock.lock();
		try
		{
			CompiledEntityMeta compiledEntityMeta = this.compiledEntityMetaIndex.get(complexType);
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
			CompiledEntityMeta compiledEntityMeta = this.compiledEntityMetaIndex.get(complexType);
			if(compiledEntityMeta != null)
			{
				return compiledEntityMeta;
			}
			
			Class<T> modelType = (Class<T>)complexType.getClass();
			
			List<CompiledEntityFieldMeta> list = new ArrayList<CompiledEntityFieldMeta>();
			for(Field field : modelType.getDeclaredFields())
			{
				boolean isField = false;
				for(Class<?> clazz : field.getType().getInterfaces())
				{
					if(clazz == IField.class)
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
						if(pType.getActualTypeArguments()[0] == modelType)
						{
							Type type2 = pType.getActualTypeArguments()[1];
							CompiledEntityFieldMeta compiledEntityFieldMeta = new CompiledEntityFieldMeta();
							compiledEntityFieldMeta.clazz = ((Class<?>)type2);
							compiledEntityFieldMeta.fieldName = field.getName();
							if(pType.getRawType() == SingularBasicField.class)
							{
								compiledEntityFieldMeta.relationType = CompiledEntityFieldMeta.RelationType.SingularSimple;
							}
							else if(pType.getRawType() == MultipleBasicField.class)
							{
								compiledEntityFieldMeta.relationType = CompiledEntityFieldMeta.RelationType.MultipleSimple;
							}
							else if(pType.getRawType() == SingularComplexField.class)
							{
								compiledEntityFieldMeta.relationType = CompiledEntityFieldMeta.RelationType.SingularComplex;
							}
							else if(pType.getRawType() == MultipleComplexField.class)
							{
								compiledEntityFieldMeta.relationType = CompiledEntityFieldMeta.RelationType.MultipleComplex;
							}
							compiledEntityFieldMeta.staticFieldInstance = field.get(complexType);
							list.add(compiledEntityFieldMeta);
						}
					}
				}
			}
			
			compiledEntityMeta = new CompiledEntityMeta();
			compiledEntityMeta.fieldList = Collections.unmodifiableList(list);
			compiledEntityMeta.fieldNames = new String[compiledEntityMeta.fieldList.size()];
			Map<String,Integer> fieldIndexByName = new HashMap<String,Integer>();
			Map<Object,Integer> fieldIndexByClass = new HashMap<Object,Integer>();
			for(int i = 0; i < compiledEntityMeta.fieldList.size(); i++)
			{
				CompiledEntityFieldMeta compiledEntityFieldMeta = compiledEntityMeta.fieldList.get(i);
				compiledEntityMeta.fieldNames[i] = compiledEntityFieldMeta.getFieldName();
				fieldIndexByName.put(compiledEntityFieldMeta.getFieldName(), i);
				fieldIndexByClass.put(compiledEntityFieldMeta.staticFieldInstance, i);
			}
			compiledEntityMeta.fieldIndexByName = Collections.unmodifiableMap(fieldIndexByName);
			compiledEntityMeta.fieldIndexByClass = Collections.unmodifiableMap(fieldIndexByClass);
			
			this.compiledEntityMetaIndex.put(complexType,compiledEntityMeta);
			return compiledEntityMeta;
		}
		finally 
		{
			this.writeLock.unlock();
		}
	}
	
	protected static class CompiledEntityMeta
	{
		private ComplexType model = null;
		private String[] fieldNames = null;
		private List<CompiledEntityFieldMeta> fieldList = null;
		private Map<String, Integer> fieldIndexByName = null;
		private Map<Object, Integer> fieldIndexByClass = null;

		protected List<CompiledEntityFieldMeta> getFieldList()
		{
			return fieldList;
		}
		protected String[] getFieldNames()
		{
			return fieldNames;
		}
		protected Map<String, Integer> getFieldIndexByName()
		{
			return fieldIndexByName;
		}
		protected Map<Object, Integer> getFieldIndexByClass()
		{
			return fieldIndexByClass;
		}
		protected ComplexType getModel()
		{
			return model;
		}
	}
	
	protected static class CompiledEntityFieldMeta
	{
		protected enum RelationType {SingularSimple,MultipleSimple,SingularComplex,MultipleComplex};
		
		private Class<?> clazz = null;
		private String fieldName = null;
		private RelationType relationType = null;
		private Object staticFieldInstance = null;
		
		protected Class<?> getClazz()
		{
			return clazz;
		}
		protected String getFieldName()
		{
			return fieldName;
		}
		protected RelationType getRelationType()
		{
			return relationType;
		}
		public Object getStaticFieldInstance()
		{
			return staticFieldInstance;
		}
	}
	
}
