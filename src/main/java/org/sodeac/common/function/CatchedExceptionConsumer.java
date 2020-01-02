package org.sodeac.common.function;

import java.sql.SQLException;
import java.util.function.Consumer;

public interface CatchedExceptionConsumer<T> extends Consumer<T>
{

	@Override
	default void accept(T t)
	{
		try
		{
			acceptWithException(t);
		}
		catch (Exception e) 
		{
			if(e instanceof RuntimeException)
			{
				throw (RuntimeException)e;
			}
			throw new RuntimeException(e);
		}
	}
	
	public void acceptWithException(T t) throws Exception;
	
	public static <T> Consumer<T> wrap(CatchedExceptionConsumer<T> consumer)
	{
		return new Consumer<T>()
		{
			@Override
			public void accept(T t)
			{
				consumer.accept(t);
			}
		};
	}
}
