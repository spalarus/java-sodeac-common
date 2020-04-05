package org.sodeac.common.misc;

import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;

import org.sodeac.common.xuri.AuthoritySubComponent;
import org.sodeac.common.xuri.IExtension;
import org.sodeac.common.xuri.PathSegment;
import org.sodeac.common.xuri.QuerySegment;
import org.sodeac.common.xuri.URI;
import org.sodeac.common.xuri.ldapfilter.DefaultMatchableWrapper;
import org.sodeac.common.xuri.ldapfilter.IFilterItem;
import org.sodeac.common.xuri.ldapfilter.IMatchable;
import org.sodeac.common.xuri.ldapfilter.LDAPFilterDecodingHandler;
import org.sodeac.common.xuri.ldapfilter.LDAPFilterExtension;

public class Test
{
	public static void main(String[] args)
	{
		Map<String,IMatchable> props = new HashMap<String, IMatchable>();
		URI uri = new URI("sdc://messageservice:domain/servicename/1.0.0?prio=int:100");
		
		// sdc://messageservice:registration/domain/servicename/1.0.0?prio=int:100
		// sdc://messageservice:registration/domain/servicename/1.0.0{{"prio":3}}
		// sdc://messageservice/registration/domain/servicename/1.0.0?prio=int:100
		// sdc://messageservice/registration/domain/servicename/1.0.0{{"prio":3}}
		
		for(AuthoritySubComponent component : uri.getAuthority().getSubComponentList())
		{
			if("messageservice".equals( component.getValue()))
			{
				continue;
			}
			props.put("domain", new DefaultMatchableWrapper(component.getValue()));
		}
		int i = 0;
		for(PathSegment pathSegment: uri.getPath().getSubComponentList())
		{
			if(i == 0)
			{
				props.put("servicename", new DefaultMatchableWrapper(pathSegment.getValue()));
			}
			else if(i == 1)
			{
				props.put("serviceversion", new DefaultMatchableWrapper(pathSegment.getValue()));
			}
			i++;
		}
		for(QuerySegment querySegment : uri.getQuery().getSubComponentList())
		{
			// Besser in type => Format ist eher sowas wie eine codierung
			if("int".equals(querySegment.getCoding()))
			{
				props.put(querySegment.getName(), new DefaultMatchableWrapper(Integer.parseInt(querySegment.getValue())));
			}
			else
			{
				props.put(querySegment.getName(), new DefaultMatchableWrapper(querySegment.getValue()));
			}
		}
		
		URI uri2 = new URI("sdc://messageservice:domain/servicename(&(prio>=10)(prio<=110))");
		
		StringBuilder expression = new StringBuilder("(&");
		expression.append("(domain=" + uri2.getAuthority().getSubComponentList().get(1).getValue() + ")");
		expression.append("(servicename=" + uri2.getPath().getSubComponentList().get(0).getValue() + ")");
		
		IExtension<IFilterItem> ldapFilterExtension = (IExtension<IFilterItem>) uri2.getPath().getSubComponentList().get(0).getExtension(LDAPFilterExtension.TYPE);
		
		expression.append(ldapFilterExtension.getExpression());
		expression.append(")");
		System.out.println(expression);
		
		
		boolean match = LDAPFilterDecodingHandler.getInstance().decodeFromString(expression.toString()).matches(props);
		System.out.println(match);
		
		JsonObject json = Json.createObjectBuilder()
			     .add("name", "Fal\"c'{:}, \\ \n \to")
			     .add("age", 3.0)
			     .add("biteable", Boolean.FALSE).build();
		String result = json.toString();
		System.out.println(result);
		
	}
	
	
}

/*Außerdem: 
Parser Für Authority/Paths => Convertiert in Beans ...
Wie werden Priorisierte Nodes ... angegeben? Im Filter?
Match Bewertung ???
Cachen von IFilterItems
Irgenwas extends Driver  => Performance wegen Check Domain/ServiceName???*/
