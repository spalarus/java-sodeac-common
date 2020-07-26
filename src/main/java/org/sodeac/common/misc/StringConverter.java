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
package org.sodeac.common.misc;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.function.Function;

public class StringConverter
{
	private static Map<String,Function<Object, String>> toStringIndex = null;
	private static Map<String,Function<String, Object>> fromStringIndex = null;
	
	static
	{
		Map<String,Function<Object, String>> toString = new HashMap<String,Function<Object, String>>();
		Map<String,Function<String, Object>> fromString = new HashMap<String,Function<String, Object>>();
		
		toString.put(String.class.getCanonicalName(), p -> (String)p);
		fromString.put(String.class.getCanonicalName(), p -> p);
		
		toString.put(String.class.getCanonicalName(), p -> (String)p);
		fromString.put(String.class.getCanonicalName(), p -> p);
		
		toString.put(Long.class.getCanonicalName(), p -> Long.toString((Long)p));
		fromString.put(Long.class.getCanonicalName(), p -> Long.parseLong(p));
		
		toString.put(Integer.class.getCanonicalName(), p -> Integer.toString((Integer)p));
		fromString.put(Integer.class.getCanonicalName(), p -> Integer.parseInt(p));
		
		toString.put(Double.class.getCanonicalName(), p -> Double.toString((Double)p));
		fromString.put(Double.class.getCanonicalName(), p -> Double.parseDouble(p));
		
		toString.put(Boolean.class.getCanonicalName(), p -> Boolean.toString((Boolean)p));
		fromString.put(Boolean.class.getCanonicalName(), p -> Boolean.parseBoolean(p));
		
		toString.put(Date.class.getCanonicalName(), p -> 
		{
			Date date = (Date)p;
			SimpleDateFormat ISO8601Local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			TimeZone timeZone = TimeZone.getDefault(); //local JVM time zone
			ISO8601Local.setTimeZone(timeZone);
			
			DecimalFormat twoDigits = new DecimalFormat("00");
			
			int offset = ISO8601Local.getTimeZone().getOffset(date.getTime());
			String sign = "+";
			
			if (offset < 0)
			{
				offset = -offset;
				sign = "-";
			}
			int hours = offset / 3600000;
			int minutes = (offset - hours * 3600000) / 60000;
			
			String ISO8601Now = ISO8601Local.format(date) + sign + twoDigits.format(hours) + ":" + twoDigits.format(minutes);
			return ISO8601Now; 
		});
		fromString.put(Date.class.getCanonicalName(), p -> 
		{
			try
			{
				SimpleDateFormat ISO8601Local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
				TimeZone timeZone = TimeZone.getDefault(); //local JVM time zone
				ISO8601Local.setTimeZone(timeZone);
				return ISO8601Local.parse(p); 
			}
			catch (ParseException e) 
			{
				throw new RuntimeException(e);
			}
		});
		
		toString.put(UUID.class.getCanonicalName(), p -> ((UUID)p).toString());
		fromString.put(UUID.class.getCanonicalName(), p -> UUID.fromString(p));
		
		StringConverter.fromStringIndex = Collections.unmodifiableMap(fromString);
		StringConverter.toStringIndex = Collections.unmodifiableMap(toString);
	}

	public static Map<String, Function<Object, String>> toStringIndex()
	{
		return toStringIndex;
	}

	public static Map<String, Function<String, Object>> fromStringIndex()
	{
		return fromStringIndex;
	}
}
