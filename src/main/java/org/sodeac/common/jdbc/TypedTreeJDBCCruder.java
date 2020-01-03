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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.sql.DataSource;

import org.sodeac.common.function.ConplierBean;
import org.sodeac.common.function.CatchedExceptionConsumer;
import org.sodeac.common.jdbc.TypedTreeJDBCCruder.Session.RuntimeParameter;
import org.sodeac.common.typedtree.BranchNode;
import org.sodeac.common.typedtree.BranchNodeListType;
import org.sodeac.common.typedtree.BranchNodeMetaModel;
import org.sodeac.common.typedtree.BranchNodeType;
import org.sodeac.common.typedtree.INodeType;
import org.sodeac.common.typedtree.LeafNode;
import org.sodeac.common.typedtree.LeafNodeType;
import org.sodeac.common.typedtree.ModelRegistry;
import org.sodeac.common.typedtree.Node;
import org.sodeac.common.typedtree.NodeHelper;
import org.sodeac.common.typedtree.TypedTreeHelper;
import org.sodeac.common.typedtree.TypedTreeMetaModel;
import org.sodeac.common.typedtree.TypedTreeMetaModel.RootBranchNode;
import org.sodeac.common.typedtree.annotation.Association.AssociationType;
import org.sodeac.common.typedtree.annotation.SQLColumn;
import org.sodeac.common.typedtree.annotation.SQLColumn.SQLColumnType;

import org.sodeac.common.typedtree.annotation.SQLPrimaryKey;
import org.sodeac.common.typedtree.annotation.SQLReferencedByColumn;
import org.sodeac.common.typedtree.annotation.SQLReplace;
import org.sodeac.common.typedtree.annotation.SQLTable;

public class TypedTreeJDBCCruder implements AutoCloseable 
{
	
	protected TypedTreeJDBCCruder()
	{
		super();
		this.persistDefinitionContainer = new HashMap<INodeType, PreparedPersistDefinitionContainer>();
		this.loadDefinitionContainer = new HashMap<INodeType, PreparedLoadDefinitionContainer>();
		this.deleteDefinitionContainer = new HashMap<INodeType, PreparedDeleteDefinitionContainer>();
		this.lock = new ReentrantLock();
		this.rootNodeFactories = new HashMap<INodeType, Function<Object[], Collection<RootBranchNode<? extends TypedTreeMetaModel,? extends BranchNodeMetaModel>>>>();
	}
	
	public static final TypedTreeJDBCCruder get()
	{
		return new TypedTreeJDBCCruder();
	}
	
	private Map<INodeType, PreparedPersistDefinitionContainer> persistDefinitionContainer = null; 
	private Map<INodeType, PreparedDeleteDefinitionContainer> deleteDefinitionContainer = null; 
	private Map<INodeType, PreparedLoadDefinitionContainer> loadDefinitionContainer = null; 
	private Map<INodeType, Function<Object[], Collection<RootBranchNode<? extends TypedTreeMetaModel,? extends BranchNodeMetaModel>>>> rootNodeFactories = null; 
	
	private Lock lock = null;
	
	public Session openSession(DataSource mainDatasource)
	{
		return new Session(mainDatasource);
	}
	
	@Override
	public void close()
	{
		lock.lock();
		try
		{
			if(this.persistDefinitionContainer != null)
			{
				for(PreparedPersistDefinitionContainer container : this.persistDefinitionContainer.values())
				{
					container.close();
				}
				this.persistDefinitionContainer.clear();
				this.persistDefinitionContainer = null;
			}
			
			if(this.deleteDefinitionContainer != null)
			{
				for(PreparedDeleteDefinitionContainer container : this.deleteDefinitionContainer.values())
				{
					container.close();
				}
				this.deleteDefinitionContainer.clear();
				this.deleteDefinitionContainer = null;
			}
			
			if(this.loadDefinitionContainer != null)
			{
				for(PreparedLoadDefinitionContainer container : this.loadDefinitionContainer.values())
				{
					container.close();
				}
				this.loadDefinitionContainer.clear();
				this.loadDefinitionContainer = null;
			}
			if(this.rootNodeFactories != null)
			{
				this.rootNodeFactories.clear();
			}
			this.rootNodeFactories = null;
		}
		finally 
		{
			lock.unlock();
		}
	}
	
	public Collection<INodeType<?,?>> getNodeTypeList(BranchNodeType<? extends BranchNodeMetaModel,? extends BranchNodeMetaModel> type) throws SQLException
	{
		return TypedTreeJDBCCruder.this.getPreparedLoadDefinitionContainer(type).loadDefinition.nodeTypeList;
	}
	
	private PreparedDeleteDefinitionContainer getPreparedDeleteDefinitionContainer(INodeType nodeType)
	{
		lock.lock();
		try
		{
			PreparedDeleteDefinitionContainer container = deleteDefinitionContainer.get(nodeType);
			if(container != null)
			{
				return container;
			}
			container = new PreparedDeleteDefinitionContainer(nodeType);
			deleteDefinitionContainer.put(nodeType,container);
			return container;
		}
		finally 
		{
			lock.unlock();
		}
	}
	
	private PreparedPersistDefinitionContainer getPreparedPersistDefinitionContainer(INodeType nodeType)
	{
		lock.lock();
		try
		{
			PreparedPersistDefinitionContainer container = persistDefinitionContainer.get(nodeType);
			if(container != null)
			{
				return container;
			}
			container = new PreparedPersistDefinitionContainer(nodeType);
			persistDefinitionContainer.put(nodeType,container);
			return container;
		}
		finally 
		{
			lock.unlock();
		}
	}
	
	private PreparedLoadDefinitionContainer getPreparedLoadDefinitionContainer(INodeType nodeType)
	{
		lock.lock();
		try
		{
			PreparedLoadDefinitionContainer container = loadDefinitionContainer.get(nodeType);
			if(container != null)
			{
				return container;
			}
			container = new PreparedLoadDefinitionContainer(nodeType);
			loadDefinitionContainer.put(nodeType,container);
			return container;
		}
		finally 
		{
			lock.unlock();
		}
	}
	
	
	public class Session implements AutoCloseable
	{
		private volatile boolean error = false;
		private volatile DataSource mainDatasource = null;
		private volatile Connection mainConnection = null;
		private Map<String,PreparedStatement> preparedStatementCache = new HashMap<String,PreparedStatement>();
		private Map<String,PreparedStatement> preparedStatementWithoutBatchesCache = new HashMap<String,PreparedStatement>();
		private boolean isPostgreSQL = false;
		
		protected Session(DataSource mainDatasource)
		{
			super();
			this.mainDatasource = mainDatasource;
		}
		
		@Override
		public void close() throws Exception 
		{
			if(this.error)
			{
				try
				{
					if(this.mainConnection != null)
					{
						this.mainConnection.rollback();
					}
				}
				catch (Exception e) {}
			}
			for(PreparedStatement preparedStatement : this.preparedStatementCache.values())
			{
				try
				{
					preparedStatement.close();
				}
				catch (Exception e) {}
			}
			for(PreparedStatement preparedStatement : this.preparedStatementWithoutBatchesCache.values())
			{
				try
				{
					preparedStatement.close();
				}
				catch (Exception e) {}
			}
			this.preparedStatementCache.clear();
			this.preparedStatementWithoutBatchesCache.clear();
			try
			{
				if(this.mainConnection != null)
				{
					this.mainConnection.close();
				}
			}
			catch (Exception e) {e.printStackTrace();}
			
			this.mainDatasource = null;
			this.mainConnection = null;
			this.preparedStatementCache = null;
			this.preparedStatementWithoutBatchesCache = null;
		}
		
		public <T extends BranchNodeMetaModel> List<BranchNode<?,T>> loadList(BranchNodeType<? extends BranchNodeMetaModel,T> type, INodeType<T,?> searchField, Object[] searchValues, Function<Object[], Collection<BranchNode<? extends BranchNodeMetaModel,T>>> nodeFactory) throws SQLException
		{
			if(error)
			{
				throw new RuntimeException("Session is invalid by thrown exception");
			}
			List<BranchNode<?,T>> collector = new ArrayList<BranchNode<?,T>>();
			boolean valid = false;
			try
			{
				PreparedLoadDefinitionContainer preparedDefinitionContainer = TypedTreeJDBCCruder.this.getPreparedLoadDefinitionContainer(type);
				
				if(this.mainConnection == null)
				{
					this.mainConnection = mainDatasource.getConnection();
					this.mainConnection.setAutoCommit(false);
					
					if(this.mainConnection.getMetaData().getDatabaseProductName().equalsIgnoreCase("PostgreSQL"))
					{
						this.isPostgreSQL = true;
					}
				}
				
				RuntimeParameter runtimeParameter = new RuntimeParameter();
				runtimeParameter.connection = mainConnection;
				runtimeParameter.searchField = searchField;
				runtimeParameter.searchValues = searchValues;
				runtimeParameter.nodeFactory = (Function)nodeFactory;
				runtimeParameter.session = this;
				
				preparedDefinitionContainer.loadDefinition.selectNode(runtimeParameter,collector);
				
				runtimeParameter.close();
				valid = true;
			}
			finally 
			{
				if(! valid)
				{
					this.error = true;
				}
			}
			
			return collector;
		}
		
		public <P extends TypedTreeMetaModel,T extends BranchNodeMetaModel> RootBranchNode<P, T> loadRootNode(BranchNodeType<P,T> nodeType, Object id) throws SQLException
		{
			LeafNodeType<T,?> searchField = TypedTreeHelper.getPrimaryKeyNode(nodeType.getTypeClass());
			RootBranchNode<P, T> node = (RootBranchNode)loadItem((BranchNodeType)nodeType, (INodeType)searchField, new Object[] {id}, (Function)getRootNodeFactory(nodeType));
			return node;
		}
		
		public void loadItem(BranchNode branchNode) throws SQLException
		{
			if(branchNode == null)
			{
				return;
			}
			LeafNodeType searchField = TypedTreeHelper.getPrimaryKeyNode(branchNode.getNodeType().getTypeClass());
			Object id = branchNode.getValue(searchField);
			if(id == null)
			{
				throw new IllegalStateException("can not load data without primary key value");
			}
			loadItem((BranchNodeType)branchNode.getNodeType(), (INodeType)searchField, new Object[] {id}, ids -> Collections.singleton(branchNode));
		}
		
		public <T extends BranchNodeMetaModel> BranchNode<?,T> loadItem(BranchNodeType<? extends BranchNodeMetaModel,T> type, INodeType<T,?> searchField, Object[] searchValues, Function<Object[], Collection<BranchNode<? extends BranchNodeMetaModel,T>>> nodeFactory) throws SQLException
		{
			if(error)
			{
				throw new RuntimeException("Session is invalid by thrown exception");
			}
			List<BranchNode<?,T>> collector = new ArrayList<BranchNode<?,T>>();
			boolean valid = false;
			try
			{
				PreparedLoadDefinitionContainer preparedDefinitionContainer = TypedTreeJDBCCruder.this.getPreparedLoadDefinitionContainer(type);
				
				if(this.mainConnection == null)
				{
					this.mainConnection = mainDatasource.getConnection();
					this.mainConnection.setAutoCommit(false);
					
					if(this.mainConnection.getMetaData().getDatabaseProductName().equalsIgnoreCase("PostgreSQL"))
					{
						this.isPostgreSQL = true;
					}
				}
				
				RuntimeParameter runtimeParameter = new RuntimeParameter();
				runtimeParameter.connection = mainConnection;
				runtimeParameter.searchField = searchField;
				runtimeParameter.searchValues = searchValues;
				runtimeParameter.nodeFactory = (Function)nodeFactory;
				runtimeParameter.session = this;
				
				preparedDefinitionContainer.loadDefinition.selectNode(runtimeParameter,collector);
				
				runtimeParameter.close();
				valid = true;
				
				if(collector.isEmpty())
				{
					return null;
				}
				
				return collector.get(0);
			}
			finally 
			{
				if(! valid)
				{
					this.error = true;
				}
				
				collector.clear();
			}
		}
		
