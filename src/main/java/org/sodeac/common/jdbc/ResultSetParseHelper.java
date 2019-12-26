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
package org.sodeac.common.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import org.sodeac.common.function.ConplierBean;
import org.sodeac.common.jdbc.ResultSetParseHelper.ResultSetParseHelperBuilder.NodeConfiguration;
import org.sodeac.common.jdbc.ResultSetParseHelper.ResultSetParseHelperBuilder.ParsePhase;

/**
 * 
 * A helper class to convert  data from more or less complex linked {@link ResultSet} to a tree like / object based data structure.
 * <br><br>
 * The rows are divided into clusters. After all parse phases process a cluster, the next cluster is processed by parse phases. 
 *  <br><br>
 *  The requirements:<br>
 *  1. The processed {@link ResultSet} must be scrollable<br>
 *  2. The data for a root object must be delivered in consecutively rows<br>
 *  3. Each object requires an unique-key-column<br>
 *  4. The root configurations of all project phases must specify the same key column<br>
 *  <br>
 * 
 * @author Sebastian Palarus
 *
 */
public class ResultSetParseHelper implements AutoCloseable
{
	public static final String DEFAULT_PHASE = "DEFAULT";
	
	private ResultSetParseHelper()
	{
		super();
	}
	
	private List<ParsePhase> parsePhaseList = null;
	private volatile boolean closable = true;
	
	public ResultSetParseHelper setUnclosable()
	{
		this.closable = false;
		return this;
	}
	
	public void parse(PreparedStatement preparedStatement, Object superRoot, int rootPhaseSize) throws Exception
	{
		if(rootPhaseSize < 1)
		{
			throw new IllegalStateException("rootPhaseSize < 1");
		}
		
		ResultSet resultSet = preparedStatement.executeQuery();
		try
		{
			resultSet.last();
			int lastRow = resultSet.getRow();
			resultSet.beforeFirst();
			
			if(lastRow < 1)
			{
				return;
			}
			
			List<ParsePhaseInstance> parsePhaseInstanceList = new ArrayList<ParsePhaseInstance>();
			
			NodeConfiguration rootConfiguration = null;
			
			for(ParsePhase parsePhase : this.parsePhaseList)
			{
				if(parsePhase.nodeConfiguration == null)
				{
					continue;
				}
				ParsePhaseInstance parsePhaseInstance = new ParsePhaseInstance();
				parsePhaseInstance.parsePhase = parsePhase;
				parsePhaseInstance.rootNode = new Node();
				parsePhaseInstance.rootNode.configuration = parsePhase.nodeConfiguration;
				parsePhaseInstance.rootNode.objects = new HashMap<>();
				parsePhaseInstance.rootNode.childNodes = new HashMap<>();
				parsePhaseInstanceList.add(parsePhaseInstance);
				if(rootConfiguration == null)
				{
					rootConfiguration = (NodeConfiguration)parsePhase.getNodeConfiguration();
				}
			}
			
			if(parsePhaseInstanceList.isEmpty())
			{
				return;
			}
				
			Cursor cursor = new Cursor<>();
			cursor.setResultSet(resultSet);
			
			int beforeFirstInCluster = -1;
			boolean parse = true;
			while(parse)
			{
				if(beforeFirstInCluster > -1)
				{
					resultSet.absolute(beforeFirstInCluster);
				}
				
				int firstInCluster = -1;
				int lastInCluster = -1;
				
				Set<Object> ids = new HashSet<>();
				
				while(resultSet.next())
				{
					if(firstInCluster == -1)
					{
						firstInCluster = resultSet.getRow();
					}
					
					ids.add(fetchId(rootConfiguration.idType, rootConfiguration.idColumnName, resultSet));
					if(ids.size() > rootPhaseSize)
					{
						break;
					}
					
					lastInCluster = resultSet.getRow();
				}
				
				beforeFirstInCluster = lastInCluster;
				
				if(lastInCluster == lastRow)
				{
					parse = false;
				}
				
				List<Object> rootNodeList = new ArrayList<>();
				Set<Object> rootNodeSet = new HashSet<>();
				Object lastRootNode = null;
				
				for(ParsePhaseInstance parsePhaseInstance : parsePhaseInstanceList)
				{
					lastRootNode = null;
					
					if(firstInCluster == 1)
					{
						resultSet.beforeFirst();
					}
					else
					{
						resultSet.absolute(firstInCluster - 1);
					}
					
					while(resultSet.next())
					{
						cursor.setRootObject(null);
						cursor.setParentObject(superRoot);
						
						Object rootNode = parsePhaseInstance.rootNode.fetch(resultSet, cursor, true);
						Objects.requireNonNull(rootNode);
						
						if(! rootNode.equals(rootNode))
						{
							throw new IllegalStateException("root nodes must equals to self");
						}
						
						if(! rootNodeSet.contains(rootNode))
						{
							rootNodeSet.add(rootNode);
							rootNodeList.add(rootNode);
							
							if((lastRootNode != null) && (parsePhaseInstance.parsePhase.consumerRootNodeComplete != null))
							{
								parsePhaseInstance.parsePhase.consumerRootNodeComplete.accept(lastRootNode);
							}
							lastRootNode = rootNode;
						}
						
						if(lastInCluster == resultSet.getRow())
						{
							break;
						}
					}
					
					if((lastRootNode != null) && (parsePhaseInstance.parsePhase.consumerRootNodeComplete != null))
					{
						parsePhaseInstance.parsePhase.consumerRootNodeComplete.accept(lastRootNode);
					}
					
					if((! rootNodeList.isEmpty()) && (parsePhaseInstance.parsePhase.consumerClusterComplete != null))
					{
						parsePhaseInstance.parsePhase.consumerClusterComplete.accept(rootNodeList);
					}
					
					parsePhaseInstance.rootNode.reset();
					rootNodeList.clear();
					rootNodeSet.clear();
					lastRootNode = null;
				}
				
				rootNodeList = null;
				rootNodeSet = null;
				lastRootNode = null;
				
				if(resultSet.getRow() == lastRow)
				{
					parse = false;
				}
			}
			
			parsePhaseInstanceList.forEach(i -> i.clear());
			parsePhaseInstanceList.clear();
			cursor.clear();
			
			parsePhaseInstanceList = null;
			cursor = null;
		}
		finally 
		{
			resultSet.close();
		}
		
		
	}
	
