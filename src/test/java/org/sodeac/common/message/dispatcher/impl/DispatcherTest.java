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
package org.sodeac.common.message.dispatcher.impl;

import java.util.UUID;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sodeac.common.message.dispatcher.api.DispatcherChannelSetup;
import org.sodeac.common.message.dispatcher.api.IChannelManager;
import org.sodeac.common.message.dispatcher.api.IMessage;
import org.sodeac.common.message.dispatcher.api.IMessageDispatcher;
import org.sodeac.common.message.dispatcher.api.IMessageDispatcherFactory;
import org.sodeac.common.message.dispatcher.api.IOnMessageStore;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DispatcherTest 
{
	@Test
	public void test001EqualsString()
	{
		IMessageDispatcher dispatcher = IMessageDispatcherFactory.newInstance().dispatcher(UUID.randomUUID().toString());
		
		dispatcher.registerChannelManager(new SimpleManagerTest001());
		
		dispatcher.sendMessage(SimpleManagerTest001.CHANNEL_ID, "Ich bin einen einfache Nachricht");
	}
	
	private class SimpleManagerTest001 implements IChannelManager,IOnMessageStore
	{
		public static final String CHANNEL_ID = "abcd";
		
		@Override
		public void configure(IChannelControllerPolicy configurationPolicy) 
		{
			configurationPolicy.addConfigurationDetail(new DispatcherChannelSetup.BoundedByChannelId(CHANNEL_ID).setAutoCreateChannel(true));
		}

		@Override
		public <T> void onMessageStore(IMessage<T> message) 
		{
			System.out.println("onPublish " + message.getPayload());
			message.removeFromChannel();
		}
	}
}
