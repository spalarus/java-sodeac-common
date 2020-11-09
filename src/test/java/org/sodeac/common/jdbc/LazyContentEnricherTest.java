/*******************************************************************************
 * Copyright (c) 2020 Sebastian Palarus
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Sebastian Palarus - initial API and implementation
 *******************************************************************************/
package org.sodeac.common.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Test;
import org.sodeac.common.function.ExceptionCatchedConsumer;
import org.sodeac.common.jdbc.ResultSetParseHelper.ResultSetParseHelperBuilder;
import org.sodeac.common.jdbc.classicmodelcars.CustomerNodeType;
import org.sodeac.common.jdbc.classicmodelcars.EmployeeNodeType;
import org.sodeac.common.jdbc.classicmodelcars.OfficeNodeType;
import org.sodeac.common.jdbc.classicmodelcars.OfficeTreeModel;
import org.sodeac.common.jdbc.classicmodelcars.OrderDetailNodeType;
import org.sodeac.common.jdbc.classicmodelcars.PaymentNodeType;
import org.sodeac.common.jdbc.classicmodelcars.ProductNodeType;
import org.sodeac.common.jdbc.classicmodelcars.OfficeTreeModel.OfficeResultSetNodeType;
import org.sodeac.common.jdbc.classicmodelcars.OrderNodeType;
import org.sodeac.common.misc.CloseableCollector;
import org.sodeac.common.misc.LazyContentEnricher;
import org.sodeac.common.typedtree.BranchNode;
import org.sodeac.common.typedtree.ModelRegistry;
import org.sodeac.common.typedtree.TypedTreeMetaModel.RootBranchNode;

