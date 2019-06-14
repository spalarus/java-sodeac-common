package org.sodeac.common.typedtree;

import org.sodeac.common.typedtree.BranchNodeType;
import org.sodeac.common.typedtree.TypedTreeMetaModel;

public class TestModel extends TypedTreeMetaModel<TestModel>
{
	public static final BranchNodeType<TestModel,UserType> user = new BranchNodeType<TestModel,UserType>(TestModel.class,UserType.class);
}
