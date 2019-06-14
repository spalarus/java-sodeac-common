package org.sodeac.common.typedtree;

import org.sodeac.common.typedtree.BranchNodeType;
import org.sodeac.common.typedtree.BranchNodeMetaModel;
import org.sodeac.common.typedtree.LeafNodeType;

public class UserType extends BranchNodeMetaModel
{
	public static final LeafNodeType<UserType,String> name = new LeafNodeType<UserType,String>(UserType.class,String.class);
	public static final BranchNodeType<UserType,AddressType> address = new BranchNodeType<UserType,AddressType>(UserType.class,AddressType.class);
}
