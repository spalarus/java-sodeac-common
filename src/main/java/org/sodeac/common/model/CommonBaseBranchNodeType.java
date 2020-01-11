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
package org.sodeac.common.model;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.sodeac.common.typedtree.BranchNodeMetaModel;
import org.sodeac.common.typedtree.LeafNode;
import org.sodeac.common.typedtree.LeafNodeType;
import org.sodeac.common.typedtree.ModelRegistry;
import org.sodeac.common.typedtree.Node;
import org.sodeac.common.typedtree.annotation.IgnoreIfFalse;
import org.sodeac.common.typedtree.annotation.IgnoreIfNull;
import org.sodeac.common.typedtree.annotation.IgnoreIfTrue;
import org.sodeac.common.typedtree.annotation.SQLColumn;
import org.sodeac.common.typedtree.annotation.SQLColumn.SQLColumnType;
import org.sodeac.common.typedtree.annotation.SQLPrimaryKey;
import org.sodeac.common.typedtree.annotation.TypedTreeModel;

@TypedTreeModel(modelClass=CoreTreeModel.class)
public class CommonBaseBranchNodeType extends BranchNodeMetaModel
{
	static{ModelRegistry.getBranchNodeMetaModel(CoreTreeModel.class);}
	
	@SQLColumn(name="id",type=SQLColumnType.CHAR,length=36,nullable=false,updatable=false,onInsert=GenerateUUIDIfNull.class)
	@SQLPrimaryKey
	@XmlAttribute(name="id")
	public static volatile LeafNodeType<CommonBaseBranchNodeType,UUID> id;
	
	@SQLColumn(name="persist_version_no",type=SQLColumnType.BIGINT) // TODO nullable=false / updates
	@XmlAttribute(name="pversionnumber")
	@IgnoreIfNull
	public static volatile LeafNodeType<CommonBaseBranchNodeType,Long> persistVersionNumber;
	
	@SQLColumn(name="persist_version_id",type=SQLColumnType.CHAR,length=36,nullable=false,onUpsert=GenerateUUID.class)
	@XmlAttribute(name="pversionid")
	@IgnoreIfNull
	public static volatile LeafNodeType<CommonBaseBranchNodeType,UUID> persistVersionId;
	
	@SQLColumn(name="create_timestamp",type=SQLColumnType.TIMESTAMP,onInsert=CurrentTimestamp.class,updatable=false)
	@XmlElement(name="CreateTimestamp")
	@IgnoreIfNull
	public static volatile LeafNodeType<CommonBaseBranchNodeType,Date> createTimestamp;
	
	@SQLColumn(name="persist_timestamp",type=SQLColumnType.TIMESTAMP,onUpsert=CurrentTimestamp.class)
	@XmlElement(name="PersistTimestamp")
	@IgnoreIfNull
	public static volatile LeafNodeType<CommonBaseBranchNodeType,Date> persistTimestamp;
	
	@SQLColumn(name="create_node_id",type=SQLColumnType.CHAR,length=36,updatable=false)
	@XmlElement(name="CreateNodeId")
	@IgnoreIfNull
	public static volatile LeafNodeType<CommonBaseBranchNodeType,UUID> createNodeId;
	
	@SQLColumn(name="persist_node_id",type=SQLColumnType.CHAR,length=36)
	@XmlElement(name="PersistNodeId")
	@IgnoreIfNull
	public static volatile LeafNodeType<CommonBaseBranchNodeType,UUID> persistNodeId;

	@SQLColumn(name="create_client_uri",type=SQLColumnType.VARCHAR,length=1080,updatable=false)
	@XmlElement(name="CreateClientURI")
	@IgnoreIfNull
	public static volatile LeafNodeType<CommonBaseBranchNodeType,String> createClientURI;
	
	@SQLColumn(name="persist_client_uri",type=SQLColumnType.VARCHAR,length=1080)
	@XmlElement(name="PersistClientURI")
	@IgnoreIfNull
	public static volatile LeafNodeType<CommonBaseBranchNodeType,String> persistClientURI;
	
