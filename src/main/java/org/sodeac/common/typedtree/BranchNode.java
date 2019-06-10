package org.sodeac.common.typedtree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sodeac.common.typedtree.ModelingProcessor.CompiledEntityFieldMeta;
import org.sodeac.common.typedtree.ModelingProcessor.CompiledEntityMeta;

public class BranchNode<P extends BranchNodeType, T extends BranchNodeType> extends EntityField<P,T>
{
	private CompiledEntityMeta entityMeta = null;
	private List<AttributeContainer<T,?>> fieldList = null;
	
	protected BranchNode(Class<T> modelType)
	{
		try
		{
			BranchNodeType model = ModelingProcessor.DEFAULT_INSTANCE.getModel(modelType);
			this.entityMeta = ModelingProcessor.DEFAULT_INSTANCE.getModelCache(model);
			
			
			this.fieldList = new ArrayList<>();
			for(int i = 0; i < this.entityMeta.getFieldNames().length; i++)
			{
				CompiledEntityFieldMeta compiledEntityFieldMeta = entityMeta.getFieldList().get(i);
				EntityField<?,?> field = null;
				// TODO
				if(compiledEntityFieldMeta.getRelationType() == CompiledEntityFieldMeta.RelationType.SingularSimple)
				{
					field = new LeafNode<>();
				}
				else if(compiledEntityFieldMeta.getRelationType() == CompiledEntityFieldMeta.RelationType.SingularComplex)
				{
					//field = new ComplexObject(compiledEntityFieldMeta.getClazz());
				}
				else if (compiledEntityFieldMeta.getRelationType() == CompiledEntityFieldMeta.RelationType.MultipleComplex)
				{
					//field = new BasicList<>();
				}
				AttributeContainer<T, ?> attribute = new AttributeContainer<>();
				attribute.field = field;
				attribute.fieldSpec =compiledEntityFieldMeta;
				fieldList.add(attribute);
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
	
	public BranchNodeType getModel()
	{
		return (BranchNodeType)this.entityMeta.getModel();
	}
	
	public <X> LeafNode<T,X> get(LeafNodeField<T,X> field)
	{
		return (LeafNode<T,X>) this.fieldList.get(this.entityMeta.getFieldIndexByClass().get(field)).getField();
	}
	public <X extends BranchNodeType> LeafNode<T,X> get(BranchNodeField<T,X> field)
	{
		return (LeafNode<T,X>) this.fieldList.get(this.entityMeta.getFieldIndexByClass().get(field)).getField();
	}
	
	public <X> LeafNode<T,X> getSingleValue(ModelPath<T,X> path)
	{
		return null;
	}
	
	private static class AttributeContainer<P,T>
	{
		private CompiledEntityFieldMeta fieldSpec = null;
		private EntityField field = null;
		
		private CompiledEntityFieldMeta getFieldSpec()
		{
			return fieldSpec;
		}
		private void setFieldSpec(CompiledEntityFieldMeta fieldSpec)
		{
			this.fieldSpec = fieldSpec;
		}
		private EntityField getField()
		{
			return field;
		}
		private void setField(EntityField field)
		{
			this.field = field;
		}
	}
}