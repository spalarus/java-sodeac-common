/*******************************************************************************
 * Copyright (c) 2018, 2019 Sebastian Palarus
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Sebastian Palarus - initial API and implementation
 *******************************************************************************/
package org.sodeac.common.jdbc.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.sodeac.common.jdbc.IColumnType;
import org.sodeac.common.jdbc.IDBSchemaUtilsDriver;
import org.sodeac.common.misc.Driver;
import org.sodeac.common.misc.Driver.IDriver;
import org.sodeac.common.model.dbschema.ColumnNodeType;
import org.sodeac.common.model.dbschema.DBSchemaNodeType;
import org.sodeac.common.model.dbschema.TableNodeType;
import org.sodeac.common.typedtree.BranchNode;

@Component(service=IDBSchemaUtilsDriver.class,immediate=true)
public class PGDBUtilDriver implements IDBSchemaUtilsDriver
{

	@Override
	public int driverIsApplicableFor(Map<String, Object> properties)
	{
		try
		{
			Connection connection = (Connection)properties.get(Connection.class.getCanonicalName());
			if(connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("PostgreSQL"))
			{
				return IDriver.APPLICABLE_DEFAULT;
			}
		}
		catch (Exception e) {}
		return IDriver.APPLICABLE_NONE;
	}

	@Override
	public boolean columnExists
	(
		Connection connection, BranchNode<?, DBSchemaNodeType> schema,
		BranchNode<?, TableNodeType> table, BranchNode<?, ColumnNodeType> column,
		Map<String, Object> columnProperties
	) throws SQLException
	{
		boolean columnExists = IDBSchemaUtilsDriver.super.columnExists(connection, schema, table, column, columnProperties);
		String defaultValue = (String)columnProperties.get("COLUMN_COLUMN_DEF");
		if(defaultValue != null)
		{
			// example: 'defaultvalue'::character varying
			
			String[] splitArray = defaultValue.split("::");
			if(splitArray.length == 2)
			{
				if((! splitArray[1].contains("'")) && (! splitArray[0].isEmpty()))
				{
					columnProperties.put("COLUMN_COLUMN_DEF", splitArray[0]);
				}
			}
		}
		return columnExists;
	}

	@Override
	public String determineColumnType
	(
		Connection connection, BranchNode<?, DBSchemaNodeType> schema,
		BranchNode<?, TableNodeType> table, BranchNode<?, ColumnNodeType> column,
		Map<String, Object> columnProperties
	) throws SQLException
	{
		if(columnProperties == null)
		{
			return null;
		}
		if(columnProperties.get("COLUMN_TYPE_NAME") == null)
		{
			return null;
		}
		
		if("bpchar".equalsIgnoreCase(columnProperties.get("COLUMN_TYPE_NAME").toString()))
		{
			return IColumnType.ColumnType.CHAR.toString();
		}
		if("bytea".equalsIgnoreCase(columnProperties.get("COLUMN_TYPE_NAME").toString()))
		{
			return IColumnType.ColumnType.BINARY.toString();
		}
		if("oid".equalsIgnoreCase(columnProperties.get("COLUMN_TYPE_NAME").toString()))
		{
			return IColumnType.ColumnType.BLOB.toString();
		}
		return IDBSchemaUtilsDriver.super.determineColumnType(connection, schema, table, column, columnProperties);
	}

