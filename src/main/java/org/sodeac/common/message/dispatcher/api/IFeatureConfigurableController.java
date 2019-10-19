/*******************************************************************************
 * Copyright (c) 2018, 2019 Sebastian Palarus
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Sebastian Palarus - initial API and implementation
 *******************************************************************************/
package org.sodeac.common.message.dispatcher.api;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public interface IFeatureConfigurableController extends 
						IChannelManager,
						IOnTaskDone,
						IOnTaskError,
						IOnTaskTimeout,
						IOnChannelAttach,
						IOnChannelDetach,
						IOnChannelSignal,
						IOnMessageStore,
						IOnMessageRemove
{
	
	public default boolean implementsOnMessageStore()
	{
		return implementsControllerMethod("onMessageStore", Void.TYPE, IMessage.class);
	}
	
	public default boolean implementsOnChannelSignal()
	{
		return implementsControllerMethod("onChannelSignal", Void.TYPE, IChannel.class, String.class);
	}
	
	public default boolean implementsOnChannelDetach()
	{
		return implementsControllerMethod("onChannelDetach", Void.TYPE, IChannel.class);
	}
	
	public default boolean implementsOnChannelAttach()
	{
		return implementsControllerMethod("onChannelAttach", Void.TYPE, IChannel.class);
	}
	
	public default boolean implementsOnTaskError()
	{
		return implementsControllerMethod("onTaskError", Void.TYPE, IChannel.class, IChannelTask.class, Throwable.class);
	}
	
	public default boolean implementsOnTaskDone()
	{
		return implementsControllerMethod("onTaskDone", Void.TYPE, IChannel.class, IChannelTask.class);
	}
	
	public default boolean implementsOnTaskTimeout()
	{
		return implementsControllerMethod("onTaskTimeout", Void.TYPE, IChannel.class, IChannelTask.class);
	}

	public default boolean implementsOnMessageRemove()
	{
		return implementsControllerMethod("onMessageRemove", Void.TYPE, IMessage.class);
	}

	@Override
	default <T> void onMessageStore(IMessage<T> message){}

	@Override
	default void onChannelSignal(IChannel channel, String signal){}

	@Override
	default void onChannelDetach(IChannel channel){}

	@Override
	default void onChannelAttach(IChannel channel){}

	@Override
	default void onTaskError(IChannel channel, IChannelTask task, Throwable throwable){}

	@Override
	default void onTaskDone(IChannel channel,IChannelTask task){}

	@Override
	default void onTaskTimeout(IChannel channel, IChannelTask task){}

	@Override
	default <T> void onMessageRemove(IMessage<T> message) {};
	
	
	default boolean implementsControllerMethod(String name,Class<?> returnType, Class<?>... parameterTypes)
	{
		Class<?> clazz = this.getClass();
		while(clazz != null)
		{
			try
			{
				Method m = clazz.getDeclaredMethod(name,parameterTypes);
				if((m != null) && (m.getReturnType() == returnType) && (m.getModifiers() == Modifier.PUBLIC))
				{
					return true;
				}
			} 
			catch (NoSuchMethodException e){}
			clazz = clazz.getSuperclass();
		}
		return false;
	}
	
}
