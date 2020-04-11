package org.sodeac.common.xuri.ldapfilter;

import static org.junit.Assert.assertEquals;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LDAPFilterBuilderTest
{
	@Test
	public void test001SimpleTest()
	{
		assertEquals("value should be correct", "(A=B)", LDAPFilterBuilder.andLinker().criteriaWithName("A").eq("B").build().toString());
		assertEquals("value should be correct", "(!(A=B))", LDAPFilterBuilder.nandLinker().criteriaWithName("A").eq("B").build().toString());
	}
	
	@Test
	public void test002OperatorTest()
	{
		assertEquals("value should be correct", "(A=B)", LDAPFilterBuilder.andLinker().criteriaWithName("A").eq("B").build().toString());
		assertEquals("value should be correct", "(A~=B)", LDAPFilterBuilder.andLinker().criteriaWithName("A").approx("B").build().toString());
		assertEquals("value should be correct", "(A>=B)", LDAPFilterBuilder.andLinker().criteriaWithName("A").gte("B").build().toString());
		assertEquals("value should be correct", "(A<=B)", LDAPFilterBuilder.andLinker().criteriaWithName("A").lte("B").build().toString());
		
		assertEquals("value should be correct", "(!(A=B))", LDAPFilterBuilder.nandLinker().criteriaWithName("A").eq("B").build().toString());
		assertEquals("value should be correct", "(!(A~=B))", LDAPFilterBuilder.nandLinker().criteriaWithName("A").approx("B").build().toString());
		assertEquals("value should be correct", "(!(A>=B))", LDAPFilterBuilder.nandLinker().criteriaWithName("A").gte("B").build().toString());
		assertEquals("value should be correct", "(!(A<=B))", LDAPFilterBuilder.nandLinker().criteriaWithName("A").lte("B").build().toString());
	}
	
	@Test
	public void test003LinkerTest()
	{
		assertEquals
		(
			"value should be correct", "(&(A=B)(C=D))", 
			LDAPFilterBuilder.andLinker()
				.criteriaWithName("A").eq("B")
				.criteriaWithName("C").eq("D")
			.build().toString()
		);
		
		assertEquals
		(
			"value should be correct", "(!(&(A=B)(C=D)))", 
			LDAPFilterBuilder.nandLinker()
				.criteriaWithName("A").eq("B")
				.criteriaWithName("C").eq("D")
			.build().toString()
		);
		
		assertEquals
		(
			"value should be correct", "(|(A=B)(C=D))", 
			LDAPFilterBuilder.orLinker()
				.criteriaWithName("A").eq("B")
				.criteriaWithName("C").eq("D")
			.build().toString()
		);
		
		assertEquals
		(
			"value should be correct", "(!(|(A=B)(C=D)))", 
			LDAPFilterBuilder.norLinker()
				.criteriaWithName("A").eq("B")
				.criteriaWithName("C").eq("D")
			.build().toString()
		);
	}
	
	@Test
	public void test004NestedTermTest()
	{
		assertEquals
		(
			"value should be correct", "(&(abc=123)(xyz=abc)(&(ABC<=1)(!(ABC<=1000)))(xyzF=abcA))", 
			LDAPFilterBuilder.andLinker()
				.criteriaWithName("abc").eq("123")
				.criteriaWithName("xyz").eq("abc")
				.nestedAndLinker()
					.criteriaWithName("ABC").lte("1")
					.criteriaWithName("ABC").notLte("1000")
				.closeLinker()
				.criteriaWithName("xyzF").eq("abcA")
			.build().toString()
		);
		
		assertEquals
		(
			"value should be correct", "(&(abc=123)(xyz=abc)(!(&(ABC<=1)(!(ABC<=1000))))(xyzF=abcA))", 
			LDAPFilterBuilder.andLinker()
				.criteriaWithName("abc").eq("123")
				.criteriaWithName("xyz").eq("abc")
				.nestedNandLinker()
					.criteriaWithName("ABC").lte("1")
					.criteriaWithName("ABC").notLte("1000")
				.closeLinker()
				.criteriaWithName("xyzF").eq("abcA")
			.build().toString()
		);
		
		assertEquals
		(
			"value should be correct", "(&(abc=123)(xyz=abc)(|(ABC<=1)(!(ABC<=1000)))(xyzF=abcA))", 
			LDAPFilterBuilder.andLinker()
				.criteriaWithName("abc").eq("123")
				.criteriaWithName("xyz").eq("abc")
				.nestedOrLinker()
					.criteriaWithName("ABC").lte("1")
					.criteriaWithName("ABC").notLte("1000")
				.closeLinker()
				.criteriaWithName("xyzF").eq("abcA")
			.build().toString()
		);
		
		assertEquals
		(
			"value should be correct", "(&(abc=123)(xyz=abc)(!(|(ABC<=1)(!(ABC<=1000))))(xyzF=abcA))", 
			LDAPFilterBuilder.andLinker()
				.criteriaWithName("abc").eq("123")
				.criteriaWithName("xyz").eq("abc")
				.nestedNorLinker()
					.criteriaWithName("ABC").lte("1")
					.criteriaWithName("ABC").notLte("1000")
				.closeLinker()
				.criteriaWithName("xyzF").eq("abcA")
			.build().toString()
		);
	}
	
	@Test
	public void test004EscapeTest()
	{
		Criteria criteria = (Criteria)LDAPFilterBuilder.andLinker().
			criteriaWithName("A").eq("Please store the file in temp directory ( C:\\Temp )")
		.build();
		
		assertEquals
		(
			"value should be correct", "(A=Please store the file in temp directory \\28 C:\\5cTemp \\29)", 
			criteria.toString()
		);
		
		assertEquals
		(
			"value should be correct", "Please store the file in temp directory ( C:\\Temp )",
			criteria.getValue()
		);
	}
}
