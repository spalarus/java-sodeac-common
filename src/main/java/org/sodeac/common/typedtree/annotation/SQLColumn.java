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
package org.sodeac.common.typedtree.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.function.BiConsumer;

import org.sodeac.common.function.ConplierBean;
import org.sodeac.common.typedtree.BranchNodeMetaModel;
import org.sodeac.common.typedtree.Node;

@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface SQLColumn 
{
	public enum SQLColumnType {AUTO,CHAR,VARCHAR,CLOB,BOOELAN,SMALLINT,INTEGER,BIGINT,REAL,DOUBLE,TIMESTAMP,DATE,TIME,BINARY,BLOB}
	
	String name();
	boolean nullable() default true;
	SQLColumnType type() default SQLColumnType.AUTO;
	int length() default 255;
	boolean readable() default true;
	boolean insertable() default true;
	boolean updatable() default true;
	Class<? extends BiConsumer<Node<? extends BranchNodeMetaModel,?>, Map<String,?>>> onInsert() default NoConsumer.class;
	Class<? extends BiConsumer<Node<? extends BranchNodeMetaModel,?>, Map<String,?>>> onUpdate() default NoConsumer.class;
	Class<? extends BiConsumer<Node<? extends BranchNodeMetaModel,?>, Map<String,?>>> onUpsert() default NoConsumer.class;
	Class<? extends BiConsumer<Node<? extends BranchNodeMetaModel,?>, ConplierBean<?>>> node2JDBC() default NoNode2JDBC.class;
	Class<? extends BiConsumer<ConplierBean<?>, Node<? extends BranchNodeMetaModel, ?>>> JDBC2Node() default NoJDBC2Node.class ;
	
	public class NoConsumer implements BiConsumer<Node<? extends BranchNodeMetaModel,?>, Map<String,?>>
	{
		@Override
		public void accept(Node<? extends BranchNodeMetaModel,?> t, Map<String,?> u) {}
	}
	
	public class NoNode2JDBC implements BiConsumer<Node<? extends BranchNodeMetaModel,?>, ConplierBean<?>>
	{
		@Override
		public void accept(Node<? extends BranchNodeMetaModel, ?> t, ConplierBean<?> u) {}
	}
	
	public class NoJDBC2Node implements BiConsumer<ConplierBean<?>, Node<? extends BranchNodeMetaModel, ?>>
	{
		@Override
		public void accept(ConplierBean<?> t, Node<? extends BranchNodeMetaModel, ?> u) {}
	}
}
