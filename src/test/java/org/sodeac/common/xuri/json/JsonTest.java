package org.sodeac.common.xuri.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sodeac.common.xuri.IExtension;
import org.sodeac.common.xuri.URI;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JsonTest
{
	@Test
	public void test001Simple()
	{
		JsonObject json = Json.createObjectBuilder()
			.add("id", 31)
			.add("name", "Max\tMuster-\nmann}\"")
			.add("mother", Json.createObjectBuilder()
					.add("id", 1)
					.add("name", "Diana")
			)
		.build();
		
		URI uri = new URI("http://XXX" + new JsonExtension().getEncoder().encodeToString(json));
		
		IExtension<JsonObject> extension = (IExtension<JsonObject>)uri.getAuthority().getSubComponentList().get(0).getExtensionList().get(0);
		
		assertTrue("condition should be corrext", extension.getExpression().length() > 0);
		JsonObject json2 = extension.getDecoder().decodeFromString(extension.getExpression());
		
		assertEquals("value should be correct", extension.getExpression(), json.toString());
		assertEquals("value should be correct", extension.getExpression(), json2.toString());
	}
}