		public <T extends BranchNodeMetaModel> List<BranchNode<?,T>> loadListByReferencedNode(BranchNodeType<? extends BranchNodeMetaModel,T> type, Object[] searchValues, Function<Object[], Collection<BranchNode<? extends BranchNodeMetaModel,T>>> nodeFactory) throws SQLException
		{
			if(error)
			{
				throw new RuntimeException("Session is invalid by thrown exception");
			}
			List<BranchNode<?,T>> collector = new ArrayList<BranchNode<?,T>>();
			boolean valid = false;
			try
			{
				PreparedLoadDefinitionContainer preparedDefinitionContainer = TypedTreeJDBCCruder.this.getPreparedLoadDefinitionContainer(type);
				
				if(this.mainConnection == null)
				{
					this.mainConnection = mainDatasource.getConnection();
					this.mainConnection.setAutoCommit(false);
					
					if(this.mainConnection.getMetaData().getDatabaseProductName().equalsIgnoreCase("PostgreSQL"))
					{
						this.isPostgreSQL = true;
					}
				}
				
				RuntimeParameter runtimeParameter = new RuntimeParameter();
				runtimeParameter.connection = mainConnection;
				runtimeParameter.searchField = type;
				runtimeParameter.searchValues = searchValues;
				runtimeParameter.nodeFactory = (Function)nodeFactory;
				runtimeParameter.session = this;
				
				preparedDefinitionContainer.loadDefinition.selectNode(runtimeParameter,collector);
				
				runtimeParameter.close();
				valid = true;
			}
			finally 
			{
				if(! valid)
				{
					this.error = true;
				}
			}
			
			return collector;
		}
		
		public <T extends BranchNodeMetaModel> List<BranchNode<?,T>> loadListByReferencedNode(BranchNodeListType<? extends BranchNodeMetaModel,T> type, Object[] searchValues, Function<Object[], Collection<BranchNode<? extends BranchNodeMetaModel,T>>> nodeFactory) throws SQLException
		{
			if(error)
			{
				throw new RuntimeException("Session is invalid by thrown exception");
			}
			List<BranchNode<?,T>> collector = new ArrayList<BranchNode<?,T>>();
			boolean valid = false;
			try
			{
				PreparedLoadDefinitionContainer preparedDefinitionContainer = TypedTreeJDBCCruder.this.getPreparedLoadDefinitionContainer(type);
				
				if(this.mainConnection == null)
				{
					this.mainConnection = mainDatasource.getConnection();
					this.mainConnection.setAutoCommit(false);
					
					if(this.mainConnection.getMetaData().getDatabaseProductName().equalsIgnoreCase("PostgreSQL"))
					{
						this.isPostgreSQL = true;
					}
				}
				
				RuntimeParameter runtimeParameter = new RuntimeParameter();
				runtimeParameter.connection = mainConnection;
				runtimeParameter.searchField = type;
				runtimeParameter.searchValues = searchValues;
				runtimeParameter.nodeFactory = (Function)nodeFactory;
				runtimeParameter.session = this;
				
				preparedDefinitionContainer.loadDefinition.selectNode(runtimeParameter,collector);
				
				runtimeParameter.close();
				valid = true;
			}
			finally 
			{
				if(! valid)
				{
					this.error = true;
				}
			}
			
			return collector;
		}
		
		public <P extends BranchNodeMetaModel,T extends BranchNodeMetaModel> void loadReferencedChildNodes(BranchNode<? extends BranchNodeMetaModel,P> node, BranchNodeListType<P, T> childNodeType) throws SQLException
		{
			if(! node.getUnmodifiableNodeList(childNodeType).isEmpty()) // node.isEmpty(nodeType);
			{
				node.clear(childNodeType);
			}
			loadListByReferencedNode(childNodeType, Collections.singleton(node.get(TypedTreeHelper.getPrimaryKeyNode(node.getNodeType().getTypeClass())).getValue() ).toArray(), ids -> Collections.singletonList(node.create(childNodeType))).clear();
		}
		
		public void persist(BranchNode< ? extends BranchNodeMetaModel, ? extends BranchNodeMetaModel> node) throws SQLException
		{
			if(error)
			{
				throw new RuntimeException("Session is invalid by thrown exception");
			}
			boolean valid = false;
			try
			{
				PreparedPersistDefinitionContainer preparedDefinitionContainer = TypedTreeJDBCCruder.this.getPreparedPersistDefinitionContainer(node.getNodeType());
				
				if(this.mainConnection == null)
				{
					this.mainConnection = mainDatasource.getConnection();
					this.mainConnection.setAutoCommit(false);
					
					if(this.mainConnection.getMetaData().getDatabaseProductName().equalsIgnoreCase("PostgreSQL"))
					{
						this.isPostgreSQL = true;
					}
				}
				
				RuntimeParameter runtimeParameter = new RuntimeParameter();
				runtimeParameter.branchNode = node;
				runtimeParameter.connection = mainConnection;
				runtimeParameter.session = this;
				
				if(preparedDefinitionContainer.checkPersistableIsNew.checkIsNew(runtimeParameter))
				{
					preparedDefinitionContainer.insertStatement.insertNode(runtimeParameter);
				}
				else
				{
					preparedDefinitionContainer.updateStatement.updateNode(runtimeParameter);
				}
				
				runtimeParameter.close();
				valid = true;
			}
			finally 
			{
				if(! valid)
				{
					this.error = true;
				}
			}
		}
		
		public void delete(BranchNode< ? extends BranchNodeMetaModel, ? extends BranchNodeMetaModel> node) throws SQLException
		{
			if(error)
			{
				throw new RuntimeException("Session is invalid by thrown exception");
			}
			boolean valid = false;
			try
			{
				PreparedDeleteDefinitionContainer preparedDefinitionContainer = TypedTreeJDBCCruder.this.getPreparedDeleteDefinitionContainer(node.getNodeType());
				
				if(this.mainConnection == null)
				{
					this.mainConnection = mainDatasource.getConnection();
					this.mainConnection.setAutoCommit(false);
					
					if(this.mainConnection.getMetaData().getDatabaseProductName().equalsIgnoreCase("PostgreSQL"))
					{
						this.isPostgreSQL = true;
					}
				}
				
				RuntimeParameter runtimeParameter = new RuntimeParameter();
				runtimeParameter.branchNode = node;
				runtimeParameter.connection = mainConnection;
				runtimeParameter.session = this;
				
				preparedDefinitionContainer.preparedDeleteStatementDefinition.deleteNode(runtimeParameter);
				
				runtimeParameter.close();
				valid = true;
			}
			finally 
			{
				if(! valid)
				{
					this.error = true;
				}
			}
		}
		
		public void flush()throws SQLException
		{
			if(mainConnection != null)
			{
				// TODO
			}
		}
		
		public void commit() throws SQLException
		{
			if(mainConnection != null)
			{
				mainConnection.commit();
			}
		}
		
		public void rollback() throws SQLException
		{
			if(mainConnection != null)
			{
				mainConnection.rollback();
			}
		}
		
		protected class RuntimeParameter implements IRuntimeParameter
		{
			private RuntimeParameter()
			{
				super();
				this.isNew = new ConplierBean<Boolean>(Boolean.FALSE);
				this.isExisting = new ConplierBean<Boolean>(Boolean.FALSE);
				this.conplierBean = new ConplierBean<Object>();
				this.converterProperties = new HashMap<String,Object>();
			}
			
			private Connection connection = null;
			private PreparedStatement preparedStatement;
			private ResultSet resultSet = null;
			
			private LeafNodeType<? extends BranchNodeMetaModel,?> type;
			private BranchNodeType<? extends BranchNodeMetaModel,? extends BranchNodeMetaModel> childType = null;
			private BranchNode<? extends BranchNodeMetaModel, ? extends  BranchNodeMetaModel> branchNode;
			private BranchNode<? extends BranchNodeMetaModel, ? extends  BranchNodeMetaModel> workingBranchNode;
			
			
			private Object staticValue;
			private ConplierBean<Boolean> isNew;
			private ConplierBean<Boolean> isExisting;
			private ConplierBean<Object> conplierBean;
			private Map<String,Object> converterProperties;
			private INodeType searchField;
			private Session session;
			private Object[] searchValues;
			private Object[] values = null;
			
			private Function<Object[], Collection<BranchNode<? extends BranchNodeMetaModel, ? extends BranchNodeMetaModel>>> nodeFactory;
			
			public PreparedStatement getPreparedStatement() 
			{
				return preparedStatement;
			}
			public ResultSet getResultSet() 
			{
				return resultSet;
			}
			public BranchNode<? extends BranchNodeMetaModel, ? extends BranchNodeMetaModel> getBranchNode() 
			{
				return branchNode;
			}
			public Object getStaticValue() 
			{
				return staticValue;
			}
			public Connection getConnection() 
			{
				return connection;
			}
			public void close()
			{
				this.connection = null;
				this.preparedStatement = null;
				this.resultSet = null;
				this.branchNode = null;
				this.workingBranchNode = null;
				this.staticValue = null;
				this.isNew = null;
				this.isExisting = null;
				if(this.conplierBean != null)
				{
					this.conplierBean.setValue(null);
				}
				this.conplierBean = null;
				if(this.converterProperties != null)
				{
					this.converterProperties.clear();
				}
				this.converterProperties = null;
				
				this.session = null;
				this.searchField = null;
				this.searchValues = null;
				this.nodeFactory = null;
				this.values = null;
				this.type = null;
				this.childType = null;
			}
			
			public PreparedStatement getPreparedStatement(String sql) throws SQLException
			{
				PreparedStatement preparedStatement = Session.this.preparedStatementCache.get(sql);
				if((preparedStatement != null) && (! preparedStatement.isClosed()))
				{
					return preparedStatement;
				}
				preparedStatement = connection.prepareStatement(sql);
				Session.this.preparedStatementCache.put(sql,preparedStatement);
				return preparedStatement;
			}
			public PreparedStatement getPreparedStatementWithoutBatch(String sql) throws SQLException
			{
				PreparedStatement preparedStatement = Session.this.preparedStatementWithoutBatchesCache.get(sql);
				if((preparedStatement != null) && (! preparedStatement.isClosed()))
				{
					return preparedStatement;
				}
				preparedStatement = connection.prepareStatement(sql);
				Session.this.preparedStatementWithoutBatchesCache.put(sql,preparedStatement);
				return preparedStatement;
			}
		}
	
	}
	
