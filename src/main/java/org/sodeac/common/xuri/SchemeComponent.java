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
package org.sodeac.common.xuri;

/*
 * @see {@link https://tools.ietf.org/html/rfc3986#section-3.1}
 */

/**
 * Scheme component of URI.
 * 
 * @author Sebastian Palarus
 * @since 1.0
 * @version 1.0
 * 
 */
public class SchemeComponent extends AbstractComponent<NoSubComponent>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5126925115989645523L;
	
	private String value = null;
	
	/**
	 * constructor for scheme component
	 * 
	 * @param value representative scheme string
	 */
	public SchemeComponent(String value)
	{
		super(ComponentType.SCHEME);
		this.value = value;
	}

	/**
	 * getter for representative scheme string
	 * 
	 * @return representative scheme string
	 */
	public String getValue()
	{
		return this.value;
	}
}