	private class ParsePhaseInstance
	{
		ParsePhase parsePhase;
		Node rootNode;
		
		private void clear()
		{
			this.parsePhase = null;
			if(this.rootNode != null)
			{
				this.rootNode.clear();
				this.rootNode = null;
			}
		}
	}
	
	protected class Node
	{
		private Node parent = null;
		private Object id = null;
		private NodeConfiguration configuration = null;
		private Map<Object,Object> objects = null;				// PK / Object
		private Map<Object,List<Node>> childNodes = null;		// PK / ConfigurationName,ChildNode
		private Object lastId = UUID.randomUUID().toString();
		private Object lastObject = null;
		
		private void reset()
		{
			NodeConfiguration configurationBackup = this.configuration;
			this.clear();
			this.configuration = configurationBackup;
			this.objects = new HashMap<>();
			this.childNodes = new HashMap<>();
		}
		
		private void clear()
		{
			if(this.objects != null)
			{
				this.objects.clear();
			}
			if(this.childNodes != null)
			{
				for(List<Node> child : this.childNodes.values())
				{
					if(child != null )
					{
						child.forEach(n -> n.clear());
					}
					child.clear();
				}
				this.childNodes.clear();
			}
			this.parent = null;
			this.configuration = null;
			this.objects = null;
			this.childNodes = null;
			this.lastId = null;
			this.id = null;
			this.lastObject = null;
		}
		
