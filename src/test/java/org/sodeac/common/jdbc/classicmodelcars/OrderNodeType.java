package org.sodeac.common.jdbc.classicmodelcars;

import java.util.Date;

import org.sodeac.common.typedtree.BranchNodeListType;
import org.sodeac.common.typedtree.BranchNodeMetaModel;
import org.sodeac.common.typedtree.LeafNodeType;
import org.sodeac.common.typedtree.ModelRegistry;

public class OrderNodeType extends BranchNodeMetaModel
{
	static{ModelRegistry.getBranchNodeMetaModel(OrderNodeType.class);}
	
	public static volatile LeafNodeType<OrderNodeType,Integer> ORDERNUMBER;
	public static volatile LeafNodeType<OrderNodeType,Date> ORDERREQUIREDDATE;
	public static volatile LeafNodeType<OrderNodeType,Date> ORDERORDERDATE;
	public static volatile LeafNodeType<OrderNodeType,Date> ORDERSHIPPEDDATE;
	public static volatile LeafNodeType<OrderNodeType,String> ORDERSTATUS;
	public static volatile LeafNodeType<OrderNodeType,String> ORDERCOMMENTS;
	
	public static volatile BranchNodeListType<OrderNodeType,OrderDetailNodeType> ORDERDETAILS;
	
}
