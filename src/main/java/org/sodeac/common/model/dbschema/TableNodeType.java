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
package org.sodeac.common.model.dbschema;

import org.sodeac.common.jdbc.DBSchemaUtils.DBSchemaEvent;
import org.sodeac.common.function.ExceptionConsumer;
import org.sodeac.common.jdbc.IColumnType;
import org.sodeac.common.typedtree.BranchNode;
import org.sodeac.common.typedtree.BranchNodeListType;
import org.sodeac.common.typedtree.BranchNodeMetaModel;
import org.sodeac.common.typedtree.LeafNodeType;
import org.sodeac.common.typedtree.ModelRegistry;

public class TableNodeType extends BranchNodeMetaModel
{
	static{ModelRegistry.getBranchNodeMetaModel(TableNodeType.class);}
	
	public static volatile LeafNodeType<TableNodeType,String> name;
	public static volatile LeafNodeType<TableNodeType,String> dbmsSchemaName;	
	public static volatile LeafNodeType<TableNodeType,String> tableSpace;
	public static volatile LeafNodeType<TableNodeType,Boolean> quotedName;
	public static volatile BranchNodeListType<TableNodeType,ColumnNodeType> columns;
	public static volatile BranchNodeListType<TableNodeType,IndexNodeType> indices;
	public static volatile BranchNodeListType<TableNodeType,EventConsumerNodeType> consumers;
	
	public static BranchNode<TableNodeType, ColumnNodeType> createCharColumn(BranchNode<?, TableNodeType> table, String columnName,boolean nullable, int length)
	{
		return table.create(TableNodeType.columns)
			.setValue(ColumnNodeType.columntype, IColumnType.ColumnType.CHAR.name())
			.setValue(ColumnNodeType.name, columnName)
			.setValue(ColumnNodeType.size, length)
			.setValue(ColumnNodeType.nullable, nullable);
	}
	
	public static BranchNode<TableNodeType, ColumnNodeType> createVarcharColumn(BranchNode<?, TableNodeType> table, String columnName,boolean nullable, int length)
	{
		return table.create(TableNodeType.columns)
			.setValue(ColumnNodeType.columntype, IColumnType.ColumnType.VARCHAR.name())
			.setValue(ColumnNodeType.name, columnName)
			.setValue(ColumnNodeType.size, length)
			.setValue(ColumnNodeType.nullable, nullable);
	}
	
	public static BranchNode<TableNodeType, ColumnNodeType> createClobColumn(BranchNode<?, TableNodeType> table, String columnName,boolean nullable)
	{
		return table.create(TableNodeType.columns)
			.setValue(ColumnNodeType.columntype, IColumnType.ColumnType.CLOB.name())
			.setValue(ColumnNodeType.name, columnName)
			.setValue(ColumnNodeType.nullable, nullable);
	}
	
	public static BranchNode<TableNodeType, ColumnNodeType> createBooleanColumn(BranchNode<?, TableNodeType> table, String columnName,boolean nullable)
	{
		return table.create(TableNodeType.columns)
			.setValue(ColumnNodeType.columntype, IColumnType.ColumnType.BOOLEAN.name())
			.setValue(ColumnNodeType.name, columnName)
			.setValue(ColumnNodeType.nullable, nullable);
	}
	
	public static BranchNode<TableNodeType, ColumnNodeType> createBooleanColumnWithDefault(BranchNode<?, TableNodeType> table, String columnName,boolean defaultValue)
	{
		return table.create(TableNodeType.columns)
			.setValue(ColumnNodeType.columntype, IColumnType.ColumnType.BOOLEAN.name())
			.setValue(ColumnNodeType.name, columnName)
			.setValue(ColumnNodeType.nullable, false)
			.setValue(ColumnNodeType.defaultValue, new Boolean(defaultValue).toString());
	}
	
	public static BranchNode<TableNodeType, ColumnNodeType> createSmallIntColumn(BranchNode<?, TableNodeType> table, String columnName,boolean nullable)
	{
		return table.create(TableNodeType.columns)
			.setValue(ColumnNodeType.columntype, IColumnType.ColumnType.SMALLINT.name())
			.setValue(ColumnNodeType.name, columnName)
			.setValue(ColumnNodeType.nullable, nullable);
	}
	
