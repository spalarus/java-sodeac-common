package org.sodeac.common.jdbc.misc;


import static org.junit.Assert.assertEquals;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.junit.Test;
import org.sodeac.common.ILogService;
import org.sodeac.common.function.ConplierBean;
import org.sodeac.common.misc.CloseableCollector;

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
		
		ILogService logService = ILogService.newLogService(LogServiceTest.class,dataSourceProvider,null);
		
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
		
		
	}
}
