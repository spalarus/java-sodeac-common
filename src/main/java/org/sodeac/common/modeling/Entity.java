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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Entity<T extends ComplexType<T>>
{
	private ComplexType<T> model = null;
	private String[] fieldNames = null;
	private Map<String,EntityField<?>> fieldIndex = null;
	public Entity(Class<T> model)
	{
		System.out.println("" + model.getCanonicalName());
		try
		{
			this.model = model.newInstance(); // TODO Cache
			
			List<EntityField> list = new ArrayList<EntityField>();
			for(Field field : model.getDeclaredFields())
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
						if(pType.getActualTypeArguments()[0] == model)
						{
							Type rawType = pType.getRawType();
							Type type2 = pType.getActualTypeArguments()[1]; 
							System.out.println("\t\t\ttype2: " + rawType  + " -- " + type2);
							EntityField<?> entitiyField = new EntityField<>();
							entitiyField.setClazz(Class.forName(type2.getTypeName()));
							entitiyField.setFieldName(field.getName());
							list.add(entitiyField);
						}
					}
				}
				
			}
			
			fieldIndex = new HashMap<>();
			this.fieldNames = new String[list.size()];
			for(int i = 0; i < fieldNames.length; i++)
			{
				fieldNames[i] = list.get(i).getFieldName();
				fieldIndex.put(fieldNames[i], list.get(i));
			}
			
		}
		catch (Exception e) 
		{
			if(e instanceof RuntimeException)
			{
				throw (RuntimeException)e;
			}
			throw new RuntimeException(e);
		}
		// mit Registry-Cache: Reflection des Modells
		// erstellen eines Arrays Mit Feldern
		// Felder können über Index addressiert werden
		// Eigentlich kann auch alles in dem ComplexType hinterlegt werden
		
		// Wichtig von Anfang an: GC
	}
	public ComplexType<T> getModel()
	{
		return model;
	}
	
	public <X> X getSingleValue(ModelPath<T,X> path)
	{
		return null;
	}
	
	// getPathValue(). <= returns PathBuilder
	// => ein Path benötigt 2 Typen => den Einstiegspunkt (Model und das Ziel)
}
