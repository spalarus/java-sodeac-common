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
package org.sodeac.common.misc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Helper class around package resources
 * 
 * @author "Sebastian Palarus"
 *
 */
public class ResourceLoader 
{
	public static final LoaderConfiguration LOADER_CONFIGURATION = new LoaderConfiguration();
	
	/**
	 * Load package resource as string in OSGi or Non-OSGi environment. 
	 * 
	 * @param fileName name of string resource
	 * @param packageClass class represents the package
	 * @return string resource
	 * @throws IOException
	 */
	public static String loadPackageFileAsString(String fileName, Class<?> packageClass) throws IOException
	{
		ResourceLoader.init();
		if(ResourceLoader.LOADER_CONFIGURATION.isOSGI.booleanValue())
		{
			String stringResource = ResourceLoaderOSGi.loadPackageFileAsString(fileName,packageClass);
			if(stringResource != null)
			{
				return stringResource;
			}
		}
		InputStream inputStream = packageClass.getClassLoader().getResourceAsStream(packageClass.getPackage().getName().replaceAll("\\.", "/") + "/" + fileName);
		if(inputStream == null)
		{
			return null;
		}
		ByteArrayOutputStream byos = new ByteArrayOutputStream();
		try
		{
			int len;
			byte[] buf = new byte[1080];
			while((len = inputStream.read(buf)) > 0)
			{
				byos.write(buf, 0, len);
			}
		}
		finally
		{
			try
			{
				inputStream.close();
			}
			catch (Exception e) {}
			try
			{
				byos.flush();
			}
			catch (Exception e) {}
			try
			{
				byos.close();
			}
			catch (Exception e) {}
		}
		
		return byos.toString();
	}
	
	private static void init()
	{
		if(ResourceLoader.LOADER_CONFIGURATION.isOSGI != null)
		{
			return;
		}
		
		ResourceLoader.LOADER_CONFIGURATION.isOSGI = false;
		
		try
		{
			Class<?> clazz = ResourceLoader.class.getClassLoader().loadClass("org.osgi.framework.FrameworkUtil");
			Objects.requireNonNull(clazz);
			ResourceLoaderOSGi.checkFramework();
			ResourceLoader.LOADER_CONFIGURATION.isOSGI = true;
		}
		catch (ClassNotFoundException e) {}
		catch (Error e)
		{
			e.printStackTrace();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	private static class LoaderConfiguration
	{
		private Boolean isOSGI = null;
	}
}
