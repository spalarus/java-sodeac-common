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

public class Entity<T extends ComplexType<T>>
{
	
	public static <T extends ComplexType<T>> Entity<T> newInstance(Class<T> type)
	{
		return new Entity<T>(type);
	}
	
	private CompiledEntityMeta entityMeta = null;
	private List<EntityField> fieldList = null;
	
	private Entity(Class<T> modelType)
	{
		try
		{
			ComplexType<T> model = ModelingProcessor.DEFAULT_INSTANCE.getModel(modelType);
			this.entityMeta = ModelingProcessor.DEFAULT_INSTANCE.getModelCache(model);
			
			
			this.fieldList = new ArrayList<>();
			for(int i = 0; i < this.entityMeta.getFieldNames().length; i++)
			{
				CompiledEntityFieldMeta compiledEntityFieldMeta = entityMeta.getFieldList().get(i);
				EntityField<?> field = null;
				if(compiledEntityFieldMeta.getRelationType() == CompiledEntityFieldMeta.RelationType.Singular)
				{
					field = new SingularEntityField<>();
				}
				else if (compiledEntityFieldMeta.getRelationType() == CompiledEntityFieldMeta.RelationType.Singular)
				{
					field = new MultipleEntityField<>();
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
	public ComplexType<T> getModel()
	{
		return (ComplexType<T>)this.entityMeta.getModel();
	}
	
	public <X extends IType<?>> SingularEntityField<X> get(SingularField<T,X> field)
	{
		return (SingularEntityField<X>) this.fieldList.get(this.entityMeta.getFieldIndexByClass().get(field));
	}
	
	public <X extends IType<?>> MultipleEntityField<X> get(MultipleField<T,X> field)
	{
		return (MultipleEntityField<X>) this.fieldList.get(this.entityMeta.getFieldIndexByClass().get(field));
	}
	
	public <X extends IType<?>> SingularEntityField<X> getSingleValue(ModelPath<T,X> path)
	{
		return null;
	}
	
	// getPathValue(). <= returns PathBuilder
	// => ein Path benötigt 2 Typen => den Einstiegspunkt (Model und das Ziel)
}
