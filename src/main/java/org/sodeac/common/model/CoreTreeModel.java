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

import javax.xml.bind.annotation.XmlElement;

import org.sodeac.common.typedtree.BranchNodeType;
import org.sodeac.common.typedtree.TypedTreeMetaModel;
import org.sodeac.common.typedtree.annotation.Domain;

@Domain(name="org.sodeac.core.model")
public class CoreTreeModel extends TypedTreeMetaModel<CoreTreeModel> 
{
	@XmlElement(name="Throwable")
	public static volatile BranchNodeType<CoreTreeModel,ThrowableNodeType> throwable;
	
	@XmlElement(name="Stacktrace")
	public static volatile BranchNodeType<CoreTreeModel,StacktraceNodeType> stacktrace;
}
