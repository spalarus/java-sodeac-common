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
package org.sodeac.common.model.logging;

import org.sodeac.common.typedtree.BranchNodeType;
import org.sodeac.common.typedtree.ModelRegistry;
import org.sodeac.common.typedtree.TypedTreeMetaModel;


public class LoggingTreeModel extends TypedTreeMetaModel<LoggingTreeModel> 
{
	static{ModelRegistry.getTypedTreeMetaModel(LoggingTreeModel.class);}
	
	public static volatile BranchNodeType<LoggingTreeModel,LogEventNodeType> logEvent;
}
