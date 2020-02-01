package org.sodeac.common.jdbc.cruder;

import org.sodeac.common.typedtree.BranchNodeType;
import org.sodeac.common.typedtree.ModelRegistry;
import org.sodeac.common.typedtree.TypedTreeMetaModel;
import org.sodeac.common.typedtree.annotation.Domain;
import org.sodeac.common.typedtree.annotation.Version;

@Domain(name="test.sodeac.org",module="minimerchandisemanagement")
@Version(major=0,minor=5)
public class MiniMerchandiseManagementModel extends TypedTreeMetaModel<MiniMerchandiseManagementModel>
{
	static{get();}
	
	public static MiniMerchandiseManagementModel get()
	{
		return ModelRegistry.getTypedTreeMetaModel(MiniMerchandiseManagementModel.class);
	}
	
	public static volatile BranchNodeType<MiniMerchandiseManagementModel,ArticleNodeType> article;
}
