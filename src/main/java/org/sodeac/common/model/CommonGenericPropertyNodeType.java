package org.sodeac.common.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.sodeac.common.typedtree.LeafNodeType;
import org.sodeac.common.typedtree.ModelRegistry;
import org.sodeac.common.typedtree.annotation.IgnoreIfNull;
import org.sodeac.common.typedtree.annotation.SQLColumn;
import org.sodeac.common.typedtree.annotation.SQLColumn.SQLColumnType;
import org.sodeac.common.typedtree.annotation.SQLIndex;
import org.sodeac.common.typedtree.annotation.TypedTreeModel;

@TypedTreeModel(modelClass=CoreTreeModel.class)
public class CommonGenericPropertyNodeType extends CommonBaseBranchNodeType
{
	static{ModelRegistry.getBranchNodeMetaModel(CommonGenericPropertyNodeType.class);}
	
	@SQLColumn(name="property_type",type=SQLColumnType.VARCHAR, nullable=false, length=256)
	@XmlAttribute(name="type")
	@SQLIndex
	public static volatile LeafNodeType<CommonGenericPropertyNodeType,String> type;
	
	@SQLColumn(name="property_key",type=SQLColumnType.VARCHAR, nullable=false, length=256)
	@SQLIndex
	@XmlAttribute(name="key")
	@IgnoreIfNull
	public static volatile LeafNodeType<CommonGenericPropertyNodeType,String> key;
	
	@SQLColumn(name="property_domain",type=SQLColumnType.VARCHAR, nullable=true, length=512)
	@XmlElement(name="Domain")
	public static volatile LeafNodeType<CommonGenericPropertyNodeType,String> domain;
	
	@SQLColumn(name="property_module",type=SQLColumnType.VARCHAR, nullable=true, length=512)
	@XmlElement(name="Module")
	@IgnoreIfNull
	public static volatile LeafNodeType<CommonGenericPropertyNodeType,String> module;
	
	@SQLColumn(name="property_format",type=SQLColumnType.VARCHAR, nullable=true, length=4000)
	@XmlElement(name="Format")
	public static volatile LeafNodeType<CommonGenericPropertyNodeType,String> format;
	
	@SQLColumn(name="property_value",type=SQLColumnType.CLOB, nullable=true)
	@XmlElement(name="Value")
	public static volatile LeafNodeType<CommonGenericPropertyNodeType,String> value;
	

}
