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
import java.net.URL;
import java.util.Objects;

import org.osgi.framework.Bundle;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;

public class ResourceLoaderOSGi 
{
	protected static String loadPackageFileAsString(String fileName, Class<?> packageClass) throws IOException
	{
		Bundle bundle = FrameworkUtil.getBundle(packageClass);
		if(bundle == null)
		{
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		URL url = bundle.getResource(packageClass.getPackage().getName().replaceAll("\\.", "/") + "/" + fileName);
		InputStream inputStream = url.openStream();
		try
		{
			
			url = null;
			bundle = null;
			if(inputStream == null)
			{
				return null;
			}
			
			int len;
			byte[] buf = new byte[1080];
			while((len = inputStream.read(buf)) > 0)
			{
				baos.write(buf, 0, len);
			}
		}
		finally
		{
			try
			{
				if(inputStream != null)
				{
					inputStream.close();
					inputStream = null;
				}
			}
			catch (Exception e) {}
			try
			{
				baos.flush();
			}
			catch (Exception e) {}
			try
			{
				baos.close();
			}
			catch (Exception e) {}
		}
		return baos.toString();
	}
	
	protected static boolean checkFramework()
	{
		try
		{
			Filter filter = FrameworkUtil.createFilter("(objectClass=sodeac)");
			Objects.requireNonNull(filter);
		}
		catch (Exception e) {e.printStackTrace();}
		catch (Error e) {e.printStackTrace();}
		return false;
	}
}
