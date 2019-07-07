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

public interface IModifyListener
{
	public <C extends INodeType<?,?>, T> void onModify(BranchNode<?, ?> parentNode, String nodeTypeName, Object staticNodeTypeInstance, Class<C> type, T oldValue, T newValue, ConplierBean<Boolean> doit);
}
