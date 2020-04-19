/*******************************************************************************
 * Copyright (c) 2019, 2020 Sebastian Palarus
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Sebastian Palarus - initial API and implementation
 *******************************************************************************/
package org.sodeac.common.message.dispatcher.impl;

import java.util.UUID;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sodeac.common.message.dispatcher.api.DispatcherChannelSetup;
import org.sodeac.common.message.dispatcher.api.IDispatcherChannelManager;
import org.sodeac.common.message.dispatcher.api.IMessage;
import org.sodeac.common.message.dispatcher.api.IMessageDispatcher;
import org.sodeac.common.message.dispatcher.api.IMessageDispatcherManager;
import org.sodeac.common.message.dispatcher.api.IOnMessageStore;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DispatcherTest 
{
	@Test
	public void test001EqualsString()
	{
		IMessageDispatcher dispatcher = IMessageDispatcherManager.get().getOrCreateDispatcher(UUID.randomUUID().toString());
		
		dispatcher.registerChannelManager(new SimpleManagerTest001());
		
		dispatcher.sendMessage(SimpleManagerTest001.CHANNEL_ID, "Ich bin einen einfache Nachricht");
		
		//dispatcher.shutdown();
	}
	
	private class SimpleManagerTest001 implements IDispatcherChannelManager,IOnMessageStore
	{
		public static final String CHANNEL_ID = "abcd";
		
		@Override
		public void configure(IChannelControllerPolicy configurationPolicy) 
		{
			configurationPolicy.addConfigurationDetail(new DispatcherChannelSetup.BoundedByChannelId(CHANNEL_ID).setChannelMaster(true).setName("Test"));
		}

		@Override
		public <T> void onMessageStore(IMessage<T> message) 
		{
			System.out.println("onPublish " + message.getPayload());
			message.removeFromChannel();
		}
	}
}