		protected Object fetch(ResultSet resultSet, Cursor cursor, boolean isRoot) throws Exception
		{
			Object currentId = fetchId(configuration.idType, configuration.idColumnName, resultSet);
			cursor.setId(currentId);
			List<Node> childs = this.childNodes.get(currentId);
			
			if
			(
				(!((currentId == null) && (lastId == null))) &&
				((currentId == null) || (!currentId.equals(lastId)))
			)
			{
				this.lastId = currentId;
				
				if(objects.containsKey(currentId))
				{
					this.lastObject = objects.get(currentId);
				}
				else
				{
					if(isRoot)
					{
						this.reset();
					}
					Object object = null;
					if(isRoot)
					{
						cursor.rootObject = null;
					}
					if((currentId == null) && (configuration.recordParserIfNull != null))
					{
						object = configuration.recordParserIfNull.parse(cursor);
					}
					else if((currentId != null) && (configuration.recordParser != null))
					{
						object = configuration.recordParser.parse(cursor);
					}
					objects.put(currentId, object);
					this.lastObject = object;
					
					if((configuration.childList != null) && (! configuration.childList.isEmpty()))
					{
						childs = new ArrayList<>();
						this.childNodes.put(currentId, childs);
						
						for(NodeConfiguration childNodeConfiguration : (List<NodeConfiguration>)configuration.childList)
						{
							Node node = new Node();
							node.parent = this;
							node.configuration = childNodeConfiguration;
							node.objects = new HashMap<>();
							node.childNodes = new HashMap<>();
							node.id = currentId;
							
							childs.add(node);
						}
					}
				}
			}
			
			if(isRoot)
			{
				cursor.rootObject = this.lastObject;
			}
			
			cursor.setParentObject(this.lastObject);
			
			if(childs != null)
			{
				Object backupParent = cursor.parentObject;
				Object backupId = cursor.id;
				for(Node childNode : childs)
				{
					cursor.parentObject = backupParent;
					cursor.id = backupId;
					childNode.fetch(resultSet, cursor, false);
				}
			}
			
			return this.lastObject;
		}
		
	}
	
	private Object fetchId(Class idType, String idColumnName, ResultSet resultSet) throws SQLException
	{
		Object id = null;
		
		if(idType == String.class)
		{
			id = resultSet.getString(idColumnName);
		}
		else if(idType == Integer.class)
		{
			int x = resultSet.getInt(idColumnName);
			if(resultSet.wasNull())
			{
				id = null;
			}
			else
			{
				id = x;
			}
		}
		else if(idType == Long.class)
		{
			long x = resultSet.getLong(idColumnName);
			if(resultSet.wasNull())
			{
				id = null;
			}
			else
			{
				id = x;
			}
		}
		else if(idType == UUID.class)
		{
			String x = resultSet.getString(idColumnName);
			if(x == null)
			{
				id = null;
			}
			else
			{
				id = UUID.fromString(x);
			}
		}
		
		return id;
	}
	
	/**
	 * Builder to build a {@link ResultSetParseHelper}
	 * 
	 * @author Sebastian Palarus
	 *
	 * @param <B> data type of current configuration
	 * @param <I> key / id type of current configuration
	 * @param <R> data type of root object
	 * @param <P> data type of parent object
	 */
	public static class ResultSetParseHelperBuilder<B,I,R,P>
	{
		private List<ParsePhase> parsePhaseList = null;
		private ParsePhase currentParsePhase = null;
		
		/**
		 * Creates a new builder to build a {@link ResultSetParseHelper}. A default parser phase is automatically created.
		 * 
		 * @param rootIdColumnName column name for key / id of root node object
		 * @param rootIdType type of root node objects key / id
		 * @param rootNodeOjectType type root node object
		 * @param superRootType type of super root object
		 * @param recordParser parser logic
		 * 
		 * @return builder new builder to build a {@link ResultSetParseHelper}
		 */
		public static <B,I,S>  ResultSetParseHelperBuilder<B,I,B,S> newBuilder
		(
			String rootIdColumnName, 
			Class<I> rootIdType,
			Class<B> rootNodeOjectType, 
			Class<S> superRootType, 
			IRecordParser<I, B, S, B> recordParser
		)
		{
			return newBuilder(rootIdColumnName, rootIdType, rootNodeOjectType, superRootType, recordParser, null, null);
		}
		