	private class PreparedDeleteDefinitionContainer
	{
		private INodeType nodeType = null;
		private PreparedDeleteStatementDefinition preparedDeleteStatementDefinition = null;
		
		private PreparedDeleteDefinitionContainer(INodeType nodeType)
		{
			super();
			this.nodeType = nodeType;
			this.preparedDeleteStatementDefinition = new PreparedDeleteStatementDefinition(nodeType);
		}
		
		private void close()
		{
			this.nodeType = null;
			this.preparedDeleteStatementDefinition.close();
			this.preparedDeleteStatementDefinition = null;
		}
	}
	private class PreparedPersistDefinitionContainer
	{
		private PreparedPersistDefinitionContainer(INodeType nodeType) 
		{
			super();
			this.nodeType = nodeType;
			this.insertStatement = new PreparedInsertStatementDefinition(nodeType);
			this.updateStatement = new PreparedUpdateStatementDefinition(nodeType);
			this.checkPersistableIsNew = new CheckPersistableIsNewDefinition(nodeType);
		}
		
		private INodeType nodeType = null;
		private PreparedInsertStatementDefinition insertStatement = null;
		private PreparedUpdateStatementDefinition updateStatement = null;
		private CheckPersistableIsNewDefinition checkPersistableIsNew = null;
		
		
		private void close()
		{
			try
			{
				this.insertStatement.close();
			}
			catch (Exception e) {e.printStackTrace();}
			
			try
			{
				this.updateStatement.close();
			}
			catch (Exception e) {e.printStackTrace();}
			
			try
			{
				this.checkPersistableIsNew.close();
			}
			catch (Exception e) {e.printStackTrace();}
			
			this.nodeType = null;
			this.insertStatement = null;
			this.checkPersistableIsNew = null;
		}
	}
	
	private class PreparedLoadDefinitionContainer
	{
		private PreparedLoadDefinitionContainer(INodeType nodeType) 
		{
			super();
			this.nodeType = nodeType;
			this.loadDefinition = new PreparedLoadResultSetDefinition(this.nodeType);
		}
		
		private PreparedLoadResultSetDefinition loadDefinition = null;
		private INodeType nodeType = null;
		
		private void close()
		{
			try
			{
				this.loadDefinition.close();
			}
			catch (Exception e) {e.printStackTrace();}
			
			this.nodeType = null;
			this.loadDefinition =  null;
		}
	}
	
	private class PreparedLoadResultSetDefinition
	{
		public PreparedLoadResultSetDefinition(INodeType nodeType) 
		{
			super();
			this.nodeType = nodeType;
			this.create();
		}
		
		private void create()
		{
			BranchNodeMetaModel defaultInstance = (BranchNodeMetaModel)this.nodeType.getValueDefaultInstance();
			
			SQLTable table = (SQLTable)this.nodeType.getTypeClass().getAnnotation(SQLTable.class);
			
			// replaces by referenced field ???
			
			SQLReplace[] replaces = (SQLReplace[])this.nodeType.referencedByField().getAnnotationsByType(SQLReplace.class);
			
			for(SQLReplace replace : replaces)
			{
				for(SQLTable replacedTable : replace.table())
				{
					table = replacedTable;
					break;
				}
			}
			
			if(table == null)
			{ 
				throw new RuntimeException("Annotation SQLTable not found in model " + nodeType.getTypeClass());
			}
			
			this.columns = new ArrayList<JDBCGetterDefinition>();
			this.nodeTypeList = new ArrayList<INodeType<?,?>>();
			this.constrainHelperIndex = Collections.synchronizedMap(new HashMap<>());
			
			StringBuilder sqlColumns = new StringBuilder();
			
			int cursorPosititon = 1;
			
			
			for(LeafNodeType leafNode : defaultInstance.getLeafNodeTypeList())
			{
				SQLColumn sqlColumn = (SQLColumn)leafNode.referencedByField().getAnnotation(SQLColumn.class);
				
				for(SQLReplace replace : replaces)
				{
					if(! leafNode.getNodeName().equals(replace.nodeName()))
					{
						continue;
					}
					for(SQLColumn replacedColumn : replace.column())
					{
						
						sqlColumn = replacedColumn;
						break;
					}
				}
				if(sqlColumn == null)
				{
					continue;
				}
				
				if(! sqlColumn.insertable())
				{
					continue;
				}
				
				if(sqlColumns.length() > 0)
				{
					sqlColumns.append(",");
				}
				
				sqlColumns.append(sqlColumn.name());
				BiConsumer<RuntimeParameter, PreparedLoadResultSetDefinition> nodeSetter = (r,d) -> {r.branchNode.get((LeafNodeType)r.type).setValue(r.staticValue);};
				this.columns.add(new JDBCGetterDefinition(leafNode, sqlColumn, cursorPosititon++, nodeSetter));
				this.nodeTypeList.add(leafNode);
			}
			
			for(BranchNodeType branchNode : defaultInstance.getBranchNodeTypeList())
			{
				SQLColumn sqlColumn = (SQLColumn)branchNode.referencedByField().getAnnotation(SQLColumn.class);
				SQLReferencedByColumn sqlRefrencedByColumn = (SQLReferencedByColumn)branchNode.referencedByField().getAnnotation(SQLReferencedByColumn.class);
				for(SQLReplace replace : replaces)
				{
					if(! branchNode.getNodeName().equals(replace.nodeName()))
					{
						continue;
					}
					for(SQLColumn replacedColumn : replace.column())
					{
						
						sqlColumn = replacedColumn;
						break;
					}
					for(SQLReferencedByColumn replacedReferencedByColumn : replace.referencedByColumn())
					{
						
						sqlRefrencedByColumn = replacedReferencedByColumn;
						break;
					}
				}
				
				if((sqlColumn == null) || (sqlRefrencedByColumn != null))
				{
					continue;
				}
				
				if(! sqlColumn.insertable())
				{
					continue;
				}
				
				LeafNodeType pkLeafNode = null;
				
				JDBCGetterDefinition getColumnDefinition = null;
				BranchNodeMetaModel childInstance = branchNode.getValueDefaultInstance();
				for(LeafNodeType childLeafNode : childInstance.getLeafNodeTypeList())
				{
					SQLColumn sqlColumnChild = (SQLColumn)childLeafNode.referencedByField().getAnnotation(SQLColumn.class);
					SQLPrimaryKey primaryKey = (SQLPrimaryKey)childLeafNode.referencedByField().getAnnotation(SQLPrimaryKey.class);
					
					for(SQLReplace replace : (SQLReplace[])branchNode.referencedByField().getAnnotationsByType(SQLReplace.class))
					{
						if(! childLeafNode.getNodeName().equals(replace.nodeName()))
						{
							continue;
						}
						for(SQLColumn replacedColumn : replace.column())
						{
							
							sqlColumn = replacedColumn;
							break;
						}
						for(SQLPrimaryKey replacedPrimaryKey : replace.primaryKey())
						{
							
							primaryKey = replacedPrimaryKey;
							break;
						}
					}
					
					if((sqlColumnChild == null) || (primaryKey == null))
					{
						continue;
					}
					
					BiConsumer<RuntimeParameter, PreparedLoadResultSetDefinition> nodeSetter = (r,d) -> 
					{
						if(r.staticValue == null)
						{
							return;
						}
						BranchNode<? extends BranchNodeMetaModel,? extends BranchNodeMetaModel> childNode = r.branchNode.create((BranchNodeType)r.childType);
						childNode.setValue((LeafNodeType)r.type,r.staticValue);
					};
					getColumnDefinition = new JDBCGetterDefinition(childLeafNode, sqlColumn, cursorPosititon++,nodeSetter);//, branchNode.referencedByField().getAnnotationsByType(SQLReplace.class));
					pkLeafNode = childLeafNode;
					
					break;
					
				}
				
				if(getColumnDefinition == null)
				{
					throw new RuntimeException("no primary key found in " + branchNode.getTypeClass().getCanonicalName());
				}
				
				getColumnDefinition.childType = branchNode;
				getColumnDefinition.type = pkLeafNode;
				
				if(sqlColumns.length() > 0)
				{
					sqlColumns.append(",");
				}
				sqlColumns.append(sqlColumn.name());
				
				this.columns.add(getColumnDefinition);
				this.nodeTypeList.add(branchNode);
			}
			
			 // columns defined by Parent
			
			/*SQLReferencedByColumn referencedBy = (SQLReferencedByColumn)this.nodeType.referencedByField().getAnnotation(SQLReferencedByColumn.class);
			// TODO replaceByParent
			if(referencedBy != null)
			{
				
				try
				{
					// TODO getDefaultInstanceParent
					// TODO INodeType.getParentNodeType ???
					BranchNodeMetaModel defaultInstanceParent = (BranchNodeMetaModel)this.nodeType.getParentNodeClass().newInstance();
					for(LeafNodeType<BranchNodeMetaModel, ?>  childNodeType : defaultInstanceParent.getLeafNodeTypeList())
					{
						SQLColumn sqlColumn = childNodeType.referencedByField().getAnnotation(SQLColumn.class);
						SQLPrimaryKey primaryKey = childNodeType.referencedByField().getAnnotation(SQLPrimaryKey.class);
						// TODO replaceByParent
						if((primaryKey != null) && (sqlColumn != null))
						{
							JDBCGetterDefinition setColumnDefinition = new JDBCGetterDefinition(childNodeType, sqlColumn, cursorPosititon++, null);
							setColumnDefinition.parentType = true;
							
							if(sqlColumns.length() > 0)
							{
								sqlColumns.append(",");
							}
							
							sqlColumns.append(referencedBy.name());
							
							this.columns.add(setColumnDefinition);
							this.nodeTypeList.add(childNodeType);
						}
					}
					
				}
				catch (Exception e) 
				{
					throw new RuntimeException(e);
				}
			}*/
			
			this.sql = "select " + sqlColumns + "  from " + table.name() + " ";
			this.nodeTypeList = Collections.unmodifiableList(this.nodeTypeList);
		}
		
		private INodeType nodeType = null;
		private BranchNodeMetaModel type = null;
		private String domain = null;
		private String boundedContext = null;
		private String service = null;
		private String sql = null;
		private List<JDBCGetterDefinition> columns = null;
		protected List<INodeType<?,?>> nodeTypeList = null;
		private Map<INodeType,ConstraintHelper> constrainHelperIndex = null;
		
		private void close()
		{
			if(columns != null)
			{
				columns.forEach(c -> c.close());
				columns.clear();
			}
			if(constrainHelperIndex != null)
			{
				for(ConstraintHelper helper : constrainHelperIndex.values())
				{
					helper.column = null;
					helper.sqlType = null;
				}
				constrainHelperIndex.clear();
			}
			
			nodeType = null;
			type = null;
			domain = null;
			boundedContext = null;
			service = null;
			sql = null;
			columns = null;
			nodeTypeList = null;
			constrainHelperIndex = null;
		}
		
