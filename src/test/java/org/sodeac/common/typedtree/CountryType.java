package org.sodeac.common.typedtree;

import org.sodeac.common.typedtree.BranchNodeMetaModel;
import org.sodeac.common.typedtree.LeafNodeType;

public class CountryType extends BranchNodeMetaModel
{
	public static final LeafNodeType<CountryType,String> name = new LeafNodeType<CountryType,String>(CountryType.class,String.class);
}
