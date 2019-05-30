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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.sodeac.common.modeling.ModelingProcessor.CompiledEntityFieldMeta;
import org.sodeac.common.modeling.ModelingProcessor.CompiledEntityMeta;

public class Entity<T extends ComplexType>
{
	
	public static <T extends ComplexType> Entity<T> newInstance(Class<T> type)
	{
		return new Entity<T>(type);
	}
	
	private CompiledEntityMeta entityMeta = null;
	private List<EntityField> fieldList = null;
	
	private Entity(Class<T> modelType)
	{
		try
		{
			ComplexType model = ModelingProcessor.DEFAULT_INSTANCE.getModel(modelType);
			this.entityMeta = ModelingProcessor.DEFAULT_INSTANCE.getModelCache(model);
			
			
			this.fieldList = new ArrayList<>();
			for(int i = 0; i < this.entityMeta.getFieldNames().length; i++)
			{
				CompiledEntityFieldMeta compiledEntityFieldMeta = entityMeta.getFieldList().get(i);
				EntityField<?> field = null;
				// TODO
				if(compiledEntityFieldMeta.getRelationType() == CompiledEntityFieldMeta.RelationType.SingularSimple)
				{
					field = new BasicObject<>();
				}
				else if(compiledEntityFieldMeta.getRelationType() == CompiledEntityFieldMeta.RelationType.SingularComplex)
				{
					field = new BasicObject<>();
				}
				else if (compiledEntityFieldMeta.getRelationType() == CompiledEntityFieldMeta.RelationType.MultipleSimple)
				{
					field = new BasicList<>();
				}
				else if (compiledEntityFieldMeta.getRelationType() == CompiledEntityFieldMeta.RelationType.MultipleComplex)
				{
					field = new BasicList<>();
				}
				field.setFieldSpec(compiledEntityFieldMeta);
				fieldList.add(field);
			}
			this.fieldList = Collections.unmodifiableList(this.fieldList);
			
		}
		catch (Exception e) 
		{
			if(e instanceof RuntimeException)
			{
				throw (RuntimeException)e;
			}
			throw new RuntimeException(e);
		}
	}
	public ComplexType getModel()
	{
		return (ComplexType)this.entityMeta.getModel();
	}
	
	public <X> BasicObject<X> get(SingularBasicField<T,X> field)
	{
		return (BasicObject<X>) this.fieldList.get(this.entityMeta.getFieldIndexByClass().get(field));
	}
	public <X extends ComplexType> BasicObject<X> get(SingularComplexField<T,X> field)
	{
		return (BasicObject<X>) this.fieldList.get(this.entityMeta.getFieldIndexByClass().get(field));
	}
	
	public <X> BasicList<X> get(MultipleBasicField<T,X> field)
	{
		return (BasicList<X>) this.fieldList.get(this.entityMeta.getFieldIndexByClass().get(field));
	}
	
	public <X extends ComplexType> BasicList<X> get(MultipleComplexField<T,X> field)
	{
		return (BasicList<X>) this.fieldList.get(this.entityMeta.getFieldIndexByClass().get(field));
	}
	
	public <X> BasicObject<X> getSingleValue(ModelPath<T,X> path)
	{
		return null;
	}
	
	// getPathValue(). <= returns PathBuilder
	// => ein Path benötigt 2 Typen => den Einstiegspunkt (Model und das Ziel)
}
