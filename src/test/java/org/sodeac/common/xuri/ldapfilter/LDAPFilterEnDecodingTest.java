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
package org.sodeac.common.xuri.ldapfilter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sodeac.common.xuri.ldapfilter.Attribute;
import org.sodeac.common.xuri.ldapfilter.AttributeLinker;
import org.sodeac.common.xuri.ldapfilter.ComparativeOperator;
import org.sodeac.common.xuri.ldapfilter.IFilterItem;
import org.sodeac.common.xuri.ldapfilter.LDAPFilterDecodingHandler;
import org.sodeac.common.xuri.ldapfilter.LDAPFilterEncodingHandler;
import org.sodeac.common.xuri.ldapfilter.LogicalOperator;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LDAPFilterEnDecodingTest
{
	@Test
	public void test001SimpleEqualAttribute()
	{
		Attribute attribute = new Attribute().setName("a").setOperator(ComparativeOperator.EQUAL).setValue("b");
		String filterExpression = LDAPFilterEncodingHandler.getInstance().encodeToString(attribute);
		assertEquals("ldapfilterexpression should be correct","(a=b)", filterExpression);
		
		IFilterItem filterItem = LDAPFilterDecodingHandler.getInstance().decodeFromString(filterExpression);
		assertNotNull("parsed ldapfilter should not be null", filterItem);
		assertTrue("parsed ldapfilter should be correct filteritem type", filterItem instanceof Attribute);
		
		attribute = (Attribute)filterItem;
		assertEquals("attributename should be correct","a", attribute.getName());
		assertEquals("attributeoperator should be correct",ComparativeOperator.EQUAL, attribute.getOperator());
		assertEquals("attributevalue should be correct","b", attribute.getValue());
		assertEquals("invertflag should be correct", false,attribute.isInvert());
	}
	
	@Test
	public void test002SimpleNotEqualAttribute()
	{
		Attribute attribute = new Attribute().setName("a").setOperator(ComparativeOperator.EQUAL).setValue("b").setInvert(true);
		String filterExpression = LDAPFilterEncodingHandler.getInstance().encodeToString(attribute);
		assertEquals("ldapfilterexpression should be correct","(!(a=b))", filterExpression);
		
		IFilterItem filterItem = LDAPFilterDecodingHandler.getInstance().decodeFromString(filterExpression);
		assertNotNull("parsed ldapfilter should not be null", filterItem);
		assertTrue("parsed ldapfilter should be correct filteritem type", filterItem instanceof Attribute);
		
		attribute = (Attribute)filterItem;
		assertEquals("attributename should be correct","a", attribute.getName());
		assertEquals("attributeoperator should be correct",ComparativeOperator.EQUAL, attribute.getOperator());
		assertEquals("attributevalue should be correct","b", attribute.getValue());
		assertEquals("invertflag should be correct", true,attribute.isInvert());
	}
	
	@Test
	public void test003SimpleLessAttribute()
	{
		Attribute attribute = new Attribute().setName("a").setOperator(ComparativeOperator.LESS).setValue("b");
		String filterExpression = LDAPFilterEncodingHandler.getInstance().encodeToString(attribute);
		assertEquals("ldapfilterexpression should be correct","(a<=b)", filterExpression);
		
		IFilterItem filterItem = LDAPFilterDecodingHandler.getInstance().decodeFromString(filterExpression);
		assertNotNull("parsed ldapfilter should not be null", filterItem);
		assertTrue("parsed ldapfilter should be correct filteritem type", filterItem instanceof Attribute);
		
		attribute = (Attribute)filterItem;
		assertEquals("attributename should be correct","a", attribute.getName());
		assertEquals("attributeoperator should be correct",ComparativeOperator.LESS, attribute.getOperator());
		assertEquals("attributevalue should be correct","b", attribute.getValue());
		assertEquals("invertflag should be correct", false,attribute.isInvert());
	}
	
	@Test
	public void test004SimpleNotLessAttribute()
	{
		Attribute attribute = new Attribute().setName("a").setOperator(ComparativeOperator.LESS).setValue("b").setInvert(true);
		String filterExpression = LDAPFilterEncodingHandler.getInstance().encodeToString(attribute);
		assertEquals("ldapfilterexpression should be correct","(!(a<=b))", filterExpression);
		
		IFilterItem filterItem = LDAPFilterDecodingHandler.getInstance().decodeFromString(filterExpression);
		assertNotNull("parsed ldapfilter should not be null", filterItem);
		assertTrue("parsed ldapfilter should be correct filteritem type", filterItem instanceof Attribute);
		
		attribute = (Attribute)filterItem;
		assertEquals("attributename should be correct","a", attribute.getName());
		assertEquals("attributeoperator should be correct",ComparativeOperator.LESS, attribute.getOperator());
		assertEquals("attributevalue should be correct","b", attribute.getValue());
		assertEquals("invertflag should be correct", true,attribute.isInvert());
	}
	
	@Test
	public void test005SimpleLessAttribute()
	{
		Attribute attribute = new Attribute().setName("a").setOperator(ComparativeOperator.GREATER).setValue("b");
		String filterExpression = LDAPFilterEncodingHandler.getInstance().encodeToString(attribute);
		assertEquals("ldapfilterexpression should be correct","(a>=b)", filterExpression);
		
		IFilterItem filterItem = LDAPFilterDecodingHandler.getInstance().decodeFromString(filterExpression);
		assertNotNull("parsed ldapfilter should not be null", filterItem);
		assertTrue("parsed ldapfilter should be correct filteritem type", filterItem instanceof Attribute);
		
		attribute = (Attribute)filterItem;
		assertEquals("attributename should be correct","a", attribute.getName());
		assertEquals("attributeoperator should be correct",ComparativeOperator.GREATER, attribute.getOperator());
		assertEquals("attributevalue should be correct","b", attribute.getValue());
		assertEquals("invertflag should be correct", false,attribute.isInvert());
	}
	
	@Test
	public void test006SimpleNotLessAttribute()
	{
		Attribute attribute = new Attribute().setName("a").setOperator(ComparativeOperator.GREATER).setValue("b").setInvert(true);
		String filterExpression = LDAPFilterEncodingHandler.getInstance().encodeToString(attribute);
		assertEquals("ldapfilterexpression should be correct","(!(a>=b))", filterExpression);
		
		IFilterItem filterItem = LDAPFilterDecodingHandler.getInstance().decodeFromString(filterExpression);
		assertNotNull("parsed ldapfilter should not be null", filterItem);
		assertTrue("parsed ldapfilter should be correct filteritem type", filterItem instanceof Attribute);
		
		attribute = (Attribute)filterItem;
		assertEquals("attributename should be correct","a", attribute.getName());
		assertEquals("attributeoperator should be correct",ComparativeOperator.GREATER, attribute.getOperator());
		assertEquals("attributevalue should be correct","b", attribute.getValue());
		assertEquals("invertflag should be correct", true,attribute.isInvert());
	}
	
	@Test
	public void test007SimpleLessAttribute()
	{
		Attribute attribute = new Attribute().setName("a").setOperator(ComparativeOperator.APPROX).setValue("b");
		String filterExpression = LDAPFilterEncodingHandler.getInstance().encodeToString(attribute);
		assertEquals("ldapfilterexpression should be correct","(a~=b)", filterExpression);
		
		IFilterItem filterItem = LDAPFilterDecodingHandler.getInstance().decodeFromString(filterExpression);
		assertNotNull("parsed ldapfilter should not be null", filterItem);
		assertTrue("parsed ldapfilter should be correct filteritem type", filterItem instanceof Attribute);
		
		attribute = (Attribute)filterItem;
		assertEquals("attributename should be correct","a", attribute.getName());
		assertEquals("attributeoperator should be correct",ComparativeOperator.APPROX, attribute.getOperator());
		assertEquals("attributevalue should be correct","b", attribute.getValue());
		assertEquals("invertflag should be correct", false,attribute.isInvert());
	}
	
	@Test
	public void test008SimpleNotLessAttribute()
	{
		Attribute attribute = new Attribute().setName("a").setOperator(ComparativeOperator.APPROX).setValue("b").setInvert(true);
		String filterExpression = LDAPFilterEncodingHandler.getInstance().encodeToString(attribute);
		assertEquals("ldapfilterexpression should be correct","(!(a~=b))", filterExpression);
		
		IFilterItem filterItem = LDAPFilterDecodingHandler.getInstance().decodeFromString(filterExpression);
		assertNotNull("parsed ldapfilter should not be null", filterItem);
		assertTrue("parsed ldapfilter should be correct filteritem type", filterItem instanceof Attribute);
		
		attribute = (Attribute)filterItem;
		assertEquals("attributename should be correct","a", attribute.getName());
		assertEquals("attributeoperator should be correct",ComparativeOperator.APPROX, attribute.getOperator());
		assertEquals("attributevalue should be correct","b", attribute.getValue());
		assertEquals("invertflag should be correct", true,attribute.isInvert());
	}
	
	@Test
	public void test020SimpleAndLinker()
	{
		Attribute attr1 = new Attribute().setName("a").setOperator(ComparativeOperator.EQUAL).setValue("b");
		Attribute attr2 = new Attribute().setName("x").setOperator(ComparativeOperator.APPROX).setValue("z");
		AttributeLinker attributeLinker = new AttributeLinker().setOperator(LogicalOperator.AND);
		attributeLinker.addItem(attr1);
		attributeLinker.addItem(attr2);
		
		String filterExpression = LDAPFilterEncodingHandler.getInstance().encodeToString(attributeLinker);
		assertEquals("ldapfilterexpression should be correct","(&(a=b)(x~=z))", filterExpression);
		
		IFilterItem filterItem = LDAPFilterDecodingHandler.getInstance().decodeFromString(filterExpression);
		assertNotNull("parsed ldapfilter should not be null", filterItem);
		assertTrue("parsed ldapfilter should be correct filteritem type", filterItem instanceof AttributeLinker);
		
		attributeLinker = (AttributeLinker)filterItem;
		assertEquals("parsed linker should contains correct operator", LogicalOperator.AND, attributeLinker.getOperator());
		assertEquals("invertflag should be correct", false,attributeLinker.isInvert());
		
		List<IFilterItem> childs1 = attributeLinker.getLinkedItemList();
		assertEquals("parsed linker should contains correct linked item size", 2, childs1.size());
		
		for(int i = 0; i < childs1.size(); i++)
		{
			filterItem = childs1.get(i);
			assertTrue("parsed ldapfilter should be correct filteritem type", filterItem instanceof Attribute);
			
			if(i == 0)
			{
				attr1 = (Attribute)filterItem;
				assertEquals("attributename should be correct","a", attr1.getName());
				assertEquals("attributeoperator should be correct",ComparativeOperator.EQUAL, attr1.getOperator());
				assertEquals("attributevalue should be correct","b", attr1.getValue());
				assertEquals("invertflag should be correct", false,attr1.isInvert());
			}
			
			if(i == 1)
			{
				attr2 = (Attribute)filterItem;
				assertEquals("attributename should be correct","x", attr2.getName());
				assertEquals("attributeoperator should be correct",ComparativeOperator.APPROX, attr2.getOperator());
				assertEquals("attributevalue should be correct","z", attr2.getValue());
				assertEquals("invertflag should be correct", false,attr2.isInvert());
			}
		}
	}
	
	@Test
	public void test021SimpleAndLinkerNot1()
	{
		Attribute attr1 = new Attribute().setName("a").setOperator(ComparativeOperator.EQUAL).setValue("b").setInvert(true);
		Attribute attr2 = new Attribute().setName("x").setOperator(ComparativeOperator.APPROX).setValue("z");
		AttributeLinker attributeLinker = new AttributeLinker().setOperator(LogicalOperator.AND);
		attributeLinker.addItem(attr1);
		attributeLinker.addItem(attr2);
		
		String filterExpression = LDAPFilterEncodingHandler.getInstance().encodeToString(attributeLinker);
		assertEquals("ldapfilterexpression should be correct","(&(!(a=b))(x~=z))", filterExpression);
		
		IFilterItem filterItem = LDAPFilterDecodingHandler.getInstance().decodeFromString(filterExpression);
		assertNotNull("parsed ldapfilter should not be null", filterItem);
		assertTrue("parsed ldapfilter should be correct filteritem type", filterItem instanceof AttributeLinker);
		
		attributeLinker = (AttributeLinker)filterItem;
		assertEquals("parsed linker should contains correct operator", LogicalOperator.AND, attributeLinker.getOperator());
		assertEquals("invertflag should be correct", false,attributeLinker.isInvert());
		
		List<IFilterItem> childs1 = attributeLinker.getLinkedItemList();
		assertEquals("parsed linker should contains correct linked item size", 2, childs1.size());
		
		for(int i = 0; i < childs1.size(); i++)
		{
			filterItem = childs1.get(i);
			assertTrue("parsed ldapfilter should be correct filteritem type", filterItem instanceof Attribute);
			
			if(i == 0)
			{
				attr1 = (Attribute)filterItem;
				assertEquals("attributename should be correct","a", attr1.getName());
				assertEquals("attributeoperator should be correct",ComparativeOperator.EQUAL, attr1.getOperator());
				assertEquals("attributevalue should be correct","b", attr1.getValue());
				assertEquals("invertflag should be correct", true,attr1.isInvert());
			}
			
			if(i == 1)
			{
				attr2 = (Attribute)filterItem;
				assertEquals("attributename should be correct","x", attr2.getName());
				assertEquals("attributeoperator should be correct",ComparativeOperator.APPROX, attr2.getOperator());
				assertEquals("attributevalue should be correct","z", attr2.getValue());
				assertEquals("invertflag should be correct", false,attr2.isInvert());
			}
		}
	}
	
	@Test
	public void test022SimpleAndLinkerNot2()
	{
		Attribute attr1 = new Attribute().setName("a").setOperator(ComparativeOperator.EQUAL).setValue("b");
		Attribute attr2 = new Attribute().setName("x").setOperator(ComparativeOperator.APPROX).setValue("z").setInvert(true);
		AttributeLinker attributeLinker = new AttributeLinker().setOperator(LogicalOperator.AND);
		attributeLinker.addItem(attr1);
		attributeLinker.addItem(attr2);
		
		String filterExpression = LDAPFilterEncodingHandler.getInstance().encodeToString(attributeLinker);
		assertEquals("ldapfilterexpression should be correct","(&(a=b)(!(x~=z)))", filterExpression);
		
		IFilterItem filterItem = LDAPFilterDecodingHandler.getInstance().decodeFromString(filterExpression);
		assertNotNull("parsed ldapfilter should not be null", filterItem);
		assertTrue("parsed ldapfilter should be correct filteritem type", filterItem instanceof AttributeLinker);
		
		attributeLinker = (AttributeLinker)filterItem;
		assertEquals("parsed linker should contains correct operator", LogicalOperator.AND, attributeLinker.getOperator());
		assertEquals("invertflag should be correct", false,attributeLinker.isInvert());
		
		List<IFilterItem> childs1 = attributeLinker.getLinkedItemList();
		assertEquals("parsed linker should contains correct linked item size", 2, childs1.size());
		
		for(int i = 0; i < childs1.size(); i++)
		{
			filterItem = childs1.get(i);
			assertTrue("parsed ldapfilter should be correct filteritem type", filterItem instanceof Attribute);
			
			if(i == 0)
			{
				attr1 = (Attribute)filterItem;
				assertEquals("attributename should be correct","a", attr1.getName());
				assertEquals("attributeoperator should be correct",ComparativeOperator.EQUAL, attr1.getOperator());
				assertEquals("attributevalue should be correct","b", attr1.getValue());
				assertEquals("invertflag should be correct", false,attr1.isInvert());
			}
			
			if(i == 1)
			{
				attr2 = (Attribute)filterItem;
				assertEquals("attributename should be correct","x", attr2.getName());
				assertEquals("attributeoperator should be correct",ComparativeOperator.APPROX, attr2.getOperator());
				assertEquals("attributevalue should be correct","z", attr2.getValue());
				assertEquals("invertflag should be correct", true,attr2.isInvert());
			}
		}
	}
	
	@Test
	public void test023SimpleAndLinkerNot3()
	{
		Attribute attr1 = new Attribute().setName("a").setOperator(ComparativeOperator.EQUAL).setValue("b").setInvert(true);
		Attribute attr2 = new Attribute().setName("x").setOperator(ComparativeOperator.APPROX).setValue("z").setInvert(true);
		AttributeLinker attributeLinker = new AttributeLinker().setOperator(LogicalOperator.AND);
		attributeLinker.addItem(attr1);
		attributeLinker.addItem(attr2);
		
		String filterExpression = LDAPFilterEncodingHandler.getInstance().encodeToString(attributeLinker);
		assertEquals("ldapfilterexpression should be correct","(&(!(a=b))(!(x~=z)))", filterExpression);
		
		IFilterItem filterItem = LDAPFilterDecodingHandler.getInstance().decodeFromString(filterExpression);
		assertNotNull("parsed ldapfilter should not be null", filterItem);
		assertTrue("parsed ldapfilter should be correct filteritem type", filterItem instanceof AttributeLinker);
		
		attributeLinker = (AttributeLinker)filterItem;
		assertEquals("parsed linker should contains correct operator", LogicalOperator.AND, attributeLinker.getOperator());
		assertEquals("invertflag should be correct", false,attributeLinker.isInvert());
		
		List<IFilterItem> childs1 = attributeLinker.getLinkedItemList();
		assertEquals("parsed linker should contains correct linked item size", 2, childs1.size());
		
		for(int i = 0; i < childs1.size(); i++)
		{
			filterItem = childs1.get(i);
			assertTrue("parsed ldapfilter should be correct filteritem type", filterItem instanceof Attribute);
			
			if(i == 0)
			{
				attr1 = (Attribute)filterItem;
				assertEquals("attributename should be correct","a", attr1.getName());
				assertEquals("attributeoperator should be correct",ComparativeOperator.EQUAL, attr1.getOperator());
				assertEquals("attributevalue should be correct","b", attr1.getValue());
				assertEquals("invertflag should be correct", true,attr1.isInvert());
			}
			
			if(i == 1)
			{
				attr2 = (Attribute)filterItem;
				assertEquals("attributename should be correct","x", attr2.getName());
				assertEquals("attributeoperator should be correct",ComparativeOperator.APPROX, attr2.getOperator());
				assertEquals("attributevalue should be correct","z", attr2.getValue());
				assertEquals("invertflag should be correct", true,attr2.isInvert());
			}
		}
	}
	
	@Test
	public void test024SimpleAndLinkerNot4()
	{
		Attribute attr1 = new Attribute().setName("a").setOperator(ComparativeOperator.EQUAL).setValue("b");
		Attribute attr2 = new Attribute().setName("x").setOperator(ComparativeOperator.APPROX).setValue("z");
		AttributeLinker attributeLinker = new AttributeLinker().setOperator(LogicalOperator.AND).setInvert(true);
		attributeLinker.addItem(attr1);
		attributeLinker.addItem(attr2);
		
		String filterExpression = LDAPFilterEncodingHandler.getInstance().encodeToString(attributeLinker);
		assertEquals("ldapfilterexpression should be correct","(!(&(a=b)(x~=z)))", filterExpression);
		
		IFilterItem filterItem = LDAPFilterDecodingHandler.getInstance().decodeFromString(filterExpression);
		assertNotNull("parsed ldapfilter should not be null", filterItem);
		assertTrue("parsed ldapfilter should be correct filteritem type", filterItem instanceof AttributeLinker);
		
		attributeLinker = (AttributeLinker)filterItem;
		assertEquals("parsed linker should contains correct operator", LogicalOperator.AND, attributeLinker.getOperator());
		assertEquals("invertflag should be correct", true,attributeLinker.isInvert());
		
		List<IFilterItem> childs1 = attributeLinker.getLinkedItemList();
		assertEquals("parsed linker should contains correct linked item size", 2, childs1.size());
		
		for(int i = 0; i < childs1.size(); i++)
		{
			filterItem = childs1.get(i);
			assertTrue("parsed ldapfilter should be correct filteritem type", filterItem instanceof Attribute);
			
			if(i == 0)
			{
				attr1 = (Attribute)filterItem;
				assertEquals("attributename should be correct","a", attr1.getName());
				assertEquals("attributeoperator should be correct",ComparativeOperator.EQUAL, attr1.getOperator());
				assertEquals("attributevalue should be correct","b", attr1.getValue());
				assertEquals("invertflag should be correct", false,attr1.isInvert());
			}
			
			if(i == 1)
			{
				attr2 = (Attribute)filterItem;
				assertEquals("attributename should be correct","x", attr2.getName());
				assertEquals("attributeoperator should be correct",ComparativeOperator.APPROX, attr2.getOperator());
				assertEquals("attributevalue should be correct","z", attr2.getValue());
				assertEquals("invertflag should be correct", false,attr2.isInvert());
			}
		}
	}
	
	@Test
	public void test040Nested1()
	{
		Attribute attr1 = new Attribute().setName("a").setOperator(ComparativeOperator.EQUAL).setValue("b");
		Attribute attr2 = new Attribute().setName("x").setOperator(ComparativeOperator.APPROX).setValue("z");
		Attribute attr3 = new Attribute().setName("y").setOperator(ComparativeOperator.LESS).setValue("1");
		AttributeLinker attributeLinkerNested = new AttributeLinker().setOperator(LogicalOperator.AND).setInvert(true);
		attributeLinkerNested.addItem(attr2);
		attributeLinkerNested.addItem(attr3);
		AttributeLinker attributeLinkerRoot = new AttributeLinker().setOperator(LogicalOperator.OR);
		attributeLinkerRoot.addItem(attr1);
		attributeLinkerRoot.addItem(attributeLinkerNested);
		
		String filterExpression = LDAPFilterEncodingHandler.getInstance().encodeToString(attributeLinkerRoot);
		assertEquals("ldapfilterexpression should be correct","(|(a=b)(!(&(x~=z)(y<=1))))", filterExpression);
		
		IFilterItem filterItem = LDAPFilterDecodingHandler.getInstance().decodeFromString(filterExpression);
		assertNotNull("parsed ldapfilter should not be null", filterItem);
		assertTrue("parsed ldapfilter should be correct filteritem type", filterItem instanceof AttributeLinker);
		
		attributeLinkerRoot = (AttributeLinker)filterItem;
		assertEquals("parsed linker should contains correct operator", LogicalOperator.OR, attributeLinkerRoot.getOperator());
		assertEquals("invertflag should be correct", false,attributeLinkerRoot.isInvert());
		
		List<IFilterItem> childs1 = attributeLinkerRoot.getLinkedItemList();
		assertEquals("parsed linker should contains correct linked item size", 2, childs1.size());
		
		for(int i = 0; i < childs1.size(); i++)
		{
			filterItem = childs1.get(i);
			
			
			if(i == 0)
			{
				assertTrue("parsed ldapfilter should be correct filteritem type", filterItem instanceof Attribute);
				attr1 = (Attribute)filterItem;
				assertEquals("attributename should be correct","a", attr1.getName());
				assertEquals("attributeoperator should be correct",ComparativeOperator.EQUAL, attr1.getOperator());
				assertEquals("attributevalue should be correct","b", attr1.getValue());
				assertEquals("invertflag should be correct", false,attr1.isInvert());
			}
			
			if(i == 1)
			{
				assertTrue("parsed ldapfilter should be correct filteritem type", filterItem instanceof AttributeLinker);
				attributeLinkerNested = (AttributeLinker)filterItem;
				assertEquals("parsed linker should contains correct operator", LogicalOperator.AND, attributeLinkerNested.getOperator());
				assertEquals("invertflag should be correct", true,attributeLinkerNested.isInvert());
				
				List<IFilterItem> childs2 = attributeLinkerNested.getLinkedItemList();
				assertEquals("parsed linker should contains correct linked item size", 2, childs2.size());
				
				for(int j = 0; j < childs2.size(); j++)
				{
					filterItem = childs2.get(j);
					
					if(j == 0)
					{
						assertTrue("parsed ldapfilter should be correct filteritem type", filterItem instanceof Attribute);
						attr2 = (Attribute)filterItem;
						assertEquals("attributename should be correct","x", attr2.getName());
						assertEquals("attributeoperator should be correct",ComparativeOperator.APPROX, attr2.getOperator());
						assertEquals("attributevalue should be correct","z", attr2.getValue());
						assertEquals("invertflag should be correct", false,attr2.isInvert());
					}
					
					if(j == 1)
					{
						assertTrue("parsed ldapfilter should be correct filteritem type", filterItem instanceof Attribute);
						attr3 = (Attribute)filterItem;
						assertEquals("attributename should be correct","y", attr3.getName());
						assertEquals("attributeoperator should be correct",ComparativeOperator.LESS, attr3.getOperator());
						assertEquals("attributevalue should be correct","1", attr3.getValue());
						assertEquals("invertflag should be correct", false,attr3.isInvert());
					}
				}
			}
		}
	}
}
