/*******************************************************************************
 * Copyright (c) 2019, 2020 Sebastian Palarus
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Sebastian Palarus - initial API and implementation
 *******************************************************************************/
package org.sodeac.common.misc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.sodeac.common.misc.Driver.IDriver;

public class Driver
{
	public static <T extends IDriver> T getSingleDriver(Class<T> driverClass, Map<String,Object> properties)
	{
		if(OSGiUtils.isOSGi())
		{
			return OSGiUtils.getSingleDriver(driverClass, properties);
		}
		ServiceLoader<T> serviceLoader = ServiceLoader.load(driverClass);
		Iterator<T> iterator = serviceLoader.iterator();
		T bestDriver = null;
		int bestIndex = -1;
		while(iterator.hasNext())
		{
			T driverInstance = iterator.next();
			int applicableIndex = driverInstance.driverIsApplicableFor(properties);
			if(applicableIndex > bestIndex)
			{
				bestDriver = driverInstance;
				bestIndex = applicableIndex;
			}
		}
		return bestDriver;
	}
	
	public static <T extends IDriver> List<T> getDriverList(Class<T> driverClass, Map<String,Object> properties)
	{
		if(OSGiUtils.isOSGi())
		{
			return OSGiUtils.getDriverList(driverClass, properties);
		}
		ServiceLoader<T> serviceLoader = ServiceLoader.load(driverClass);
		Iterator<T> iterator = serviceLoader.iterator();
		List<T> list = new ArrayList<T>();
		Set<String> uniqueIndex = new HashSet<String>();
		while(iterator.hasNext())
		{
			T driverInstance = iterator.next();
			if(uniqueIndex.contains(driverInstance.getClass().getCanonicalName()))
			{
				continue;
			}
			int applicableIndex = driverInstance.driverIsApplicableFor(properties);
			if(applicableIndex > IDriver.APPLICABLE_NONE)
			{
				list.add(driverInstance);
				uniqueIndex.add(driverInstance.getClass().getCanonicalName());
			}
		}
		uniqueIndex.clear();
		return list;
	}
	
	public interface IDriver
	{
		public static final String TYPE = "TYPE";
		
		public static final int APPLICABLE_NONE = -1;
		public static final int APPLICABLE_FALLBACK = 0;
		public static final int APPLICABLE_DEFAULT = 10000;
		
		public int driverIsApplicableFor(Map<String,Object> properties);
	}
}
