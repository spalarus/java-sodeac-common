package org.sodeac.common.jdbc.classicmodelcars;

import org.sodeac.common.typedtree.BranchNodeListType;
import org.sodeac.common.typedtree.BranchNodeMetaModel;
import org.sodeac.common.typedtree.LeafNodeType;
import org.sodeac.common.typedtree.ModelRegistry;

public class EmployeeNodeType extends BranchNodeMetaModel
{
	static{ModelRegistry.getBranchNodeMetaModel(EmployeeNodeType.class);}	
	
	public static volatile LeafNodeType<EmployeeNodeType,Integer> EMPLOYEENUMBER;
	public static volatile LeafNodeType<EmployeeNodeType,String> EMPLOYEEEXTENSION;
	public static volatile LeafNodeType<EmployeeNodeType,String> EMPLOYEEFIRSTNAME;
	public static volatile LeafNodeType<EmployeeNodeType,String> EMPLOYEELASTNAME;
	public static volatile LeafNodeType<EmployeeNodeType,String> EMPLOYEEJOBTITLE;
	public static volatile LeafNodeType<EmployeeNodeType,String> EMPLOYEEEMAIL;
	public static volatile LeafNodeType<EmployeeNodeType,Integer> EMPLOYEEREPORTSTO;
	
	public static volatile BranchNodeListType<EmployeeNodeType,CustomerNodeType> CUSTOMERS;
}
