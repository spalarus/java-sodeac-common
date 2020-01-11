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
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.sodeac.common.misc.Driver.IDriver;
import org.sodeac.common.model.dbschema.ColumnNodeType;
import org.sodeac.common.model.dbschema.DBSchemaNodeType;
import org.sodeac.common.model.dbschema.TableNodeType;
import org.sodeac.common.typedtree.BranchNode;
import org.sodeac.common.jdbc.IColumnType;
import org.sodeac.common.jdbc.IDBSchemaUtilsDriver;

@Component(name="DefaultColumnType", service=IColumnType.class)
public class DefaultColumnTypeImpl implements IColumnType
{
	private static final List<String> typeList = Arrays.asList(new String[] 
	{
		ColumnType.CHAR.toString(),
		ColumnType.VARCHAR.toString(),
		ColumnType.CLOB.toString(),
		ColumnType.BOOLEAN.toString(),
		ColumnType.SMALLINT.toString(),
		ColumnType.INTEGER.toString(),
		ColumnType.BIGINT.toString(),
		ColumnType.REAL.toString(),
		ColumnType.DOUBLE.toString(),
		ColumnType.TIMESTAMP.toString(),
		ColumnType.DATE.toString(),
		ColumnType.TIME.toString(),
		ColumnType.BINARY.toString(),
		ColumnType.BLOB.toString()
	});
	
	@Override
	public int driverIsApplicableFor(Map<String, Object> properties)
	{
		return IDriver.APPLICABLE_FALLBACK;
	}
	
	@Override
	public List<String> getTypeList()
	{
		return typeList;
	}
	

	@Override
	public String getTypeExpression
	(
		Connection connection, 
		BranchNode<?, DBSchemaNodeType> schema, 
		BranchNode<?, TableNodeType> table,
		BranchNode<?, ColumnNodeType> column,
		String dbProduct,
		IDBSchemaUtilsDriver schemaDriver
	) throws SQLException
	{
		String columnType = column.getValue(ColumnNodeType.columnType);
		if((columnType == null) || ColumnType.VARCHAR.toString().equalsIgnoreCase(columnType) || ColumnType.CHAR.toString().equalsIgnoreCase(columnType) )
		{
			String type = (columnType == null) || ColumnType.VARCHAR.toString().equalsIgnoreCase(columnType) ? "VARCHAR" : "CHAR";
			
			if((column.getValue(ColumnNodeType.size) != null) && (column.getValue(ColumnNodeType.size).intValue() > 0))
			{
				if(ColumnType.VARCHAR.toString().equalsIgnoreCase(type) && connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("Oracle"))
				{
					return schemaDriver.objectNameGuidelineFormat(schema, connection, type + "(" + column.getValue(ColumnNodeType.size) + " CHAR)", "COLUMN_TYPE") ;
				}
				return schemaDriver.objectNameGuidelineFormat(schema, connection, type + "(" + column.getValue(ColumnNodeType.size) + ")", "COLUMN_TYPE") ;
			}
			else
			{
				return schemaDriver.objectNameGuidelineFormat(schema, connection, type , "COLUMN_TYPE") ;
			}
		}
		
		if(ColumnType.CLOB.toString().equalsIgnoreCase(columnType))
		{
			if(connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("PostgreSQL"))
			{
				return schemaDriver.objectNameGuidelineFormat(schema, connection, "text", "COLUMN_TYPE");
			}
		}
		if(ColumnType.REAL.toString().equalsIgnoreCase(columnType))
		{
			if(connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("PostgreSQL"))
			{
				return schemaDriver.objectNameGuidelineFormat(schema, connection, "float4", "COLUMN_TYPE");
			}
			if(connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("Oracle"))
			{
				return schemaDriver.objectNameGuidelineFormat(schema, connection, "FLOAT(63)", "COLUMN_TYPE");
			}
		}
		if(ColumnType.DOUBLE.toString().equalsIgnoreCase(columnType))
		{
			if(connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("PostgreSQL"))
			{
				return schemaDriver.objectNameGuidelineFormat(schema, connection, "float8", "COLUMN_TYPE");
			}
			if(connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("Oracle"))
			{
				return schemaDriver.objectNameGuidelineFormat(schema, connection, "FLOAT(126)", "COLUMN_TYPE");
			}
		}
		if(ColumnType.BINARY.toString().equalsIgnoreCase(columnType))
		{
			if(connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("PostgreSQL"))
			{
				return schemaDriver.objectNameGuidelineFormat(schema, connection, "bytea", "COLUMN_TYPE");
			}
			if(connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("Oracle"))
			{
				return schemaDriver.objectNameGuidelineFormat(schema, connection, "LONG RAW", "COLUMN_TYPE");
			}
		}
		
		if(ColumnType.BLOB.toString().equalsIgnoreCase(columnType))
		{
			if(connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("PostgreSQL"))
			{
				return schemaDriver.objectNameGuidelineFormat(schema, connection, "oid", "COLUMN_TYPE");
			}
		}
		
		if(ColumnType.BOOLEAN.toString().equalsIgnoreCase(columnType))
		{
			if(connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("Oracle"))
			{
				return schemaDriver.objectNameGuidelineFormat(schema, connection, "CHAR(1)", "COLUMN_TYPE");
			}
		}
		
		if(ColumnType.SMALLINT.toString().equalsIgnoreCase(columnType))
		{
			if(connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("Oracle"))
			{
				return schemaDriver.objectNameGuidelineFormat(schema, connection, "NUMBER(5)", "COLUMN_TYPE");
			}
		}
		
		if(ColumnType.INTEGER.toString().equalsIgnoreCase(columnType))
		{
			if(connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("Oracle"))
			{
				return schemaDriver.objectNameGuidelineFormat(schema, connection, "NUMBER(10)", "COLUMN_TYPE");
			}
		}
		
		if(ColumnType.BIGINT.toString().equalsIgnoreCase(columnType))
		{
			if(connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("Oracle"))
			{
				return schemaDriver.objectNameGuidelineFormat(schema, connection, "NUMBER(19)", "COLUMN_TYPE");
			}
		}
		
		if(ColumnType.TIME.toString().equalsIgnoreCase(columnType))
		{
			if(connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("Oracle"))
			{
				return schemaDriver.objectNameGuidelineFormat(schema, connection, "DATE", "COLUMN_TYPE");
			}
		}
		
		return schemaDriver.objectNameGuidelineFormat(schema, connection, columnType, "COLUMN_TYPE");
	}

