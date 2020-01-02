package org.sodeac.common.jdbc;

import java.sql.SQLException;
import java.util.function.Consumer;

public interface SQLConsumer<T> extends Consumer<T>
{

	@Override
	default void accept(T t)
	{
		try
		{
			acceptWithSQLException(t);
		}
		catch (SQLException e) 
		{
			throw new RuntimeException(e);
		}
	}
	
	public void acceptWithSQLException(T t) throws SQLException;
}