		private class ConstraintHelper
		{
			private String column;
			private String sqlType;
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		protected void selectNode(RuntimeParameter runtimeParameter, List collector) throws SQLException
		{
			ConstraintHelper constraintHelper = constrainHelperIndex.get(runtimeParameter.searchField);
			if(constraintHelper == null)
			{
				SQLColumn sqlColumn = runtimeParameter.searchField.referencedByField().getAnnotation(SQLColumn.class);
				SQLReferencedByColumn sqlReferencedByColumn = runtimeParameter.searchField.referencedByField().getAnnotation(SQLReferencedByColumn.class);
				
				constraintHelper = new ConstraintHelper();
				constraintHelper.sqlType = "VARCHAR";
				
				if(sqlReferencedByColumn != null)
				{
					constraintHelper.column = sqlReferencedByColumn.name();
					
					Class<BranchNodeMetaModel> clazz = runtimeParameter.searchField.getTypeClass();
					BranchNodeMetaModel metaModel = ModelRegistry.getBranchNodeMetaModel(clazz);
					LeafNodeType primaryKeyNode = null;
					
					for(LeafNodeType childLeafNode : metaModel.getLeafNodeTypeList())
					{
						SQLPrimaryKey primaryKey = childLeafNode.referencedByField().getAnnotation(SQLPrimaryKey.class);
						
						if(primaryKey == null)
						{
							continue;
						}
						
						primaryKeyNode = childLeafNode;
						break;
					}
					
					if(primaryKeyNode == null)
					{
						throw new RuntimeException("No primaryKey node found for " + runtimeParameter.searchField.referencedByField());
					}
					
					SQLColumnType sqlColumnType = SQLColumnType.VARCHAR;
					
					if(primaryKeyNode.getTypeClass() == Long.class)
					{
						sqlColumnType = SQLColumnType.BIGINT;
					}
					
					SQLColumn sqlColumnPK = primaryKeyNode.referencedByField().getAnnotation(SQLColumn.class);
					
					if((sqlColumnPK != null) && (sqlColumnPK.type() != SQLColumnType.AUTO))
					{
						sqlColumnType = sqlColumnPK.type();
					}
					
					if(sqlColumnType == SQLColumnType.BIGINT)
					{
						constraintHelper.sqlType = "BIGINT";
					}
					constrainHelperIndex.put(runtimeParameter.searchField, constraintHelper);
					
				}
				else
				{
					constraintHelper.column = runtimeParameter.searchField.getNodeName();
					if(sqlColumn != null)
					{
						constraintHelper.column = sqlColumn.name();
					}
					
					SQLColumnType sqlColumnType = SQLColumnType.VARCHAR;
					
					if(runtimeParameter.searchField.getTypeClass() == Long.class)
					{
						sqlColumnType = SQLColumnType.BIGINT;
					}
					if((sqlColumn != null) && (sqlColumn.type() != SQLColumnType.AUTO))
					{
						sqlColumnType = sqlColumn.type();
					}
					
					if(sqlColumnType == SQLColumnType.BIGINT)
					{
						constraintHelper.sqlType = "BIGINT";
					}
					constrainHelperIndex.put(runtimeParameter.searchField, constraintHelper);
				}
			}
			
			
			String completeSQL = null;
			if(runtimeParameter.session.isPostgreSQL)
			{
				completeSQL = sql + " where " + constraintHelper.column + " in (select * from unnest(?))";
			}
			else
			{
				completeSQL = sql + " where " + constraintHelper.column + " in (?)";
			}
			
			runtimeParameter.preparedStatement = runtimeParameter.getPreparedStatementWithoutBatch(completeSQL);
			runtimeParameter.values = new Object[this.columns.size()];
			
			runtimeParameter.converterProperties.clear();
			runtimeParameter.converterProperties.put(Connection.class.getCanonicalName(), runtimeParameter.connection);
			runtimeParameter.converterProperties.put(PreparedStatement.class.getCanonicalName(), runtimeParameter.preparedStatement);
			runtimeParameter.converterProperties.put(IRuntimeParameter.class.getCanonicalName(), runtimeParameter);
			
			runtimeParameter.preparedStatement.setArray(1, runtimeParameter.connection.createArrayOf(constraintHelper.sqlType, runtimeParameter.searchValues));
			
			ResultSet resultSet = runtimeParameter.preparedStatement.executeQuery();
			try
			{
				runtimeParameter.resultSet = resultSet;
				while(resultSet.next())
				{
					for(JDBCGetterDefinition column : this.columns)
					{
						try
						{
							column.getter.acceptWithException(runtimeParameter);
						}
						catch (SQLException e) 
						{
							throw e;
						}
						catch (Exception e) 
						{
							throw new RuntimeException(e);
						}
					}
					
					Collection<BranchNode<?, ?>> nodes = runtimeParameter.nodeFactory.apply(runtimeParameter.values);
					
					if(nodes == null)
					{
						continue;
					}
					for(BranchNode<?, ?> node : nodes)
					{
						if(node == null)
						{
							continue;
						}
						collector.add(node);
						runtimeParameter.branchNode = node;
						int i = 0;
						for(JDBCGetterDefinition column : this.columns)
						{
							runtimeParameter.childType = column.childType;
							runtimeParameter.type = column.type;
							
							runtimeParameter.staticValue = runtimeParameter.values[i++];
							if(column.nodeSetter != null)
							{
								column.nodeSetter.accept(runtimeParameter, this);
							}
						}
					}
					
					runtimeParameter.childType = null;
					runtimeParameter.type = null;
					runtimeParameter.branchNode = null;
					runtimeParameter.workingBranchNode = null;
				}
			}
			finally 
			{
				resultSet.close();
			}
			
			runtimeParameter.converterProperties.clear();
			
		}
	}
	
	private class PreparedInsertStatementDefinition
	{
		
		public PreparedInsertStatementDefinition(INodeType nodeType) 
		{
			super();
			this.nodeType = nodeType;
			this.create();
		}
		
		private void create()
		{
			BranchNodeMetaModel defaultInstance = (BranchNodeMetaModel)this.nodeType.getValueDefaultInstance();
			
			SQLTable table = (SQLTable)this.nodeType.getTypeClass().getAnnotation(SQLTable.class);
			
			// replaces by referenced field ???
			
			SQLReplace[] replaces = (SQLReplace[])this.nodeType.referencedByField().getAnnotationsByType(SQLReplace.class);
			
			for(SQLReplace replace : replaces)
			{
				for(SQLTable replacedTable : replace.table())
				{
					table = replacedTable;
					break;
				}
			}
			
			if(table == null)
			{ 
				throw new RuntimeException("Annotation SQLTable not found in model " + nodeType.getTypeClass());
			}
			
			columns = new ArrayList<JDBCSetterDefinition>();
			
			StringBuilder sqlColumns = new StringBuilder();
			StringBuilder sqlValues = new StringBuilder();
			
			int cursorPosititon = 1;
			
			for(LeafNodeType leafNode : defaultInstance.getLeafNodeTypeList())
			{
				SQLColumn sqlColumn = (SQLColumn)leafNode.referencedByField().getAnnotation(SQLColumn.class);
				
				for(SQLReplace replace : replaces)
				{
					if(! leafNode.getNodeName().equals(replace.nodeName()))
					{
						continue;
					}
					for(SQLColumn replacedColumn : replace.column())
					{
						
						sqlColumn = replacedColumn;
						break;
					}
				}
				if(sqlColumn == null)
				{
					continue;
				}
				
				if(! sqlColumn.insertable())
				{
					continue;
				}
				
				if(sqlColumns.length() > 0)
				{
					sqlColumns.append(",");
				}
				if(sqlValues.length() > 0)
				{
					sqlValues.append(",");
				}
				
				sqlColumns.append(sqlColumn.name());
				sqlValues.append("?");
				
				this.columns.add(new JDBCSetterDefinition(leafNode, sqlColumn, cursorPosititon++, replaces));
			}
			
			for(BranchNodeType branchNode : defaultInstance.getBranchNodeTypeList())
			{
				SQLColumn sqlColumn = (SQLColumn)branchNode.referencedByField().getAnnotation(SQLColumn.class);
				SQLReferencedByColumn sqlRefrencedByColumn = (SQLReferencedByColumn)branchNode.referencedByField().getAnnotation(SQLReferencedByColumn.class);
				for(SQLReplace replace : replaces)
				{
					if(! branchNode.getNodeName().equals(replace.nodeName()))
					{
						continue;
					}
					for(SQLColumn replacedColumn : replace.column())
					{
						
						sqlColumn = replacedColumn;
						break;
					}
					for(SQLReferencedByColumn replacedReferencedByColumn : replace.referencedByColumn())
					{
						
						sqlRefrencedByColumn = replacedReferencedByColumn;
						break;
					}
				}
				
				if((sqlColumn == null) || (sqlRefrencedByColumn != null))
				{
					continue;
				}
				
				if(! sqlColumn.insertable())
				{
					continue;
				}
				
				JDBCSetterDefinition setColumnDefinition = null;
				BranchNodeMetaModel childInstance = branchNode.getValueDefaultInstance();
				for(LeafNodeType childLeafNode : childInstance.getLeafNodeTypeList())
				{
					SQLColumn sqlColumnChild = (SQLColumn)childLeafNode.referencedByField().getAnnotation(SQLColumn.class);
					SQLPrimaryKey primaryKey = (SQLPrimaryKey)childLeafNode.referencedByField().getAnnotation(SQLPrimaryKey.class);
					
					for(SQLReplace replace : (SQLReplace[])branchNode.referencedByField().getAnnotationsByType(SQLReplace.class))
					{
						if(! childLeafNode.getNodeName().equals(replace.nodeName()))
						{
							continue;
						}
						for(SQLColumn replacedColumn : replace.column())
						{
							
							sqlColumn = replacedColumn;
							break;
						}
						for(SQLPrimaryKey replacedPrimaryKey : replace.primaryKey())
						{
							
							primaryKey = replacedPrimaryKey;
							break;
						}
					}
					
					if((sqlColumnChild == null) || (primaryKey == null))
					{
						continue;
					}
					setColumnDefinition = new JDBCSetterDefinition(childLeafNode, sqlColumn, cursorPosititon++, branchNode.referencedByField().getAnnotationsByType(SQLReplace.class));
					setColumnDefinition.onUpsert = null;
					setColumnDefinition.onInsert = null;
					setColumnDefinition.generateId = null;
					
					break;
					
				}
				
				if(setColumnDefinition == null)
				{
					throw new RuntimeException("no primary key found in " + branchNode.getTypeClass().getCanonicalName());
				}
				
				Class onInsertClass = sqlColumn.onInsert();
				if(onInsertClass != SQLColumn.NoConsumer.class)
				{
					try
					{
						setColumnDefinition.onInsert= (BiConsumer<Node<? extends BranchNodeMetaModel,?>, Map<String,?>>)onInsertClass.newInstance();
					}
					catch (Exception e) 
					{
						throw new RuntimeException(e);
					}
				}
				
				Class onUpsertClass = sqlColumn.onUpsert();
				if(onUpsertClass != SQLColumn.NoConsumer.class)
				{
					try
					{
						setColumnDefinition.onUpsert = (BiConsumer<Node<? extends BranchNodeMetaModel,?>, Map<String,?>>)onUpsertClass.newInstance();
					}
					catch (Exception e) 
					{
						throw new RuntimeException(e);
					}
				}
				
				setColumnDefinition.childType = branchNode;
				
				if(sqlColumns.length() > 0)
				{
					sqlColumns.append(",");
				}
				if(sqlValues.length() > 0)
				{
					sqlValues.append(",");
				}
				
				sqlColumns.append(sqlColumn.name());
				sqlValues.append("?");
				
				this.columns.add(setColumnDefinition);
			}
			
			 // columns defined by Parent
			
			try
			{
				BranchNodeMetaModel defaultInstanceParent = (BranchNodeMetaModel)this.nodeType.getParentNodeClass().newInstance();
				SQLReferencedByColumn referencedByColumn = this.nodeType.referencedByField().getAnnotation(SQLReferencedByColumn.class);
				//SQLColumn sqlColumn = this.nodeType.referencedByField().getAnnotation(SQLColumn.class);
				if(referencedByColumn != null)
				{
					LeafNodeType<BranchNodeMetaModel, ?>  leafNodePrimaryKeyParent = null;
					SQLColumn sqlColumnPrimaryKeyParent = null;
					for(LeafNodeType<BranchNodeMetaModel, ?>  childNodeType : defaultInstanceParent.getLeafNodeTypeList())
					{
						SQLColumn sqlColumnParent = childNodeType.referencedByField().getAnnotation(SQLColumn.class);
						SQLPrimaryKey primaryKey = childNodeType.referencedByField().getAnnotation(SQLPrimaryKey.class);
						if((primaryKey != null) && (sqlColumnParent != null))
						{
							leafNodePrimaryKeyParent = childNodeType;
							sqlColumnPrimaryKeyParent = sqlColumnParent;
						}
					}
					
					JDBCSetterDefinition setColumnDefinition = new JDBCSetterDefinition(leafNodePrimaryKeyParent, sqlColumnPrimaryKeyParent, cursorPosititon++, null);
					setColumnDefinition.onUpsert = null;
					setColumnDefinition.onInsert = null;
					setColumnDefinition.generateId = null;
					setColumnDefinition.parentType = true;
					
					if(sqlColumns.length() > 0)
					{
						sqlColumns.append(",");
					}
					if(sqlValues.length() > 0)
					{
						sqlValues.append(",");
					}
					
					sqlColumns.append(referencedByColumn.name());
					sqlValues.append("?");
					
					this.columns.add(setColumnDefinition);
				}
			}
			catch (Exception e) 
			{
				throw new RuntimeException(e);
			}
			
			this.sql = "insert into " + table.name() + " (" + sqlColumns + ") values (" + sqlValues + ")";
			
		}
		
