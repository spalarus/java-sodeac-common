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
package org.sodeac.common.typedtree;

import java.sql.SQLXML;
import java.util.Map;
import java.util.function.BiConsumer;

import org.sodeac.common.function.ConplierBean;
import org.sodeac.common.jdbc.TypedTreeJDBCCruder.JDBCSetterDefinition;
import org.sodeac.common.typedtree.annotation.SQLColumn;
import org.sodeac.common.typedtree.annotation.SQLPrimaryKey;
import org.sodeac.common.typedtree.annotation.SQLReplace;
import org.sodeac.common.typedtree.annotation.SQLTable;
import org.sodeac.common.typedtree.annotation.SQLColumn.SQLColumnType;

public class NodeHelper
{
	public static TableNode parseTableNode(INodeType nodeType)
	{
		TableNode tableNode = new NodeHelper().new TableNode();
		
		BranchNodeMetaModel defaultInstance = (BranchNodeMetaModel)nodeType.getValueDefaultInstance();
		
		SQLTable table = (SQLTable)nodeType.getTypeClass().getAnnotation(SQLTable.class);
		
		if(table != null)
		{
			tableNode.tableName = table.name();
			tableNode.tableCatalog = table.catalog();
			tableNode.tableSchema = table.schema();
			tableNode.tableReadable = table.readable();
			tableNode.tableInsertable = table.insertable();
			tableNode.tableUpdatable = table.updatable();
		}
		
		// replaces by referenced field ???
		
		SQLReplace[] replaces = (SQLReplace[])nodeType.referencedByField().getAnnotationsByType(SQLReplace.class);
		
		for(SQLReplace replace : replaces)
		{
			for(SQLTable replacedTable : replace.table())
			{
				if(! replacedTable.name().isEmpty())
				{
					tableNode.tableName = replacedTable.name();
				}
				if(! replacedTable.catalog().isEmpty())
				{
					tableNode.tableCatalog = replacedTable.catalog();
				}
				if(! replacedTable.schema().isEmpty())
				{
					tableNode.tableSchema = replacedTable.schema();
				}
				tableNode.tableReadable = replacedTable.readable();
				tableNode.tableInsertable = replacedTable.insertable();
				tableNode.tableUpdatable = replacedTable.updatable();
			}
		}
		
		// PK
		
		LeafNodeType leafNodePrimaryKey = null;
		SQLColumn sqlColumnPrimaryKey = null;
		SQLPrimaryKey sqlPrimaryKey = null;
		
		for(LeafNodeType leafNode : defaultInstance.getLeafNodeTypeList())
		{
			SQLColumn sqlColumn = leafNode.referencedByField().getAnnotation(SQLColumn.class);
			sqlPrimaryKey = leafNode.referencedByField().getAnnotation(SQLPrimaryKey.class);
			
			for(SQLReplace replace : replaces)
			{
				if(! leafNode.getNodeName().equals(replace.nodeName()))
				{
					continue;
				}
				for(SQLColumn replacedColumn : replace.column())
				{
					
					sqlColumn = replacedColumn;
					break;
				}
			}
			if(sqlColumn == null)
			{
				continue;
			}
			
			if(sqlPrimaryKey == null)
			{
				continue;
			}
			
			if(leafNodePrimaryKey != null)
			{
				throw new IllegalArgumentException("Multiple PKs not suppoerted");
			}
			leafNodePrimaryKey = leafNode;
			sqlColumnPrimaryKey = sqlColumn;
		}
		
		if(leafNodePrimaryKey != null)
		{
			tableNode.primaryKeyLeafNode = leafNodePrimaryKey;
		}
		if(sqlColumnPrimaryKey != null)
		{
			tableNode.primaryKeyName = sqlColumnPrimaryKey.name();
			tableNode.primaryKeyNullable = sqlColumnPrimaryKey.nullable();
			tableNode.primaryKeyType =  sqlColumnPrimaryKey.type();
			tableNode.primaryKeyLength = sqlColumnPrimaryKey.length();
			tableNode.primaryKeyReadable = sqlColumnPrimaryKey.readable();
			tableNode.primaryKeyInsertable = sqlColumnPrimaryKey.insertable();
			tableNode.primaryKeyUpdatable = sqlColumnPrimaryKey.updatable();
			tableNode.primaryKeyStaticDefaultValue = sqlColumnPrimaryKey.staticDefaultValue();
			tableNode.primaryKeyFunctionalDefaultValue = sqlColumnPrimaryKey.functionalDefaultValue();
			tableNode.primaryKeyOnInsert = sqlColumnPrimaryKey.onInsert();
			tableNode.primaryKeyOnUpdate  = sqlColumnPrimaryKey.onUpdate();
			tableNode.primaryKeyOnUpsert  = sqlColumnPrimaryKey.onUpsert();
			tableNode.primaryKeyNode2JDBC  = sqlColumnPrimaryKey.node2JDBC();
			tableNode.primaryKeyJDBC2Node  = sqlColumnPrimaryKey.JDBC2Node();
			
			if(tableNode.primaryKeyName.isEmpty() && (leafNodePrimaryKey != null))
			{
				tableNode.primaryKeyName = leafNodePrimaryKey.getNodeName();
			}
			
			if(tableNode.primaryKeyOnInsert == SQLColumn.NoConsumer.class)
			{
				tableNode.primaryKeyOnInsert = null;
			}
			if(tableNode.primaryKeyOnUpdate == SQLColumn.NoConsumer.class)
			{
				tableNode.primaryKeyOnUpdate = null;
			}
			if(tableNode.primaryKeyOnUpsert == SQLColumn.NoConsumer.class)
			{
				tableNode.primaryKeyOnUpsert = null;
			}
			if(tableNode.primaryKeyNode2JDBC == SQLColumn.NoNode2JDBC.class)
			{
				tableNode.primaryKeyNode2JDBC = null;
			}
			if(tableNode.primaryKeyJDBC2Node == SQLColumn.NoJDBC2Node.class)
			{
				tableNode.primaryKeyJDBC2Node = null;
			}
		}
		
		if(sqlPrimaryKey != null)
		{
			tableNode.primaryKeySequence = sqlPrimaryKey.sequence();
			tableNode.primaryKeyAutoGenerated = sqlPrimaryKey.autoGenerated();
		}
		
		return tableNode;
	}
	
