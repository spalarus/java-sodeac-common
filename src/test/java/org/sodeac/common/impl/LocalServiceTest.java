package org.sodeac.common.impl;

import static org.junit.Assert.assertEquals;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sodeac.common.IService.ServiceRegistrationAddress;
import org.sodeac.common.xuri.URI;

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
	public void test00002XXX()
	{
		LocalService.getLocalServiceRegistryImpl();
	}
}
