/*******************************************************************************
 * Copyright (c) 2019, 2020 Sebastian Palarus
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Sebastian Palarus - initial API and implementation
 *******************************************************************************/
package org.sodeac.common.model.logging;

import javax.xml.bind.annotation.XmlElement;

import org.sodeac.common.typedtree.BranchNodeType;
import org.sodeac.common.typedtree.ModelRegistry;
import org.sodeac.common.typedtree.TypedTreeMetaModel;
import org.sodeac.common.typedtree.annotation.Domain;
import org.sodeac.common.typedtree.annotation.Version;

@Domain(name="sodeac.org",module="logging")
@Version(major=0,minor=6)
public class LoggingTreeModel extends TypedTreeMetaModel<LoggingTreeModel> 
{
	static{ModelRegistry.getTypedTreeMetaModel(LoggingTreeModel.class);}
	
	@XmlElement(name="LogEvent")
	public static volatile BranchNodeType<LoggingTreeModel,LogEventNodeType> logEvent;
	
	
	@XmlElement(name="LogEventListChunk")
	public static volatile BranchNodeType<LoggingTreeModel,LogEventListChunkNodeType> logEventListChunk;
	
	public static RootBranchNode<LoggingTreeModel,LogEventListChunkNodeType> createLogEventListChunk(long listSize, long chunkSize, long chunkSequence, boolean last)
	{
		RootBranchNode<LoggingTreeModel,LogEventListChunkNodeType> chunk = ModelRegistry.getTypedTreeMetaModel(LoggingTreeModel.class).createRootNode(LoggingTreeModel.logEventListChunk);
		
		chunk
			.setValue(LogEventListChunkNodeType.listSize, listSize)
			.setValue(LogEventListChunkNodeType.chunkSequnece, chunkSequence)
			.setValue(LogEventListChunkNodeType.chunkSize, chunkSize)
			.setValue(LogEventListChunkNodeType.itemSize, 0)
			.setValue(LogEventListChunkNodeType.last, last);
		
		return chunk;
	}
}
