package org.sodeac.common.message.dispatcher.systemmanager;

import org.osgi.service.component.annotations.Component;
import org.sodeac.common.message.dispatcher.api.DispatcherChannelSetup;
import org.sodeac.common.message.dispatcher.api.IDispatcherChannel;
import org.sodeac.common.message.dispatcher.api.IMessage;
import org.sodeac.common.message.dispatcher.api.IOnChannelAttach;
import org.sodeac.common.message.dispatcher.api.IOnChannelDetach;
import org.sodeac.common.message.dispatcher.api.IOnChannelSignal;
import org.sodeac.common.message.dispatcher.api.IOnMessageStore;
import org.sodeac.common.message.dispatcher.api.ISubChannel;
import org.sodeac.common.message.dispatcher.impl.SubChannelImpl;
import org.sodeac.common.message.dispatcher.api.IDispatcherChannelSystemManager;
import org.sodeac.common.xuri.ldapfilter.IFilterItem;
import org.sodeac.common.xuri.ldapfilter.LDAPFilterBuilder;

@Component(service=IDispatcherChannelSystemManager.class)
public class RuleTriggeredMessageProcessingManager implements IDispatcherChannelSystemManager, IOnMessageStore, IOnChannelAttach, IOnChannelDetach
{
	public static final IFilterItem MATCH_FILTER = IDispatcherChannelSystemManager.getAdapterMatchFilter(RuleTriggeredMessageProcessingManagerAdapter.class);
	public static final String MATCH_NAME = "Rule Triggered Message Processing Manager";
			
	@Override
	public void configure(IChannelControllerPolicy configurationPolicy)
	{
		configurationPolicy.addConfigurationDetail(new DispatcherChannelSetup.BoundedByChannelConfiguration(MATCH_FILTER).setName(MATCH_NAME));
	}

	@Override
	public <T> void onMessageStore(IMessage<T> message)
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onChannelAttach(IDispatcherChannel channel)
	{
		// TODO Auto-generated method stub
		// Register ParentChannel
		// add already existing messages ?
		
		System.out.println("OnChannelAttached " + channel.getId() + " parent " + ((ISubChannel)channel).getParentChannel().getId());
	}

	@Override
	public void onChannelDetach(IDispatcherChannel channel)
	{
		// TODO Auto-generated method stub
		
	}

	protected static class RuleTriggeredMessageProcessingManagerAdapter implements IOnChannelSignal,IOnMessageStore
	{
		@Override
		public <T> void onMessageStore(IMessage<T> message)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onChannelSignal(IDispatcherChannel channel, String signal)
		{
			// TODO Auto-generated method stub
			
		}
		
	}
}
