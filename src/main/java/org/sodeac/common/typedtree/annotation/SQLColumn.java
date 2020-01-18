/*******************************************************************************
 * Copyright (c) 2019, 2020 Sebastian Palarus
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Sebastian Palarus - initial API and implementation
 *******************************************************************************/
package org.sodeac.common.typedtree.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.sql.Connection;
import java.util.Dictionary;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.sodeac.common.jdbc.IDBSchemaUtilsDriver;
import org.sodeac.common.jdbc.IDefaultValueExpressionDriver;
import org.sodeac.common.misc.Driver.IDriver;
import org.sodeac.common.model.dbschema.ColumnNodeType;
import org.sodeac.common.typedtree.BranchNode;
import org.sodeac.common.typedtree.BranchNodeMetaModel;
import org.sodeac.common.typedtree.Node;

@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface SQLColumn 
{
	public enum SQLColumnType {AUTO,CHAR,VARCHAR,CLOB,UUID,BOOLEAN,SMALLINT,INTEGER,BIGINT,REAL,DOUBLE,TIMESTAMP,DATE,TIME,BINARY,BLOB}
	
	String name();
	boolean nullable() default true;
	SQLColumnType type() default SQLColumnType.AUTO;
	int length() default 255;
	boolean readable() default true;
	boolean insertable() default true;
	boolean updatable() default true;
	String staticDefaultValue() default "";
	Class<? extends IDefaultValueExpressionDriver> defaultValueExpressionDriver() default NoDefaultValueExpressionDriver.class;
	Class<? extends BiConsumer<Node<? extends BranchNodeMetaModel,?>, Map<String,?>>> onInsert() default NoConsumer.class;
	Class<? extends BiConsumer<Node<? extends BranchNodeMetaModel,?>, Map<String,?>>> onUpdate() default NoConsumer.class;
	Class<? extends BiConsumer<Node<? extends BranchNodeMetaModel,?>, Map<String,?>>> onUpsert() default NoConsumer.class;
	Class<? extends Function<?,?>> nodeValue2JDBC() default NoNode2JDBC.class;
	Class<? extends Function<?,?>> JDBC2NodeValue() default NoJDBC2Node.class ;
	
	public class NoConsumer implements BiConsumer<Node<? extends BranchNodeMetaModel,?>, Map<String,?>>
	{
		@Override
		public void accept(Node<? extends BranchNodeMetaModel,?> t, Map<String,?> u) {}
	}
	
	public class NoNode2JDBC implements Function<Object,Object>
	{

		@Override
		public Object apply(Object t)
		{
			return t;
		}
		
	}
	
	public class NoJDBC2Node implements Function<Object,Object>
	{

		@Override
		public Object apply(Object t)
		{
			return t;
		}
		
	}
	
	public class NoDefaultValueExpressionDriver implements IDefaultValueExpressionDriver
	{

		@Override
		public int driverIsApplicableFor(Map<String, Object> properties){return IDriver.APPLICABLE_NONE;}

		@Override
		public String createExpression(BranchNode<?,ColumnNodeType> column, Connection connection, String schema, Dictionary<String, Object> properties, IDBSchemaUtilsDriver driver){ return null; }
		
	}
	
}