		private INodeType nodeType = null;
		private BranchNodeMetaModel type = null;
		private String domain = null;
		private String boundedContext = null;
		private String service = null;
		private String sql = null;
		private List<JDBCSetterDefinition> columns = null;
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void insertNode(RuntimeParameter runtimeParameter) throws SQLException
		{
			runtimeParameter.preparedStatement = runtimeParameter.getPreparedStatement(this.sql);
			
			runtimeParameter.converterProperties.clear();
			runtimeParameter.converterProperties.put(Connection.class.getCanonicalName(), runtimeParameter.connection);
			runtimeParameter.converterProperties.put(PreparedStatement.class.getCanonicalName(), runtimeParameter.preparedStatement);
			runtimeParameter.converterProperties.put(IRuntimeParameter.class.getCanonicalName(), runtimeParameter);
			
			for(JDBCSetterDefinition column : this.columns)
			{
				try
				{
					runtimeParameter.converterProperties.put(BranchNode.class.getCanonicalName(), runtimeParameter.branchNode);
					runtimeParameter.converterProperties.put("BRANCH_CHILD",column.childType);
					runtimeParameter.converterProperties.put("LEAF_CHILD",column.type);
					
					Node node = null;		// TriggerParameter
					if(column.childType instanceof BranchNodeType)
					{
						node = runtimeParameter.branchNode.get((BranchNodeType)column.childType);// TODO force no autocreate
						runtimeParameter.workingBranchNode = (BranchNode)node;
					}
					else if(column.parentType)
					{
						node = runtimeParameter.branchNode.getParentNode();
						runtimeParameter.workingBranchNode = (BranchNode)node;
					}
					else if(column.type instanceof LeafNodeType)
					{
						node = runtimeParameter.branchNode.get((LeafNodeType)column.type);
						runtimeParameter.workingBranchNode = runtimeParameter.branchNode;
					}
					else
					{
						throw new RuntimeException("invalid nodetype settings");
					}
					
					if(column.onInsert != null)
					{
						column.onInsert.accept(node, runtimeParameter.converterProperties);
						
						if(column.childType instanceof BranchNodeType)
						{
							node = runtimeParameter.branchNode.get((BranchNodeType)column.childType);
							runtimeParameter.workingBranchNode = (BranchNode)node;
						}
					}
					if(column.onUpsert != null)
					{
						column.onUpsert.accept(node, runtimeParameter.converterProperties);
						if(column.childType instanceof BranchNodeType)
						{
							node = runtimeParameter.branchNode.get((BranchNodeType)column.childType);
							runtimeParameter.workingBranchNode = (BranchNode)node;
						}
					}
					
					if(column.generateId != null)
					{
						column.generateId.accept(node, runtimeParameter.converterProperties);
					}
					try
					{
						column.setter.acceptWithException(runtimeParameter);
					}
					catch (SQLException e) 
					{
						throw e;
					}
					catch (Exception e) 
					{
						throw new RuntimeException(e);
					}
					
				}
				finally 
				{
					runtimeParameter.workingBranchNode = null;
				}
			}
			
			runtimeParameter.converterProperties.clear();
			
			runtimeParameter.preparedStatement.executeUpdate();
		}
		
		private void close()
		{
			this.nodeType = null;
			this.type = null;
			this.domain = null;
			this.boundedContext = null;
			this.service = null;
			this.sql = null;
			if(this.columns != null)
			{
				this.columns.forEach(c -> c.close());
				this.columns.clear();
			}
			this.columns = null;
		}
	}
	
	private class PreparedDeleteStatementDefinition
	{
		
		public PreparedDeleteStatementDefinition(INodeType nodeType) 
		{
			super();
			this.nodeType = nodeType;
			this.create();
		}
		
		private void create()
		{
			this.tableNode = NodeHelper.parseTableNode(this.nodeType);
			this.sql = "DELETE FROM " + tableNode.getTableName() + " WHERE " + tableNode.getPrimaryKeyName() + " = ? ";
			
		}
		
		private INodeType nodeType = null;
		private NodeHelper.TableNode tableNode = null;
		private BranchNodeMetaModel type = null;
		private String sql = null;
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void deleteNode(RuntimeParameter runtimeParameter) throws SQLException
		{
			runtimeParameter.preparedStatement = runtimeParameter.getPreparedStatement(this.sql);
			
			Object value = runtimeParameter.branchNode.getValue(tableNode.getPrimaryKeyLeafNode());
			
			if(tableNode.getPrimaryKeyLeafNode().getTypeClass() == String.class)
			{
				runtimeParameter.preparedStatement.setString(1, (String)value);
			}
			if(tableNode.getPrimaryKeyLeafNode().getTypeClass() == UUID.class)
			{
				runtimeParameter.preparedStatement.setString(1, ((UUID)value).toString());
			}
			if(tableNode.getPrimaryKeyLeafNode().getTypeClass() == Long.class)
			{
				runtimeParameter.preparedStatement.setLong(1, (Long)value);
			}
			else
			{
				runtimeParameter.preparedStatement.setString(1, value.toString());
			}
			
			runtimeParameter.preparedStatement.executeUpdate();
		}
		
		private void close()
		{
			this.nodeType = null;
			this.type = null;
			this.sql = null;
			this.tableNode = null;
		}
	}
	
	private class PreparedUpdateStatementDefinition
	{
		
		public PreparedUpdateStatementDefinition(INodeType nodeType) 
		{
			super();
			this.nodeType = nodeType;
			this.create();
		}
		
