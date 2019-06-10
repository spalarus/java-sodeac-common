package org.sodeac.common.typedtree;

import org.sodeac.common.typedtree.BranchNodeField;
import org.sodeac.common.typedtree.TypedTreeModel;

public class TestModel extends TypedTreeModel<TestModel>
{
	public static final BranchNodeField<TestModel,UserType> user = new BranchNodeField<TestModel,UserType>(TestModel.class,UserType.class);
}
