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
package org.sodeac.common.jdbc.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.sodeac.common.jdbc.IDBSchemaUtilsDriver;
import org.sodeac.common.misc.Driver.IDriver;
import org.sodeac.common.model.dbschema.DBSchemaNodeType;
import org.sodeac.common.model.dbschema.TableNodeType;
import org.sodeac.common.typedtree.BranchNode;

@Component(service=IDBSchemaUtilsDriver.class,immediate=true)
public class H2DBUtilDriver implements IDBSchemaUtilsDriver
{

	@Override
	public int driverIsApplicableFor(Map<String, Object> properties)
	{
		try
		{
			Connection connection = (Connection)properties.get(Connection.class.getCanonicalName());
			if(connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("H2"))
			{
				return IDriver.APPLICABLE_DEFAULT;
			}
		}
		catch (Exception e) {}
		return IDriver.APPLICABLE_NONE;
	}

	@Override
	public void setPrimaryKey
	(
		Connection connection, BranchNode<?, DBSchemaNodeType> schema,
		BranchNode<?, TableNodeType> table, Map<String, Object> tableProperties
	) throws SQLException
	{
		IDBSchemaUtilsDriver.setPrimaryKeyWithIndex(connection, schema, table, tableProperties,this);
	}

	@Override
	public String objectNameGuidelineFormat
	(
		BranchNode<?, DBSchemaNodeType> schema, Connection connection, 
		String name, String type
	)
	{
		return name == null ? name : name.toUpperCase();
	}

	
}
