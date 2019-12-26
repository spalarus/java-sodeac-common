package org.sodeac.common.jdbc.classicmodelcars;

import org.sodeac.common.typedtree.BranchNodeMetaModel;
import org.sodeac.common.typedtree.LeafNodeType;
import org.sodeac.common.typedtree.ModelRegistry;

public class ProductNodeType extends BranchNodeMetaModel
{
	static{ModelRegistry.getBranchNodeMetaModel(ProductNodeType.class);}
	
	public static volatile LeafNodeType<ProductNodeType,String> PRODUCTCODE;
	public static volatile LeafNodeType<ProductNodeType,String> PRODUCTNAME;
	public static volatile LeafNodeType<ProductNodeType,String> PRODUCTLINE;
	public static volatile LeafNodeType<ProductNodeType,String> PRODUCTSCALE;
	public static volatile LeafNodeType<ProductNodeType,String> PRODUCTVENDOR;
	public static volatile LeafNodeType<ProductNodeType,String> PRODUCTDESCRIPTION;
	public static volatile LeafNodeType<ProductNodeType,Integer> PRODUCTQUANTITYINSTOCK;
	public static volatile LeafNodeType<ProductNodeType,Double> PRODUCTBUYPRICE;
	public static volatile LeafNodeType<ProductNodeType,Double> PRODUCTMSRP;
	
}
