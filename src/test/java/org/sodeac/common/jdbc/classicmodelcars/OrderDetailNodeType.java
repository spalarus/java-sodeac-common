package org.sodeac.common.jdbc.classicmodelcars;

import org.sodeac.common.typedtree.BranchNodeMetaModel;
import org.sodeac.common.typedtree.BranchNodeType;
import org.sodeac.common.typedtree.LeafNodeType;
import org.sodeac.common.typedtree.ModelRegistry;

public class OrderDetailNodeType extends BranchNodeMetaModel
{
	static{ModelRegistry.getBranchNodeMetaModel(OrderDetailNodeType.class);}
	
	public static volatile LeafNodeType<OrderDetailNodeType,Integer> ORDERDETAILID;
	public static volatile LeafNodeType<OrderDetailNodeType,Integer> ORDERDETAILORDERLINENUMBER;
	public static volatile LeafNodeType<OrderDetailNodeType,Double> ORDERDETAILPRICEEACH;
	public static volatile LeafNodeType<OrderDetailNodeType,Integer> ORDERDETAILQUANTITYORDERED;
	
	public static volatile BranchNodeType<OrderDetailNodeType,ProductNodeType> PRODUCT;
}
