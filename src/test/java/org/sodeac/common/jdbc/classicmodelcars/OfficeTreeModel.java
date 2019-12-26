package org.sodeac.common.jdbc.classicmodelcars;

import org.sodeac.common.typedtree.BranchNodeListType;
import org.sodeac.common.typedtree.BranchNodeMetaModel;
import org.sodeac.common.typedtree.BranchNodeType;
import org.sodeac.common.typedtree.ModelRegistry;
import org.sodeac.common.typedtree.TypedTreeMetaModel;

public class OfficeTreeModel extends TypedTreeMetaModel<OfficeTreeModel>
{
	static{ModelRegistry.getTypedTreeMetaModel(OfficeTreeModel.class);}
	
	public static volatile BranchNodeType<OfficeTreeModel,OfficeResultSetNodeType> resultSet;
	
	public static class OfficeResultSetNodeType extends BranchNodeMetaModel
	{
		static{ModelRegistry.getBranchNodeMetaModel(OfficeResultSetNodeType.class);}
		
		public static volatile BranchNodeListType<OfficeResultSetNodeType,OfficeNodeType> officeList;
	}
}
