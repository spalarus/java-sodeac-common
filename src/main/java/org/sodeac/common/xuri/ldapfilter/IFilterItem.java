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

import java.util.Map;

/**
 * Filter items represent ldap filter or ldap sub filter
 * 
 * @author Sebastian Palarus
 * @since 1.0
 * @version 1.0
 *
 */
public interface IFilterItem 
{
	public static final char OPENER = '(';
	public static final char CLOSER = ')';
	public static final char ESCAPE = '\\';
	public static final char NOT = '!';
	public static final char AND = '&';
	public static final char OR = '|';
	public static final char LESS_STARTSEQ = '<';
	public static final char GREATER_STARTSEQ = '>';
	public static final char APPROX_STARTSEQ = '~';
	public static final char EQUAL = '=';
	
	/**
	 * getter for invert state
	 * 
	 * @return true if converted, otherwise false
	 */
	public boolean isInvert() ;
	
	/**
	 * setter for inverted state
	 * @param invert invert state
	 * 
	 * @return filter item
	 */
	public IFilterItem setInvert(boolean invert) ;
	
	/**
	 * getter for parent filter item
	 * 
	 * @return parent filter item
	 */
	public AttributeLinker getParent();
	
	/**
	 * check {@code properties} match filter item
	 * 
	 * @param properties
	 * @return true if {@code properties} match filter item, otherwise false
	 */
	public boolean matches(Map<String,IMatchable> properties);
}
