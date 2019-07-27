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
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class BranchNodeMetaModel
{
	protected BranchNodeMetaModel()
	{
		super();
		
		Class<?> modelClass = getClass();
		
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
						
						if(Modifier.isStatic(fieldModifier) && (! Modifier.isFinal(fieldModifier)))
						{
							try
							{
								if(field.get(null) == null)
								{
									Object nodeType = fieldClass.getConstructor(Class.class,Class.class).newInstance(modelClass,type2);
									field.set(null, nodeType);
								}
							}
							catch (Exception e) 
							{
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}
}
