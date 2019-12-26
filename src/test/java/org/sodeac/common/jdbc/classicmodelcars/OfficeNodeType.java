package org.sodeac.common.jdbc.classicmodelcars;

import org.sodeac.common.typedtree.BranchNodeListType;
import org.sodeac.common.typedtree.BranchNodeMetaModel;
import org.sodeac.common.typedtree.LeafNodeType;
import org.sodeac.common.typedtree.ModelRegistry;

public class OfficeNodeType extends BranchNodeMetaModel
{
	static{ModelRegistry.getBranchNodeMetaModel(OfficeNodeType.class);}
	
	public static volatile LeafNodeType<OfficeNodeType,String> OFFICECODE;
	public static volatile LeafNodeType<OfficeNodeType,String> OFFICEADDRESSLINE1;
	public static volatile LeafNodeType<OfficeNodeType,String> OFFICEADDRESSLINE2;
	public static volatile LeafNodeType<OfficeNodeType,String> OFFICECITY;
	public static volatile LeafNodeType<OfficeNodeType,String> OFFICECOUNTRY;
	public static volatile LeafNodeType<OfficeNodeType,String> OFFICEPOSTALCODE;
	public static volatile LeafNodeType<OfficeNodeType,String> OFFICESTATE;
	public static volatile LeafNodeType<OfficeNodeType,String> OFFICETERRITORY;
	public static volatile LeafNodeType<OfficeNodeType,String> OFFICEPHONE;
	
	public static volatile BranchNodeListType<OfficeNodeType,EmployeeNodeType> EMPLOYEES;
}
