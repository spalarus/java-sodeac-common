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
package org.sodeac.common.message.service.api;

import org.sodeac.common.xuri.URI;

public interface ISessionDrivenConnectionFactory
{
	public URI getServiceURI();
	public void setup(IServiceConnection connection);
}
