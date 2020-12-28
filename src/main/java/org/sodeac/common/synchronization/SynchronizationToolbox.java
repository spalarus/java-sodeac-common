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

import org.sodeac.common.typedtree.BranchNode;
import org.sodeac.common.typedtree.BranchNodeMetaModel;
import org.sodeac.common.typedtree.LeafNodeType;

public class SynchronizationToolbox 
{
	public static BiFunction<JsonObject, String, String> JsonStringValueParser = (o,n) -> o.isNull(n) ? null : o.getString(n);
	public static BiFunction<JsonObject, String, Long> JsonLongValueParser = (o,n) -> o.getJsonNumber(n) == null ? null : o.getJsonNumber(n).longValue();
	public static BiFunction<JsonObject, String, Integer> JsonIntegerValueParser = (o,n) -> o.getJsonNumber(n) == null ? null : o.getJsonNumber(n).intValue();
	public static BiFunction<JsonObject, String, Double> JsonDoubleValueParser = (o,n) -> o.getJsonNumber(n) == null ? null : o.getJsonNumber(n).doubleValue();
	public static BiFunction<JsonObject, String, Boolean> JsonBooleanValueParser = (o,n) -> o.isNull(n) ? null : o.getBoolean(n);
	
	public static <T extends BranchNodeMetaModel> boolean equalsByLeafNodes(BranchNode<?,T> node1, BranchNode<?,T> node2, LeafNodeType<T,?>... attributes)
	{
		if(attributes == null)
		{
			return true;
		}
		if(attributes.length == 0)
		{
			return true;
		}
		
		for(LeafNodeType<T,?> attribute : attributes)
		{
			if(! attributeEquals(attribute, node1, node2))
			{
				return false;
			}
		}
		return true;
	}
	
	public static <T extends BranchNodeMetaModel> boolean attributeEquals(LeafNodeType<T,?> attribute, BranchNode<?,T> node1, BranchNode<?,T> node2)
	{
		Object value1 = node1.getValue(attribute);
		Object value2 = node2.getValue(attribute);
		
		if((value1 == null) && (value2 != null))
		{
			return false;
		}
		if((value2 == null) && (value1 != null))
		{
			return false;
		}
		
		if(value1 == null)
		{
			return true;
		}
		return value1.equals(value2);
	}
	
}
