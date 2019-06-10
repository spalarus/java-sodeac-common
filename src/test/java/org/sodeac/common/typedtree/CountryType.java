package org.sodeac.common.typedtree;

import org.sodeac.common.typedtree.BranchNodeType;
import org.sodeac.common.typedtree.LeafNodeField;

public class CountryType extends BranchNodeType
{
	public static final LeafNodeField<CountryType,String> name = new LeafNodeField<CountryType,String>(CountryType.class,String.class);
}