	public static BranchNode<TableNodeType, ColumnNodeType> createSmallIntColumn(BranchNode<?, TableNodeType> table, String columnName,short defaultValue)
	{
		return table.create(TableNodeType.columns)
			.setValue(ColumnNodeType.columntype, IColumnType.ColumnType.SMALLINT.name())
			.setValue(ColumnNodeType.name, columnName)
			.setValue(ColumnNodeType.nullable, false)
			.setValue(ColumnNodeType.defaultValue, new Short(defaultValue).toString());
	}
	
	public static BranchNode<TableNodeType, ColumnNodeType> createIntegerColumn(BranchNode<?, TableNodeType> table, String columnName,boolean nullable)
	{
		return table.create(TableNodeType.columns)
			.setValue(ColumnNodeType.columntype, IColumnType.ColumnType.INTEGER.name())
			.setValue(ColumnNodeType.name, columnName)
			.setValue(ColumnNodeType.nullable, nullable);
	}
	
	public static BranchNode<TableNodeType, ColumnNodeType> createIntegerColumn(BranchNode<?, TableNodeType> table, String columnName,int defaultValue)
	{
		return table.create(TableNodeType.columns)
			.setValue(ColumnNodeType.columntype, IColumnType.ColumnType.INTEGER.name())
			.setValue(ColumnNodeType.name, columnName)
			.setValue(ColumnNodeType.nullable, false)
			.setValue(ColumnNodeType.defaultValue, new Integer(defaultValue).toString());
	}
	
	public static BranchNode<TableNodeType, ColumnNodeType> createBigIntColumn(BranchNode<?, TableNodeType> table, String columnName,boolean nullable)
	{
		return table.create(TableNodeType.columns)
			.setValue(ColumnNodeType.columntype, IColumnType.ColumnType.BIGINT.name())
			.setValue(ColumnNodeType.name, columnName)
			.setValue(ColumnNodeType.nullable, nullable);
	}
	
	public static BranchNode<TableNodeType, ColumnNodeType> createBigIntColumn(BranchNode<?, TableNodeType> table, String columnName,long defaultValue)
	{
		return table.create(TableNodeType.columns)
			.setValue(ColumnNodeType.columntype, IColumnType.ColumnType.BIGINT.name())
			.setValue(ColumnNodeType.name, columnName)
			.setValue(ColumnNodeType.nullable, false)
			.setValue(ColumnNodeType.defaultValue, new Long(defaultValue).toString());
	}
	
	public static BranchNode<TableNodeType, ColumnNodeType> createRealColumn(BranchNode<?, TableNodeType> table, String columnName,boolean nullable)
	{
		return table.create(TableNodeType.columns)
			.setValue(ColumnNodeType.columntype, IColumnType.ColumnType.REAL.name())
			.setValue(ColumnNodeType.name, columnName)
			.setValue(ColumnNodeType.nullable, nullable);
	}
	
	public static BranchNode<TableNodeType, ColumnNodeType> createRealColumn(BranchNode<?, TableNodeType> table, String columnName,float defaultValue)
	{
		return table.create(TableNodeType.columns)
			.setValue(ColumnNodeType.columntype, IColumnType.ColumnType.REAL.name())
			.setValue(ColumnNodeType.name, columnName)
			.setValue(ColumnNodeType.nullable, false)
			.setValue(ColumnNodeType.defaultValue, new Float(defaultValue).toString());
	}
	
	public static BranchNode<TableNodeType, ColumnNodeType> createDoubleColumn(BranchNode<?, TableNodeType> table, String columnName,boolean nullable)
	{
		return table.create(TableNodeType.columns)
			.setValue(ColumnNodeType.columntype, IColumnType.ColumnType.DOUBLE.name())
			.setValue(ColumnNodeType.name, columnName)
			.setValue(ColumnNodeType.nullable, nullable);
	}
	
	public static BranchNode<TableNodeType, ColumnNodeType> createDoubleColumn(BranchNode<?, TableNodeType> table, String columnName,double defaultValue)
	{
		return table.create(TableNodeType.columns)
			.setValue(ColumnNodeType.columntype, IColumnType.ColumnType.DOUBLE.name())
			.setValue(ColumnNodeType.name, columnName)
			.setValue(ColumnNodeType.nullable, false)
			.setValue(ColumnNodeType.defaultValue, new Double(defaultValue).toString());
	}
	
	public static BranchNode<TableNodeType, ColumnNodeType> createTimeColumn(BranchNode<?, TableNodeType> table, String columnName,boolean nullable)
	{
		return table.create(TableNodeType.columns)
			.setValue(ColumnNodeType.columntype, IColumnType.ColumnType.TIME.name())
			.setValue(ColumnNodeType.name, columnName)
			.setValue(ColumnNodeType.nullable, nullable);
	}
	