public class LazyContentEnricherTest
{
	@Test
	public void officeTest() throws Exception
	{
		try(CloseableCollector closeableCollector = CloseableCollector.newInstance())
		{
			DriverManager.registerDriver(org.h2.Driver.class.newInstance());
			
			String tempDir = System.getProperty("java.io.tmpdir");
			String database = "demo2";
			
			// create database from dump
			
			if(tempDir.endsWith("\\")) {tempDir = tempDir.substring(0, tempDir.length() -1);}
			if(tempDir.endsWith("/")) {tempDir = tempDir.substring(0, tempDir.length() -1);}
			
			new File(tempDir + "/" + database + ".mv.db").delete();
			
			int len;
			byte[] buffer = new byte[1080];
			ZipInputStream zis = closeableCollector.register(new ZipInputStream(new FileInputStream("./src/test/resources/classicmodelcars.zip")));
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) 
			{
				if(! "script.sql".equals(zipEntry.getName()))
				{
					continue;
				}
				FileOutputStream fos = closeableCollector.register(new FileOutputStream(new File(tempDir,zipEntry.getName())));
				while ((len = zis.read(buffer)) > 0)
				{
					fos.write(buffer, 0, len);
				}
				closeableCollector.close(fos);
				zipEntry = zis.getNextEntry();
			}
	        zis.closeEntry();
			
			org.h2.tools.RunScript.execute("jdbc:h2:" + tempDir + "/" + database, "", "",tempDir +"/script.sql", null,false);
			
			new File(tempDir,"script.sql").delete();
			
			Set<String> officeIds1 = new HashSet<>();
			Set<Integer> employeeIds1 = new HashSet<>();
			Set<Integer> customerIds1 = new HashSet<>();
			Set<Integer> paymentIds1 = new HashSet<>();
			Set<Integer> orderIds1 = new HashSet<>();
			Set<Integer> orderDetailIds1 = new HashSet<>();
			Set<String> productIds1 = new HashSet<>();
			
			RootBranchNode<OfficeTreeModel,OfficeResultSetNodeType> resultSetTree = ModelRegistry.getTypedTreeMetaModel(OfficeTreeModel.class).createRootNode(OfficeTreeModel.resultSet);
			
			LazyContentEnricher<BranchNode<OfficeResultSetNodeType, OfficeNodeType>, String, BranchNode<OfficeNodeType, EmployeeNodeType>> employeeContentEnricher = LazyContentEnricher.newInstance();
			LazyContentEnricher<BranchNode<OfficeNodeType,EmployeeNodeType>, Integer, BranchNode<EmployeeNodeType,CustomerNodeType>> customerContentEnricher = LazyContentEnricher.newInstance();
			LazyContentEnricher<BranchNode<EmployeeNodeType,CustomerNodeType>, Integer, BranchNode<CustomerNodeType,PaymentNodeType>> paymentContentEnricher = LazyContentEnricher.newInstance();
			LazyContentEnricher<BranchNode<EmployeeNodeType,CustomerNodeType>, Integer, BranchNode<CustomerNodeType,OrderNodeType>> orderContentEnricher = LazyContentEnricher.newInstance();
			LazyContentEnricher<BranchNode<CustomerNodeType,OrderNodeType>, Integer, BranchNode<OrderNodeType,OrderDetailNodeType>> orderDetailContentEnricher = LazyContentEnricher.newInstance();
			LazyContentEnricher<BranchNode<OrderNodeType,OrderDetailNodeType>, String, BranchNode<OrderDetailNodeType,ProductNodeType>> productContentEnricher = LazyContentEnricher.newInstance();
			
			
			Connection connection = closeableCollector.register(DriverManager.getConnection("jdbc:h2:" + tempDir + "/" + database, "", ""));
			PreparedStatement preparedStatementOffice = closeableCollector.register(connection.prepareStatement
			(
					"SELECT "
				+ 		"O.OFFICECODE AS OFFICECODE, "
				+ 		"O.ADDRESSLINE1 AS OFFICEADDRESSLINE1, "
				+ 		"O.ADDRESSLINE2 AS OFFICEADDRESSLINE2, "
				+ 		"O.CITY AS OFFICECITY, "
				+ 		"O.COUNTRY AS OFFICECOUNTRY, "
				+ 		"O.POSTALCODE AS OFFICEPOSTALCODE, "
				+ 		"O.STATE AS OFFICESTATE, "
				+ 		"O.TERRITORY AS OFFICETERRITORY, "
				+ 		"O.PHONE AS OFFICEPHONE "
				+ 	"FROM "
				+ 		"OFFICES O "
				+ 	"ORDER BY "
				+ 		"O.OFFICECODE"
				,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY
			));
			ResultSetParseHelper parseHelper = ResultSetParseHelperBuilder.newBuilder
			(
				OfficeNodeType.OFFICECODE.getNodeName(), String.class, OfficeResultSetNodeType.officeList.getBranchNodeClass(),OfficeTreeModel.resultSet.getBranchNodeClass(),
					
				c -> c.getParentObject().create(OfficeResultSetNodeType.officeList)
					.setValue(OfficeNodeType.OFFICECODE,c.getId())
					.setValue(OfficeNodeType.OFFICECITY,c.getString(OfficeNodeType.OFFICECITY.getNodeName()))
					.setValue(OfficeNodeType.OFFICECOUNTRY,c.getString(OfficeNodeType.OFFICECOUNTRY.getNodeName()))
					.setValue(OfficeNodeType.OFFICEADDRESSLINE1,c.getString(OfficeNodeType.OFFICEADDRESSLINE1.getNodeName()))
					.setValue(OfficeNodeType.OFFICEADDRESSLINE2,c.getString(OfficeNodeType.OFFICEADDRESSLINE2.getNodeName()))
					.setValue(OfficeNodeType.OFFICEPOSTALCODE,c.getString(OfficeNodeType.OFFICEPOSTALCODE.getNodeName()))
					.setValue(OfficeNodeType.OFFICESTATE,c.getString(OfficeNodeType.OFFICESTATE.getNodeName()))
					.setValue(OfficeNodeType.OFFICETERRITORY,c.getString(OfficeNodeType.OFFICETERRITORY.getNodeName()))
					.setValue(OfficeNodeType.OFFICEPHONE,c.getString(OfficeNodeType.OFFICEPHONE.getNodeName())),
				r -> 
				{
					officeIds1.add(r.getValue(OfficeNodeType.OFFICECODE));
					employeeContentEnricher.register(r, r.getValue(OfficeNodeType.OFFICECODE));
				},
				l -> 
				{
					employeeContentEnricher.invokeContentEnricher();
					customerContentEnricher.invokeContentEnricher();
					paymentContentEnricher.invokeContentEnricher();
					orderContentEnricher.invokeContentEnricher();
					orderDetailContentEnricher.invokeContentEnricher();
					productContentEnricher.invokeContentEnricher();
				}
			).buildParser();
			
			PreparedStatement preparedStatementEmployees = closeableCollector.register(connection.prepareStatement
			(
					"SELECT "
				+ 		"E.EMPLOYEENUMBER AS EMPLOYEENUMBER, "
				+ 		"E.OFFICECODE AS OFFICECODE, "
				+ 		"E.EXTENSION AS EMPLOYEEEXTENSION, "
				+ 		"E.FIRSTNAME AS EMPLOYEEFIRSTNAME, "
				+ 		"E.LASTNAME AS EMPLOYEELASTNAME, "
				+ 		"E.JOBTITLE AS EMPLOYEEJOBTITLE, "
				+ 		"E.EMAIL AS EMPLOYEEEMAIL, "
				+ 		"E.REPORTSTO AS EMPLOYEEREPORTSTO "
				+ 	"FROM "
				+ 		"EMPLOYEES E "
				+ 	"WHERE "
				+ 		"E.OFFICECODE IN (UNNEST(?)) "
				+ 	"ORDER BY "
				+ 		"E.OFFICECODE, "
				+ 		"E.EMPLOYEENUMBER"
			));
			employeeContentEnricher.defineContentEnricher(ExceptionCatchedConsumer.wrap(c -> 
			{
				preparedStatementEmployees.setArray(1, connection.createArrayOf("VARCHAR",c.getReferences().toArray()));
				ResultSet resultSet = preparedStatementEmployees.executeQuery();
				try
				{
					while(resultSet.next())
					{
						for(BranchNode<OfficeResultSetNodeType, OfficeNodeType> office : c.getObjectsToBeEnrichByReference().get(resultSet.getString("OFFICECODE")))
						{
							customerContentEnricher.register
							(
								office.create(OfficeNodeType.EMPLOYEES)
								.setValue(EmployeeNodeType.EMPLOYEENUMBER,resultSet.getInt(EmployeeNodeType.EMPLOYEENUMBER.getNodeName()))
								.setValue(EmployeeNodeType.EMPLOYEEEXTENSION,resultSet.getString(EmployeeNodeType.EMPLOYEEEXTENSION.getNodeName()))
								.setValue(EmployeeNodeType.EMPLOYEEJOBTITLE,resultSet.getString(EmployeeNodeType.EMPLOYEEJOBTITLE.getNodeName()))
								.setValue(EmployeeNodeType.EMPLOYEEFIRSTNAME,resultSet.getString(EmployeeNodeType.EMPLOYEEFIRSTNAME.getNodeName()))
								.setValue(EmployeeNodeType.EMPLOYEELASTNAME,resultSet.getString(EmployeeNodeType.EMPLOYEELASTNAME.getNodeName()))
								.setValue(EmployeeNodeType.EMPLOYEEEMAIL,resultSet.getString(EmployeeNodeType.EMPLOYEEEMAIL.getNodeName()))
								.setValue(EmployeeNodeType.EMPLOYEEREPORTSTO,resultSet.getInt(EmployeeNodeType.EMPLOYEEREPORTSTO.getNodeName())),
								resultSet.getInt(EmployeeNodeType.EMPLOYEENUMBER.getNodeName())
							);
							employeeIds1.add(resultSet.getInt(EmployeeNodeType.EMPLOYEENUMBER.getNodeName()));
						}
					}
				}
				finally 
				{
					resultSet.close();
				}
					
			}));
			
			PreparedStatement preparedStatementCustomer = closeableCollector.register(connection.prepareStatement
			(
					"SELECT "
				+ 		"C.CUSTOMERNUMBER AS CUSTOMERNUMBER, "
				+ 		"C.SALESREPEMPLOYEENUMBER AS SALESREPEMPLOYEENUMBER, "
				+ 		"C.CUSTOMERNAME AS CUSTOMERNAME, "
				+ 		"C.CONTACTLASTNAME AS CUSTOMERCONTACTLASTNAME, "
				+ 		"C.CREDITLIMIT AS CUSTOMERCREDITLIMIT, "
				+ 		"C.CONTACTFIRSTNAME AS CUSTOMERCONTACTFIRSTNAME, "
				+ 		"C.PHONE AS CUSTOMERPHONE, "
				+ 		"C.ADDRESSLINE1 AS CUSTOMERADDRESSLINE1, "
				+ 		"C.ADDRESSLINE2 AS CUSTOMERADDRESSLINE2, "
				+ 		"C.CITY AS CUSTOMERCITY, "
				+ 		"C.STATE AS CUSTOMERSTATE, "
				+ 		"C.POSTALCODE AS CUSTOMERPOSTALCODE, "
				+ 		"C.COUNTRY AS CUSTOMERCOUNTRY "
				+ 	"FROM "
				+ 		"CUSTOMERS C "
				+ 	"WHERE "
				+ 		"C.SALESREPEMPLOYEENUMBER IN (UNNEST(?)) "
				+ 	"ORDER BY "
				+ 		"C.SALESREPEMPLOYEENUMBER,"
				+ 		"C.CUSTOMERNUMBER"
			));
			customerContentEnricher.defineContentEnricher(ExceptionCatchedConsumer.wrap(c -> 
			{
				preparedStatementCustomer.setArray(1, connection.createArrayOf("INTEGER",c.getReferences().toArray()));
				ResultSet resultSet = preparedStatementCustomer.executeQuery();
				try
				{
					while(resultSet.next())
					{
						
						for(BranchNode<OfficeNodeType,EmployeeNodeType> employee : c.getObjectsToBeEnrichByReference().get(resultSet.getInt("SALESREPEMPLOYEENUMBER")))
						{
							orderContentEnricher.register
							(
								paymentContentEnricher.register
								(
									employee.create(EmployeeNodeType.CUSTOMERS)
									.setValue(CustomerNodeType.CUSTOMERNUMBER, resultSet.getInt(CustomerNodeType.CUSTOMERNUMBER.getNodeName()))
									.setValue(CustomerNodeType.CUSTOMERNAME,resultSet.getString(CustomerNodeType.CUSTOMERNAME.getNodeName()))
									.setValue(CustomerNodeType.CUSTOMERCONTACTLASTNAME,resultSet.getString(CustomerNodeType.CUSTOMERCONTACTLASTNAME.getNodeName()))
									.setValue(CustomerNodeType.CUSTOMERCONTACTFIRSTNAME,resultSet.getString(CustomerNodeType.CUSTOMERCONTACTFIRSTNAME.getNodeName()))
									.setValue(CustomerNodeType.CUSTOMERADDRESSLINE1,resultSet.getString(CustomerNodeType.CUSTOMERADDRESSLINE1.getNodeName()))
									.setValue(CustomerNodeType.CUSTOMERADDRESSLINE2,resultSet.getString(CustomerNodeType.CUSTOMERADDRESSLINE2.getNodeName()))
									.setValue(CustomerNodeType.CUSTOMERCITY,resultSet.getString(CustomerNodeType.CUSTOMERCITY.getNodeName()))
									.setValue(CustomerNodeType.CUSTOMERCOUNTRY,resultSet.getString(CustomerNodeType.CUSTOMERCOUNTRY.getNodeName()))
									.setValue(CustomerNodeType.CUSTOMERSTATE,resultSet.getString(CustomerNodeType.CUSTOMERSTATE.getNodeName()))
									.setValue(CustomerNodeType.CUSTOMERPOSTALCODE,resultSet.getString(CustomerNodeType.CUSTOMERPOSTALCODE.getNodeName()))
									.setValue(CustomerNodeType.CUSTOMERPHONE,resultSet.getString(CustomerNodeType.CUSTOMERPHONE.getNodeName()))
									.setValue(CustomerNodeType.CUSTOMERCREDITLIMIT,resultSet.getInt(CustomerNodeType.CUSTOMERCREDITLIMIT.getNodeName())),
									resultSet.getInt(CustomerNodeType.CUSTOMERNUMBER.getNodeName())
								)
								,resultSet.getInt(CustomerNodeType.CUSTOMERNUMBER.getNodeName())
							);
							customerIds1.add(resultSet.getInt(CustomerNodeType.CUSTOMERNUMBER.getNodeName()));
						}
					}
				}
				finally 
				{
					resultSet.close();
				}
			}));
			
			PreparedStatement preparedStatementPayment = closeableCollector.register(connection.prepareStatement
			(
					"SELECT "
				+ 		"P.ID AS PAYMENTID, "
				+ 		"P.CUSTOMERNUMBER AS CUSTOMERNUMBER, "
				+ 		"P.AMOUNT AS PAYMENTAMOUNT, "
				+ 		"P.CHECKNUMBER AS PAYMENTCHECKNUMBER, "
				+ 		"P.PAYMENTDATE AS PAYMENTDATE "
				+ 	"FROM "
				+ 		"PAYMENTS P "
				+ 	"WHERE "
				+ 		"P.CUSTOMERNUMBER IN (UNNEST(?)) "
				+ 	"ORDER BY "
				+ 		"P.CUSTOMERNUMBER,P.ID"
			));
			paymentContentEnricher.defineContentEnricher(ExceptionCatchedConsumer.wrap(c -> 
			{
				preparedStatementPayment.setArray(1, connection.createArrayOf("INTEGER",c.getReferences().toArray()));
				ResultSet resultSet = preparedStatementPayment.executeQuery();
				try
				{
					while(resultSet.next())
					{
						
						for(BranchNode<EmployeeNodeType,CustomerNodeType> customer : c.getObjectsToBeEnrichByReference().get(resultSet.getInt("CUSTOMERNUMBER")))
						{
							customer.create(CustomerNodeType.PAYMENTS)
							.setValue(PaymentNodeType.PAYMENTID, resultSet.getInt("PAYMENTID"))
							.setValue(PaymentNodeType.PAYMENTCHECKNUMBER,resultSet.getString(PaymentNodeType.PAYMENTCHECKNUMBER.getNodeName()))
							.setValue(PaymentNodeType.PAYMENTAMOUNT,resultSet.getDouble(PaymentNodeType.PAYMENTAMOUNT.getNodeName()))
							.setValue(PaymentNodeType.PAYMENTDATE,resultSet.getDate(PaymentNodeType.PAYMENTDATE.getNodeName()));
							
							paymentIds1.add(resultSet.getInt("PAYMENTID"));
						}
					}
				}
				finally 
				{
					resultSet.close();
				}
			}));
			
			PreparedStatement preparedStatementOrders = closeableCollector.register(connection.prepareStatement
			(
					"SELECT "
				+ 		"R.ORDERNUMBER AS ORDERNUMBER, "
				+ 		"R.CUSTOMERNUMBER AS CUSTOMERNUMBER, "
				+ 		"R.REQUIREDDATE AS ORDERREQUIREDDATE, "
				+ 		"R.ORDERDATE AS ORDERORDERDATE, "
				+ 		"R.SHIPPEDDATE AS ORDERSHIPPEDDATE, "
				+ 		"R.STATUS AS ORDERSTATUS, "
				+ 		"R.COMMENTS AS ORDERCOMMENTS "
				+ 	"FROM "
				+ 		"ORDERS R "
				+ 	"WHERE "
				+ 		"R.CUSTOMERNUMBER IN (UNNEST(?)) "
				+ 	"ORDER BY "
				+ 		"R.CUSTOMERNUMBER, R.ORDERNUMBER"
			));
			orderContentEnricher.defineContentEnricher(ExceptionCatchedConsumer.wrap(c -> 
			{
				preparedStatementOrders.setArray(1, connection.createArrayOf("INTEGER",c.getReferences().toArray()));
				ResultSet resultSet = preparedStatementOrders.executeQuery();
				try
				{
					while(resultSet.next())
					{
						for(BranchNode<EmployeeNodeType,CustomerNodeType> customer : c.getObjectsToBeEnrichByReference().get(resultSet.getInt("CUSTOMERNUMBER")))
						{
							orderDetailContentEnricher.register
							(
								customer.create(CustomerNodeType.ORDERS)
								.setValue(OrderNodeType.ORDERNUMBER, resultSet.getInt(OrderNodeType.ORDERNUMBER.getNodeName()))
								.setValue(OrderNodeType.ORDERREQUIREDDATE,resultSet.getDate(OrderNodeType.ORDERREQUIREDDATE.getNodeName()))
								.setValue(OrderNodeType.ORDERORDERDATE,resultSet.getDate(OrderNodeType.ORDERORDERDATE.getNodeName()))
								.setValue(OrderNodeType.ORDERSHIPPEDDATE,resultSet.getDate(OrderNodeType.ORDERSHIPPEDDATE.getNodeName()))
								.setValue(OrderNodeType.ORDERCOMMENTS,resultSet.getString(OrderNodeType.ORDERCOMMENTS.getNodeName()))
								.setValue(OrderNodeType.ORDERSTATUS,resultSet.getString(OrderNodeType.ORDERSTATUS.getNodeName())),
								resultSet.getInt(OrderNodeType.ORDERNUMBER.getNodeName())
							);
							orderIds1.add(resultSet.getInt(OrderNodeType.ORDERNUMBER.getNodeName()));
						}
					}
				}
				finally 
				{
					resultSet.close();
				}
			}));
			
			PreparedStatement preparedStatementOrderDetail = closeableCollector.register(connection.prepareStatement
			(
					"SELECT "
				+ 		"D.ID AS ORDERDETAILID,"
				+ 		"D.ORDERNUMBER AS ORDERNUMBER, "
				+ 		"D.PRODUCTCODE AS PRODUCTCODE, "
				+ 		"D.ORDERLINENUMBER AS ORDERDETAILORDERLINENUMBER, "
				+ 		"D.PRICEEACH AS ORDERDETAILPRICEEACH, "
				+ 		"D.QUANTITYORDERED AS ORDERDETAILQUANTITYORDERED "
				+ 	"FROM "
				+ 		"ORDERDETAILS D "
				+ 	"WHERE "
				+ 		"D.ORDERNUMBER IN (UNNEST(?)) "
				+ 	"ORDER BY "
				+ 		"D.ORDERNUMBER,D.ORDERLINENUMBER"
			));
			orderDetailContentEnricher.defineContentEnricher(ExceptionCatchedConsumer.wrap(c -> 
			{
				preparedStatementOrderDetail.setArray(1, connection.createArrayOf("INTEGER",c.getReferences().toArray()));
				ResultSet resultSet = preparedStatementOrderDetail.executeQuery();
				try
				{
					while(resultSet.next())
					{
						for(BranchNode<CustomerNodeType,OrderNodeType> order : c.getObjectsToBeEnrichByReference().get(resultSet.getInt("ORDERNUMBER")))
						{
							productContentEnricher.register
							(
								order.create(OrderNodeType.ORDERDETAILS)
								.setValue(OrderDetailNodeType.ORDERDETAILID, resultSet.getInt(OrderDetailNodeType.ORDERDETAILID.getNodeName()))
								.setValue(OrderDetailNodeType.ORDERDETAILORDERLINENUMBER,resultSet.getInt(OrderDetailNodeType.ORDERDETAILORDERLINENUMBER.getNodeName()))
								.setValue(OrderDetailNodeType.ORDERDETAILPRICEEACH,resultSet.getDouble(OrderDetailNodeType.ORDERDETAILPRICEEACH.getNodeName()))
								.setValue(OrderDetailNodeType.ORDERDETAILQUANTITYORDERED,resultSet.getInt(OrderDetailNodeType.ORDERDETAILQUANTITYORDERED.getNodeName())),
								resultSet.getString("PRODUCTCODE")
							);
							orderDetailIds1.add(resultSet.getInt(OrderDetailNodeType.ORDERDETAILID.getNodeName()));
						}
					}
				}
				finally 
				{
					resultSet.close();
				}
			}));
			
			PreparedStatement preparedStatementProduct = closeableCollector.register(connection.prepareStatement
			(
					"SELECT "
				+ 		"PR.PRODUCTCODE AS PRODUCTCODE, "
				+ 		"PR.PRODUCTNAME AS PRODUCTNAME, "
				+ 		"PR.PRODUCTLINE AS PRODUCTLINE, "
				+ 		"PR.PRODUCTSCALE AS PRODUCTSCALE, "
				+ 		"PR.PRODUCTVENDOR AS PRODUCTVENDOR, "
				+ 		"PR.PRODUCTDESCRIPTION AS PRODUCTDESCRIPTION, "
				+ 		"PR.QUANTITYINSTOCK AS PRODUCTQUANTITYINSTOCK, "
				+ 		"PR.BUYPRICE AS PRODUCTBUYPRICE, "
				+ 		"PR.MSRP AS PRODUCTMSRP "
				+ 	"FROM "
				+ 		"PRODUCTS PR "
				+ 	"WHERE "
				+ 		"PR.PRODUCTCODE IN (UNNEST(?)) "
			));
			productContentEnricher.defineContentEnricher(ExceptionCatchedConsumer.wrap(c -> 
			{
				preparedStatementProduct.setArray(1, connection.createArrayOf("VARCHAR",c.getReferences().toArray()));
				ResultSet resultSet = preparedStatementProduct.executeQuery();
				try
				{
					while(resultSet.next())
					{
						for(BranchNode<OrderNodeType,OrderDetailNodeType> orderDetail : c.getObjectsToBeEnrichByReference().get(resultSet.getString("PRODUCTCODE")))
						{
							orderDetail.create(OrderDetailNodeType.PRODUCT)
							.setValue(ProductNodeType.PRODUCTCODE,resultSet.getString(ProductNodeType.PRODUCTCODE.getNodeName()))
							.setValue(ProductNodeType.PRODUCTNAME,resultSet.getString(ProductNodeType.PRODUCTNAME.getNodeName()))
							.setValue(ProductNodeType.PRODUCTLINE,resultSet.getString(ProductNodeType.PRODUCTLINE.getNodeName()))
							.setValue(ProductNodeType.PRODUCTSCALE,resultSet.getString(ProductNodeType.PRODUCTSCALE.getNodeName()))
							.setValue(ProductNodeType.PRODUCTVENDOR,resultSet.getString(ProductNodeType.PRODUCTVENDOR.getNodeName()))
							.setValue(ProductNodeType.PRODUCTDESCRIPTION,resultSet.getString(ProductNodeType.PRODUCTDESCRIPTION.getNodeName()))
							.setValue(ProductNodeType.PRODUCTQUANTITYINSTOCK,resultSet.getInt(ProductNodeType.PRODUCTQUANTITYINSTOCK.getNodeName()))
							.setValue(ProductNodeType.PRODUCTBUYPRICE,resultSet.getDouble(ProductNodeType.PRODUCTBUYPRICE.getNodeName()))
							.setValue(ProductNodeType.PRODUCTMSRP,resultSet.getDouble(ProductNodeType.PRODUCTMSRP.getNodeName()));
							productIds1.add(resultSet.getString(ProductNodeType.PRODUCTCODE.getNodeName()));
						}
					}
				}
				finally 
				{
					resultSet.close();
				}
			}));
			
			parseHelper.parse(preparedStatementOffice,resultSetTree,3);
			parseHelper.close();
			
			Set<String> officeIds2 = new HashSet<>();
			Set<Integer> employeeIds2 = new HashSet<>();
			Set<Integer> customerIds2 = new HashSet<>();
			Set<Integer> paymentIds2 = new HashSet<>();
			Set<Integer> orderIds2 = new HashSet<>();
			Set<Integer> orderDetailIds2 = new HashSet<>();
			Set<String> productIds2 = new HashSet<>();
			
			PreparedStatement preparedStatementOffice2 = closeableCollector.register(connection.prepareStatement("SELECT * FROM OFFICES ORDER BY OFFICECODE"));
			PreparedStatement preparedStatementEmployees2 = closeableCollector.register(connection.prepareStatement("SELECT * FROM EMPLOYEES WHERE OFFICECODE = ? ORDER BY EMPLOYEENUMBER"));
			PreparedStatement preparedStatementCustomer2 = closeableCollector.register(connection.prepareStatement("SELECT * FROM CUSTOMERS WHERE SALESREPEMPLOYEENUMBER = ? ORDER BY CUSTOMERNUMBER"));
			PreparedStatement preparedStatementPayment2 = closeableCollector.register(connection.prepareStatement("SELECT * FROM PAYMENTS WHERE CUSTOMERNUMBER = ? ORDER BY ID"));
			PreparedStatement preparedStatementOrders2 = closeableCollector.register(connection.prepareStatement("SELECT * FROM ORDERS WHERE CUSTOMERNUMBER = ? ORDER BY ORDERNUMBER"));
			PreparedStatement preparedStatementOrderDetail2 = closeableCollector.register(connection.prepareStatement("SELECT * FROM ORDERDETAILS WHERE ORDERNUMBER = ? ORDER BY ORDERLINENUMBER"));
			PreparedStatement preparedStatementProduct2 = closeableCollector.register(connection.prepareStatement("SELECT * FROM PRODUCTS WHERE PRODUCTCODE = ?"));
			
			ResultSet resultSetOffice = preparedStatementOffice2.executeQuery();
			try
			{
				Iterator<BranchNode<OfficeResultSetNodeType, OfficeNodeType>> iteratorOffice = resultSetTree.getUnmodifiableNodeList(OfficeResultSetNodeType.officeList).iterator();
				
				while(resultSetOffice.next())
				{
					officeIds2.add(resultSetOffice.getString("OFFICECODE"));
					assertTrue("iteratorOffice.next should returns true", iteratorOffice.hasNext());
					BranchNode<OfficeResultSetNodeType, OfficeNodeType> officeNode = iteratorOffice.next();
					
					assertEquals("values should be equal", resultSetOffice.getString("OFFICECODE"), officeNode.getValue(OfficeNodeType.OFFICECODE));
					assertEquals("values should be equal", resultSetOffice.getString("CITY"), officeNode.getValue(OfficeNodeType.OFFICECITY));
					assertEquals("values should be equal", resultSetOffice.getString("PHONE"), officeNode.getValue(OfficeNodeType.OFFICEPHONE));
					assertEquals("values should be equal", resultSetOffice.getString("ADDRESSLINE1"), officeNode.getValue(OfficeNodeType.OFFICEADDRESSLINE1));
					assertEquals("values should be equal", resultSetOffice.getString("ADDRESSLINE2"), officeNode.getValue(OfficeNodeType.OFFICEADDRESSLINE2));
					assertEquals("values should be equal", resultSetOffice.getString("STATE"), officeNode.getValue(OfficeNodeType.OFFICESTATE));
					assertEquals("values should be equal", resultSetOffice.getString("COUNTRY"), officeNode.getValue(OfficeNodeType.OFFICECOUNTRY));
					assertEquals("values should be equal", resultSetOffice.getString("POSTALCODE"), officeNode.getValue(OfficeNodeType.OFFICEPOSTALCODE));
					assertEquals("values should be equal", resultSetOffice.getString("TERRITORY"), officeNode.getValue(OfficeNodeType.OFFICETERRITORY));
					
					preparedStatementEmployees2.setString(1, officeNode.getValue(OfficeNodeType.OFFICECODE));
					ResultSet resultSetEmployee = preparedStatementEmployees2.executeQuery();
					try
					{
						Iterator<BranchNode<OfficeNodeType,EmployeeNodeType>> iteratorEmployee = officeNode.getUnmodifiableNodeList(OfficeNodeType.EMPLOYEES).iterator();
						while(resultSetEmployee.next())
						{
							employeeIds2.add(resultSetEmployee.getInt("EMPLOYEENUMBER"));
							assertTrue("iteratorEmployee.next should returns true", iteratorEmployee.hasNext());
							BranchNode<OfficeNodeType,EmployeeNodeType> employee = iteratorEmployee.next();
							
							assertEquals("values should be equal", (Integer)resultSetEmployee.getInt("EMPLOYEENUMBER"), employee.getValue(EmployeeNodeType.EMPLOYEENUMBER));
							assertEquals("values should be equal", resultSetEmployee.getString("LASTNAME"), employee.getValue(EmployeeNodeType.EMPLOYEELASTNAME));
							assertEquals("values should be equal", resultSetEmployee.getString("FIRSTNAME"), employee.getValue(EmployeeNodeType.EMPLOYEEFIRSTNAME));
							assertEquals("values should be equal", resultSetEmployee.getString("EXTENSION"), employee.getValue(EmployeeNodeType.EMPLOYEEEXTENSION));
							assertEquals("values should be equal", resultSetEmployee.getString("EMAIL"), employee.getValue(EmployeeNodeType.EMPLOYEEEMAIL));
							assertEquals("values should be equal", resultSetEmployee.getString("JOBTITLE"), employee.getValue(EmployeeNodeType.EMPLOYEEJOBTITLE));
							assertEquals("values should be equal", (Integer)resultSetEmployee.getInt("REPORTSTO"), employee.getValue(EmployeeNodeType.EMPLOYEEREPORTSTO) == null ? new Integer(0) : employee.getValue(EmployeeNodeType.EMPLOYEEREPORTSTO));
							
							preparedStatementCustomer2.setInt(1,employee.getValue(EmployeeNodeType.EMPLOYEENUMBER));
							
							ResultSet resultSetCustomer = preparedStatementCustomer2.executeQuery();
							try
							{
								Iterator<BranchNode<EmployeeNodeType,CustomerNodeType>> iteratorCustomer = employee.getUnmodifiableNodeList(EmployeeNodeType.CUSTOMERS).iterator();
								
								while(resultSetCustomer.next())
								{
									customerIds2.add(resultSetCustomer.getInt("CUSTOMERNUMBER"));
									assertTrue("iteratorCustomer.next should returns true", iteratorCustomer.hasNext());
									BranchNode<EmployeeNodeType,CustomerNodeType> customer = iteratorCustomer.next();
									
									assertEquals("values should be equal", (Integer)resultSetCustomer.getInt("CUSTOMERNUMBER"), customer.getValue(CustomerNodeType.CUSTOMERNUMBER));
									assertEquals("values should be equal", resultSetCustomer.getString("CUSTOMERNAME"), customer.getValue(CustomerNodeType.CUSTOMERNAME));
									assertEquals("values should be equal", resultSetCustomer.getString("CONTACTLASTNAME"), customer.getValue(CustomerNodeType.CUSTOMERCONTACTLASTNAME));
									assertEquals("values should be equal", resultSetCustomer.getString("CONTACTFIRSTNAME"), customer.getValue(CustomerNodeType.CUSTOMERCONTACTFIRSTNAME));
									assertEquals("values should be equal", resultSetCustomer.getString("PHONE"), customer.getValue(CustomerNodeType.CUSTOMERPHONE));
									assertEquals("values should be equal", resultSetCustomer.getString("ADDRESSLINE1"), customer.getValue(CustomerNodeType.CUSTOMERADDRESSLINE1));
									assertEquals("values should be equal", resultSetCustomer.getString("ADDRESSLINE2"), customer.getValue(CustomerNodeType.CUSTOMERADDRESSLINE2));
									assertEquals("values should be equal", resultSetCustomer.getString("CITY"), customer.getValue(CustomerNodeType.CUSTOMERCITY));
									assertEquals("values should be equal", resultSetCustomer.getString("STATE"), customer.getValue(CustomerNodeType.CUSTOMERSTATE));
									assertEquals("values should be equal", resultSetCustomer.getString("POSTALCODE"), customer.getValue(CustomerNodeType.CUSTOMERPOSTALCODE));
									assertEquals("values should be equal", resultSetCustomer.getString("COUNTRY"), customer.getValue(CustomerNodeType.CUSTOMERCOUNTRY));
									assertEquals("values should be equal", (Integer)resultSetCustomer.getInt("CREDITLIMIT"), customer.getValue(CustomerNodeType.CUSTOMERCREDITLIMIT));
									
									Iterator<BranchNode<CustomerNodeType,PaymentNodeType>> iteratorPayment = customer.getUnmodifiableNodeList(CustomerNodeType.PAYMENTS).iterator();
									preparedStatementPayment2.setInt(1, customer.getValue(CustomerNodeType.CUSTOMERNUMBER));
									ResultSet resultSetPayment = preparedStatementPayment2.executeQuery();
									try
									{
										while(resultSetPayment.next())
										{
											paymentIds2.add(resultSetPayment.getInt("ID"));
											assertTrue("iteratorPayment.next should returns true", iteratorPayment.hasNext());
											BranchNode<CustomerNodeType,PaymentNodeType> payment = iteratorPayment.next();
											
											assertEquals("values should be equal", (Integer)resultSetPayment.getInt("ID"), payment.getValue(PaymentNodeType.PAYMENTID));
											assertEquals("values should be equal", resultSetPayment.getString("CHECKNUMBER"), payment.getValue(PaymentNodeType.PAYMENTCHECKNUMBER));
											assertEquals("values should be equal", (Double)resultSetPayment.getDouble("AMOUNT"), payment.getValue(PaymentNodeType.PAYMENTAMOUNT));
											assertEquals("values should be equal", resultSetPayment.getDate("PAYMENTDATE"), payment.getValue(PaymentNodeType.PAYMENTDATE));
											
										}
										assertFalse("iteratorPayment.next should returns false", iteratorPayment.hasNext());
									}
									finally 
									{
										resultSetPayment.close();
										resultSetPayment = null;
									}
									
									Iterator<BranchNode<CustomerNodeType,OrderNodeType>> iteratorOrders = customer.getUnmodifiableNodeList(CustomerNodeType.ORDERS).iterator();
									preparedStatementOrders2.setInt(1, customer.getValue(CustomerNodeType.CUSTOMERNUMBER));
									ResultSet resultSetOrders = preparedStatementOrders2.executeQuery();
									try
									{
										
										while(resultSetOrders.next())
										{
											orderIds2.add(resultSetOrders.getInt("ORDERNUMBER"));
											assertTrue("iteratorOrders.next should returns true", iteratorOrders.hasNext());
											BranchNode<CustomerNodeType,OrderNodeType> order = iteratorOrders.next();
											
											assertEquals("values should be equal", (Integer)resultSetOrders.getInt("ORDERNUMBER"), order.getValue(OrderNodeType.ORDERNUMBER));
											assertEquals("values should be equal", resultSetOrders.getDate("ORDERDATE"), order.getValue(OrderNodeType.ORDERORDERDATE));
											assertEquals("values should be equal", resultSetOrders.getDate("REQUIREDDATE"), order.getValue(OrderNodeType.ORDERREQUIREDDATE));
											assertEquals("values should be equal", resultSetOrders.getDate("SHIPPEDDATE"), order.getValue(OrderNodeType.ORDERSHIPPEDDATE));
											assertEquals("values should be equal", resultSetOrders.getString("STATUS"), order.getValue(OrderNodeType.ORDERSTATUS));
											assertEquals("values should be equal", resultSetOrders.getString("COMMENTS"), order.getValue(OrderNodeType.ORDERCOMMENTS));
					
											Iterator<BranchNode<OrderNodeType, OrderDetailNodeType>> iteratorOrderDetails = order.getUnmodifiableNodeList(OrderNodeType.ORDERDETAILS).iterator();
											preparedStatementOrderDetail2.setInt(1, order.getValue(OrderNodeType.ORDERNUMBER));
											ResultSet resultSetOrderDetails = preparedStatementOrderDetail2.executeQuery();
											try
											{
												while(resultSetOrderDetails.next())
												{
													orderDetailIds2.add(resultSetOrderDetails.getInt("ID"));
													assertTrue("iteratorOrderDetails.next should returns true", iteratorOrderDetails.hasNext());
													BranchNode<OrderNodeType, OrderDetailNodeType> detail = iteratorOrderDetails.next();
													
													assertEquals("values should be equal", (Integer)resultSetOrderDetails.getInt("ID"), detail.getValue(OrderDetailNodeType.ORDERDETAILID));
													assertEquals("values should be equal", (Integer)resultSetOrderDetails.getInt("ORDERLINENUMBER"), detail.getValue(OrderDetailNodeType.ORDERDETAILORDERLINENUMBER));
													assertEquals("values should be equal", (Integer)resultSetOrderDetails.getInt("QUANTITYORDERED"), detail.getValue(OrderDetailNodeType.ORDERDETAILQUANTITYORDERED));
													assertEquals("values should be equal", (Double)resultSetOrderDetails.getDouble("PRICEEACH"), detail.getValue(OrderDetailNodeType.ORDERDETAILPRICEEACH));
													
													String productCode = resultSetOrderDetails.getString("PRODUCTCODE");if(resultSetOrderDetails.wasNull()) {productCode = null;}
													
													if(productCode == null)
													{
														assertNotNull("value should be null", detail.get(OrderDetailNodeType.PRODUCT));
													}
													else
													{
														preparedStatementProduct2.setString(1, productCode);
														ResultSet resultSetProduct = preparedStatementProduct2.executeQuery();
														try
														{
															productIds2.add(resultSetOrderDetails.getString("PRODUCTCODE"));
															assertTrue("resultSetProduct.next should be true", resultSetProduct.next());
															BranchNode<OrderDetailNodeType, ProductNodeType> product = detail.get(OrderDetailNodeType.PRODUCT);
															assertNotNull("value should not be null",product);
															
															assertEquals("values should be equal", resultSetOrderDetails.getString("PRODUCTCODE"), product.getValue(ProductNodeType.PRODUCTCODE));
															assertEquals("values should be equal", resultSetProduct.getString("PRODUCTCODE"), product.getValue(ProductNodeType.PRODUCTCODE));
															assertEquals("values should be equal", resultSetProduct.getString("PRODUCTNAME"), product.getValue(ProductNodeType.PRODUCTNAME));
															assertEquals("values should be equal", resultSetProduct.getString("PRODUCTLINE"), product.getValue(ProductNodeType.PRODUCTLINE));
															assertEquals("values should be equal", resultSetProduct.getString("PRODUCTSCALE"), product.getValue(ProductNodeType.PRODUCTSCALE));
															assertEquals("values should be equal", resultSetProduct.getString("PRODUCTVENDOR"), product.getValue(ProductNodeType.PRODUCTVENDOR));
															assertEquals("values should be equal", resultSetProduct.getString("PRODUCTDESCRIPTION"), product.getValue(ProductNodeType.PRODUCTDESCRIPTION));
															assertEquals("values should be equal", (Integer)resultSetProduct.getInt("QUANTITYINSTOCK"), product.getValue(ProductNodeType.PRODUCTQUANTITYINSTOCK));
															assertEquals("values should be equal", (Double)resultSetProduct.getDouble("BUYPRICE"), product.getValue(ProductNodeType.PRODUCTBUYPRICE));
															assertEquals("values should be equal", (Double)resultSetProduct.getDouble("MSRP"), product.getValue(ProductNodeType.PRODUCTMSRP));
															
															assertFalse("resultSetProduct.next should be false", resultSetProduct.next());
														}
														finally 
														{
															resultSetProduct.close();
														}
													}
												}
												assertFalse("iteratorOrderDetails.next should returns false", iteratorOrderDetails.hasNext());
												
											}
											finally 
											{
												resultSetOrderDetails.close();
											}
										}
										
										assertFalse("iteratorOrders.next should returns false", iteratorOrders.hasNext());
									}
									finally 
									{
										resultSetOrders.close();
									}
								}
								
								assertFalse("iteratorCustomer.next should returns false", iteratorCustomer.hasNext());
							}
							finally 
							{
								resultSetCustomer.close();
							}
						}
						
						assertFalse("iteratorEmployee.next should returns false", iteratorEmployee.hasNext());
					}
					finally 
					{
						resultSetEmployee.close();
					}
				}
				assertFalse("iteratorOffice.next should returns false", iteratorOffice.hasNext());
			}
			finally 
			{
				resultSetOffice.close();
			}
			
			
			assertEquals("values should be equal", officeIds1.size(), officeIds2.size());
			assertEquals("values should be equal", employeeIds1.size(), employeeIds2.size());
			assertEquals("values should be equal", customerIds1.size(), customerIds2.size());
			assertEquals("values should be equal", paymentIds1.size(), paymentIds2.size());
			assertEquals("values should be equal", orderIds1.size(), orderIds2.size());
			assertEquals("values should be equal", orderDetailIds1.size(), orderDetailIds2.size());
			assertEquals("values should be equal", productIds1.size(), productIds2.size());
			
			new File(tempDir + "/" + database + ".mv.db").delete();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
}
