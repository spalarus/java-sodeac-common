package org.sodeac.common.jdbc;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.sodeac.common.jdbc.IColumnType.ColumnType;
import org.sodeac.common.model.dbschema.ColumnNodeType;
import org.sodeac.common.model.dbschema.DBSchemaNodeType;
import org.sodeac.common.model.dbschema.DBSchemaTreeModel;
import org.sodeac.common.model.dbschema.ForeignKeyNodeType;
import org.sodeac.common.model.dbschema.PrimaryKeyNodeType;
import org.sodeac.common.model.dbschema.TableNodeType;
import org.sodeac.common.model.logging.LogEventNodeType;
import org.sodeac.common.typedtree.ITypedTreeModelParserHandler;
import org.sodeac.common.typedtree.ModelRegistry;
import org.sodeac.common.typedtree.TypedTreeMetaModel.RootBranchNode;
import org.sodeac.common.typedtree.BranchNode;
import org.sodeac.common.typedtree.BranchNodeMetaModel;
import org.sodeac.common.typedtree.INodeType;
import org.sodeac.common.typedtree.annotation.SQLColumn;
import org.sodeac.common.typedtree.annotation.SQLPrimaryKey;
import org.sodeac.common.typedtree.annotation.SQLReferencedByColumn;
import org.sodeac.common.typedtree.annotation.SQLReplace;
import org.sodeac.common.typedtree.annotation.SQLTable;
import org.sodeac.common.typedtree.annotation.SQLColumn.SQLColumnType;

public class ParseDBSchemaHandler implements ITypedTreeModelParserHandler 
{
	public ParseDBSchemaHandler(String database)
	{
		super();
		this.schema = DBSchemaTreeModel.newSchema(database);
	}
	
	public ParseDBSchemaHandler(RootBranchNode<?, DBSchemaNodeType> schema)
	{
		super();
		this.schema = schema;
	}
	
	private RootBranchNode<?, DBSchemaNodeType> schema = null;
	private List<Table> tableList = new ArrayList<Table>();
	private List<TableDefinition> currentDefinitionList = new ArrayList<TableDefinition>();
	
