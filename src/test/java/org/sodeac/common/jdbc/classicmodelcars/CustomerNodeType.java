package org.sodeac.common.jdbc.classicmodelcars;

import org.sodeac.common.typedtree.BranchNodeListType;
import org.sodeac.common.typedtree.BranchNodeMetaModel;
import org.sodeac.common.typedtree.LeafNodeType;
import org.sodeac.common.typedtree.ModelRegistry;

public class CustomerNodeType extends BranchNodeMetaModel
{
	static{ModelRegistry.getBranchNodeMetaModel(CustomerNodeType.class);}	
	
	public static volatile LeafNodeType<CustomerNodeType,Integer> CUSTOMERNUMBER;

	public static volatile LeafNodeType<CustomerNodeType,String> CUSTOMERNAME;
	public static volatile LeafNodeType<CustomerNodeType,String> CUSTOMERCONTACTLASTNAME;
	public static volatile LeafNodeType<CustomerNodeType,Integer> CUSTOMERCREDITLIMIT;
	public static volatile LeafNodeType<CustomerNodeType,String> CUSTOMERCONTACTFIRSTNAME;
	public static volatile LeafNodeType<CustomerNodeType,String> CUSTOMERPHONE;
	public static volatile LeafNodeType<CustomerNodeType,String> CUSTOMERADDRESSLINE1;
	public static volatile LeafNodeType<CustomerNodeType,String> CUSTOMERADDRESSLINE2;
	public static volatile LeafNodeType<CustomerNodeType,String> CUSTOMERCITY;
	public static volatile LeafNodeType<CustomerNodeType,String> CUSTOMERSTATE;
	public static volatile LeafNodeType<CustomerNodeType,String> CUSTOMERPOSTALCODE;
	public static volatile LeafNodeType<CustomerNodeType,String> CUSTOMERCOUNTRY;
	
	public static volatile BranchNodeListType<CustomerNodeType,PaymentNodeType> PAYMENTS;
	public static volatile BranchNodeListType<CustomerNodeType,OrderNodeType> ORDERS;
}
