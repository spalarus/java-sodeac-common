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

import java.util.UUID;

import org.sodeac.common.typedtree.BranchNodeMetaModel;
import org.sodeac.common.typedtree.LeafNodeType;
import org.sodeac.common.typedtree.ModelRegistry;
import org.sodeac.common.typedtree.annotation.Domain;
import org.sodeac.common.typedtree.annotation.SQLColumn;
import org.sodeac.common.typedtree.annotation.SQLPrimaryKey;
import org.sodeac.common.typedtree.annotation.SQLTable;
import org.sodeac.common.typedtree.annotation.SQLColumn.SQLColumnType;

@Domain(name="org.sodeac.system.logging")
@SQLTable(name="sdc_log_property",updatable= false)
public class LogPropertyNodeType extends BranchNodeMetaModel 
{
	static{ModelRegistry.getBranchNodeMetaModel(LogPropertyNodeType.class);}
	
	@SQLPrimaryKey()
	@SQLColumn(name="id",nullable=false)
	public static volatile LeafNodeType<LogPropertyNodeType,UUID> id;
	
	@SQLColumn(name="log_property_type",type=SQLColumnType.VARCHAR, nullable=false, length=128)
	public static volatile LeafNodeType<LogPropertyNodeType,String> type;
	
	@SQLColumn(name="log_property_domain",type=SQLColumnType.VARCHAR, nullable=true, length=512)
	public static volatile LeafNodeType<LogPropertyNodeType,String> domain;
	
	@SQLColumn(name="log_property_key",type=SQLColumnType.VARCHAR, nullable=true, length=256)
	public static volatile LeafNodeType<LogPropertyNodeType,String> key;
	
	@SQLColumn(name="log_property_format",type=SQLColumnType.VARCHAR, nullable=true, length=4000)
	public static volatile LeafNodeType<LogPropertyNodeType,String> format;
	
	@SQLColumn(name="log_property_value",type=SQLColumnType.VARCHAR, nullable=true, length=-1)
	public static volatile LeafNodeType<LogPropertyNodeType,String> value;
}
