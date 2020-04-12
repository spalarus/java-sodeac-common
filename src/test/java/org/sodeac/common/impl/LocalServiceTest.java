package org.sodeac.common.impl;

import static org.junit.Assert.assertEquals;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sodeac.common.IService.ServiceRegistrationAddress;
import org.sodeac.common.IService.ServiceSelectorAddress;
import org.sodeac.common.misc.Version;
import org.sodeac.common.xuri.URI;
import org.sodeac.common.xuri.ldapfilter.LDAPFilterBuilder;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LocalServiceTest
{
	@Test
	public void test00001ServiceRegistrationURI()
	{
		URI uri = ServiceRegistrationAddress.newBuilder()
					.forDomain("sodeac.org")
					.withServiceName("TestService")
					.andVersion(1, 0, 0)
					.addOption("priority", 1000)
				.build();
		
		assertEquals("value should be correct", "sdc://serviceaddress:sodeac.org/TestService/1.0.0/options{\"priority\":1000}", uri.toString());
	}
	
	@Test
	public void test00003ServiceSelectorURI()
	{
		URI uri = ServiceSelectorAddress.newBuilder()
					.forDomain("sodeac.org")
					.withServiceName("TestService")
					.setFilter
					(
						LDAPFilterBuilder.andLinker()
							.criteriaWithName("version").gte(new Version(1).toString())
							.criteriaWithName("version").notGte(new Version(2).toString())
						.build()
					)
					.scoreThePreferenceFilter
					(
						LDAPFilterBuilder.andLinker().criteriaWithName("version").gte(new Version(1,5).toString()).build()
					).with(1000).points()
					.scoreThePreferenceFilter
					(
						LDAPFilterBuilder.andLinker().criteriaWithName("writable").eq(Boolean.TRUE.toString()).build()
					).with(2000).points()
				.build();
		
		assertEquals("value should be correct",
				"sdc://serviceselector:sodeac.org/TestService(&(version>=1.0.0)(!(version>=2.0.0)))/preferences{\"score\":1000,\"filter\":\"(version>=1.5.0)\"}{\"score\":2000,\"filter\":\"(writable=true)\"}",
				uri.toString());
	}
	
	@Test
	public void test00003XXX()
	{
		LocalService.getLocalServiceRegistryImpl();
	}
}
