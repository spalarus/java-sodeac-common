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

import org.sodeac.common.model.CommonGenericPropertyNodeType;
import org.sodeac.common.typedtree.BranchNodeType;
import org.sodeac.common.typedtree.LeafNodeType;
import org.sodeac.common.typedtree.ModelRegistry;
import org.sodeac.common.typedtree.annotation.Association;
import org.sodeac.common.typedtree.annotation.SQLColumn;
import org.sodeac.common.typedtree.annotation.SQLTable;
import org.sodeac.common.typedtree.annotation.Transient;
import org.sodeac.common.typedtree.annotation.TypedTreeModel;
import org.sodeac.common.typedtree.annotation.Association.AssociationType;

@SQLTable(name="sdc_log_property",updatable= false)
@TypedTreeModel(modelClass=LoggingTreeModel.class)
public class LogPropertyNodeType extends CommonGenericPropertyNodeType
{
	static{ModelRegistry.getBranchNodeMetaModel(LogPropertyNodeType.class);}
	
	@SQLColumn(name="correlated_log_event_id", nullable=true)
	@Association(type=AssociationType.AGGREGATION)
	@XmlElement(name="CorrelatedLogEvent")
	public static volatile BranchNodeType<LogPropertyNodeType,LogEventNodeType> correlatedLogEvent;
	
	@Transient
	public static volatile LeafNodeType<LogPropertyNodeType,Object> originValue;
}
