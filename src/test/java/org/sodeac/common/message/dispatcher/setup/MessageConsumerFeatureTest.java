package org.sodeac.common.message.dispatcher.setup;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.BiConsumer;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sodeac.common.message.dispatcher.api.IDispatcherChannel.IDispatcherChannelReference;
import org.sodeac.common.message.dispatcher.api.IMessage;
import org.sodeac.common.message.dispatcher.api.IMessageDispatcherManager;
import org.sodeac.common.message.dispatcher.impl.DispatcherTest;
import org.sodeac.common.message.dispatcher.setup.MessageDispatcherChannelSetup.MessageConsumeHelper;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MessageConsumerFeatureTest
{
	@Test
	public void test00003CompresserWithHeartbeat() throws IOException, InterruptedException
	{
		String channelID = "test00003CompresserWithHeartbeat";
		
		BiConsumer<IMessage<Long>, MessageConsumeHelper<Long, Object>> payloadConsumer = (n,h) -> {};
		BiConsumer<IMessage<Long>, MessageConsumeHelper<Long, Object>> heartbeatConsumer = (n,h) -> {};
		
		IDispatcherChannelReference channelCloser = MessageDispatcherChannelSetup.create()
		.addFeature
		(
			MessageConsumerFeature.newBuilder().
				inMessageMonitoringPool().minPoolSize(1)
				.consumeMessage(payloadConsumer).memberOfGroup("payload")
				.ifLastConsumeEvent().ofGroup("payload").isOlderThan(1).seconds()
			.or()
				.inMessageMonitoringPool().useFilter(m -> false).minPoolSize(0)
				.consumeMessage(heartbeatConsumer)
				.ifLastConsumeEvent().isOlderThan(1).seconds()
			.buildFeature()						
		)
		.preparedBuilder().inManagedDispatcher(DispatcherTest.TEST_DISPATCHER_ID).buildChannelWithId(channelID);
		
		assertNotNull("channel should not be null", IMessageDispatcherManager.get().getOrCreateDispatcher(DispatcherTest.TEST_DISPATCHER_ID).getChannel(channelID));
		
		Thread.sleep(1000L);
		
		Thread.sleep(10000L);
		channelCloser.close();
		
		assertNull("channel should be null", IMessageDispatcherManager.get().getOrCreateDispatcher(DispatcherTest.TEST_DISPATCHER_ID).getChannel(channelID));
	}
}