	@Override
	public void startModel(BranchNodeMetaModel model, Set<INodeType<BranchNodeMetaModel, ?>> references) 
	{
		Objects.nonNull(this.schema);
		Objects.nonNull(this.tableList);
		Objects.nonNull(this.currentDefinitionList);
		
		ITypedTreeModelParserHandler.super.startModel(model, references);
		
		{
			SQLTable sqlTable = model.getClass().getDeclaredAnnotation(SQLTable.class);
			if(sqlTable != null)
			{
				String key = null;
				String schema = sqlTable.schema();
				String tableName = sqlTable.name();
				if((schema == null) || schema.isEmpty())
				{
					key = tableName;
				}
				else
				{
					key = schema + "." + tableName;
				}
				
				
				if((! tableName.isEmpty()) && (! key.isEmpty()))
				{
					Table table = null;
					for(Table tbl : this.tableList)
					{
						if(tbl.key.equals(key))
						{
							table = tbl;
							break;
						}
					}
					if(table == null)
					{
						table = new Table();
						table.key = key;
						table.schema = schema;
						table.tableName = tableName;
						this.tableList.add(table);
					}
					
					TableDefinition tableDefinition = new TableDefinition();
					tableDefinition.metaModel = model;
					tableDefinition.references = references;
					table.tableDefinitionList.add(tableDefinition);
					this.currentDefinitionList.add(tableDefinition);
				}
			}
			
		}
		
		if(references != null)
		{
			for(INodeType<BranchNodeMetaModel, ?> reference : references)
			{
				SQLReplace[] sqlReplaces = reference.referencedByField().getDeclaredAnnotationsByType(SQLReplace.class);
				if(sqlReplaces != null)
				{
					for(SQLReplace  replace : sqlReplaces)
					{
						if(replace.table() != null)
						{
							for(SQLTable sqlTable : replace.table())
							{
								String key = null;
								String schema = sqlTable.schema();
								String tableName = sqlTable.name();
								if((schema == null) || schema.isEmpty())
								{
									key = tableName;
								}
								else
								{
									key = schema + "." + tableName;
								}
								
								if((! tableName.isEmpty()) && (! key.isEmpty()))
								{
									Table table = null;
									for(Table tbl : this.tableList)
									{
										if(tbl.key.equals(key))
										{
											table = tbl;
											break;
										}
									}
									if(table == null)
									{
										table = new Table();
										table.key = key;
										table.schema = schema;
										table.tableName = tableName;
										this.tableList.add(table);
									}
									
									boolean exits = false;
									
									for(TableDefinition def : table.tableDefinitionList)
									{
										if(def.metaModel == model)
										{
											exits = true;
										}
									}
									if(! exits)
									{
										TableDefinition tableDefinition = new TableDefinition();
										tableDefinition.metaModel = model;
										tableDefinition.references = references;
										table.tableDefinitionList.add(tableDefinition);
										this.currentDefinitionList.add(tableDefinition);
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public void endModel(BranchNodeMetaModel model, Set<INodeType<BranchNodeMetaModel, ?>> references) 
	{
		ITypedTreeModelParserHandler.super.endModel(model, references);
		this.currentDefinitionList.clear();
	}

	@Override
	public void onNodeType(BranchNodeMetaModel model, INodeType<BranchNodeMetaModel, ?> nodeType) 
	{
		for(TableDefinition tableDefintion : this.currentDefinitionList)
		{
			NodeTypeDefinition nodeTypeDefinition = new NodeTypeDefinition();
			nodeTypeDefinition.nodeType = nodeType;
			tableDefintion.nodeTypeList.add(nodeTypeDefinition);
		}
	}
	
	private class Table 
	{
		private String key = null;
		private String schema = null;
		private String tableName = null;
		private BranchNode<DBSchemaNodeType,TableNodeType> tableSpec = null;
		private List<TableDefinition> tableDefinitionList = new ArrayList<>();
		
		private String idName = null;
		private Class<?> idType = null;
		private SQLColumnType idSqlType = SQLColumnType.AUTO;
		private ColumnType idColumnType = ColumnType.VARCHAR;
		private int idColumnLength = 36;
		private String sequence = null;
		private int fkCounter = 1;
		
		@Override
		public int hashCode() 
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Table other = (Table) obj;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			return true;
		}
		
		
		
	}
	
	private class TableDefinition
	{
		private BranchNodeMetaModel metaModel;
		private Set<INodeType<BranchNodeMetaModel, ?>> references;
		private List<NodeTypeDefinition> nodeTypeList = new ArrayList<NodeTypeDefinition>();
	}
	
	private class NodeTypeDefinition
	{
		private INodeType<BranchNodeMetaModel, ?> nodeType = null;
	}
	
	public RootBranchNode<?, DBSchemaNodeType> fillSchemaSpec() 
	{
		try
		{
			// first: determine id
			
			for(Table table : this.tableList)
			{
				for(TableDefinition tableDefinition : table.tableDefinitionList)
				{
					for(NodeTypeDefinition nodeTypeDefinition : tableDefinition.nodeTypeList)
					{
						SQLPrimaryKey primaryKey;
						if((primaryKey = nodeTypeDefinition.nodeType.referencedByField().getDeclaredAnnotation(SQLPrimaryKey.class)) == null)
						{
							continue;
						}
						
						SQLColumn column;
						if((column = nodeTypeDefinition.nodeType.referencedByField().getDeclaredAnnotation(SQLColumn.class)) == null)
						{
							continue;
						}
						
						String nm = nodeTypeDefinition.nodeType.getNodeName();
						if((column.name() != null) && (! column.name().isEmpty()))
						{
							nm = column.name();
						}
						
						if((nm == null) || nm.isEmpty())
						{
							continue;
						}
						
						if((table.idName != null) && (! table.idName.equals(nm)))
						{
							throw new IllegalStateException(table.key + " multiple ids: " + table.idName + " vs. " + nm);
						}
						
						table.idName = nm;
						if(column.type() != SQLColumnType.AUTO)
						{
							table.idSqlType = column.type();
						}
						table.idType = nodeTypeDefinition.nodeType.getTypeClass();
						
						if((primaryKey.sequence() != null) && (! primaryKey.sequence().isEmpty()))
						{
							table.sequence = primaryKey.sequence();
						}
						
						/*if(referencedByColumn.type == SQLColumnType.AUTO)
						{
							referencedByColumn.type = column.referencedByField().getDeclaredAnnotation(SQLColumn.class).type();
						}*/
					}
				}
				
			}
			
			// 2.: pk sequence by SQLReplace
			// TODO
			
			// 3. TableSpec and PKs
			
			for(Table table : this.tableList)
			{
				table.tableSpec = this.schema.create(DBSchemaNodeType.tables).setValue(TableNodeType.name,table.tableName);
				if((table.schema != null) && (! table.schema.isEmpty()))
				{
					table.tableSpec.setValue(TableNodeType.dbmsSchemaName, table.schema);
				}
				
				if((table.idName == null) || table.idName.isEmpty())
				{
					throw new IllegalStateException("unknown pk for " + table.key);
				}
				
				if
				(!(
					(table.idType == String.class) ||
					(table.idType == UUID.class) ||
					(table.idType == Long.class)
				))
				{
					throw new IllegalStateException("Unsupported PK type");
				}
				
				BranchNode<TableNodeType, ColumnNodeType> primaryKeyColumn =  null;
				if((table.idType == Long.class))
				{
					primaryKeyColumn = TableNodeType.createBigIntColumn(table.tableSpec, table.idName, false);
					table.idColumnType = ColumnType.BIGINT;
					table.idColumnLength = 36;
				}
				else
				{
					primaryKeyColumn = TableNodeType.createCharColumn(table.tableSpec, table.idName, false, 36);
				}
				primaryKeyColumn.create(ColumnNodeType.primaryKey);
				// TODO squence
			}
			// 4. fks by referenced By flags
			
			Map<Class<? extends BranchNodeMetaModel>,Table> modelClassToTableIndex = new HashMap<>();
			Map<Class<? extends BranchNodeMetaModel>,BranchNodeMetaModel> modelClassToModelIndex = new HashMap<>();
			for(Table table : this.tableList)
			{
				for(TableDefinition tableDefinition : table.tableDefinitionList)
				{
					modelClassToTableIndex.put(tableDefinition.metaModel.getClass(), table);
					modelClassToModelIndex.put(tableDefinition.metaModel.getClass(), tableDefinition.metaModel);
				}
			}
			for(Table table : this.tableList)
			{
				for(TableDefinition tableDefinition : table.tableDefinitionList)
				{
					if(tableDefinition.references != null)
					{
						for(INodeType<BranchNodeMetaModel, ?> nodeType : tableDefinition.references)
						{
							SQLReferencedByColumn referencedByColumn = nodeType.referencedByField().getDeclaredAnnotation(SQLReferencedByColumn.class);
							if(referencedByColumn == null)
							{
								continue;
							}
							
							String refName = referencedByColumn.name();
							if((refName == null) || refName.isEmpty())
							{
								continue;
							}
							
							Table referenceTable = modelClassToTableIndex.get(nodeType.getParentNodeClass());
							if(referenceTable == null)
							{
								throw new IllegalStateException("Table not found for field " + nodeType.getParentNodeClass().getCanonicalName() + "." + nodeType.referencedByField());
							}
							
							table.tableSpec.create(TableNodeType.columns)
								.setValue(ColumnNodeType.name, refName)
								.setValue(ColumnNodeType.columntype, referenceTable.idColumnType.name())
								.setValue(ColumnNodeType.nullable, referencedByColumn.nullable())
								.setValue(ColumnNodeType.size, referenceTable.idColumnLength)
								.create(ColumnNodeType.foreignKey)
									.setValue(ForeignKeyNodeType.constraintName, "fk" + (table.fkCounter++) + "_" + table.tableName)
									.setValue(ForeignKeyNodeType.referencedTableName, referenceTable.tableName)
									.setValue(ForeignKeyNodeType.referencedColumnName, referenceTable.idName);
							
						}
					}
				}
				
				
			}
			
			// 5. Columns
			for(Table table : this.tableList)
			{
				Set<String> columnNameSet = new HashSet<String>();
				
				for(BranchNode<TableNodeType, ColumnNodeType> column : table.tableSpec.getUnmodifiableNodeList(TableNodeType.columns))
				{
					columnNameSet.add(column.getValue(ColumnNodeType.name));
				}
				
				for(TableDefinition tableDefinition : table.tableDefinitionList)
				{
					for (NodeTypeDefinition nodeTypeDefinition : tableDefinition.nodeTypeList)
					{
						SQLColumn sqlColumn = nodeTypeDefinition.nodeType.referencedByField().getDeclaredAnnotation(SQLColumn.class);
						if(sqlColumn == null)
						{
							continue;
						}
						String columnName = sqlColumn.name();
						if((columnName == null) || columnName.isEmpty())
						{
							columnName = nodeTypeDefinition.nodeType.getNodeName();
						}
						
						if(columnNameSet.contains(columnName))
						{
							continue;
						}
						
						columnNameSet.add(columnName);
						
						ColumnType columnType = getColumnType(sqlColumn.type(), nodeTypeDefinition.nodeType.getTypeClass());
						if(columnType == columnType.VARCHAR)
						{
							if(sqlColumn.length() < 0)
							{
								columnType = columnType.CLOB;
							}
						}
							
						BranchNode<TableNodeType, ColumnNodeType>  column = table.tableSpec.create(TableNodeType.columns)
							.setValue(ColumnNodeType.name, columnName)
							.setValue(ColumnNodeType.columntype, columnType.name())
							.setValue(ColumnNodeType.nullable, sqlColumn.nullable());
						
						if((columnType == columnType.VARCHAR) || (columnType == columnType.CHAR))
						{
							column.setValue(ColumnNodeType.size, sqlColumn.length());
						}
						
						if(! sqlColumn.staticDefaultValue().isEmpty())
						{
							column.setValue(ColumnNodeType.defaultValue, sqlColumn.staticDefaultValue());
						}
						else if(! sqlColumn.functionalDefaultValue().isEmpty())
						{
							column.setValue(ColumnNodeType.defaultValue, sqlColumn.functionalDefaultValue());
							column.setValue(ColumnNodeType.defaultValueByFunction,true);
						}
						
					}
				}
				
				columnNameSet.clear();
			}
			
			return schema;
		}
		finally 
		{
			for(Table  table :  this.tableList)
			{
				for(TableDefinition tableDefinition :  table.tableDefinitionList)
				{
					for (NodeTypeDefinition nodeTypeDefinition : tableDefinition.nodeTypeList)
					{
						nodeTypeDefinition.nodeType =  null;
					}
					tableDefinition.nodeTypeList.clear();
					tableDefinition.metaModel = null;
					tableDefinition.references = null;
					tableDefinition.nodeTypeList = null;
				}
				table.tableDefinitionList.clear();
				table.key = null;
				table.schema = null;
				table.tableName = null;
				table.tableDefinitionList = null;
			}
			tableList.clear();
			tableList = null;
			currentDefinitionList.clear();
			currentDefinitionList = null;
			schema = null;
		}
	}
	
	private ColumnType getColumnType(SQLColumnType type , Class clazz)
	{
		if(type == SQLColumnType.AUTO)
		{
			if(clazz == Boolean.class)
			{
				type = SQLColumnType.BOOLEAN;
			}
			else if(clazz == Integer.class)
			{
				type = SQLColumnType.INTEGER;
			}
			else if(clazz == Long.class)
			{
				type = SQLColumnType.BIGINT;
			}
			else if(clazz == Float.class)
			{
				type = SQLColumnType.REAL;
			}
			else if(clazz == Double.class)
			{
				type = SQLColumnType.DOUBLE;
			}
			else if(clazz == Date.class)
			{
				type = SQLColumnType.TIMESTAMP;
			}
			else
			{
				type = SQLColumnType.VARCHAR;
			}
		}
		
		if(type == SQLColumnType.BIGINT)
		{
			return ColumnType.BIGINT;
		}
		if(type == SQLColumnType.INTEGER)
		{
			return ColumnType.INTEGER;
		}
		if(type == SQLColumnType.SMALLINT)
		{
			return ColumnType.SMALLINT;
		}
		if(type == SQLColumnType.REAL)
		{
			return ColumnType.REAL;
		}
		if(type == SQLColumnType.DOUBLE)
		{
			return ColumnType.DOUBLE;
		}
		if(type == SQLColumnType.TIMESTAMP)
		{
			return ColumnType.TIMESTAMP;
		}
		if(type == SQLColumnType.DATE)
		{
			return ColumnType.DATE;
		}
		if(type == SQLColumnType.TIME)
		{
			return ColumnType.TIME;
		}
		if(type == SQLColumnType.BOOLEAN)
		{
			return ColumnType.BOOLEAN;
		}
		return ColumnType.VARCHAR;
	}

	public static void main(String[] args)
	{
		ParseDBSchemaHandler parseDBSchemaHandler = new ParseDBSchemaHandler("Logging");
		ModelRegistry.parse(LogEventNodeType.class, parseDBSchemaHandler);
		BranchNode<?, DBSchemaNodeType> schemaSpec = parseDBSchemaHandler.fillSchemaSpec();
		for(BranchNode<DBSchemaNodeType, TableNodeType> tableSpec : schemaSpec.getUnmodifiableNodeList(DBSchemaNodeType.tables))
		{
			System.out.println(tableSpec.getValue(TableNodeType.name));
			for(BranchNode<TableNodeType, ColumnNodeType> columnSpec : tableSpec.getUnmodifiableNodeList(TableNodeType.columns))
			{
				System.out.println("\t: " + columnSpec.getValue(ColumnNodeType.name) + " " + columnSpec.getValue(ColumnNodeType.columntype) + " " + columnSpec.getValue(ColumnNodeType.nullable) + " " + columnSpec.getValue(ColumnNodeType.size));
				if(columnSpec.get(ColumnNodeType.primaryKey) != null)
				{
					System.out.println("\t\t: PK : " +  columnSpec.get(ColumnNodeType.primaryKey).getValue(PrimaryKeyNodeType.constraintName) + " / " + columnSpec.get(ColumnNodeType.primaryKey).getValue(PrimaryKeyNodeType.indexName));
				}
				if(columnSpec.get(ColumnNodeType.foreignKey) != null)
				{
					System.out.println("\t\t: FK : " + columnSpec.get(ColumnNodeType.foreignKey).getValue(ForeignKeyNodeType.constraintName) + " / " + columnSpec.get(ColumnNodeType.foreignKey).getValue(ForeignKeyNodeType.referencedTableName)  + "" + columnSpec.get(ColumnNodeType.foreignKey).getValue(ForeignKeyNodeType.referencedColumnName));
				}
			}
		}
		
	}

}
