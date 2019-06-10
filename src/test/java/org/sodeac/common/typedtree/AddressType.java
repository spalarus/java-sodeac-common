package org.sodeac.common.typedtree;

import org.sodeac.common.typedtree.BranchNodeField;
import org.sodeac.common.typedtree.BranchNodeType;
import org.sodeac.common.typedtree.LeafNodeField;

public class AddressType extends BranchNodeType
{
	public static final LeafNodeField<AddressType,String> street = new LeafNodeField<AddressType,String>(AddressType.class,String.class);
	public static final BranchNodeField<AddressType,CountryType> country = new BranchNodeField<AddressType,CountryType>(AddressType.class,CountryType.class);
	public static final BranchNodeField<AddressType,UserType> parentuser = new BranchNodeField<AddressType,UserType>(AddressType.class,UserType.class);
}
