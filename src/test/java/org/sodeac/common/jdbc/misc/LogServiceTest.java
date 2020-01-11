package org.sodeac.common.jdbc.misc;


import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.junit.Test;
import org.sodeac.common.ILogService;
import org.sodeac.common.function.ConplierBean;
import org.sodeac.common.misc.CloseableCollector;
import org.sodeac.common.model.logging.LogEventListChunkNodeType;
import org.sodeac.common.model.logging.LoggingTreeModel;
import org.sodeac.common.typedtree.ModelRegistry;
import org.sodeac.common.typedtree.XMLMarshaller;
import org.sodeac.common.typedtree.TypedTreeMetaModel.RootBranchNode;

public class LogServiceTest
{
	@Test
	public void test00001LogServiceDatasoure() throws Exception
	{
		File dbFile = new File("./target/logger.mv.db");
		if(dbFile.exists())
		{
			dbFile.delete();
		}
		org.h2.jdbcx.JdbcDataSource ds = new org.h2.jdbcx.JdbcDataSource();
		ds.setUrl("jdbc:h2:./target/logger");
		ds.setUser("sa");
		ds.setPassword("sa");
		
		ConplierBean<DataSource> dataSourceProvider = new ConplierBean<DataSource>(ds);
		
		RootBranchNode<LoggingTreeModel,LogEventListChunkNodeType> chunk = LoggingTreeModel.createLogEventListChunk(2, 1, 0, true);
		
		ILogService logService = ILogService.newLogService(LogServiceTest.class,dataSourceProvider,null)
				.addLoggerBackend(e -> LogEventListChunkNodeType.addLogToEventListChunk(chunk, e));
		
		logService.error("TEST_MESSAGE_1", new RuntimeException("xxx"));
		
		logService.info("TEST_MESSAGE_2");
		
		logService.close();
		
		
		try(CloseableCollector closeableCollector = CloseableCollector.newInstance())
		{
			Connection connection = closeableCollector.register(dataSourceProvider.get().getConnection());
			
			ResultSet resultSetLogEventCount = closeableCollector.register(closeableCollector.register(connection.prepareStatement("SELECT COUNT(*) FROM SDC_LOG_EVENT")).executeQuery());
			resultSetLogEventCount.next();
			assertEquals("size should be correct", 2, resultSetLogEventCount.getInt(1));
			
			ResultSet resultSetLogPropertyCount = closeableCollector.register(closeableCollector.register(connection.prepareStatement("SELECT COUNT(*) FROM SDC_LOG_PROPERTY")).executeQuery());
			resultSetLogPropertyCount.next();
			assertEquals("size should be correct", 1, resultSetLogPropertyCount.getInt(1));
			
		}
		
		XMLMarshaller marshaller = ModelRegistry.getTypedTreeMetaModel(LoggingTreeModel.class).getXMLMarshaller();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		marshaller.marshal(chunk, baos, true);
		
		String xml1 = baos.toString();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		RootBranchNode<LoggingTreeModel,LogEventListChunkNodeType> chunk2 = ModelRegistry.getTypedTreeMetaModel(LoggingTreeModel.class).createRootNode(LoggingTreeModel.logEventListChunk);
		marshaller.unmarshal(chunk2, bais, true);
		
		baos = new ByteArrayOutputStream();
		marshaller.marshal(chunk2, baos, true);
		
		String xml2 = baos.toString();
		assertEquals("value should be correct",xml1, xml2);
	}
}
