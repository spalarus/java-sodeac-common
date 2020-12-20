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
package org.sodeac.common.synchronization;

import java.util.function.BiFunction;

import javax.json.JsonObject;

public class SynchronizationToolbox 
{
	public static BiFunction<JsonObject, String, String> JsonStringValueParser = (o,n) -> o.isNull(n) ? null : o.getString(n);
	public static BiFunction<JsonObject, String, Long> JsonLongValueParser = (o,n) -> o.getJsonNumber(n) == null ? null : o.getJsonNumber(n).longValue();
	public static BiFunction<JsonObject, String, Integer> JsonIntegerValueParser = (o,n) -> o.getJsonNumber(n) == null ? null : o.getJsonNumber(n).intValue();
	public static BiFunction<JsonObject, String, Double> JsonDoubleValueParser = (o,n) -> o.getJsonNumber(n) == null ? null : o.getJsonNumber(n).doubleValue();
	public static BiFunction<JsonObject, String, Boolean> JsonBooleanValueParser = (o,n) -> o.isNull(n) ? null : o.getBoolean(n);
	
}
