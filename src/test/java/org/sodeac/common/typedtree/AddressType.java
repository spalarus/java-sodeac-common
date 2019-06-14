package org.sodeac.common.typedtree;

import org.sodeac.common.typedtree.BranchNodeType;
import org.sodeac.common.typedtree.BranchNodeMetaModel;
import org.sodeac.common.typedtree.LeafNodeType;

public class AddressType extends BranchNodeMetaModel
{
	public static final LeafNodeType<AddressType,String> street = new LeafNodeType<AddressType,String>(AddressType.class,String.class);
	public static final BranchNodeType<AddressType,CountryType> country = new BranchNodeType<AddressType,CountryType>(AddressType.class,CountryType.class);
	public static final BranchNodeType<AddressType,UserType> parentuser = new BranchNodeType<AddressType,UserType>(AddressType.class,UserType.class);
}
