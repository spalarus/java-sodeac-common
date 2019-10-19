/*******************************************************************************
 * Copyright (c) 2016, 2019 Sebastian Palarus
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Sebastian Palarus - initial API and implementation
 *******************************************************************************/
package org.sodeac.common.xuri.ldapfilter;

import java.io.Serializable;

import org.sodeac.common.xuri.IExtension;

/**
 * XURI filter extension for ldap filter.
 * 
 * @author Sebastian Palarus
 * @since 1.0
 * @version 1.0
 */
public class LDAPFilterExtension implements IExtension<IFilterItem>, Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 9054192628876497162L;
	
	public static final String TYPE = "org.sodeac.xuri.ldapfilter";
	
	public LDAPFilterExtension(String rawString)
	{
		super();
		this.rawString = rawString;
	}
	
	private String rawString = null;

	@Override
	public String getExpression()
	{
		return rawString;
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	public IFilterItem decodeFromString(String expression)
	{
		return LDAPFilterDecodingHandler.getInstance().decodeFromString(expression);
	}

	@Override
	public String encodeToString(IFilterItem extensionDataObject)
	{
		return LDAPFilterEncodingHandler.getInstance().encodeToString(extensionDataObject);
	}
}
