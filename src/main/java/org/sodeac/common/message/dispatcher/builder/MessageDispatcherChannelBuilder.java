package org.sodeac.common.message.dispatcher.builder;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.sodeac.common.message.dispatcher.api.DispatcherChannelSetup;
import org.sodeac.common.message.dispatcher.api.IDispatcherChannel;
import org.sodeac.common.message.dispatcher.api.IDispatcherChannelManager;
import org.sodeac.common.message.dispatcher.api.IMessageDispatcher;

public class MessageDispatcherChannelBuilder
{
	private MessageDispatcherChannelBuilder(String channelId)
	{
		super();
		this.channelId = channelId;
	}
	
	private String channelId = null;
	
	
	public Closeable buildForDispatcher(Supplier<IMessageDispatcher> dispatcherSupplier)
	{
		IMessageDispatcher dispatcher = dispatcherSupplier.get();
		MasterChannelManager masterManager = new MasterChannelManager(channelId, null, dispatcher);
		dispatcher.registerChannelManager(masterManager);
		return masterManager;
	}
	
	public MessageDispatcherChannelBuilder addFeature(IChannelFeature channelFeature)
	{
		// TODO
		return this;
	}
	
	public interface IChannelFeature
	{
		
	}
	
	public interface IPreparedChannelFeature extends IChannelFeature
	{
		public void applyToChannel(IDispatcherChannel<?> channel);
	}
	
	protected class MasterChannelManager implements IDispatcherChannelManager,Closeable
	{
		private String id;
		private String name;
		private IMessageDispatcher dispatcher;
		
		protected MasterChannelManager(String id, String name, IMessageDispatcher dispatcher)
		{
			super();
			this.id = id;
			this.name = name;
			this.dispatcher = dispatcher;
		}
		
		@Override
		public void configure(IChannelControllerPolicy configurationPolicy) 
		{
			configurationPolicy.addConfigurationDetail(new DispatcherChannelSetup.BoundedByChannelId(id).setChannelMaster(true).setName(name));
		}

		@Override
		public void close() throws IOException
		{
			dispatcher.unregisterChannelManager(this);
		}
		
	}
}