	@Override
	public void setValidColumnProperties
	(
		Connection connection, BranchNode<?, DBSchemaNodeType> schema,
		BranchNode<?, TableNodeType> table, BranchNode<?, ColumnNodeType> column,
		Map<String, Object> columnProperties
	) throws SQLException
	{
		String schemaName = connection.getSchema();
		if((schema.getValue(DBSchemaNodeType.dbmsSchemaName) != null) && (! schema.getValue(DBSchemaNodeType.dbmsSchemaName).isEmpty()))
		{
			schemaName = schema.getValue(DBSchemaNodeType.dbmsSchemaName);
		}
		if((table.getValue(TableNodeType.dbmsSchemaName) != null) && (! table.getValue(TableNodeType.dbmsSchemaName).isEmpty()))
		{
			schemaName = table.getValue(TableNodeType.dbmsSchemaName);
		}
		boolean tableQuoted = false;
		if(table.getValue(TableNodeType.quotedName) != null)
		{
			tableQuoted = table.getValue(TableNodeType.quotedName).booleanValue();
		}
		
		boolean columnQuoted = false;
		if(column.getValue(ColumnNodeType.quotedName) != null)
		{
			columnQuoted = column.getValue(ColumnNodeType.quotedName).booleanValue();
		}
		
		String tablePart = tableQuoted ? 
				" " + schemaName + "." + quotedChar() +  "" + table.getValue(TableNodeType.name) + "" + quotedChar() +  " " :
				" " + schemaName + "." + objectNameGuidelineFormat(schema, connection, table.getValue(TableNodeType.name), "TABLE") + " " ;
			
		String columnPart =  columnQuoted ? 
				" " + quotedChar() +  "" + column.getValue(ColumnNodeType.name) + "" + quotedChar() +  " " :
				" " + objectNameGuidelineFormat(schema, connection, column.getValue(ColumnNodeType.name), "COLUMN") + " " ;
		
		boolean nullable = column.getValue(ColumnNodeType.nullable) == null ? true : column.getValue(ColumnNodeType.nullable).booleanValue();
		
		if(columnProperties.get("INVALID_NULLABLE") != null)
		{
			PreparedStatement updateNullableStatment = null;
			try
			{
				String nullableStatement = "ALTER TABLE  " + tablePart +  " ALTER COLUMN " + columnPart + " " + ( nullable ? " DROP NOT NULL " : " SET NOT NULL" ) ;
				updateNullableStatment = connection.prepareStatement(nullableStatement);
				updateNullableStatment.executeUpdate();
			}
			finally
			{
				if(updateNullableStatment != null)
				{
					try
					{
						updateNullableStatment.close();
					}
					catch(Exception e){}
				}
			}
		}
		
		if
		(
			(columnProperties.get("INVALID_SIZE") != null) ||
			(columnProperties.get("INVALID_TYPE") != null)
		)
		{
			PreparedStatement createColumnStatement = null;
			try
			{
				StringBuilder sqlBuilder = new StringBuilder("ALTER TABLE  " + tablePart + " ALTER " + columnPart + " TYPE ");
				
				Map<String,Object> driverProperties = new HashMap<String, Object>();
				driverProperties.put(Connection.class.getCanonicalName(), connection);
				driverProperties.put("SCHEMA", schema);
				driverProperties.put("TABLE", table);
				driverProperties.put("COLUMN", column);
				IColumnType columnType = Driver.getSingleDriver(IColumnType.class, driverProperties);
				
				if(columnType == null)
				{
					throw new SQLException("No ColumnType Provider found for \"" + column.getValue(ColumnNodeType.columnType) + "\"");
				}
				
				sqlBuilder.append(" " + columnType.getTypeExpression(connection, schema, table, column, "TODO", this));
				
				createColumnStatement = connection.prepareStatement(sqlBuilder.toString());
				createColumnStatement.executeUpdate();
			}
			finally
			{
				if(createColumnStatement != null)
				{
					try
					{
						createColumnStatement.close();
					}
					catch(Exception e){}
				}
			}
		}
		
		if
		(
			(columnProperties.get("INVALID_DEFAULT") != null)
		)
		{
			PreparedStatement createColumnStatement = null;
			try
			{
				if(column.getValue(ColumnNodeType.defaultValue) == null)
				{
					createColumnStatement = connection.prepareStatement("ALTER TABLE  " + tablePart + " ALTER " + columnPart + " DROP DEFAULT ");
					createColumnStatement.executeUpdate();
				}
				else
				{
					StringBuilder sqlBuilder = new StringBuilder("ALTER TABLE  " + tablePart + " ALTER " + columnPart + " SET  ");
					
					Map<String,Object> driverProperties = new HashMap<String, Object>();
					driverProperties.put(Connection.class.getCanonicalName(), connection);
					driverProperties.put("SCHEMA", schema);
					driverProperties.put("TABLE", table);
					driverProperties.put("COLUMN", column);
					IColumnType columnType = Driver.getSingleDriver(IColumnType.class, driverProperties);
					
					if(columnType == null)
					{
						throw new SQLException("No ColumnType Provider found for \"" + column.getValue(ColumnNodeType.columnType) + "\"");
					}
					
					sqlBuilder.append(" " + columnType.getDefaultValueExpression(connection, schema, table, column, "TODO", this));
					
					createColumnStatement = connection.prepareStatement(sqlBuilder.toString());
					createColumnStatement.executeUpdate();
				}
			}
			finally
			{
				if(createColumnStatement != null)
				{
					try
					{
						createColumnStatement.close();
					}
					catch(Exception e){}
				}
			}
		}
	}

	@Override
	public String objectNameGuidelineFormat
	(
		BranchNode<?, DBSchemaNodeType> schema, Connection connection, 
		String name,String type
	)
	{
		return name == null ? name : name.toLowerCase();
	}

	@Override
	public String tableSpaceAppendix
	(
		Connection connection, BranchNode<?, DBSchemaNodeType> schema,
		BranchNode<?, TableNodeType> table, Map<String, Object> properties, 
		String tableSpace, String type
	)
	{
		if("PRIMARYKEY".equals(type))
		{
			return  " USING INDEX TABLESPACE " + tableSpace;
		}
		
		return  " TABLESPACE " + tableSpace;
	}

	@Override
	public String getFunctionExpression(String function)
	{
		return function ;
	}
	
	
}
