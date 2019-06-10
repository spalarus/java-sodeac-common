package org.sodeac.common.typedtree;

import org.sodeac.common.typedtree.BranchNodeField;
import org.sodeac.common.typedtree.BranchNodeType;
import org.sodeac.common.typedtree.LeafNodeField;

public class UserType extends BranchNodeType
{
	public static final LeafNodeField<UserType,String> name = new LeafNodeField<UserType,String>(UserType.class,String.class);
	public static final BranchNodeField<UserType,AddressType> address = new BranchNodeField<UserType,AddressType>(UserType.class,AddressType.class);
}