		private void create()
		{
			BranchNodeMetaModel defaultInstance = (BranchNodeMetaModel)this.nodeType.getValueDefaultInstance();
			
			SQLTable table = (SQLTable)this.nodeType.getTypeClass().getAnnotation(SQLTable.class);
			
			// replaces by referenced field ???
			
			SQLReplace[] replaces = (SQLReplace[])this.nodeType.referencedByField().getAnnotationsByType(SQLReplace.class);
			
			for(SQLReplace replace : replaces)
			{
				for(SQLTable replacedTable : replace.table())
				{
					table = replacedTable;
					break;
				}
			}
			
			if(table == null)
			{ 
				throw new RuntimeException("Annotation SQLTable not found in model " + nodeType.getTypeClass());
			}
			
			columns = new ArrayList<JDBCSetterDefinition>();
			
			StringBuilder sqlColumns = new StringBuilder();
			
			int cursorPosititon = 1;
			
			LeafNodeType leafNodePrimaryKey = null;
			SQLColumn sqlColumnPrimaryKey = null;
			
			for(LeafNodeType leafNode : defaultInstance.getLeafNodeTypeList())
			{
				SQLColumn sqlColumn = leafNode.referencedByField().getAnnotation(SQLColumn.class);
				SQLPrimaryKey sqlPrimaryKey = leafNode.referencedByField().getAnnotation(SQLPrimaryKey.class);
				
				for(SQLReplace replace : replaces)
				{
					if(! leafNode.getNodeName().equals(replace.nodeName()))
					{
						continue;
					}
					for(SQLColumn replacedColumn : replace.column())
					{
						
						sqlColumn = replacedColumn;
						break;
					}
				}
				if(sqlColumn == null)
				{
					continue;
				}
				
				if(sqlPrimaryKey != null)
				{
					leafNodePrimaryKey = leafNode;
					sqlColumnPrimaryKey = sqlColumn;
					continue;
				}
				
				if(! sqlColumn.updatable())
				{
					continue;
				}
				
				if(sqlColumns.length() > 0)
				{
					sqlColumns.append(" , ");
				}
				
				sqlColumns.append(sqlColumn.name() + " = ? ");
				
				this.columns.add(new JDBCSetterDefinition(leafNode, sqlColumn, cursorPosititon++, replaces));
			}
			
			if(leafNodePrimaryKey == null)
			{
				throw new RuntimeException("PrimaryKey not found in " + defaultInstance.getClass());
			}
			
			for(BranchNodeType branchNode : defaultInstance.getBranchNodeTypeList())
			{
				SQLColumn sqlColumn = (SQLColumn)branchNode.referencedByField().getAnnotation(SQLColumn.class);
				SQLReferencedByColumn sqlRefrencedByColumn = (SQLReferencedByColumn)branchNode.referencedByField().getAnnotation(SQLReferencedByColumn.class);
				for(SQLReplace replace : replaces)
				{
					if(! branchNode.getNodeName().equals(replace.nodeName()))
					{
						continue;
					}
					for(SQLColumn replacedColumn : replace.column())
					{
						
						sqlColumn = replacedColumn;
						break;
					}
					for(SQLReferencedByColumn replacedReferencedByColumn : replace.referencedByColumn())
					{
						
						sqlRefrencedByColumn = replacedReferencedByColumn;
						break;
					}
				}
				
				if((sqlColumn == null) || (sqlRefrencedByColumn != null))
				{
					continue;
				}
				
				if(! sqlColumn.insertable())
				{
					continue;
				}
				
				JDBCSetterDefinition setColumnDefinition = null;
				BranchNodeMetaModel childInstance = branchNode.getValueDefaultInstance();
				for(LeafNodeType childLeafNode : childInstance.getLeafNodeTypeList())
				{
					SQLColumn sqlColumnChild = (SQLColumn)childLeafNode.referencedByField().getAnnotation(SQLColumn.class);
					SQLPrimaryKey primaryKey = (SQLPrimaryKey)childLeafNode.referencedByField().getAnnotation(SQLPrimaryKey.class);
					
					for(SQLReplace replace : (SQLReplace[])branchNode.referencedByField().getAnnotationsByType(SQLReplace.class))
					{
						if(! childLeafNode.getNodeName().equals(replace.nodeName()))
						{
							continue;
						}
						for(SQLColumn replacedColumn : replace.column())
						{
							
							sqlColumn = replacedColumn;
							break;
						}
						for(SQLPrimaryKey replacedPrimaryKey : replace.primaryKey())
						{
							
							primaryKey = replacedPrimaryKey;
							break;
						}
					}
					
					if((sqlColumnChild == null) || (primaryKey == null))
					{
						continue;
					}
					setColumnDefinition = new JDBCSetterDefinition(childLeafNode, sqlColumn, cursorPosititon++, branchNode.referencedByField().getAnnotationsByType(SQLReplace.class));
					setColumnDefinition.onUpsert = null;
					setColumnDefinition.onInsert = null;
					setColumnDefinition.generateId = null;
					
					break;
					
				}
				
				if(setColumnDefinition == null)
				{
					throw new RuntimeException("no primary key found in " + branchNode.getTypeClass().getCanonicalName());
				}
				
				Class onInsertClass = sqlColumn.onInsert();
				if(onInsertClass != SQLColumn.NoConsumer.class)
				{
					try
					{
						setColumnDefinition.onInsert= (BiConsumer<Node<? extends BranchNodeMetaModel,?>, Map<String,?>>)onInsertClass.newInstance();
					}
					catch (Exception e) 
					{
						throw new RuntimeException(e);
					}
				}
				
				Class onUpsertClass = sqlColumn.onUpsert();
				if(onUpsertClass != SQLColumn.NoConsumer.class)
				{
					try
					{
						setColumnDefinition.onUpsert = (BiConsumer<Node<? extends BranchNodeMetaModel,?>, Map<String,?>>)onUpsertClass.newInstance();
					}
					catch (Exception e) 
					{
						throw new RuntimeException(e);
					}
				}
				
				setColumnDefinition.childType = branchNode;
				
				if(sqlColumns.length() > 0)
				{
					sqlColumns.append(" , ");
				}
				
				sqlColumns.append(sqlColumn.name() + " = ? ");
				
				this.columns.add(setColumnDefinition);
			}
			
			 // columns defined by Parent
			
			try
			{
				BranchNodeMetaModel defaultInstanceParent = (BranchNodeMetaModel)this.nodeType.getParentNodeClass().newInstance();
				SQLReferencedByColumn referencedByColumn = this.nodeType.referencedByField().getAnnotation(SQLReferencedByColumn.class);
				//SQLColumn sqlColumn = this.nodeType.referencedByField().getAnnotation(SQLColumn.class);
				if(referencedByColumn != null)
				{
					LeafNodeType<BranchNodeMetaModel, ?>  leafNodePrimaryKeyParent = null;
					SQLColumn sqlColumnPrimaryKeyParent = null;
					for(LeafNodeType<BranchNodeMetaModel, ?>  childNodeType : defaultInstanceParent.getLeafNodeTypeList())
					{
						SQLColumn sqlColumnParent = childNodeType.referencedByField().getAnnotation(SQLColumn.class);
						SQLPrimaryKey primaryKey = childNodeType.referencedByField().getAnnotation(SQLPrimaryKey.class);
						if((primaryKey != null) && (sqlColumnParent != null))
						{
							leafNodePrimaryKeyParent = childNodeType;
							sqlColumnPrimaryKeyParent = sqlColumnParent;
						}
					}
					
					JDBCSetterDefinition setColumnDefinition = new JDBCSetterDefinition(leafNodePrimaryKeyParent, sqlColumnPrimaryKeyParent, cursorPosititon++, null);
					setColumnDefinition.onUpsert = null;
					setColumnDefinition.onInsert = null;
					setColumnDefinition.generateId = null;
					setColumnDefinition.parentType = true;
					
					if(sqlColumns.length() > 0)
					{
						sqlColumns.append("  ,  ");
					}
					
					sqlColumns.append(referencedByColumn.name() + " = ? ");
					
					this.columns.add(setColumnDefinition);
				}
			}
			catch (Exception e) 
			{
				throw new RuntimeException(e);
			}
			
			
			this.columns.add(new JDBCSetterDefinition(leafNodePrimaryKey, sqlColumnPrimaryKey, cursorPosititon++, null));
			
			this.sql = "update " + table.name() + " set " + sqlColumns +  " where " + sqlColumnPrimaryKey.name() + " = ? ";
			
		}
		
		private INodeType nodeType = null;
		private BranchNodeMetaModel type = null;
		private String domain = null;
		private String boundedContext = null;
		private String service = null;
		private String sql = null;
		private List<JDBCSetterDefinition> columns = null;
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void updateNode(RuntimeParameter runtimeParameter) throws SQLException
		{
			runtimeParameter.preparedStatement = runtimeParameter.getPreparedStatement(this.sql);
			
			runtimeParameter.converterProperties.clear();
			runtimeParameter.converterProperties.put(Connection.class.getCanonicalName(), runtimeParameter.connection);
			runtimeParameter.converterProperties.put(PreparedStatement.class.getCanonicalName(), runtimeParameter.preparedStatement);
			runtimeParameter.converterProperties.put(IRuntimeParameter.class.getCanonicalName(), runtimeParameter);
			
			for(JDBCSetterDefinition column : this.columns)
			{
				try
				{
					runtimeParameter.converterProperties.put(BranchNode.class.getCanonicalName(), runtimeParameter.branchNode);
					runtimeParameter.converterProperties.put("BRANCH_CHILD",column.childType);
					runtimeParameter.converterProperties.put("LEAF_CHILD",column.type);
					
					Node node = null;		// TriggerParameter
					if(column.childType instanceof BranchNodeType)
					{
						node = runtimeParameter.branchNode.get((BranchNodeType)column.childType);// TODO force no autocreate
						runtimeParameter.workingBranchNode = (BranchNode)node;
					}
					else if(column.parentType)
					{
						node = runtimeParameter.branchNode.getParentNode();
						runtimeParameter.workingBranchNode = (BranchNode)node;
					}
					else if(column.type instanceof LeafNodeType)
					{
						node = runtimeParameter.branchNode.get((LeafNodeType)column.type);
						runtimeParameter.workingBranchNode = runtimeParameter.branchNode;
					}
					else
					{
						throw new RuntimeException("invalid nodetype settings");
					}
					
					
					if(column.onUpsert != null)
					{
						column.onUpsert.accept(node, runtimeParameter.converterProperties);
						if(column.childType instanceof BranchNodeType)
						{
							node = runtimeParameter.branchNode.get((BranchNodeType)column.childType);
							runtimeParameter.workingBranchNode = (BranchNode)node;
						}
					}
					try
					{
						column.setter.acceptWithException(runtimeParameter);
					}
					catch (SQLException e) 
					{
						throw e;
					}
					catch (Exception e) 
					{
						throw new RuntimeException(e);
					}
				}
				finally 
				{
					runtimeParameter.workingBranchNode = null;
				}
			}
			
			runtimeParameter.converterProperties.clear();
			
			runtimeParameter.preparedStatement.executeUpdate();
		}
		
		private void close()
		{
			this.nodeType = null;
			this.type = null;
			this.domain = null;
			this.boundedContext = null;
			this.service = null;
			this.sql = null;
			if(this.columns != null)
			{
				this.columns.forEach(c -> c.close());
				this.columns.clear();
			}
			this.columns = null;
		}
	}
	
	private class CheckPersistableIsNewDefinition
	{
		public CheckPersistableIsNewDefinition(INodeType nodeType)
		{
			super();
			this.nodeType = nodeType;
			this.checks = new ArrayList<CatchedExceptionConsumer<RuntimeParameter>>();
			this.create();
		}
		
		private INodeType nodeType = null;
		private List<CatchedExceptionConsumer<RuntimeParameter>> checks = null;
		 
		private void create()
		{
			BranchNodeMetaModel defaultInstance = (BranchNodeMetaModel)this.nodeType.getValueDefaultInstance();
			SQLTable table = (SQLTable)this.nodeType.getTypeClass().getAnnotation(SQLTable.class);
			SQLReplace[] replaces = (SQLReplace[])this.nodeType.referencedByField().getAnnotationsByType(SQLReplace.class);
			for(SQLReplace replace : replaces)
			{
				for(SQLTable replacedTable : replace.table())
				{
					table = replacedTable;
					break;
				}
			}
			
			for(LeafNodeType leafNodeType : defaultInstance.getLeafNodeTypeList())
			{
				SQLColumn sqlColumn = (SQLColumn)leafNodeType.referencedByField().getAnnotation(SQLColumn.class);
				SQLPrimaryKey sqlPrimaryKey = (SQLPrimaryKey)leafNodeType.referencedByField().getAnnotation(SQLPrimaryKey.class);
				
				for(SQLReplace replace : replaces)
				{
					if(! leafNodeType.getNodeName().equals(replace.nodeName()))
					{
						continue;
					}
					for(SQLColumn replacedColumn : replace.column())
					{
						sqlColumn = replacedColumn;
						break;
					}
					for(SQLPrimaryKey replacedPrimaryKey : replace.primaryKey())
					{
						sqlPrimaryKey = replacedPrimaryKey;
						break;
					}
				}
				
				if(sqlPrimaryKey == null)
				{
					continue;
				}
				
				if(sqlColumn == null)
				{
					throw new RuntimeException("No annotation SQLColumn found for primary key " + leafNodeType.referencedByField());
				}
				
				checks.add(new CheckPKLeafNode(nodeType, leafNodeType,table, sqlColumn, sqlPrimaryKey));
			}
		}
		
		private boolean checkIsNew(RuntimeParameter runtimeParameter)
		{
			runtimeParameter.isExisting.setValue(false);
			runtimeParameter.isNew.setValue(false);
			
			for(Consumer<RuntimeParameter> check : checks)
			{
				check.accept(runtimeParameter);
			}
			
			if(runtimeParameter.isExisting.getValue().booleanValue() && runtimeParameter.isNew.getValue().booleanValue())
			{
				throw new RuntimeException("conflict detected in new state for branch node");
			}
			
			if((!runtimeParameter.isExisting.getValue().booleanValue()) && (!runtimeParameter.isNew.getValue().booleanValue()))
			{
				throw new RuntimeException("new-state not detected for branch node");
			}
			
			return runtimeParameter.isNew.getValue().booleanValue();
		}
		
		private void close()
		{
			if(checks != null)
			{
				for(Consumer<RuntimeParameter> check : checks)
				{
					if(check instanceof AutoCloseable)
					{
						try
						{
							((AutoCloseable)check).close();
						}
						catch (Exception e) {}
					}
				}
				checks.clear();
			}
			checks = null;
			nodeType = null;
		}
		
