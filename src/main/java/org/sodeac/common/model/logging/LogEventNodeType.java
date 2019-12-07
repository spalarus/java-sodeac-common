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

import java.util.Date;
import java.util.UUID;

import org.sodeac.common.typedtree.BranchNodeListType;
import org.sodeac.common.typedtree.BranchNodeMetaModel;
import org.sodeac.common.typedtree.LeafNodeType;
import org.sodeac.common.typedtree.ModelRegistry;
import org.sodeac.common.typedtree.annotation.Association;
import org.sodeac.common.typedtree.annotation.Domain;
import org.sodeac.common.typedtree.annotation.SQLColumn;
import org.sodeac.common.typedtree.annotation.SQLPrimaryKey;
import org.sodeac.common.typedtree.annotation.SQLReferencedByColumn;
import org.sodeac.common.typedtree.annotation.SQLTable;
import org.sodeac.common.typedtree.annotation.Association.AssociationType;
import org.sodeac.common.typedtree.annotation.SQLColumn.SQLColumnType;


@Domain(name="org.sodeac.system.logging")
@SQLTable(name="sdc_log_event",updatable= false)
public class LogEventNodeType extends BranchNodeMetaModel 
{
	static{ModelRegistry.getBranchNodeMetaModel(LogEventNodeType.class);}
	
	@SQLPrimaryKey()
	@SQLColumn(name="id",nullable=false)
	public static volatile LeafNodeType<LogEventNodeType,UUID> id;
	
	@SQLColumn(name="log_timestamp",type=SQLColumnType.TIMESTAMP, nullable=false)
	public static volatile LeafNodeType<LogEventNodeType,Date> timestamp;
	
	@SQLColumn(name="log_date",type=SQLColumnType.DATE, nullable=false)
	public static volatile LeafNodeType<LogEventNodeType,Date> date;
	
	@SQLColumn(name="log_time",type=SQLColumnType.TIME, nullable=false)
	public static volatile LeafNodeType<LogEventNodeType,Date> time;
	
	@SQLColumn(name="log_level_value",type=SQLColumnType.INTEGER, nullable=false)
	public static volatile LeafNodeType<LogEventNodeType,Integer> logLevelValue;
	
	@SQLColumn(name="log_level_name",type=SQLColumnType.VARCHAR, nullable=false, length=7)
	public static volatile LeafNodeType<LogEventNodeType,String> logLevelName;
	
	@SQLColumn(name="log_seq",type=SQLColumnType.BIGINT, nullable=false)
	public static volatile LeafNodeType<LogEventNodeType,Long> sequence;
	
	@SQLColumn(name="log_type",type=SQLColumnType.VARCHAR, nullable=false, length=128)
	public static volatile LeafNodeType<LogEventNodeType,String> type;
	
	@SQLColumn(name="log_domain",type=SQLColumnType.VARCHAR, nullable=true, length=512)
	public static volatile LeafNodeType<LogEventNodeType,String> domain;
	
	@SQLColumn(name="log_source",type=SQLColumnType.VARCHAR, nullable=true, length=512)
	public static volatile LeafNodeType<LogEventNodeType,String> source;
	
	@SQLColumn(name="log_task",type=SQLColumnType.VARCHAR, nullable=true, length=512)
	public static volatile LeafNodeType<LogEventNodeType,String> task;
	
	@SQLColumn(name="log_msg_format",type=SQLColumnType.VARCHAR, nullable=true, length=4000)
	public static volatile LeafNodeType<LogEventNodeType,String> format;
	
	@SQLColumn(name="log_msg_value",type=SQLColumnType.VARCHAR, nullable=true, length=-1)
	public static volatile LeafNodeType<LogEventNodeType,String> message;
	
	@SQLReferencedByColumn(name="sdc_log_event_id", nullable=false)
	@Association(type=AssociationType.COMPOSITION)
	public static volatile BranchNodeListType<LogEventNodeType,LogPropertyNodeType> propertyList;
}