	public class TableNode
	{
		private String tableName = null;
		private String tableCatalog = null;
		private String tableSchema = null;
		private boolean tableReadable = true;
		private boolean tableInsertable = true;
		private boolean tableUpdatable = true;
		private LeafNodeType primaryKeyLeafNode = null;
		private String primaryKeyName = null;
		private boolean primaryKeyNullable = false;
		private SQLColumnType primaryKeyType =  null;
		private int primaryKeyLength = 0;
		private boolean primaryKeyReadable = true;
		private boolean primaryKeyInsertable = true;
		private boolean primaryKeyUpdatable = false;
		private String primaryKeyStaticDefaultValue = null;
		private String primaryKeyFunctionalDefaultValue = null;
		private Class<? extends BiConsumer<Node<? extends BranchNodeMetaModel,?>, Map<String,?>>> primaryKeyOnInsert = null;
		private Class<? extends BiConsumer<Node<? extends BranchNodeMetaModel,?>, Map<String,?>>> primaryKeyOnUpdate  = null;
		private Class<? extends BiConsumer<Node<? extends BranchNodeMetaModel,?>, Map<String,?>>> primaryKeyOnUpsert  = null;
		private Class<? extends BiConsumer<Node<? extends BranchNodeMetaModel,?>, ConplierBean<?>>> primaryKeyNode2JDBC  = null;
		private Class<? extends BiConsumer<ConplierBean<?>, Node<? extends BranchNodeMetaModel, ?>>> primaryKeyJDBC2Node  = null;
		private String primaryKeySequence =  null;
		private boolean primaryKeyAutoGenerated = false;
		
		public String getTableName()
		{
			return tableName;
		}
		public String getTableCatalog()
		{
			return tableCatalog;
		}
		public String getTableSchema()
		{
			return tableSchema;
		}
		public boolean isTableReadable()
		{
			return tableReadable;
		}
		public boolean isTableInsertable()
		{
			return tableInsertable;
		}
		public boolean isTableUpdatable()
		{
			return tableUpdatable;
		}
		public LeafNodeType getPrimaryKeyLeafNode()
		{
			return primaryKeyLeafNode;
		}
		public String getPrimaryKeyName()
		{
			return primaryKeyName;
		}
		public boolean isPrimaryKeyNullable()
		{
			return primaryKeyNullable;
		}
		public SQLColumnType getPrimaryKeyType()
		{
			return primaryKeyType;
		}
		public int getPrimaryKeyLength()
		{
			return primaryKeyLength;
		}
		public boolean isPrimaryKeyReadable()
		{
			return primaryKeyReadable;
		}
		public boolean isPrimaryKeyInsertable()
		{
			return primaryKeyInsertable;
		}
		public boolean isPrimaryKeyUpdatable()
		{
			return primaryKeyUpdatable;
		}
		public String getPrimaryKeyStaticDefaultValue()
		{
			return primaryKeyStaticDefaultValue;
		}
		public String getPrimaryKeyFunctionalDefaultValue()
		{
			return primaryKeyFunctionalDefaultValue;
		}
		public Class<? extends BiConsumer<Node<? extends BranchNodeMetaModel, ?>, Map<String, ?>>> getPrimaryKeyOnInsert()
		{
			return primaryKeyOnInsert;
		}
		public Class<? extends BiConsumer<Node<? extends BranchNodeMetaModel, ?>, Map<String, ?>>> getPrimaryKeyOnUpdate()
		{
			return primaryKeyOnUpdate;
		}
		public Class<? extends BiConsumer<Node<? extends BranchNodeMetaModel, ?>, Map<String, ?>>> getPrimaryKeyOnUpsert()
		{
			return primaryKeyOnUpsert;
		}
		public Class<? extends BiConsumer<Node<? extends BranchNodeMetaModel, ?>, ConplierBean<?>>> getPrimaryKeyNode2JDBC()
		{
			return primaryKeyNode2JDBC;
		}
		public Class<? extends BiConsumer<ConplierBean<?>, Node<? extends BranchNodeMetaModel, ?>>> getPrimaryKeyJDBC2Node()
		{
			return primaryKeyJDBC2Node;
		}
		public String getPrimaryKeySequence()
		{
			return primaryKeySequence;
		}
		public boolean isPrimaryKeyAutoGenerated()
		{
			return primaryKeyAutoGenerated;
		}
	}

}