	@Override
	public String getDefaultValueExpression
	(
		Connection connection, 
		BranchNode<?, DBSchemaNodeType> schema, 
		BranchNode<?, TableNodeType> table,
		BranchNode<?, ColumnNodeType> column,
		String dbProduct,
		IDBSchemaUtilsDriver schemaDriver
	) throws SQLException
	{
		String columnType = column.getValue(ColumnNodeType.columnType);
		String defaultValue = column.getValue(ColumnNodeType.defaultValue);
		boolean defaultValueByFunction = column.getValue(ColumnNodeType.defaultValueByFunction) == null ? false : column.getValue(ColumnNodeType.defaultValueByFunction).booleanValue();
		
		if((columnType == null) || ColumnType.VARCHAR.toString().equalsIgnoreCase(columnType) || ColumnType.CHAR.toString().equalsIgnoreCase(columnType) )
		{
			if(defaultValue != null)
			{
				return "DEFAULT " + (defaultValueByFunction ? schemaDriver.getFunctionExpression(defaultValue) :  defaultValue );
			}
		}
		if(ColumnType.BOOLEAN.toString().equalsIgnoreCase(columnType))
		{
			if((defaultValue != null) && (!defaultValue.isEmpty()))
			{
				return "DEFAULT " + 
					(
						defaultValue.equalsIgnoreCase("true") ? 
						schemaDriver.objectNameGuidelineFormat(schema, connection, "TRUE" , "BOOLEAN") : 
						schemaDriver.objectNameGuidelineFormat(schema, connection, "FALSE" , "BOOLEAN")
					);
			}
		}
		
		if(ColumnType.TIMESTAMP.toString().equals(columnType))
		{
			if((defaultValue != null) && (!defaultValue.isEmpty()))
			{
				if(defaultValue.equals("NOW"))
				{
					return "DEFAULT " + schemaDriver.getFunctionExpression(IColumnType.Function.CURRENT_TIMESTAMP.toString()); 
				}
			}
		}
		else if(ColumnType.DATE.toString().equals(columnType))
		{
			if((defaultValue != null) && (!defaultValue.isEmpty()))
			{
				if(defaultValue.equals("NOW"))
				{
					return "DEFAULT " + schemaDriver.getFunctionExpression(IColumnType.Function.CURRENT_DATE.toString()); 
				}
			}
		}
		else if(ColumnType.TIME.toString().equals(columnType))
		{
			if((defaultValue != null) && (!defaultValue.isEmpty()))
			{
				if(defaultValue.equals("NOW"))
				{
					return "DEFAULT " + schemaDriver.getFunctionExpression(IColumnType.Function.CURRENT_TIME.toString()); 
				}
			}	
		}
		if((defaultValue != null) && (defaultValue.length() > 0))
		{
			
			return "DEFAULT " + (defaultValueByFunction ? schemaDriver.getFunctionExpression(defaultValue) : defaultValue);
		}
		
		return new String();
	}
	

}
