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
package org.sodeac.common.typedtree;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.sodeac.common.model.CoreTreeModel;
import org.sodeac.common.typedtree.annotation.Domain;
import org.sodeac.common.typedtree.annotation.IgnoreIfEmpty;
import org.sodeac.common.typedtree.annotation.IgnoreIfFalse;
import org.sodeac.common.typedtree.annotation.IgnoreIfNull;
import org.sodeac.common.typedtree.annotation.IgnoreIfTrue;
import org.sodeac.common.typedtree.annotation.XMLNodeList;

// quick and dirty - poc
public class XMLMarshaller 
{
	private String namespace;
	
	private Map<Class<? extends BranchNodeMetaModel>,XMLNodeMarshaller> nodeMarshallerIndex;
	//private Map<Class<?>, NodeTypeMarshaller> classMarshallerIndex;
	private Map<String,Function<Object, String>> toStringIndex = new HashMap<String,Function<Object, String>>();
	private Map<String,Function<String, ?>> fromStringIndex = new HashMap<String,Function<String, ?>>();
	
	
	protected XMLMarshaller(String namespace)
	{
		super();
		this.namespace = namespace;
		this.nodeMarshallerIndex = new HashMap<Class<? extends BranchNodeMetaModel>, XMLMarshaller.XMLNodeMarshaller>();
		//this.classMarshallerIndex = new HashMap<Class<?>, NodeTypeMarshaller>();
		
		toStringIndex.put(String.class.getCanonicalName(), p -> (String)p);
		fromStringIndex.put(String.class.getCanonicalName(), p -> p);
		
		toStringIndex.put(String.class.getCanonicalName(), p -> (String)p);
		fromStringIndex.put(String.class.getCanonicalName(), p -> p);
		
		toStringIndex.put(Long.class.getCanonicalName(), p -> Long.toString((Long)p));
		fromStringIndex.put(Long.class.getCanonicalName(), p -> Long.parseLong(p));
		
		toStringIndex.put(Integer.class.getCanonicalName(), p -> Integer.toString((Integer)p));
		fromStringIndex.put(Integer.class.getCanonicalName(), p -> Integer.parseInt(p));
		
		toStringIndex.put(Double.class.getCanonicalName(), p -> Double.toString((Double)p));
		fromStringIndex.put(Double.class.getCanonicalName(), p -> Double.parseDouble(p));
		
		toStringIndex.put(Boolean.class.getCanonicalName(), p -> Boolean.toString((Boolean)p));
		fromStringIndex.put(Boolean.class.getCanonicalName(), p -> Boolean.parseBoolean(p));
		
		toStringIndex.put(Date.class.getCanonicalName(), p -> 
		{
			Date date = (Date)p;
			SimpleDateFormat ISO8601Local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			TimeZone timeZone = TimeZone.getDefault(); //local JVM time zone
			ISO8601Local.setTimeZone(timeZone);
			
			DecimalFormat twoDigits = new DecimalFormat("00");
			
			int offset = ISO8601Local.getTimeZone().getOffset(date.getTime());
			String sign = "+";
			
			if (offset < 0)
			{
				offset = -offset;
				sign = "-";
			}
			int hours = offset / 3600000;
			int minutes = (offset - hours * 3600000) / 60000;
			
			String ISO8601Now = ISO8601Local.format(date) + sign + twoDigits.format(hours) + ":" + twoDigits.format(minutes);
			return ISO8601Now; 
		});
		fromStringIndex.put(Date.class.getCanonicalName(), p -> 
		{
			try
			{
				SimpleDateFormat ISO8601Local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
				TimeZone timeZone = TimeZone.getDefault(); //local JVM time zone
				ISO8601Local.setTimeZone(timeZone);
				return ISO8601Local.parse(p); 
			}
			catch (ParseException e) 
			{
				throw new RuntimeException(e);
			}
		});
		
		toStringIndex.put(UUID.class.getCanonicalName(), p -> ((UUID)p).toString());
		fromStringIndex.put(UUID.class.getCanonicalName(), p -> UUID.fromString(p));
	}
	
	protected void publish(BranchNodeMetaModel model)
	{
		Class<? extends BranchNodeMetaModel> nodeModelClass = model.getClass();
		if(this.nodeMarshallerIndex.containsKey(nodeModelClass))
		{
			throw new IllegalStateException("marshallerIndex.containsKey(nodeModelClass)");
		}
		
		XMLNodeMarshaller nodeMarshaller = new XMLNodeMarshaller(nodeModelClass);
		this.nodeMarshallerIndex.put(nodeModelClass, nodeMarshaller);
		
	}
	
