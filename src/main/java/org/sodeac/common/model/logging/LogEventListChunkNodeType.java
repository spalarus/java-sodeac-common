/*******************************************************************************
 * Copyright (c) 2020 Sebastian Palarus
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Sebastian Palarus - initial API and implementation
 *******************************************************************************/
package org.sodeac.common.model.logging;

import org.sodeac.common.model.CommonListChunkBaseBranchNodeType;
import org.sodeac.common.typedtree.BranchNode;
import org.sodeac.common.typedtree.BranchNodeListType;
import org.sodeac.common.typedtree.BranchNodeMetaModel;
import org.sodeac.common.typedtree.ModelRegistry;
import org.sodeac.common.typedtree.annotation.XMLNodeList;

public class LogEventListChunkNodeType extends CommonListChunkBaseBranchNodeType
{
	static{ModelRegistry.getBranchNodeMetaModel(LogEventListChunkNodeType.class);}
	
	@XMLNodeList(childElementName="LogEvent", listElement=false)
	public static volatile BranchNodeListType<LogEventListChunkNodeType, LogEventNodeType> chunk;
	
	public static void addLogToEventListChunk
	(
		BranchNode<? extends BranchNodeMetaModel, LogEventListChunkNodeType> chunkNode,
		BranchNode<?extends BranchNodeMetaModel, ? extends LogEventNodeType> logEventNode
	)
	{
		Integer itemSize = chunkNode.getValue(LogEventListChunkNodeType.itemSize); if(itemSize == null) {itemSize = 0;}
		chunkNode.create(LogEventListChunkNodeType.chunk).copyFrom(logEventNode);
		chunkNode.setValue(LogEventListChunkNodeType.itemSize, itemSize.intValue() + 1);
	}
}
