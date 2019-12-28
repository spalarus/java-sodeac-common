package org.sodeac.common.typedtree;

import org.sodeac.common.typedtree.annotation.SQLPrimaryKey;

public class TypedTreeHelper
{
	public static <M  extends BranchNodeMetaModel,T> LeafNodeType<M, T> getPrimaryKeyNode(Class<M> clazz)
	{
		try
		{
			BranchNodeMetaModel model = ModelRegistry.getBranchNodeMetaModel(clazz);
			for(LeafNodeType ln : model.getLeafNodeTypeList())
			{
				if(ln.referencedByField().getAnnotation(SQLPrimaryKey.class) != null)
				{
					return ln;
				}
			}
		}
		catch (RuntimeException e)
		{
			throw e;
		}
		catch (Exception e) 
		{
			throw new RuntimeException(e);
		}
		return null;
	}
}