		/**
		 * Creates a new builder to build a {@link ResultSetParseHelper}. A default parser phase is automatically created.
		 * 
		 * @param rootIdColumnName column name for key / id of root node object
		 * @param rootIdType type of root node objects key / id
		 * @param rootNodeOjectType type root node object
		 * @param superRootType type of super root object
		 * @param recordParser parser logic
		 * @param consumerOnRootNodeComplete listener to notify if root node is completely parsed with all sub parsers
		 * @param consumerClusterComplete listener to notify if a cluster is completely parsed by parser phase
		 * 
		 * @return new builder to build a {@link ResultSetParseHelper}
		 */
		public static <B,I,S>  ResultSetParseHelperBuilder<B,I,B,S> newBuilder
		(
			String rootIdColumnName, 
			Class<I> rootIdType,
			Class<B> rootNodeOjectType, 
			Class<S> superRootType, 
			IRecordParser<I, B, S, B> recordParser,
			Consumer<B> consumerOnRootNodeComplete,
			Consumer<List<B>> consumerClusterComplete
		)
		{
			ResultSetParseHelperBuilder<B,I,B,S> builder = new ResultSetParseHelperBuilder<B,I,B,S>();
			builder.parsePhaseList = new ArrayList<>();
			builder.newParsePhase(DEFAULT_PHASE, rootIdColumnName, rootIdType, rootNodeOjectType, superRootType, recordParser, consumerOnRootNodeComplete, consumerClusterComplete);
			
			return builder;
		}
		
		/**
		 * Creates a sub parser configuration as child of current parser configuration.
		 * 
		 * @param idColumnName column name for key / id of node object
		 * @param idType type of node objects key / id 
		 * @param nodeOjectType type node object
		 * @param recordParser parser logic
		 * @return builder
		 */
		public <C,J> ResultSetParseHelperBuilder<C,J,R,B> subParser(String idColumnName, Class<J> idType,Class<C> nodeOjectType, IRecordParser<J, R, B, C> recordParser)
		{
			NodeConfiguration parentConfiguration = currentParsePhase.currentNodeConfiguration;
			currentParsePhase.currentNodeConfiguration = new NodeConfiguration(UUID.randomUUID().toString(),new ConplierBean(),idColumnName,(Class)idType,parentConfiguration);
			parentConfiguration.childList.add(currentParsePhase.currentNodeConfiguration);
			
			currentParsePhase.currentNodeConfiguration.recordParser = (IRecordParser)recordParser;
			
			return (ResultSetParseHelperBuilder)this;
		}
		
		/**
		 * Defines parser logic of current configuration in case of id null values
		 *  
		 * @param recordParser parser logic
		 * @return builder
		 */
		public ResultSetParseHelperBuilder<B,I,R,P> onNullRecord(IRecordParser<I, R, P, B> recordParser)
		{
			currentParsePhase.currentNodeConfiguration.recordParserIfNull = recordParser;
			return this;
		}
		
		/**
		 * close current configuration object and set parent configuration object as current configuration object.
		 * 
		 * @return builder
		 */
		public ResultSetParseHelperBuilder<P,?,R,?> build()
		{
			if(currentParsePhase.currentNodeConfiguration.parent != null)
			{
				currentParsePhase.currentNodeConfiguration = currentParsePhase.currentNodeConfiguration.parent;
			}
			return (ResultSetParseHelperBuilder)this;
		}
		
		/**
		 * Opens a new parser phase 
		 * 
		 * @param name name of parser phase
		 * @param rootIdColumnName column name for key / id of root node object
		 * @param rootIdType type of root node objects key / id
		 * @param rootNodeOjectType type root node object
		 * @param superRootType type of super root object
		 * @param recordParser parser logic
		 * 
		 * @return builder
		 */
		public <B,I,S>  ResultSetParseHelperBuilder<B,I,B,S> newParsePhase
		(
			String name, 
			String rootIdColumnName, 
			Class<I> rootIdType,
			Class<B> rootNodeOjectType, 
			Class<S> superRootType, 
			IRecordParser<I, B, S, B> recordParser
		)
		{
			return newParsePhase(name, rootIdColumnName, rootIdType, rootNodeOjectType, superRootType, recordParser, null, null);
		}
		