		private class CheckPKLeafNode implements CatchedExceptionConsumer<RuntimeParameter>, AutoCloseable
		{
			private JDBCSetterDefinition columnDefinition = null;
			private LeafNodeType leafNodeType = null;
			
			private CheckPKLeafNode(INodeType nodeType,LeafNodeType leafNodeType,SQLTable sqlTable, SQLColumn sqlColumn,SQLPrimaryKey sqlPrimaryKey)
			{
				super();
				this.columnDefinition = new JDBCSetterDefinition(leafNodeType,sqlColumn, -1, null);
				this.leafNodeType = leafNodeType;
			}

			@Override
			public void acceptWithException(RuntimeParameter runtimeParameter) 
			{
				LeafNode<BranchNodeMetaModel, ?> leafNode = runtimeParameter.branchNode.get(leafNodeType);
				if(leafNode.getValue() == null)
				{
					runtimeParameter.isNew.setValue(true);
				}
				else
				{
					// TODO Check DB
					runtimeParameter.isExisting.setValue(true);
				}				
			}

			@Override
			public void close() throws Exception
			{
				this.columnDefinition = null;
				this.leafNodeType = null;
			}
		}
	}
	
	public class JDBCSetterDefinition
	{
		public JDBCSetterDefinition(LeafNodeType childNodeType, SQLColumn sqlColumn, int cursorPosition, SQLReplace[] replacesByParent)
		{
			super();
			this.cursorPosition = cursorPosition;
			this.type = childNodeType;
			
			SQLColumnType type = sqlColumn.type();
			SQLPrimaryKey primaryKey = (SQLPrimaryKey)this.type.referencedByField().getAnnotation(SQLPrimaryKey.class);
			
			if(replacesByParent != null)
			{
				if(primaryKey != null)
				{
					for(SQLReplace replace : replacesByParent)
					{
						if(! "".equals(replace.nodeName()))
						{
							continue;
						}
						for(SQLPrimaryKey replacedPrimaryKey : replace.primaryKey())
						{
							
							primaryKey = replacedPrimaryKey;
							break;
						}
					
					}
				}
				for(SQLReplace replace : replacesByParent)
				{
					if(! this.type.getNodeName().equals(replace.nodeName()))
					{
						continue;
					}
					for(SQLPrimaryKey replacedPrimaryKey : replace.primaryKey())
					{
						primaryKey = replacedPrimaryKey;
						break;
					}
				}
			}
			
			
			if(type == SQLColumnType.AUTO)
			{
				if(childNodeType.getTypeClass() == Boolean.class)
				{
					type = SQLColumnType.BOOLEAN;
				}
				else if(childNodeType.getTypeClass() == Integer.class)
				{
					type = SQLColumnType.INTEGER;
				}
				else if(childNodeType.getTypeClass() == Long.class)
				{
					type = SQLColumnType.BIGINT;
				}
				else if(childNodeType.getTypeClass() == Float.class)
				{
					type = SQLColumnType.REAL;
				}
				else if(childNodeType.getTypeClass() == Double.class)
				{
					type = SQLColumnType.DOUBLE;
				}
				else if(childNodeType.getTypeClass() == Date.class)
				{
					type = SQLColumnType.TIMESTAMP;
				}
				else
				{
					type = SQLColumnType.VARCHAR;
				}
				
			}
			Class converterClass = sqlColumn.node2JDBC();
			if(converterClass == SQLColumn.NoNode2JDBC.class)
			{
				converterClass = null;
			}
			
			Class onInsertClass = sqlColumn.onInsert();
			if(onInsertClass != SQLColumn.NoConsumer.class)
			{
				try
				{
					this.onInsert = (BiConsumer<Node<? extends BranchNodeMetaModel,?>, Map<String,?>>)onInsertClass.newInstance();
				}
				catch (Exception e) 
				{
					throw new RuntimeException(e);
				}
			}
			
			Class onUpsertClass = sqlColumn.onUpsert();
			if(onUpsertClass != SQLColumn.NoConsumer.class)
			{
				try
				{
					this.onUpsert = (BiConsumer<Node<? extends BranchNodeMetaModel,?>, Map<String,?>>)onUpsertClass.newInstance();
				}
				catch (Exception e) 
				{
					throw new RuntimeException(e);
				}
			}
			
			// TODO onUpdate
			
			if((type == SQLColumnType.BOOLEAN))
			{
				this.setter = new LeafNodeBooleanJDBCSetter(); 
			}
			else if((type == SQLColumnType.INTEGER))
			{
				this.setter = new LeafNodeIntegerJDBCSetter(); 
			}
			else if((type == SQLColumnType.BIGINT))
			{
				this.setter = new LeafNodeLongJDBCSetter(); 
			}
			else if((type == SQLColumnType.REAL))
			{
				this.setter = new LeafNodeFloatJDBCSetter(); 
			}
			else if((type == SQLColumnType.DOUBLE))
			{
				this.setter = new LeafNodeDoubleJDBCSetter(); 
			}
			else if((type == SQLColumnType.TIMESTAMP))
			{
				this.setter = new LeafNodeTimestampJDBCSetter(); 
			}
			else if((type == SQLColumnType.DATE))
			{
				this.setter = new LeafNodeDateJDBCSetter(); 
			}
			else if((type == SQLColumnType.TIME))
			{
				this.setter = new LeafNodeTimeJDBCSetter(); 
			}
			else
			{
				this.setter = new LeafNodeStringJDBCSetter();
			}
			
			if(primaryKey != null)
			{
				if (this.type.getTypeClass() == UUID.class)
				{
					this.generateId = new PrimaryKeyByNewUUID();
				}
			}
			
			if(converterClass != null)
			 {
				 try
				 {
					 this.converter = (Function)converterClass.newInstance();
				 }
				 catch (Exception e) 
				 {
					throw new RuntimeException(e);
				}
			 }
		}
		
		private LeafNodeType<? extends BranchNodeMetaModel,?> type;
		private BranchNodeType<? extends BranchNodeMetaModel,?> childType = null;
		private boolean parentType = false;
		private int cursorPosition;
		private Function<Object, Object> converter = null;
		private CatchedExceptionConsumer<RuntimeParameter> setter = null;
		private AssociationType associationType = null;
		private BiConsumer<Node<? extends BranchNodeMetaModel,?>, Map<String,?>> onUpsert = null;
		private BiConsumer<Node<? extends BranchNodeMetaModel,?>, Map<String,?>> onInsert = null;
		private BiConsumer<Node<? extends BranchNodeMetaModel,?>, Map<String,?>> generateId = null;
		
		public INodeType<? extends BranchNodeMetaModel, ?> getType() 
		{
			return type;
		}
		public int getCursorPosition() 
		{
			return cursorPosition;
		}
		public Function<?, ?> getConverter() 
		{
			return converter;
		}
		public AssociationType getAssociationType() 
		{
			return associationType;
		}
		
		private abstract class LeafNodeJDBCSetter implements CatchedExceptionConsumer<RuntimeParameter>
		{
			@Override
			public void acceptWithException(RuntimeParameter runtimeParameter) throws SQLException
			{
				Object value = null;
				if(converter != null)
				{
					if(runtimeParameter.workingBranchNode == null)
					{
						value = converter.apply(null);
					}
					else
					{
						value = converter.apply(runtimeParameter.workingBranchNode.getValue((LeafNodeType)JDBCSetterDefinition.this.type));
					}
				}
				else
				{
					
					if(runtimeParameter.workingBranchNode == null)
					{
						value = null;
					}
					else
					{
						value = runtimeParameter.workingBranchNode.getValue((LeafNodeType)JDBCSetterDefinition.this.type);
					}
				}
				
				this.setValue(runtimeParameter, value);
			}
			
			public abstract void setValue(RuntimeParameter runtimeParameter, Object value) throws SQLException;
		}
		
		private class LeafNodeStringJDBCSetter extends LeafNodeJDBCSetter
		{
			@Override
			public void setValue(RuntimeParameter runtimeParameter, Object value) throws SQLException
			{
				if(value == null)
				{
					runtimeParameter.preparedStatement.setNull(JDBCSetterDefinition.this.cursorPosition, Types.VARCHAR);
				}
				else
				{
					if(value instanceof String)
					{
						runtimeParameter.preparedStatement.setString(JDBCSetterDefinition.this.cursorPosition, (String) value);
					}
					else
					{
						runtimeParameter.preparedStatement.setString(JDBCSetterDefinition.this.cursorPosition, value.toString());
					}
				}
			}
		}
		
		private class LeafNodeBooleanJDBCSetter extends LeafNodeJDBCSetter
		{
			@Override
			public void setValue(RuntimeParameter runtimeParameter, Object value) throws SQLException
			{
				if(value == null)
				{
					runtimeParameter.preparedStatement.setNull(JDBCSetterDefinition.this.cursorPosition, Types.BOOLEAN);
				}
				else
				{
					runtimeParameter.preparedStatement.setBoolean(JDBCSetterDefinition.this.cursorPosition, (Boolean) value);
				}
			}
		}
		
		private class LeafNodeIntegerJDBCSetter extends LeafNodeJDBCSetter
		{
			@Override
			public void setValue(RuntimeParameter runtimeParameter, Object value) throws SQLException
			{
				if(value == null)
				{
					runtimeParameter.preparedStatement.setNull(JDBCSetterDefinition.this.cursorPosition, Types.INTEGER);
				}
				else
				{
					runtimeParameter.preparedStatement.setInt(JDBCSetterDefinition.this.cursorPosition, (Integer) value);
				}
			}
		}
		
		private class LeafNodeLongJDBCSetter extends LeafNodeJDBCSetter
		{
			@Override
			public void setValue(RuntimeParameter runtimeParameter, Object value) throws SQLException
			{
				if(value == null)
				{
					runtimeParameter.preparedStatement.setNull(JDBCSetterDefinition.this.cursorPosition, Types.BIGINT);
				}
				else
				{
					runtimeParameter.preparedStatement.setLong(JDBCSetterDefinition.this.cursorPosition, (Long) value);
				}
			}
		}
		
		private class LeafNodeFloatJDBCSetter extends LeafNodeJDBCSetter
		{
			@Override
			public void setValue(RuntimeParameter runtimeParameter, Object value) throws SQLException
			{
				if(value == null)
				{
					runtimeParameter.preparedStatement.setNull(JDBCSetterDefinition.this.cursorPosition, Types.FLOAT);
				}
				else
				{
					runtimeParameter.preparedStatement.setFloat(JDBCSetterDefinition.this.cursorPosition, (Float) value);
				}
			}
		}
		
		private class LeafNodeDoubleJDBCSetter extends LeafNodeJDBCSetter
		{
			@Override
			public void setValue(RuntimeParameter runtimeParameter, Object value) throws SQLException
			{
				if(value == null)
				{
					runtimeParameter.preparedStatement.setNull(JDBCSetterDefinition.this.cursorPosition, Types.DOUBLE);
				}
				else
				{
					runtimeParameter.preparedStatement.setDouble(JDBCSetterDefinition.this.cursorPosition, (Double) value);
				}
			}
		}
		
