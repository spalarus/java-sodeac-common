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
package org.sodeac.common.model;

import org.sodeac.common.typedtree.BranchNodeListType;
import org.sodeac.common.typedtree.BranchNodeMetaModel;
import org.sodeac.common.typedtree.ModelRegistry;
import org.sodeac.common.typedtree.annotation.Domain;
import org.sodeac.common.typedtree.annotation.TypedTreeModel;
import org.sodeac.common.typedtree.annotation.XMLNodeList;

@Domain(name="org.sodeac.core.model")
@TypedTreeModel(modelClass=CoreTreeModel.class)
public class StacktraceNodeType extends BranchNodeMetaModel
{
	static{ModelRegistry.getBranchNodeMetaModel(StacktraceNodeType.class);}
	
	@XMLNodeList(childElementName="StacktraceElement", listElement=false)
	public static volatile BranchNodeListType<StacktraceNodeType,StacktraceElementNodeType> elements;
}