	public static BranchNode<TableNodeType, ColumnNodeType> createTimeColumnDefaultCurrent(BranchNode<?, TableNodeType> table, String columnName)
	{
		return table.create(TableNodeType.columns)
			.setValue(ColumnNodeType.columntype, IColumnType.ColumnType.TIME.name())
			.setValue(ColumnNodeType.name, columnName)
			.setValue(ColumnNodeType.nullable, false)
			.setValue(ColumnNodeType.defaultValue, "NOW")
			.setValue(ColumnNodeType.defaultValueByFunction,true);
	}
	
	public static BranchNode<TableNodeType, ColumnNodeType> createDateColumn(BranchNode<?, TableNodeType> table, String columnName,boolean nullable)
	{
		return table.create(TableNodeType.columns)
			.setValue(ColumnNodeType.columntype, IColumnType.ColumnType.DATE.name())
			.setValue(ColumnNodeType.name, columnName)
			.setValue(ColumnNodeType.nullable, nullable);
	}
	
	public static BranchNode<TableNodeType, ColumnNodeType> createDateColumnDefaultCurrent(BranchNode<?, TableNodeType> table, String columnName)
	{
		return table.create(TableNodeType.columns)
			.setValue(ColumnNodeType.columntype, IColumnType.ColumnType.DATE.name())
			.setValue(ColumnNodeType.name, columnName)
			.setValue(ColumnNodeType.nullable, false)
			.setValue(ColumnNodeType.defaultValue, "NOW")
			.setValue(ColumnNodeType.defaultValueByFunction,true);
	}
	
	public static BranchNode<TableNodeType, ColumnNodeType> createTimestampColumn(BranchNode<?, TableNodeType> table, String columnName,boolean nullable)
	{
		return table.create(TableNodeType.columns)
			.setValue(ColumnNodeType.columntype, IColumnType.ColumnType.TIMESTAMP.name())
			.setValue(ColumnNodeType.name, columnName)
			.setValue(ColumnNodeType.nullable, nullable);
	}
	
	public static BranchNode<TableNodeType, ColumnNodeType> createTimestampColumnDefaultCurrent(BranchNode<?, TableNodeType> table, String columnName)
	{
		return table.create(TableNodeType.columns)
			.setValue(ColumnNodeType.columntype, IColumnType.ColumnType.TIMESTAMP.name())
			.setValue(ColumnNodeType.name, columnName)
			.setValue(ColumnNodeType.nullable, false)
			.setValue(ColumnNodeType.defaultValue, "NOW")
			.setValue(ColumnNodeType.defaultValueByFunction,true);
	}
	
	public static BranchNode<TableNodeType, ColumnNodeType> createBinaryColumn(BranchNode<?, TableNodeType> table, String columnName,boolean nullable)
	{
		return table.create(TableNodeType.columns)
			.setValue(ColumnNodeType.columntype, IColumnType.ColumnType.BINARY.name())
			.setValue(ColumnNodeType.name, columnName)
			.setValue(ColumnNodeType.nullable, nullable);
	}
	
	public static BranchNode<TableNodeType, ColumnNodeType> createBlobColumn(BranchNode<?, TableNodeType> table, String columnName,boolean nullable)
	{
		return table.create(TableNodeType.columns)
			.setValue(ColumnNodeType.columntype, IColumnType.ColumnType.BLOB.name())
			.setValue(ColumnNodeType.name, columnName)
			.setValue(ColumnNodeType.nullable, nullable);
	}
	
	public static BranchNode<TableNodeType, IndexNodeType> createIndex(BranchNode<?, TableNodeType> table, boolean unique, String keyName, String... columnNames )
	{
		if(columnNames == null)
		{
			throw new IllegalStateException("no columns defined");
		}
		if(columnNames.length == 0)
		{
			throw new IllegalStateException("no columns defined");
		}
		BranchNode<TableNodeType,IndexNodeType> index = table.create(TableNodeType.indices)
				.setValue(IndexNodeType.unique, unique)
				.setValue(IndexNodeType.name, keyName);
		for(String columnName : columnNames)
		{
			index.create(IndexNodeType.members).setValue(IndexColumnNodeType.columName, columnName);
		}
		
		return index;
	}
	
	public static void addConsumer(BranchNode<?, TableNodeType> table, ExceptionConsumer<DBSchemaEvent> consumer)
	{
		table.create(TableNodeType.consumers).setValue(EventConsumerNodeType.eventConsumer, consumer);
	}
}