		private class LeafNodeTimestampJDBCSetter extends LeafNodeJDBCSetter
		{
			@Override
			public void setValue(RuntimeParameter runtimeParameter, Object value) throws SQLException
			{
				if(value == null)
				{
					runtimeParameter.preparedStatement.setNull(JDBCSetterDefinition.this.cursorPosition, Types.TIMESTAMP);
				}
				else
				{
					runtimeParameter.preparedStatement.setTimestamp(JDBCSetterDefinition.this.cursorPosition, new java.sql.Timestamp(( (Date) value).getTime()));
				}
			}
		}
		
		private class LeafNodeDateJDBCSetter extends LeafNodeJDBCSetter
		{
			@Override
			public void setValue(RuntimeParameter runtimeParameter, Object value) throws SQLException
			{
				if(value == null)
				{
					runtimeParameter.preparedStatement.setNull(JDBCSetterDefinition.this.cursorPosition, Types.DATE);
				}
				else
				{
					runtimeParameter.preparedStatement.setDate(JDBCSetterDefinition.this.cursorPosition, new java.sql.Date(( (Date) value).getTime()));
				}
			}
		}
		
		private class LeafNodeTimeJDBCSetter extends LeafNodeJDBCSetter
		{
			@Override
			public void setValue(RuntimeParameter runtimeParameter, Object value) throws SQLException
			{
				if(value == null)
				{
					runtimeParameter.preparedStatement.setNull(JDBCSetterDefinition.this.cursorPosition, Types.TIME);
				}
				else
				{
					runtimeParameter.preparedStatement.setTime(JDBCSetterDefinition.this.cursorPosition, new java.sql.Time(( (Date) value).getTime()));
				}
			}
		}
		
		public void close()
		{
			this.type = null;
			this.childType = null;
			this.parentType = false;
			this.converter = null;
			this.setter = null;
			this.associationType = null;
			this.onUpsert = null;
			this.onInsert = null;
			this.generateId = null;
		}
	}
	
	public class JDBCGetterDefinition
	{
		public JDBCGetterDefinition(LeafNodeType childNodeType, SQLColumn sqlColumn, int cursorPosition,  BiConsumer<RuntimeParameter, PreparedLoadResultSetDefinition> nodeSetter)
		{
			super();
			this.cursorPosition = cursorPosition;
			this.type = childNodeType;
			this.nodeSetter = nodeSetter;
			
			SQLColumnType type = sqlColumn.type();
			
			if(type == SQLColumnType.AUTO)
			{
				if(childNodeType.getTypeClass() == Boolean.class)
				{
					type = SQLColumnType.BOOLEAN;
				}
				else if(childNodeType.getTypeClass() == Integer.class)
				{
					type = SQLColumnType.INTEGER;
				}
				else if(childNodeType.getTypeClass() == Long.class)
				{
					type = SQLColumnType.BIGINT;
				}
				else if(childNodeType.getTypeClass() == Float.class)
				{
					type = SQLColumnType.REAL;
				}
				else if(childNodeType.getTypeClass() == Double.class)
				{
					type = SQLColumnType.DOUBLE;
				}
				else if(childNodeType.getTypeClass() == Date.class)
				{
					type = SQLColumnType.TIMESTAMP;
				}
				else
				{
					type = SQLColumnType.VARCHAR;
				}
				
			}
			Class converterClass = sqlColumn.JDBC2Node();
			if(converterClass == SQLColumn.NoJDBC2Node.class)
			{
				converterClass = null;
			}
			
			if((type == SQLColumnType.BOOLEAN))
			{
				this.getter = new LeafNodeBooleanJDBCGetter(); 
			}
			else if((type == SQLColumnType.INTEGER))
			{
				this.getter = new LeafNodeIntegerJDBCGetter(); 
			}
			else if((type == SQLColumnType.BIGINT))
			{
				this.getter = new LeafNodeLongJDBCGetter(); 
			}
			else if((type == SQLColumnType.REAL))
			{
				this.getter = new LeafNodeFloatJDBCGetter(); 
			}
			else if((type == SQLColumnType.DOUBLE))
			{
				this.getter = new LeafNodeDoubleJDBCGetter(); 
			}
			else if((type == SQLColumnType.TIMESTAMP))
			{
				this.getter = new LeafNodeTimestampJDBCGetter(); 
			}
			else if((type == SQLColumnType.DATE))
			{
				this.getter = new LeafNodeDateJDBCGetter(); 
			}
			else if((type == SQLColumnType.TIME))
			{
				this.getter = new LeafNodeTimeJDBCGetter(); 
			}
			else
			{
				this.getter = new LeafNodeStringJDBCGetter();
			}
			
			
			 if(converterClass != null)
			 {
				 try
				 {
					 this.converter = (Function)converterClass.newInstance();
				 }
				 catch (Exception e) 
				 {
					throw new RuntimeException(e);
				}
			 }
		}
		
		private LeafNodeType<? extends BranchNodeMetaModel,?> type;
		private BranchNodeType<? extends BranchNodeMetaModel,?> childType = null;
		private boolean parentType = false;
		private int cursorPosition;
		private Function<Object, Object> converter = null;
		private CatchedExceptionConsumer<RuntimeParameter> getter = null;
		private AssociationType associationType = null;
		private BiConsumer<RuntimeParameter, PreparedLoadResultSetDefinition> nodeSetter = null;
		
		public void close()
		{
			this.type = null;
			this.childType = null;
			this.converter = null;
			this.getter = null;
			this.associationType = null;
			this.nodeSetter = null;
		}
		
		public INodeType<? extends BranchNodeMetaModel, ?> getType() 
		{
			return type;
		}
		public int getCursorPosition() 
		{
			return cursorPosition;
		}
		public Function<?, ?> getConverter() 
		{
			return converter;
		}
		public AssociationType getAssociationType() 
		{
			return associationType;
		}
		
		private abstract class LeafNodeJDBCGetter implements CatchedExceptionConsumer<RuntimeParameter>
		{
			@Override
			public void acceptWithException(RuntimeParameter runtimeParameter) throws Exception
			{
				Object value = this.getValue(runtimeParameter);
				if(converter != null)
				{
					value = converter.apply(value);
				}
				runtimeParameter.values[JDBCGetterDefinition.this.cursorPosition - 1] = value;
			}
			
			public abstract Object getValue(RuntimeParameter runtimeParameter) throws SQLException;
		}
		
		private class LeafNodeStringJDBCGetter extends LeafNodeJDBCGetter
		{
			@Override
			public Object getValue(RuntimeParameter runtimeParameter) throws SQLException
			{
				return runtimeParameter.getResultSet().getString(JDBCGetterDefinition.this.cursorPosition);
			}
		}
		
		private class LeafNodeBooleanJDBCGetter extends LeafNodeJDBCGetter
		{
			@Override
			public Object getValue(RuntimeParameter runtimeParameter) throws SQLException
			{
				boolean value = runtimeParameter.getResultSet().getBoolean(JDBCGetterDefinition.this.cursorPosition);
				return runtimeParameter.getResultSet().wasNull() ? null : value;
			}
		}
		
		private class LeafNodeIntegerJDBCGetter extends LeafNodeJDBCGetter
		{
			@Override
			public Object getValue(RuntimeParameter runtimeParameter) throws SQLException
			{
				int value = runtimeParameter.getResultSet().getInt(JDBCGetterDefinition.this.cursorPosition);
				return runtimeParameter.getResultSet().wasNull() ? null : value;
			}
		}
		
		private class LeafNodeLongJDBCGetter extends LeafNodeJDBCGetter
		{
			@Override
			public Object getValue(RuntimeParameter runtimeParameter) throws SQLException
			{
				long value = runtimeParameter.getResultSet().getLong(JDBCGetterDefinition.this.cursorPosition);
				return runtimeParameter.getResultSet().wasNull() ? null : value;
			}
		}
		
		private class LeafNodeFloatJDBCGetter extends LeafNodeJDBCGetter
		{
			@Override
			public Object getValue(RuntimeParameter runtimeParameter) throws SQLException
			{
				float value = runtimeParameter.getResultSet().getFloat(JDBCGetterDefinition.this.cursorPosition);
				return runtimeParameter.getResultSet().wasNull() ? null : value;
			}
		}
		
		private class LeafNodeDoubleJDBCGetter extends LeafNodeJDBCGetter
		{
			@Override
			public Object getValue(RuntimeParameter runtimeParameter) throws SQLException
			{
				double value = runtimeParameter.getResultSet().getDouble(JDBCGetterDefinition.this.cursorPosition);
				return runtimeParameter.getResultSet().wasNull() ? null : value;
			}
		}
		
		private class LeafNodeTimestampJDBCGetter extends LeafNodeJDBCGetter
		{
			@Override
			public Object getValue(RuntimeParameter runtimeParameter) throws SQLException
			{
				Timestamp value = runtimeParameter.getResultSet().getTimestamp(JDBCGetterDefinition.this.cursorPosition);
				return value == null ? null : new Date(value.getTime());
			}
		}
		
		private class LeafNodeDateJDBCGetter extends LeafNodeJDBCGetter
		{
			@Override
			public Object getValue(RuntimeParameter runtimeParameter) throws SQLException
			{
				java.sql.Date value = runtimeParameter.getResultSet().getDate(JDBCGetterDefinition.this.cursorPosition);
				return value == null ? null : new Date(value.getTime());
			}
		}
		
		private class LeafNodeTimeJDBCGetter extends LeafNodeJDBCGetter
		{
			@Override
			public Object getValue(RuntimeParameter runtimeParameter) throws SQLException
			{
				java.sql.Time value = runtimeParameter.getResultSet().getTime(JDBCGetterDefinition.this.cursorPosition);
				return value == null ? null : new Date(value.getTime());
			}
		}
	}
	
	private class PrimaryKeyByNewUUID implements BiConsumer<Node<? extends BranchNodeMetaModel,?>, Map<String,?>>
	{
		@Override
		public void accept(Node<? extends BranchNodeMetaModel, ?> node, Map<String, ?> properties) 
		{
			if(node.getNodeType().getTypeClass() == UUID.class)
			{
				((LeafNode)node).setValue(UUID.randomUUID());
			}
			else
			{
				((LeafNode)node).setValue(UUID.randomUUID().toString());
			}
		}
	}
	
	public interface IRuntimeParameter
	{
		public PreparedStatement getPreparedStatement(String sql)throws SQLException;
		public PreparedStatement getPreparedStatementWithoutBatch(String sql) throws SQLException;
		//public ResultSet getResultSet();
	}
	
	private <P extends TypedTreeMetaModel,T extends BranchNodeMetaModel> Function<Object[],Collection<RootBranchNode<P,T>>> getRootNodeFactory(BranchNodeType<P,T> nodeType)
	{
		Objects.requireNonNull(nodeType);
		
		lock.lock();
		try
		{
			Function<Object[],Collection<RootBranchNode<P,T>>> factory = (Function) this.rootNodeFactories.get(nodeType);
			if(factory != null)
			{
				return factory;
			}
			Class<? extends P> treeModelClass = nodeType.getParentNodeClass();
			
			factory = t -> Collections.singletonList(TypedTreeMetaModel.getInstance(treeModelClass).createRootNode(nodeType));
			this.rootNodeFactories.put(nodeType,(Function)factory);
			return factory;
		}
		finally 
		{
			lock.unlock();
		}
	}

}