	@SQLColumn(name="record_enabled",type=SQLColumnType.BOOLEAN,nullable=false,onInsert=TrueIfNull.class)
	@XmlAttribute(name="enabled")
	@IgnoreIfTrue
	public static volatile LeafNodeType<CommonBaseBranchNodeType,Boolean> enabled;
	
	@SQLColumn(name="record_disabled",type=SQLColumnType.BOOLEAN,nullable=false,onInsert=FalseIfNull.class)
	@XmlAttribute(name="disabled")
	@IgnoreIfFalse
	public static volatile LeafNodeType<CommonBaseBranchNodeType,Boolean> disabled;
	
	@SQLColumn(name="record_valid_from",type=SQLColumnType.TIMESTAMP)
	@XmlAttribute(name="validfrom")
	@IgnoreIfNull
	public static volatile LeafNodeType<CommonBaseBranchNodeType,Date> validFrom;
	
	@SQLColumn(name="record_valid_through",type=SQLColumnType.TIMESTAMP)
	@XmlAttribute(name="validthrough")
	@IgnoreIfNull
	public static volatile LeafNodeType<CommonBaseBranchNodeType,Date> validThrough;
	
	public static class GenerateUUIDIfNull implements BiConsumer<Node<? extends BranchNodeMetaModel,?>, Map<String,?>>
	{
		@Override
		public void accept(Node<? extends BranchNodeMetaModel,?> t, Map<String,?> u)
		{
			if(t.getNodeType().getTypeClass() == UUID.class)
			{
				if(((LeafNode<?,UUID>)t).getValue() == null)
				{
					((LeafNode<?,UUID>)t).setValue(UUID.randomUUID());
				}
			}
			else if(t.getNodeType().getTypeClass() == String.class)
			{
				if((((LeafNode<?,String>)t).getValue() == null) || ((LeafNode<?,String>)t).getValue().isEmpty())
				{
					((LeafNode<?,String>)t).setValue(UUID.randomUUID().toString());
				}
			}
			else
			{
				throw new IllegalStateException("Unsupported type for generating UUID: " + t.getNodeType().getTypeClass());
			}
		}
	}
	
	public static class GenerateUUID implements BiConsumer<Node<? extends BranchNodeMetaModel,?>, Map<String,?>>
	{
		@Override
		public void accept(Node<? extends BranchNodeMetaModel,?> t, Map<String,?> u)
		{
			if(t.getNodeType().getTypeClass() == UUID.class)
			{
				((LeafNode<?,UUID>)t).setValue(UUID.randomUUID());
			}
			else if(t.getNodeType().getTypeClass() == String.class)
			{
				((LeafNode<?,String>)t).setValue(UUID.randomUUID().toString());
			}
			else
			{
				throw new IllegalStateException("Unsupported type for generating UUID: " + t.getNodeType().getTypeClass());
			}
		}
	}
	
	public static class CurrentTimestamp implements BiConsumer<Node<? extends BranchNodeMetaModel,?>, Map<String,?>>
	{
		@Override
		public void accept(Node<? extends BranchNodeMetaModel,?> t, Map<String,?> u)
		{
			((LeafNode<?,Date>)t).setValue(new Date());
		}
	}
	
	public static class TrueIfNull implements BiConsumer<Node<? extends BranchNodeMetaModel,?>, Map<String,?>>
	{
		@Override
		public void accept(Node<? extends BranchNodeMetaModel,?> t, Map<String,?> u)
		{
			if(((LeafNode)t).getValue() == null) 
			{
				((LeafNode<?,Boolean>)t).setValue(true);
			}
		}
	}
	
	public static class FalseIfNull implements BiConsumer<Node<? extends BranchNodeMetaModel,?>, Map<String,?>>
	{
		@Override
		public void accept(Node<? extends BranchNodeMetaModel,?> t, Map<String,?> u)
		{
			if(((LeafNode)t).getValue() == null) 
			{
				((LeafNode<?,Boolean>)t).setValue(false);
			}
		}
	}
}