	protected void build()
	{
		for(Entry<Class<? extends BranchNodeMetaModel>,XMLNodeMarshaller> entry : this.nodeMarshallerIndex.entrySet())
		{
			// TODO ModelingProcessor
			BranchNodeMetaModel metaModel = null;
			try
			{
				metaModel = (BranchNodeMetaModel) entry.getValue().nodeModelClass.newInstance();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
			for(INodeType nodeType : metaModel.getNodeTypeList())
			{
				if(nodeType instanceof LeafNodeType)
				{
					XmlAttribute xmlAttribute = nodeType.referencedByField().getAnnotation(XmlAttribute.class);
					XmlElement xmlElement = nodeType.referencedByField().getAnnotation(XmlElement.class);
					
					SubMarshallerContainer container = new SubMarshallerContainer();
					container.nodeType = nodeType;
					container.valueToString = toStringIndex.get(nodeType.getTypeClass().getCanonicalName());
					
					container.ignoreIfNull = nodeType.referencedByField().getAnnotation(IgnoreIfNull.class) != null;
					
					if(nodeType.getTypeClass() == Boolean.class)
					{
						container.ignoreIfTrue = nodeType.referencedByField().getAnnotation(IgnoreIfTrue.class) != null;
						container.ignoreIfFalse = nodeType.referencedByField().getAnnotation(IgnoreIfFalse.class) != null;
					}
					
					container.name = nodeType.getNodeName();
					if(xmlAttribute != null)
					{
						if((xmlAttribute.name() != null) && (! xmlAttribute.name().isEmpty()) && (! "##default".equals(xmlAttribute.name())))
						{
							container.name = xmlAttribute.name();
						}
					}
					else if(xmlElement != null)
					{
						if((xmlElement.name() != null) && (! xmlElement.name().isEmpty()))
						{
							container.name = xmlElement.name();
						}
					}
					
					if(container.valueToString == null)
					{
						throw new RuntimeException("Serializer for class " + nodeType.getTypeClass().getCanonicalName()  + " not found");
					}
					if(xmlAttribute != null)
					{
						container.runner = container::runLeafNodeAsAttribute;
						entry.getValue().attributeSubMarshallerList.add(container);
					}
					else
					{
						container.runner = container::runLeafNodeAsElement;
						entry.getValue().elementMarshallerList.add(container);
					}
					
					
				}
				
				if(nodeType instanceof BranchNodeType)
				{
					XmlElement xmlElement = nodeType.referencedByField().getAnnotation(XmlElement.class);
					SubMarshallerContainer container = new SubMarshallerContainer();
					
					container.ignoreIfNull = nodeType.referencedByField().getAnnotation(IgnoreIfNull.class) != null;
					
					container.nodeType = nodeType;
					container.marshaller = this.nodeMarshallerIndex.get(nodeType.getTypeClass());
					container.name = nodeType.getNodeName();
					
					if(xmlElement != null)
					{
						if((xmlElement.name() != null) && (! xmlElement.name().isEmpty()) && (! "##default".equals(xmlElement.name())))
						{
							container.name = xmlElement.name();
						}
					}
					
					if(container.marshaller == null)
					{
						throw new RuntimeException("Marshaller for class " + nodeType.getTypeClass().getCanonicalName()  + " not found");
					}
					container.runner = container::runBranchNode;
					
					entry.getValue().elementMarshallerList.add(container);
				}
				
				if(nodeType instanceof BranchNodeListType)
				{
					XmlElement xmlElement = nodeType.referencedByField().getAnnotation(XmlElement.class);
					XMLNodeList xmlNodeList =  nodeType.referencedByField().getAnnotation(XMLNodeList.class);
					
					SubMarshallerContainer container = new SubMarshallerContainer();
					container.ignoreIfEmpty = nodeType.referencedByField().getAnnotation(IgnoreIfEmpty.class) != null;
					
					container.nodeType = nodeType;
					container.marshaller = this.nodeMarshallerIndex.get(nodeType.getTypeClass());
					container.name = nodeType.getNodeName();
					if(xmlElement != null)
					{
						if((xmlElement.name() != null) && (! xmlElement.name().isEmpty()) && (! "##default".equals(xmlElement.name())))
						{
							container.name = xmlElement.name();
						}
					}
					
					container.singleName = nodeType.getTypeClass().getSimpleName();
					
					if((xmlNodeList != null) && (xmlNodeList.childElementName() != null) && (! xmlNodeList.childElementName().isEmpty()))
					{
						container.singleName = xmlNodeList.childElementName();
					}
					
					if((xmlNodeList != null) && (! xmlNodeList.listElement()))
					{
						container.listElement = false;
					}
					
					if(container.marshaller == null)
					{
						throw new RuntimeException("Marshaller for class " + nodeType.getTypeClass().getCanonicalName()  + " not found");
					}
					container.runner = container::runBranchNodeList;
					
					entry.getValue().elementMarshallerList.add(container);
				}
			}
			

		}
	}
	
	public void marshal(BranchNode<?,?> node, OutputStream os, boolean closeStream) throws IOException, XMLStreamException, FactoryConfigurationError
	{
		try
		{
			XMLNodeMarshaller rootMarshaller = this.nodeMarshallerIndex.get(node.getNodeType().getTypeClass());
			if(rootMarshaller ==  null)
			{
				throw new IllegalStateException("Marshaller not found for " + node.getNodeType().getTypeClass());
			}
			XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter( new OutputStreamWriter(os, "utf-8"));
			try
			{
				String rootName = node.getNodeType().getNodeName();
				XmlElement xmlElement = node.getNodeType().referencedByField().getAnnotation(XmlElement.class);
				if((xmlElement != null) &&  (xmlElement.name() != null) && (! xmlElement.name().isEmpty()) && (! "##default".equals(xmlElement.name())))
				{
					rootName = xmlElement.name();
				}
				out.writeStartDocument();
				out.writeStartElement(rootName);
				rootMarshaller.marshal(out, node);
				out.writeEndElement();
				out.writeEndDocument();
			}
			finally 
			{
				out.close();
			}
		}
		finally 
		{
			if(closeStream)
			{
				os.close();
			}
		}
	}
	
	private static class XMLNodeMarshaller
	{
		protected XMLNodeMarshaller(Class<? extends BranchNodeMetaModel> nodeModelClass)
		{
			super();
			this.nodeModelClass = nodeModelClass;
		}
		
		protected Class<? extends BranchNodeMetaModel> nodeModelClass = null;
		protected List<SubMarshallerContainer> attributeSubMarshallerList = new ArrayList<>();
		protected List<SubMarshallerContainer> elementMarshallerList = new ArrayList<>();

		protected void marshal(XMLStreamWriter out, BranchNode<? extends BranchNodeMetaModel, ? extends BranchNodeMetaModel > node) throws XMLStreamException 
		{
			for(SubMarshallerContainer container : attributeSubMarshallerList)
			{
				container.runner.accept(out, node);
			}
			for(SubMarshallerContainer container : elementMarshallerList)
			{
				container.runner.accept(out, node);
			}
		}
	}
	
	private static class SubMarshallerContainer
	{
		protected INodeType nodeType;
		protected BiConsumer<XMLStreamWriter, BranchNode> runner = null;
		protected Function<Object, String> valueToString = null;
		protected XMLNodeMarshaller marshaller = null;
		protected String name = null;
		protected String singleName = null;
		protected boolean listElement = true;
		boolean ignoreIfNull = false;
		boolean ignoreIfTrue = false;
		boolean ignoreIfFalse = false;
		boolean ignoreIfEmpty = false;
		
		
		protected void runLeafNodeAsElement(XMLStreamWriter out, BranchNode node)
		{
			try
			{
				
				LeafNode<?,?> leafNode = node.get((LeafNodeType)nodeType);
				if(leafNode.getValue() == null)
				{
					if(ignoreIfNull)
					{
						return;
					}
					out.writeStartElement(name);
					out.writeAttribute("null", Boolean.TRUE.toString());
					out.writeEndElement();
				}
				else
				{
					if((ignoreIfFalse) && (!((Boolean)leafNode.getValue()).booleanValue()))
					{
						return;
					}
					if((ignoreIfTrue) && ((Boolean)leafNode.getValue()).booleanValue())
					{
						return;
					}
					out.writeStartElement(name);
					out.writeCharacters(this.valueToString.apply(leafNode.getValue()));
					out.writeEndElement();
				}
				
			}
			catch (Exception e) 
			{
				throw new RuntimeException(e);
			}
		}
		
		protected void runLeafNodeAsAttribute(XMLStreamWriter out, BranchNode node)
		{
			try
			{
				LeafNode<?,?> leafNode = node.get((LeafNodeType)nodeType);
				if(leafNode.getValue() == null)
				{
					if(ignoreIfNull)
					{
						return;
					}
					out.writeAttribute(this.name, "");
				}
				else
				{
					if((ignoreIfFalse) && (!((Boolean)leafNode.getValue()).booleanValue()))
					{
						return;
					}
					if((ignoreIfTrue) && ((Boolean)leafNode.getValue()).booleanValue())
					{
						return;
					}
					out.writeAttribute(this.name, this.valueToString.apply(leafNode.getValue()));
				}
			}
			catch (Exception e) 
			{
				throw new RuntimeException(e);
			}
		}
		
		protected void runBranchNode(XMLStreamWriter out, BranchNode node)
		{
			try
			{
				BranchNode<?,?> branchNode = node.get((BranchNodeType)nodeType);
				if(branchNode == null)
				{
					if(ignoreIfNull)
					{
						return;
					}
					out.writeStartElement(name);
					out.writeAttribute("null", Boolean.TRUE.toString());
					out.writeEndElement();
				}
				else
				{
					out.writeStartElement(name);
					this.marshaller.marshal(out, branchNode);
					out.writeEndElement();
				}
				
			}
			catch (Exception e) 
			{
				throw new RuntimeException(e);
			}
		}
		
		protected void runBranchNodeList(XMLStreamWriter out, BranchNode node)
		{
			try
			{
				List<BranchNode<?,?>> branchNodeList = node.getUnmodifiableNodeList((BranchNodeListType)nodeType);
				
				if(ignoreIfEmpty && branchNodeList.isEmpty())
				{
					return;
				}
				
				if(listElement)
				{
					
					out.writeStartElement(name);
				}
				
				for(BranchNode<?,?> branchNode : branchNodeList)
				{
					out.writeStartElement(singleName);
					if(branchNode == null)
					{
						out.writeAttribute("null", Boolean.TRUE.toString());
					}
					else
					{
						this.marshaller.marshal(out, branchNode);
					}
					out.writeEndElement();
				}
				if(listElement)
				{
					out.writeEndElement();
				}
			}
			catch (Exception e) 
			{
				throw new RuntimeException(e);
			}
		}
		
	}
	
	public static XMLMarshaller getForTreeModel(Class<? extends TypedTreeMetaModel<?>> modelClass)
	{
		ParseXMLMarshallerHandler xmlMarsallerHandler = new ParseXMLMarshallerHandler(modelClass);
		
		ModelRegistry.parse(modelClass, xmlMarsallerHandler);
		
		return xmlMarsallerHandler.getXMLMarshaller();
	}
	
	private static class ParseXMLMarshallerHandler implements ITypedTreeModelParserHandler 
	{
		private String namespace = null;
		private XMLMarshaller marshaller = null; 
		private volatile boolean buildDone = false;
		
		public ParseXMLMarshallerHandler(Class<? extends TypedTreeMetaModel<?>> modelClass)
		{
			super();
			Domain domain = modelClass.getDeclaredAnnotation(Domain.class);
			if(domain != null)
			{
				namespace = "http://" + domain.name() + "/" + modelClass.getCanonicalName();
			}
			this.marshaller = new XMLMarshaller(namespace);
		}
		
		@Override
		public void startModel(BranchNodeMetaModel model, Set<INodeType<BranchNodeMetaModel, ?>> references) 
		{
			ITypedTreeModelParserHandler.super.startModel(model, references);
			marshaller.publish(model);
		}

		@Override
		public void endModel(BranchNodeMetaModel model, Set<INodeType<BranchNodeMetaModel, ?>> references) 
		{
			ITypedTreeModelParserHandler.super.endModel(model, references);
		}

		@Override
		public void onNodeType(BranchNodeMetaModel model, INodeType<BranchNodeMetaModel, ?> nodeType) {}
		
		public XMLMarshaller getXMLMarshaller()
		{
			if(! buildDone)
			{
				buildDone = true;
				this.marshaller.build();
			}
			return this.marshaller;
		}

	}
	
}
