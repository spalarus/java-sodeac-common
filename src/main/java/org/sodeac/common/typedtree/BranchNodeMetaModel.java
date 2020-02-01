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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.sodeac.common.typedtree.TypedTreeMetaModel.RootBranchNode;
import org.sodeac.common.typedtree.annotation.TypedTreeModel;

/**
 * A branch node meta model defines type and cardinality of child nodes.
 * 
 * @author Sebastian Palarus
 *
 */
public class BranchNodeMetaModel
{
	protected BranchNodeMetaModel()
	{
		super();
		
		// create static instances of types
		
		List<INodeType> list = new ArrayList<INodeType>();
		
		LinkedList<Class> classList = new LinkedList<>();
		classList.add(getClass());
		
		Class<?> currentSuperClass = getClass().getSuperclass();
		
		while((currentSuperClass != null) && (currentSuperClass != Object.class))
		{
			classList.addFirst(currentSuperClass);
			currentSuperClass = currentSuperClass.getSuperclass();
		}
		
		List<Field> fieldList = new ArrayList<>();
		
		for(Class modelClass : classList)
		{
			for(Field field : modelClass.getDeclaredFields())
			{
				int pos = -1;
				int idx = 0;
				for(Field existingField : fieldList)
				{
					if(existingField.getName().equals(field.getName()))
					{
						pos = idx;
						break;
					}
					idx++;
				}
				if(pos > -1)
				{
					fieldList.set(pos,field);
				}
				else
				{
					fieldList.add(field);
				}
			}
		}
		for(Field field : fieldList)
		{
			Class<?> modelClass = field.getDeclaringClass();
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
			Class<?> fieldClass = field.getType();
			int fieldModifier = field.getModifiers();
			
			if((type instanceof ParameterizedType) && ((fieldClass == LeafNodeType.class) || (fieldClass == BranchNodeType.class) || (fieldClass == BranchNodeListType.class) ))
			{
				ParameterizedType pType = (ParameterizedType)type;
				if((pType.getActualTypeArguments() != null) && (pType.getActualTypeArguments().length == 2))
				{
					if(pType.getActualTypeArguments()[0] == modelClass)
					{
						
						Type type2 = pType.getActualTypeArguments()[1];
						
						try
						{
							if(Modifier.isStatic(fieldModifier) && (! Modifier.isFinal(fieldModifier)))
							{
								if(field.get(null) == null)
								{
									if(type2 instanceof ParameterizedType)
									{
										ParameterizedType pType2 = (ParameterizedType)type2;
										type2 = pType2.getRawType();
									}
									Object nodeType = fieldClass.getConstructor(Class.class,Class.class, Field.class).newInstance(modelClass,type2,field);
									field.set(null, nodeType);
								}
							}
							
							list.add((INodeType)field.get(this));
						}
						catch (Exception e) 
						{
							throw new RuntimeException(e);
						}
					}
				}
			}
		}
		
		fieldList.clear();
		
		// create helper stuff
		
		this.leafNodeTypeList = new ArrayList<LeafNodeType>();
		this.branchNodeTypeList = new ArrayList<BranchNodeType>();
		this.branchNodeListTypeList = new ArrayList<BranchNodeListType>();
		this.nodeTypeList = Collections.unmodifiableList(list);
		this.nodeTypeNames = new String[this.nodeTypeList.size()];
		Map<String,Integer> nodeTypeIndexByName = new HashMap<String,Integer>();
		Map<Object,Integer> nodeTypeIndexByClass = new HashMap<Object,Integer>();
		for(int i = 0; i < this.nodeTypeList.size(); i++)
		{
			INodeType staticNodeTypeInstance = this.nodeTypeList.get(i);
			this.nodeTypeNames[i] = staticNodeTypeInstance.getNodeName();
			nodeTypeIndexByName.put(staticNodeTypeInstance.getNodeName(), i);
			nodeTypeIndexByClass.put(staticNodeTypeInstance, i);
			
			if(staticNodeTypeInstance instanceof LeafNodeType)
			{
				this.leafNodeTypeList.add((LeafNodeType)staticNodeTypeInstance);
			}
			if(staticNodeTypeInstance instanceof BranchNodeType)
			{
				this.branchNodeTypeList.add((BranchNodeType)staticNodeTypeInstance);
			}
			if(staticNodeTypeInstance instanceof BranchNodeListType)
			{
				this.branchNodeListTypeList.add((BranchNodeListType)staticNodeTypeInstance);
			}
		}
		this.leafNodeTypeList = Collections.unmodifiableList(this.leafNodeTypeList);
		this.branchNodeTypeList = Collections.unmodifiableList(this.branchNodeTypeList);
		this.branchNodeListTypeList = Collections.unmodifiableList(this.branchNodeListTypeList);
		this.nodeTypeIndexByName = Collections.unmodifiableMap(nodeTypeIndexByName);
		this.nodeTypeIndexByClass = Collections.unmodifiableMap(nodeTypeIndexByClass);
	}
	
	protected volatile BranchNodeType<? extends BranchNodeMetaModel,? extends BranchNodeMetaModel> anonymous;
	
	private String[] nodeTypeNames = null;
	private List<INodeType> nodeTypeList = null;
	private List<LeafNodeType> leafNodeTypeList = null;
	private List<BranchNodeType> branchNodeTypeList = null;
	private List<BranchNodeListType> branchNodeListTypeList = null;
	private Map<String, Integer> nodeTypeIndexByName = null;
	private Map<Object, Integer> nodeTypeIndexByClass = null;
	
	protected String[] getNodeTypeNames()
	{
		return nodeTypeNames;
	}
	public List<INodeType> getNodeTypeList()
	{
		return nodeTypeList;
	}
	public List<LeafNodeType> getLeafNodeTypeList()
	{
		return leafNodeTypeList;
	}
	public List<BranchNodeType> getBranchNodeTypeList()
	{
		return branchNodeTypeList;
	}
	public List<BranchNodeListType> getBranchNodeListTypeList()
	{
		return branchNodeListTypeList;
	}
	public Map<String, Integer> getNodeTypeIndexByName()
	{
		return nodeTypeIndexByName;
	}
	public Map<Object, Integer> getNodeTypeIndexByClass()
	{
		return nodeTypeIndexByClass;
	}
	
	
}