		/**
		 * Opens a new parser phase 
		 * 
		 * @param name name of parser phase
		 * @param rootIdColumnName column name for key / id of root node object
		 * @param rootIdType type of root node objects key / id
		 * @param rootNodeOjectType type root node object
		 * @param superRootType type of super root object
		 * @param recordParser parser logic
		 * @param consumerOnRootNodeComplete listener to notify if root node is completely parsed with all sub parsers
		 * @param consumerClusterComplete listener to notify if a cluster is completely parsed by parser phase
		 * 
		 * @return builder
		 */
		public <B,I,S>  ResultSetParseHelperBuilder<B,I,B,S> newParsePhase
		(
			String name, 
			String rootIdColumnName, 
			Class<I> rootIdType,
			Class<B> rootNodeOjectType, 
			Class<S> superRootType, 
			IRecordParser<I, B, S, B> recordParser,
			Consumer<B> consumerOnRootNodeComplete,
			Consumer<List<B>> consumerClusterComplete
		)
		{
			this.currentParsePhase = this.new ParsePhase(name);
			this.parsePhaseList.add(this.currentParsePhase);
			
			this.currentParsePhase.currentNodeConfiguration = this.new NodeConfiguration(UUID.randomUUID().toString(),new ConplierBean(),rootIdColumnName,(Class)rootIdType,null);
			this.currentParsePhase.nodeConfiguration = this.currentParsePhase.currentNodeConfiguration;
			
			this.currentParsePhase.currentNodeConfiguration.recordParser = (IRecordParser)recordParser;
			this.currentParsePhase.consumerClusterComplete = (Consumer)consumerClusterComplete;
			this.currentParsePhase.consumerRootNodeComplete = (Consumer)consumerOnRootNodeComplete;
			
			return (ResultSetParseHelperBuilder)this;
		}
		
		/**
		 * Build {@link ResultSetParseHelper} from builder. After this the builder is not usable anymore.
		 * 
		 * @return ResultSetParseHelper
		 */
		public ResultSetParseHelper buildParser()
		{
			return buildParser(true);
		}
		
		/**
		 * Build {@link ResultSetParseHelper} from builder. If {@code disposeBuilder} is defined as true, after this the builder is not usable anymore.
		 * 
		 * @param disposeBuilder indicates whether the object is disposed after creates the helper object
		 * 
		 * @return ResultSetParseHelper
		 */
		public ResultSetParseHelper buildParser(boolean disposeBuilder)
		{
			ResultSetParseHelper parser = new ResultSetParseHelper();
			parser.parsePhaseList = new ArrayList<>();
			for(ParsePhase parsePhase : this.parsePhaseList)
			{
				parser.parsePhaseList.add(parsePhase.copy());
			}
			if(disposeBuilder)
			{
				this.parsePhaseList.forEach(p -> p.clear());
				this.parsePhaseList.clear();
				this.parsePhaseList = null;
			}
			return parser;
		}
		
		protected class ParsePhase
		{
			private String name = null;
			private NodeConfiguration nodeConfiguration = null;
			private NodeConfiguration currentNodeConfiguration = null;
			private Consumer<Object> consumerRootNodeComplete =  null;
			private Consumer<Object> consumerClusterComplete =  null;
			
			protected ParsePhase(String name)
			{
				super();
				this.name = name;
			}
			
			protected ParsePhase copy()
			{
				ParsePhase copy = new ParsePhase(name);
				copy.nodeConfiguration = this.nodeConfiguration.copy();
				copy.consumerClusterComplete = this.consumerClusterComplete;
				copy.consumerRootNodeComplete = this.consumerRootNodeComplete;
				return copy;
			}

			protected String getName()
			{
				return name;
			}

			protected NodeConfiguration getNodeConfiguration()
			{
				return nodeConfiguration;
			}
			
			protected void clear()
			{
				this.name = null;
				if(this.nodeConfiguration != null)
				{
					this.nodeConfiguration.clear();
					this.nodeConfiguration = null;
				}
				if(this.currentNodeConfiguration != null)
				{
					this.currentNodeConfiguration.clear();
					this.currentNodeConfiguration = null;
				}
				this.consumerRootNodeComplete = null;
				this.consumerClusterComplete = null;
			}
		}
		
		protected class NodeConfiguration
		{
			private String nodeName = null;
			private NodeConfiguration parent = null;
			private ConplierBean<B> objectReference = null;
			private Class<I> idType = null;
			private String idColumnName = null;
			private IRecordParser<I, R, P, B> recordParser = null;
			private IRecordParser<I, R, P, B> recordParserIfNull = null;
			private List<NodeConfiguration> childList = null;
			
			protected NodeConfiguration(String nodeName, ConplierBean<B> objectReference, String idColumnName, Class<I> idType, NodeConfiguration parent)
			{
				super();
				this.nodeName = nodeName;
				this.objectReference = objectReference;
				this.parent = parent;
				this.idType = idType;
				this.idColumnName = idColumnName;
				this.childList = new ArrayList<>();
			}

