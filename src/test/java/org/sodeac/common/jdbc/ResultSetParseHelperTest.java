/*******************************************************************************
 * Copyright (c) 2019 Sebastian Palarus
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Test;
import org.sodeac.common.jdbc.ResultSetParseHelper.ResultSetParseHelperBuilder;
import org.sodeac.common.jdbc.classicmodelcars.CustomerNodeType;
import org.sodeac.common.jdbc.classicmodelcars.EmployeeNodeType;
import org.sodeac.common.jdbc.classicmodelcars.OfficeNodeType;
import org.sodeac.common.jdbc.classicmodelcars.OfficeTreeModel;
import org.sodeac.common.jdbc.classicmodelcars.PaymentNodeType;
import org.sodeac.common.jdbc.classicmodelcars.ProductNodeType;
import org.sodeac.common.jdbc.classicmodelcars.OfficeTreeModel.OfficeResultSetNodeType;
import org.sodeac.common.jdbc.classicmodelcars.OrderDetailNodeType;
import org.sodeac.common.jdbc.classicmodelcars.OrderNodeType;
import org.sodeac.common.misc.CloseableCollector;
import org.sodeac.common.misc.ResourceLoader;
import org.sodeac.common.typedtree.BranchNode;
import org.sodeac.common.typedtree.ModelRegistry;
import org.sodeac.common.typedtree.TypedTreeMetaModel.RootBranchNode;

public class ResultSetParseHelperTest
{
	@Test
	public void officeTest() throws Exception
	{
		try(CloseableCollector closeableCollector = CloseableCollector.newInstance())
		{
			DriverManager.registerDriver(org.h2.Driver.class.newInstance());
			
			String tempDir = System.getProperty("java.io.tmpdir");
			String database = "demo";
			
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
			
			// run query
			
			List<String> rootIdList = new ArrayList<String>();
			AtomicInteger rootIdCount = new AtomicInteger();
			List<String> rootIdList2 = new ArrayList<String>();
			AtomicInteger rootIdCount2 = new AtomicInteger();
			AtomicInteger employeeWithoutCustomerCount = new AtomicInteger();
			
			RootBranchNode<OfficeTreeModel,OfficeResultSetNodeType> resultSetTree = ModelRegistry.getTypedTreeMetaModel(OfficeTreeModel.class).createRootNode(OfficeTreeModel.resultSet);
			
			String officeSQL = ResourceLoader.loadPackageFileAsString("office.sql", OfficeTreeModel.class);
			Connection conn = closeableCollector.register(DriverManager.getConnection("jdbc:h2:" + tempDir + "/" + database, "", ""));
			PreparedStatement preparedStatement = closeableCollector.register(conn.prepareStatement(officeSQL, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY));
			ResultSetParseHelper parseHelper = ResultSetParseHelperBuilder.newBuilder
			(
				OfficeNodeType.OFFICECODE.getNodeName(), String.class, OfficeResultSetNodeType.officeList.getBranchNodeClass(),OfficeTreeModel.resultSet.getBranchNodeClass(),
					
				c -> c.getParentObject().create(OfficeResultSetNodeType.officeList)
					.setValue(OfficeNodeType.OFFICECODE,c.getId())
					.setValue(OfficeNodeType.OFFICECITY,c.getResultSet().getString(OfficeNodeType.OFFICECITY.getNodeName()))
					.setValue(OfficeNodeType.OFFICECOUNTRY,c.getResultSet().getString(OfficeNodeType.OFFICECOUNTRY.getNodeName()))
					.setValue(OfficeNodeType.OFFICEADDRESSLINE1,c.getResultSet().getString(OfficeNodeType.OFFICEADDRESSLINE1.getNodeName()))
					.setValue(OfficeNodeType.OFFICEADDRESSLINE2,c.getResultSet().getString(OfficeNodeType.OFFICEADDRESSLINE2.getNodeName()))
					.setValue(OfficeNodeType.OFFICEPOSTALCODE,c.getResultSet().getString(OfficeNodeType.OFFICEPOSTALCODE.getNodeName()))
					.setValue(OfficeNodeType.OFFICESTATE,c.getResultSet().getString(OfficeNodeType.OFFICESTATE.getNodeName()))
					.setValue(OfficeNodeType.OFFICETERRITORY,c.getResultSet().getString(OfficeNodeType.OFFICETERRITORY.getNodeName()))
					.setValue(OfficeNodeType.OFFICEPHONE,c.getResultSet().getString(OfficeNodeType.OFFICEPHONE.getNodeName())),
				r -> rootIdList.add(r.getValue(OfficeNodeType.OFFICECODE)),
				l -> rootIdCount.addAndGet(l.size())
			)
				.subParser(EmployeeNodeType.EMPLOYEENUMBER.getNodeName(), Integer.class, OfficeNodeType.EMPLOYEES.getBranchNodeClass(),
							
					c -> c.getParentObject().create(OfficeNodeType.EMPLOYEES)
						.setValue(EmployeeNodeType.EMPLOYEENUMBER,c.getId())
						.setValue(EmployeeNodeType.EMPLOYEEEXTENSION,c.getResultSet().getString(EmployeeNodeType.EMPLOYEEEXTENSION.getNodeName()))
						.setValue(EmployeeNodeType.EMPLOYEEJOBTITLE,c.getResultSet().getString(EmployeeNodeType.EMPLOYEEJOBTITLE.getNodeName()))
						.setValue(EmployeeNodeType.EMPLOYEEFIRSTNAME,c.getResultSet().getString(EmployeeNodeType.EMPLOYEEFIRSTNAME.getNodeName()))
						.setValue(EmployeeNodeType.EMPLOYEELASTNAME,c.getResultSet().getString(EmployeeNodeType.EMPLOYEELASTNAME.getNodeName()))
						.setValue(EmployeeNodeType.EMPLOYEEEMAIL,c.getResultSet().getString(EmployeeNodeType.EMPLOYEEEMAIL.getNodeName()))
						.setValue(EmployeeNodeType.EMPLOYEEREPORTSTO,c.getResultSet().getInt(EmployeeNodeType.EMPLOYEEREPORTSTO.getNodeName()))
				)
					.subParser(CustomerNodeType.CUSTOMERNUMBER.getNodeName(), Integer.class, EmployeeNodeType.CUSTOMERS.getBranchNodeClass(),
							
						c -> c.getParentObject().create(EmployeeNodeType.CUSTOMERS)
							.setValue(CustomerNodeType.CUSTOMERNUMBER, c.getId())
							.setValue(CustomerNodeType.CUSTOMERNAME,c.getResultSet().getString(CustomerNodeType.CUSTOMERNAME.getNodeName()))
							.setValue(CustomerNodeType.CUSTOMERCONTACTLASTNAME,c.getResultSet().getString(CustomerNodeType.CUSTOMERCONTACTLASTNAME.getNodeName()))
							.setValue(CustomerNodeType.CUSTOMERCONTACTFIRSTNAME,c.getResultSet().getString(CustomerNodeType.CUSTOMERCONTACTFIRSTNAME.getNodeName()))
							.setValue(CustomerNodeType.CUSTOMERADDRESSLINE1,c.getResultSet().getString(CustomerNodeType.CUSTOMERADDRESSLINE1.getNodeName()))
							.setValue(CustomerNodeType.CUSTOMERADDRESSLINE2,c.getResultSet().getString(CustomerNodeType.CUSTOMERADDRESSLINE2.getNodeName()))
							.setValue(CustomerNodeType.CUSTOMERCITY,c.getResultSet().getString(CustomerNodeType.CUSTOMERCITY.getNodeName()))
							.setValue(CustomerNodeType.CUSTOMERCOUNTRY,c.getResultSet().getString(CustomerNodeType.CUSTOMERCOUNTRY.getNodeName()))
							.setValue(CustomerNodeType.CUSTOMERSTATE,c.getResultSet().getString(CustomerNodeType.CUSTOMERSTATE.getNodeName()))
							.setValue(CustomerNodeType.CUSTOMERPOSTALCODE,c.getResultSet().getString(CustomerNodeType.CUSTOMERPOSTALCODE.getNodeName()))
							.setValue(CustomerNodeType.CUSTOMERPHONE,c.getResultSet().getString(CustomerNodeType.CUSTOMERPHONE.getNodeName()))
							.setValue(CustomerNodeType.CUSTOMERCREDITLIMIT,c.getResultSet().getInt(CustomerNodeType.CUSTOMERCREDITLIMIT.getNodeName()))
					).onNullRecord( c -> {employeeWithoutCustomerCount.incrementAndGet(); return null;} )
						.subParser(PaymentNodeType.PAYMENTID.getNodeName(), Integer.class, CustomerNodeType.PAYMENTS.getBranchNodeClass(),
								
							c -> c.getParentObject().create(CustomerNodeType.PAYMENTS)
								.setValue(PaymentNodeType.PAYMENTID, c.getId())
								.setValue(PaymentNodeType.PAYMENTCHECKNUMBER,c.getResultSet().getString(PaymentNodeType.PAYMENTCHECKNUMBER.getNodeName()))
								.setValue(PaymentNodeType.PAYMENTAMOUNT,c.getResultSet().getDouble(PaymentNodeType.PAYMENTAMOUNT.getNodeName()))
								.setValue(PaymentNodeType.PAYMENTDATE,c.getResultSet().getDate(PaymentNodeType.PAYMENTDATE.getNodeName()))
						).build()
						.subParser(OrderNodeType.ORDERNUMBER.getNodeName(), Integer.class, CustomerNodeType.ORDERS.getBranchNodeClass(),
								
							c -> c.getParentObject().create(CustomerNodeType.ORDERS)
								.setValue(OrderNodeType.ORDERNUMBER, c.getId())
								.setValue(OrderNodeType.ORDERREQUIREDDATE,c.getResultSet().getDate(OrderNodeType.ORDERREQUIREDDATE.getNodeName()))
								.setValue(OrderNodeType.ORDERORDERDATE,c.getResultSet().getDate(OrderNodeType.ORDERORDERDATE.getNodeName()))
								.setValue(OrderNodeType.ORDERSHIPPEDDATE,c.getResultSet().getDate(OrderNodeType.ORDERSHIPPEDDATE.getNodeName()))
								.setValue(OrderNodeType.ORDERCOMMENTS,c.getResultSet().getString(OrderNodeType.ORDERCOMMENTS.getNodeName()))
								.setValue(OrderNodeType.ORDERSTATUS,c.getResultSet().getString(OrderNodeType.ORDERSTATUS.getNodeName()))
						)
							.subParser(OrderDetailNodeType.ORDERDETAILID.getNodeName(), Integer.class, OrderNodeType.ORDERDETAILS.getBranchNodeClass(), 
								c -> c.getParentObject().create(OrderNodeType.ORDERDETAILS)
									.setValue(OrderDetailNodeType.ORDERDETAILID, c.getId())
									.setValue(OrderDetailNodeType.ORDERDETAILORDERLINENUMBER,c.getResultSet().getInt(OrderDetailNodeType.ORDERDETAILORDERLINENUMBER.getNodeName()))
									.setValue(OrderDetailNodeType.ORDERDETAILPRICEEACH,c.getResultSet().getDouble(OrderDetailNodeType.ORDERDETAILPRICEEACH.getNodeName()))
									.setValue(OrderDetailNodeType.ORDERDETAILQUANTITYORDERED,c.getResultSet().getInt(OrderDetailNodeType.ORDERDETAILQUANTITYORDERED.getNodeName()))
							)
								.subParser(ProductNodeType.PRODUCTCODE.getNodeName(), String.class, OrderDetailNodeType.PRODUCT.getBranchNodeClass(), 
									c -> c.getParentObject().create(OrderDetailNodeType.PRODUCT)
										.setValue(ProductNodeType.PRODUCTCODE, c.getId())
										.setValue(ProductNodeType.PRODUCTNAME,c.getResultSet().getString(ProductNodeType.PRODUCTNAME.getNodeName()))
										.setValue(ProductNodeType.PRODUCTLINE,c.getResultSet().getString(ProductNodeType.PRODUCTLINE.getNodeName()))
										.setValue(ProductNodeType.PRODUCTSCALE,c.getResultSet().getString(ProductNodeType.PRODUCTSCALE.getNodeName()))
										.setValue(ProductNodeType.PRODUCTVENDOR,c.getResultSet().getString(ProductNodeType.PRODUCTVENDOR.getNodeName()))
										.setValue(ProductNodeType.PRODUCTDESCRIPTION,c.getResultSet().getString(ProductNodeType.PRODUCTDESCRIPTION.getNodeName()))
										.setValue(ProductNodeType.PRODUCTQUANTITYINSTOCK,c.getResultSet().getInt(ProductNodeType.PRODUCTQUANTITYINSTOCK.getNodeName()))
										.setValue(ProductNodeType.PRODUCTBUYPRICE,c.getResultSet().getDouble(ProductNodeType.PRODUCTBUYPRICE.getNodeName()))
										.setValue(ProductNodeType.PRODUCTMSRP,c.getResultSet().getDouble(ProductNodeType.PRODUCTMSRP.getNodeName()))
								)
			.newParsePhase
			(
				"Phase2", 
				OfficeNodeType.OFFICECODE.getNodeName(), 
				String.class, 
				String.class,
				OfficeTreeModel.resultSet.getBranchNodeClass(), 
				c -> c.getId(),
				r -> rootIdList2.add(r), 
				l -> rootIdCount2.addAndGet(l.size())
			)
			.buildParser();
				
			parseHelper.parse(preparedStatement,resultSetTree,3);
			parseHelper.close();
	
			closeableCollector.close(preparedStatement);
			
			// test result
			
			assertEquals("values should be equal", rootIdList.size(), rootIdCount.get());
			assertEquals("values should be equal", rootIdList, rootIdList2);
			assertEquals("values should be equal", rootIdCount.get(),rootIdCount2.get());
			
			PreparedStatement preparedStatementOffice = closeableCollector.register(conn.prepareStatement("SELECT * FROM OFFICES ORDER BY OFFICECODE"));
			PreparedStatement preparedStatementEmployee = closeableCollector.register(conn.prepareStatement("SELECT * FROM EMPLOYEES WHERE OFFICECODE = ? ORDER BY EMPLOYEENUMBER"));
			PreparedStatement preparedStatementCustomers = closeableCollector.register(conn.prepareStatement("SELECT * FROM CUSTOMERS WHERE SALESREPEMPLOYEENUMBER = ? ORDER BY CUSTOMERNUMBER"));
			PreparedStatement preparedStatementPayments = closeableCollector.register(conn.prepareStatement("SELECT * FROM PAYMENTS WHERE CUSTOMERNUMBER = ? ORDER BY ID"));
			PreparedStatement preparedStatementOrders = closeableCollector.register(conn.prepareStatement("SELECT * FROM ORDERS WHERE CUSTOMERNUMBER = ? ORDER BY ORDERNUMBER"));
			PreparedStatement preparedStatementOrderDetails = closeableCollector.register(conn.prepareStatement("SELECT * FROM ORDERDETAILS WHERE ORDERNUMBER = ? ORDER BY ORDERLINENUMBER"));
			PreparedStatement preparedStatementProduct = closeableCollector.register(conn.prepareStatement("SELECT * FROM PRODUCTS WHERE PRODUCTCODE = ?"));
			
			ResultSet resultSetOffice = preparedStatementOffice.executeQuery();
			try
			{
				Iterator<BranchNode<OfficeResultSetNodeType, OfficeNodeType>> iteratorOffice = resultSetTree.getUnmodifiableNodeList(OfficeResultSetNodeType.officeList).iterator();
				
				while(resultSetOffice.next())
				{
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
					
					assertTrue("list should contains value",rootIdList.contains(officeNode.getValue(OfficeNodeType.OFFICECODE)));
					
					preparedStatementEmployee.setString(1, officeNode.getValue(OfficeNodeType.OFFICECODE));
					ResultSet resultSetEmployee = preparedStatementEmployee.executeQuery();
					try
					{
						Iterator<BranchNode<OfficeNodeType,EmployeeNodeType>> iteratorEmployee = officeNode.getUnmodifiableNodeList(OfficeNodeType.EMPLOYEES).iterator();
						while(resultSetEmployee.next())
						{
							
							assertTrue("iteratorEmployee.next should returns true", iteratorEmployee.hasNext());
							BranchNode<OfficeNodeType,EmployeeNodeType> employee = iteratorEmployee.next();
							
							assertEquals("values should be equal", (Integer)resultSetEmployee.getInt("EMPLOYEENUMBER"), employee.getValue(EmployeeNodeType.EMPLOYEENUMBER));
							assertEquals("values should be equal", resultSetEmployee.getString("LASTNAME"), employee.getValue(EmployeeNodeType.EMPLOYEELASTNAME));
							assertEquals("values should be equal", resultSetEmployee.getString("FIRSTNAME"), employee.getValue(EmployeeNodeType.EMPLOYEEFIRSTNAME));
							assertEquals("values should be equal", resultSetEmployee.getString("EXTENSION"), employee.getValue(EmployeeNodeType.EMPLOYEEEXTENSION));
							assertEquals("values should be equal", resultSetEmployee.getString("EMAIL"), employee.getValue(EmployeeNodeType.EMPLOYEEEMAIL));
							assertEquals("values should be equal", resultSetEmployee.getString("JOBTITLE"), employee.getValue(EmployeeNodeType.EMPLOYEEJOBTITLE));
							assertEquals("values should be equal", (Integer)resultSetEmployee.getInt("REPORTSTO"), employee.getValue(EmployeeNodeType.EMPLOYEEREPORTSTO));
							
							preparedStatementCustomers.setInt(1,employee.getValue(EmployeeNodeType.EMPLOYEENUMBER));
							
							ResultSet resultSetCustomer = preparedStatementCustomers.executeQuery();
							try
							{
								if(employee.getUnmodifiableNodeList(EmployeeNodeType.CUSTOMERS).isEmpty())
								{
									employeeWithoutCustomerCount.decrementAndGet();
								}
								Iterator<BranchNode<EmployeeNodeType,CustomerNodeType>> iteratorCustomer = employee.getUnmodifiableNodeList(EmployeeNodeType.CUSTOMERS).iterator();
								
								while(resultSetCustomer.next())
								{
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
									preparedStatementPayments.setInt(1, customer.getValue(CustomerNodeType.CUSTOMERNUMBER));
									ResultSet resultSetPayment = preparedStatementPayments.executeQuery();
									try
									{
										while(resultSetPayment.next())
										{
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
									preparedStatementOrders.setInt(1, customer.getValue(CustomerNodeType.CUSTOMERNUMBER));
									ResultSet resultSetOrders = preparedStatementOrders.executeQuery();
									try
									{
										
										while(resultSetOrders.next())
										{
											assertTrue("iteratorOrders.next should returns true", iteratorOrders.hasNext());
											BranchNode<CustomerNodeType,OrderNodeType> order = iteratorOrders.next();
											
											assertEquals("values should be equal", (Integer)resultSetOrders.getInt("ORDERNUMBER"), order.getValue(OrderNodeType.ORDERNUMBER));
											assertEquals("values should be equal", resultSetOrders.getDate("ORDERDATE"), order.getValue(OrderNodeType.ORDERORDERDATE));
											assertEquals("values should be equal", resultSetOrders.getDate("REQUIREDDATE"), order.getValue(OrderNodeType.ORDERREQUIREDDATE));
											assertEquals("values should be equal", resultSetOrders.getDate("SHIPPEDDATE"), order.getValue(OrderNodeType.ORDERSHIPPEDDATE));
											assertEquals("values should be equal", resultSetOrders.getString("STATUS"), order.getValue(OrderNodeType.ORDERSTATUS));
											assertEquals("values should be equal", resultSetOrders.getString("COMMENTS"), order.getValue(OrderNodeType.ORDERCOMMENTS));
					
											Iterator<BranchNode<OrderNodeType, OrderDetailNodeType>> iteratorOrderDetails = order.getUnmodifiableNodeList(OrderNodeType.ORDERDETAILS).iterator();
											preparedStatementOrderDetails.setInt(1, order.getValue(OrderNodeType.ORDERNUMBER));
											ResultSet resultSetOrderDetails = preparedStatementOrderDetails.executeQuery();
											try
											{
												while(resultSetOrderDetails.next())
												{
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
														preparedStatementProduct.setString(1, productCode);
														ResultSet resultSetProduct = preparedStatementProduct.executeQuery();
														try
														{
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
			
			assertEquals("counter should be correct", 0, employeeWithoutCustomerCount.get());
			
			closeableCollector.close(preparedStatementOffice);
			closeableCollector.close(preparedStatementEmployee);
			closeableCollector.close(preparedStatementCustomers);
			closeableCollector.close(preparedStatementPayments);
			closeableCollector.close(preparedStatementOrders);
			closeableCollector.close(preparedStatementOrderDetails);
			closeableCollector.close(preparedStatementProduct);
			
			
			closeableCollector.close(conn);
			new File(tempDir + "/" + database + ".mv.db").delete();
		}
	}
	
}
