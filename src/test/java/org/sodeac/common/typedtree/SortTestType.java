package org.sodeac.common.typedtree;

import org.sodeac.common.typedtree.BranchNodeMetaModel;

public class SortTestType extends BranchNodeMetaModel
{
	public static final BranchNodeListType<SortTestType,SortTestItemType> list = new BranchNodeListType<SortTestType,SortTestItemType>(SortTestType.class,SortTestItemType.class);
}