			protected NodeConfiguration copy()
			{
				NodeConfiguration copy = new NodeConfiguration(this.nodeName, this.objectReference, this.idColumnName, this.idType, this.parent);
				copy.parent = null;
				copy.recordParser = this.recordParser;
				copy.recordParserIfNull = this.recordParserIfNull;
				for(NodeConfiguration child : childList)
				{
					copy.childList.add(child.copy());
				}
				return copy;
			}
			
			protected String getNodeName()
			{
				return nodeName;
			}

			protected NodeConfiguration getParent()
			{
				return parent;
			}

			protected ConplierBean<B> getObjectReference()
			{
				return objectReference;
			}

			protected Class<I> getIdType()
			{
				return idType;
			}

			protected String getIdColumnName()
			{
				return idColumnName;
			}

			protected IRecordParser<I, R, P, B> getRecordParser()
			{
				return recordParser;
			}

			protected IRecordParser<I, R, P, B> getRecordParserIfNull()
			{
				return recordParserIfNull;
			}
			
			protected void clear()
			{
				this.nodeName = null;
				this.parent = null;
				if(this.objectReference != null)
				{
					this.objectReference.dispose();
					this.objectReference = null;
				}
				this.idType = null;
				this.idColumnName = null;
				this.recordParser = null;
				this.recordParserIfNull = null;
				if(this.childList != null)
				{
					this.childList.forEach(n -> n.clear());
					this.childList.clear();
					this.childList = null;
				}
			}
		}
		
		
	}
	
	/**
	 * Close the parser helper. After this the helper is not usable anymore.
	 */
	public void close()
	{
		if(! this.closable)
		{
			return;
		}
		
		if(this.parsePhaseList != null)
		{
			this.parsePhaseList.forEach(p -> p.clear());
			this.parsePhaseList.clear();
			this.parsePhaseList = null;
		}
	}
	
	/**
	 * The cursor is the access point to database and create java objects
	 * 
	 * @author Sebastian Palarus
	 *
	 * @param <I> type of key
	 * @param <R> type of root object
	 * @param <P> type of parent object
	 */
	public static class Cursor<I,R,P>
	{
		private ResultSet resultSet;
		private I id;
		private R rootObject;
		private P parentObject;

		/**
		 * Getter for {@link ResultSet}
		 * 
		 * @return ResultSet
		 */
		public ResultSet getResultSet() 
		{
			return resultSet;
		}

		protected void setResultSet(ResultSet resultSet) 
		{
			this.resultSet = resultSet;
		}

		/**
		 * Getter for id of current data object
		 * 
		 * @return id of current data object
		 */
		public I getId() 
		{
			return id;
		}

		protected void setId(I id) 
		{
			this.id = id;
		}

		/**
		 * Getter for current root object
		 * 
		 * @return current root object
		 */
		public R getRootObject() 
		{
			return rootObject;
		}

		public void setRootObject(R rootObject) 
		{
			this.rootObject = rootObject;
		}

		/**
		 * Getter for current parent object
		 * 
		 * @return current parent object
		 */
		public P getParentObject() 
		{
			return parentObject;
		}

		public void setParentObject(P parentObject) 
		{
			this.parentObject = parentObject;
		}
		
		protected void clear()
		{
			this.resultSet = null;
			this.id = null;
			this.rootObject = null;
			this.parentObject = null;
		}
	}
	
	/**
	 * A record parser extract data from {@link ResultSet} and provide the data object with parsed values.
	 * 
	 * @author Sebastian Palarus
	 *
	 * @param <I> type of key / id
	 * @param <R> type of root object
	 * @param <P> type of parent object
	 * @param <B> type of object to provide
	 */
	@FunctionalInterface
	public static interface IRecordParser<I, R, P, B> extends Function<Cursor<I, R, P>, B>
	{

		@Override
		default B apply(Cursor<I, R, P> cursor) 
		{
			try
			{
				return this.parse(cursor);
			}
			catch (Exception e) 
			{
				if(e instanceof RuntimeException)
				{
					throw (RuntimeException)e;
				}
				else
				{
					throw new RuntimeException(e);
				}
			}
		}
		
		/**
		 * extract data of record and provide data object with parsed values
		 * 
		 * @param cursor access point to related objects
		 * @return provided data object
		 * 
		 * @throws Exception
		 */
		public B parse(Cursor<I, R, P> cursor) throws Exception;
		
	}
}
