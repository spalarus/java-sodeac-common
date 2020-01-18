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
package org.sodeac.common.jdbc.impl;

import org.osgi.service.component.annotations.Component;
import org.sodeac.common.jdbc.schemax.IDefaultCurrentTimestamp;

@Component(service=IDefaultCurrentTimestamp.class,immediate=true)
public class DefaultCurrentTimestampImpl implements IDefaultCurrentTimestamp{}
