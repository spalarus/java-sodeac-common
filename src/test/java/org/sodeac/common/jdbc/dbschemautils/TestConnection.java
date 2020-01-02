/*******************************************************************************
 * Copyright (c) 2018, 2020 Sebastian Palarus
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Sebastian Palarus - initial API and implementation
 *******************************************************************************/
package org.sodeac.common.jdbc.dbschemautils;

import java.sql.Connection;

public class TestConnection
{
	public TestConnection()
	{
		super();
	}
	
	public TestConnection(boolean enabled)
	{
		super();
		this.enabled = enabled;
	}
	public Connection connection;
	public boolean enabled = false;
	public String dbmsSchemaName = null;
	public String tableSpaceIndex = null;
	public String tableSpaceData = null;
}
