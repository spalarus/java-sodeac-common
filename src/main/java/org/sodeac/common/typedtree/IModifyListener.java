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
package org.sodeac.common.typedtree;

import org.sodeac.common.function.ConplierBean;

/**
 * 
 * A modify listener is a modify listener ;-)
 * 
 * @author Sebastian Palarus
 *
 */
public interface IModifyListener
{
	/**
	 * 
	 * Notify  before modification is invoked.
	 * 
	 * @param parentNode parent node of modified node
	 * @param staticNodeTypeInstance static child node type instance from meta model
	 * @param type
	 * @param oldValue
	 * @param newValue
	 * @param doit
	 */
	public default <C extends INodeType<?,?>, T> void beforeModify(BranchNode<?, ?> parentNode, Object staticNodeTypeInstance, Class<C> type, T oldValue, T newValue, ConplierBean<Boolean> doit) {};
	public default <C extends INodeType<?,?>, T> void afterModify(BranchNode<?, ?> parentNode, Object staticNodeTypeInstance, Class<C> type, T oldValue, T newValue) {};
}
