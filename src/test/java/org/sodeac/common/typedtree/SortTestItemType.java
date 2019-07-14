package org.sodeac.common.typedtree;

import org.sodeac.common.typedtree.BranchNodeMetaModel;
import org.sodeac.common.typedtree.LeafNodeType;

public class SortTestItemType extends BranchNodeMetaModel
{
	public static final LeafNodeType<SortTestItemType,Integer> random = new LeafNodeType<SortTestItemType,Integer>(SortTestItemType.class,Integer.class);
}
