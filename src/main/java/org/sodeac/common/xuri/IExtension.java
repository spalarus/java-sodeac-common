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

/**
 * 
 * Interface for extensions to define and handle an expression string
 * 
 * @author Sebastian Palarus
 *
 */
public interface IExtension<T>
{
	/**
	 * 
	 * @return type of extension
	 */
	public String getType();
	
	/**
	 * 
	 * @return representative expression string for extension
	 */
	public String getExpression();
	
	/**
	 * decode expression string to extension data object
	 * 
	 * @param expression expression string
	 * @return extension data object
	 */
	public T decodeFromString(String expression);
	
	/**
	 * encode extension data object to expression string
	 * 
	 * @param extensionDataObject extension data object
	 * @return expression string
	 */
	public String encodeToString(T extensionDataObject);
}
