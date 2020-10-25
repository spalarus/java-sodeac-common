/*******************************************************************************
 * Copyright (c) 2020 Sebastian Palarus
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Sebastian Palarus - initial API and implementation
 *******************************************************************************/
package org.sodeac.common.message.dispatcher.setup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sodeac.common.function.ConplierBean;
import org.sodeac.common.function.ExceptionCatchedBiConsumer;
import org.sodeac.common.function.ExceptionCatchedBiFunction;
import org.sodeac.common.message.dispatcher.api.IDispatcherChannel;
import org.sodeac.common.message.dispatcher.api.IDispatcherChannel.IDispatcherChannelReference;
import org.sodeac.common.message.dispatcher.api.IMessage;
import org.sodeac.common.message.dispatcher.api.IMessageDispatcherManager;
import org.sodeac.common.message.dispatcher.api.ISubChannel;
import org.sodeac.common.message.dispatcher.impl.DispatcherTest;
import org.sodeac.common.message.dispatcher.setup.MessageConsumerFeature.ConsumerRule;
import org.sodeac.common.message.dispatcher.setup.MessageDispatcherChannelSetup.MessageConsumeHelper;
import org.sodeac.common.misc.TaskDoneNotifier;
import org.sodeac.common.snapdeque.SnapshotableDeque;

//@Ignore
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MessageConsumerFeatureTest
{
	@Test
	public void test00001SimpleMessage() throws InterruptedException, IOException
	{
		TaskDoneNotifier taskDoneNotifier = new TaskDoneNotifier();
		String channelID = getClass().getCanonicalName()+ "." + currentMethodeName();
		
		IDispatcherChannelReference channelReference = MessageDispatcherChannelSetup.create().addFeature
		(
			MessageConsumerFeature.newBuilder().consumeMessage((m,h) -> taskDoneNotifier.setTaskDone()).immediately().buildFeature()
		)
		.preparedBuilder().inManagedDispatcher(DispatcherTest.TEST_DISPATCHER_ID).buildChannelWithId(channelID);
		
		channelReference.getChannel(String.class).sendMessage("TEST");
		
		assertTrue("task should be done",taskDoneNotifier.await(3, TimeUnit.SECONDS));
		
		channelReference.close();
	}
	
	@Test
	public void test00011TimeoutHandler() throws InterruptedException, IOException
	{
		TaskDoneNotifier taskDoneNotifier1 = new TaskDoneNotifier();
		TaskDoneNotifier taskDoneNotifier2 = new TaskDoneNotifier();
		String channelID = getClass().getCanonicalName() + "." + currentMethodeName();
		
		ConplierBean<Boolean> inTimeout = new ConplierBean<Boolean>(false);
		ConplierBean<Boolean> notInTimeout = new ConplierBean<Boolean>(false);
		ConplierBean<Boolean> handleTimeout = new ConplierBean<Boolean>(false);
		
		IDispatcherChannelReference channelReference = MessageDispatcherChannelSetup.create().addFeature
		(
			MessageConsumerFeature.newBuilder().consumeMessage(ExceptionCatchedBiConsumer.wrap( (m,h) -> 
			{
				if("TEST1".equals(m.getPayload()))
				{
					Thread.sleep(3000);
					if(h.isInTimeout())
					{
						inTimeout.setValue(true);
					}
					else
					{
						notInTimeout.setValue(true);
					}
					taskDoneNotifier1.setTaskDone();
				}
				else if ("TEST2".equals(m.getPayload()))
				{
					
					if(h.isInTimeout())
					{
						inTimeout.setValue(true);
					}
					else
					{
						notInTimeout.setValue(true);
					}
					taskDoneNotifier2.setTaskDone();
				}
			}))
			.withTimeoutForEachMessage(1).seconds().immediately()
			.onTimeout((m,h) -> 
			{
				m.removeFromChannel();
				handleTimeout.setValue(true);
			})
			.buildFeature()
		)
		.preparedBuilder().inManagedDispatcher(DispatcherTest.TEST_DISPATCHER_ID).buildChannelWithId(channelID);
		
		channelReference.getChannel(String.class).sendMessage("TEST1");
		
		assertTrue("task should be done",taskDoneNotifier1.await(6, TimeUnit.SECONDS));
		
		assertTrue("value should be correct", inTimeout.get());
		assertTrue("value should be correct", handleTimeout.get());
		assertFalse("value should be correct", notInTimeout.get());
		
		inTimeout.setValue(false);
		notInTimeout.setValue(false);
		handleTimeout.setValue(false);
		
		channelReference.getChannel(String.class).sendMessage("TEST2");
		
		assertTrue("task should be done",taskDoneNotifier2.await(3, TimeUnit.SECONDS));
		
		assertFalse("value should be correct", inTimeout.get());
		assertFalse("value should be correct", handleTimeout.get());
		assertTrue("value should be correct", notInTimeout.get());
		
		channelReference.close();
	}
	
	@Test
	public void test00021ErrorHandler() throws InterruptedException, IOException
	{
		TaskDoneNotifier taskDoneNotifier1 = new TaskDoneNotifier();
		TaskDoneNotifier taskDoneNotifier2 = new TaskDoneNotifier();
		TaskDoneNotifier taskDoneNotifier3 = new TaskDoneNotifier();
		String channelID = getClass().getCanonicalName() + "." + currentMethodeName();
		
		ConplierBean<Boolean> exceptionHandler1 = new ConplierBean<Boolean>(false);
		ConplierBean<Boolean> exceptionHandler2 = new ConplierBean<Boolean>(false);
		ConplierBean<Boolean> exceptionHandler3 = new ConplierBean<Boolean>(false);
		
		IDispatcherChannelReference channelReference = MessageDispatcherChannelSetup.create().addFeature
		(
			MessageConsumerFeature.newBuilder().consumeMessage(ExceptionCatchedBiConsumer.wrap( (m,h) -> 
			{
				if("TEST1".equals(m.getPayload()))
				{
					throw new IllegalStateException();
				}
				else if ("TEST2".equals(m.getPayload()))
				{
					
					throw new NullPointerException();
				}
				else
				{
					throw new RuntimeException();
				}
			}))
			.immediately()
			.onError(IllegalStateException.class, (t,h) -> 
			{
				if(t instanceof IllegalStateException) {exceptionHandler1.setValue(true);}; 
				taskDoneNotifier1.setTaskDone();
			})
			.onError(NullPointerException.class, (t,h) -> 
			{
				if(t instanceof NullPointerException) {exceptionHandler2.setValue(true);} 
				taskDoneNotifier2.setTaskDone();
			})
			.onError((t,h) -> 
			{
				if(t instanceof RuntimeException) {exceptionHandler3.setValue(true);}
				taskDoneNotifier3.setTaskDone();
			})
			.buildFeature()
		)
		.preparedBuilder().inManagedDispatcher(DispatcherTest.TEST_DISPATCHER_ID).buildChannelWithId(channelID);
		
		channelReference.getChannel(String.class).sendMessage("TEST1");
		
		assertTrue("task should be done",taskDoneNotifier1.await(3, TimeUnit.SECONDS));
		
		assertTrue("value should be correct", exceptionHandler1.get());
		assertFalse("value should be correct", exceptionHandler2.get());
		assertFalse("value should be correct", exceptionHandler3.get());
		
		exceptionHandler1.setValue(false);
		exceptionHandler2.setValue(false);
		exceptionHandler3.setValue(false);
		
		channelReference.getChannel(String.class).sendMessage("TEST2");
		
		assertTrue("task should be done",taskDoneNotifier2.await(3, TimeUnit.SECONDS));
		
		assertFalse("value should be correct", exceptionHandler1.get());
		assertTrue("value should be correct", exceptionHandler2.get());
		assertFalse("value should be correct", exceptionHandler3.get());
		
		exceptionHandler1.setValue(false);
		exceptionHandler2.setValue(false);
		exceptionHandler3.setValue(false);
		
		channelReference.getChannel(String.class).sendMessage("TEST3");
		
		assertTrue("task should be done",taskDoneNotifier3.await(3, TimeUnit.SECONDS));
		
		assertFalse("value should be correct", exceptionHandler1.get());
		assertFalse("value should be correct", exceptionHandler2.get());
		assertTrue("value should be correct", exceptionHandler3.get());
		
		channelReference.close();
	}
	
	@Test
	public void test00031MinMaxPool() throws InterruptedException, IOException
	{
		String channelID = getClass().getCanonicalName() + "." + currentMethodeName();
		
		AtomicLong counter = new AtomicLong(0);
		IDispatcherChannelReference channelReference = MessageDispatcherChannelSetup.create().addFeature
		(
			MessageConsumerFeature.newBuilder()
			.inMessageMonitoringPool().minPoolSize(6).maxPoolSize(8)
			.consumeMessage(ExceptionCatchedBiConsumer.wrap( (m,h) -> counter.incrementAndGet()))
			.immediately().buildFeature()
		)
		.preparedBuilder().inManagedDispatcher(DispatcherTest.TEST_DISPATCHER_ID).buildChannelWithId(channelID);
		
		channelReference.getChannel(String.class).sendMessages(Arrays.asList(new String[] {"A","B","C","D","E"}));
		
		Thread.sleep(1000);
		
		assertEquals("value should be correct", 0L, counter.get());
		
		channelReference.getChannel(String.class).sendMessages(Arrays.asList(new String[] {"F","G","H","I","J"}));
		
		Thread.sleep(1000);
		
		assertEquals("value should be correct", 8L, counter.get());
		
		channelReference.close();
	}
	
	@Test
	public void test00032MinMaxPool2() throws InterruptedException, IOException
	{
		String channelID = getClass().getCanonicalName() + "." + currentMethodeName();
		
		AtomicLong counter = new AtomicLong(0);
		IDispatcherChannelReference channelReference = MessageDispatcherChannelSetup.create().addFeature
		(
			MessageConsumerFeature.newBuilder()
				.inMessageMonitoringPool().minPoolSize(6).maxPoolSize(8)
				.consumeMessage(ExceptionCatchedBiConsumer.wrap( (m,h) -> counter.incrementAndGet()))
				.immediately()
			.or()
				.consumeMessage(ExceptionCatchedBiConsumer.wrap( (m,h) -> counter.incrementAndGet()))
				.notBeforeOneOfTheMessagesWaitsForAtLeast(3).seconds()
			.buildFeature()
		)
		.preparedBuilder().inManagedDispatcher(DispatcherTest.TEST_DISPATCHER_ID).buildChannelWithId(channelID);
		
		channelReference.getChannel(String.class).sendMessages(Arrays.asList(new String[] {"A","B","C","D","E"}));
		
		Thread.sleep(1000);
		
		assertEquals("value should be correct", 0L, counter.get());
		
		channelReference.getChannel(String.class).sendMessages(Arrays.asList(new String[] {"F","G","H","I","J"}));
		
		Thread.sleep(1000);
		
		assertEquals("value should be correct", 8L, counter.get());
		
		Thread.sleep(1000);
		
		assertEquals("value should be correct", 8L, counter.get());
		
		Thread.sleep(2000);
		
		assertEquals("value should be correct", 10L, counter.get());
		
		channelReference.close();
	}
	
	@Test
	public void test00041AtLeastOneTest() throws InterruptedException, IOException
	{
		String channelID = getClass().getCanonicalName() + "." + currentMethodeName();
		
		AtomicLong counter = new AtomicLong(0);
		IDispatcherChannelReference channelReference = MessageDispatcherChannelSetup.create().addFeature
		(
			MessageConsumerFeature.newBuilder()
				.consumeMessage(ExceptionCatchedBiConsumer.wrap( (m,h) -> counter.incrementAndGet()))
				.notBeforeOneOfTheMessagesWaitsForAtLeast(3).seconds()
			.buildFeature()
		)
		.preparedBuilder().inManagedDispatcher(DispatcherTest.TEST_DISPATCHER_ID).buildChannelWithId(channelID);
		
		channelReference.getChannel(String.class).sendMessages(Arrays.asList(new String[] {"A","B","C","D","E"}));
		
		Thread.sleep(1000);
		
		assertEquals("value should be correct", 0L, counter.get());
		
		Thread.sleep(1000);
		
		assertEquals("value should be correct", 0L, counter.get());
		
		Thread.sleep(2000);
		
		assertEquals("value should be correct", 5L, counter.get());
		
		channelReference.close();
	}
	
	@Test
	public void test00051AtLeastXTest() throws InterruptedException, IOException
	{
		String channelID = getClass().getCanonicalName() + "." + currentMethodeName();
		
		AtomicLong counter = new AtomicLong(0);
		IDispatcherChannelReference channelReference = MessageDispatcherChannelSetup.create().addFeature
		(
			MessageConsumerFeature.newBuilder()
				.consumeMessage(ExceptionCatchedBiConsumer.wrap( (m,h) -> counter.incrementAndGet()))
				.notBefore(3).waitForAtLeast(3).seconds()
			.buildFeature()
		)
		.preparedBuilder().inManagedDispatcher(DispatcherTest.TEST_DISPATCHER_ID).buildChannelWithId(channelID);
		
		channelReference.getChannel(String.class).sendMessages(Arrays.asList(new String[] {"A","B","C","D","E"}));
		
		Thread.sleep(1000);
		
		assertEquals("value should be correct", 0L, counter.get());
		
		Thread.sleep(1000);
		
		assertEquals("value should be correct", 0L, counter.get());
		
		Thread.sleep(2000);
		
		assertEquals("value should be correct", 5L, counter.get());
		
		channelReference.close();
	}
	
	@Test
	public void test00052AtLeastXTest() throws InterruptedException, IOException
	{
		String channelID = getClass().getCanonicalName() + "." + currentMethodeName();
		
		AtomicLong counter = new AtomicLong(0);
		IDispatcherChannelReference channelReference = MessageDispatcherChannelSetup.create().addFeature
		(
			MessageConsumerFeature.newBuilder()
				.consumeMessage(ExceptionCatchedBiConsumer.wrap( (m,h) -> counter.incrementAndGet()))
				.notBefore(3).waitForAtLeast(3).seconds()
			.buildFeature()
		)
		.preparedBuilder().inManagedDispatcher(DispatcherTest.TEST_DISPATCHER_ID).buildChannelWithId(channelID);
		
		channelReference.getChannel(String.class).sendMessages(Arrays.asList(new String[] {"A","B"}));
		
		Thread.sleep(2000);
		
		assertEquals("value should be correct", 0L, counter.get());
		
		channelReference.getChannel(String.class).sendMessages(Arrays.asList(new String[] {"C","D"}));
		
		Thread.sleep(2000);
		
		assertEquals("value should be correct", 0L, counter.get());
		
		Thread.sleep(2000);
		
		assertEquals("value should be correct", 4L, counter.get());
		
		channelReference.close();
	}
	
	@Test
	public void test00061AllMessageTest() throws InterruptedException, IOException
	{
		String channelID = getClass().getCanonicalName() + "." + currentMethodeName();
		
		AtomicLong counter = new AtomicLong(0);
		IDispatcherChannelReference channelReference = MessageDispatcherChannelSetup.create().addFeature
		(
			MessageConsumerFeature.newBuilder()
				.consumeMessage(ExceptionCatchedBiConsumer.wrap( (m,h) -> counter.incrementAndGet()))
				.notBeforeTheMessageWaitsForAtLeast(3).seconds()
			.buildFeature()
		)
		.preparedBuilder().inManagedDispatcher(DispatcherTest.TEST_DISPATCHER_ID).buildChannelWithId(channelID);
		
		channelReference.getChannel(String.class).sendMessages(Arrays.asList(new String[] {"A","B"}));
		
		Thread.sleep(2000);
		
		assertEquals("value should be correct", 0L, counter.get());
		
		channelReference.getChannel(String.class).sendMessages(Arrays.asList(new String[] {"C","D"}));
		
		Thread.sleep(2000);
		
		assertEquals("value should be correct", 2L, counter.get());
		
		channelReference.getChannel(String.class).sendMessages(Arrays.asList(new String[] {"E","F"}));
		
		Thread.sleep(2000);
		
		assertEquals("value should be correct", 4L, counter.get());
		
		Thread.sleep(2000);
		
		assertEquals("value should be correct", 6L, counter.get());
		
		channelReference.close();
	}
	
	@Test
	public void test00062AllMessageTest() throws InterruptedException, IOException
	{
		String channelID = getClass().getCanonicalName() + "." + currentMethodeName();
		
		AtomicLong counter = new AtomicLong(0);
		IDispatcherChannelReference channelReference = MessageDispatcherChannelSetup.create().addFeature
		(
			MessageConsumerFeature.newBuilder()
				.consumeMessage(ExceptionCatchedBiConsumer.wrap( (m,h) -> counter.incrementAndGet()))
				.notBeforeTheMessageWaitsForAtLeast(3).seconds()
			.buildFeature()
		)
		.preparedBuilder().inManagedDispatcher(DispatcherTest.TEST_DISPATCHER_ID).buildChannelWithId(channelID);
		
		channelReference.getChannel(String.class).sendMessages(Arrays.asList(new String[] {"A","B"}));
		
		Thread.sleep(1000);
		
		assertEquals("value should be correct", 0L, counter.get());
		
		channelReference.getChannel(String.class).sendMessages(Arrays.asList(new String[] {"C","D"}));
		
		Thread.sleep(1000);
		
		assertEquals("value should be correct", 0L, counter.get());
		
		channelReference.getChannel(String.class).sendMessages(Arrays.asList(new String[] {"E","F"}));
		
		Thread.sleep(1500);
		
		assertEquals("value should be correct", 2L, counter.get());
		
		Thread.sleep(1000);
		
		assertEquals("value should be correct", 4L, counter.get());
		
		Thread.sleep(1000);
		
		assertEquals("value should be correct", 6L, counter.get());
		
		channelReference.close();
	}
	
	@Test
	public void test00101ConsumeEventOlderThanTest1() throws InterruptedException, IOException
	{
		String channelID = getClass().getCanonicalName() + "." + currentMethodeName();
		
		AtomicLong counter = new AtomicLong(0);
		IDispatcherChannelReference channelReference = MessageDispatcherChannelSetup.create().addFeature
		(
			MessageConsumerFeature.newBuilder()
				.inMessageMonitoringPool().minPoolSize(0)
				.consumeMessage(ExceptionCatchedBiConsumer.wrap( (m,h) -> counter.incrementAndGet()))
				.notBeforeTheConsumeEvent().eitherTookPlaceNeverOrTookPlace(1).secondsAgo()
			.buildFeature()
		)
		.preparedBuilder().inManagedDispatcher(DispatcherTest.TEST_DISPATCHER_ID).buildChannelWithId(channelID);
		
		Thread.sleep(500);
		
		assertEquals("value should be correct", 1, counter.get() );
		
		Thread.sleep(1000);
		
		assertEquals("value should be correct", 2, counter.get() );
		
		Thread.sleep(1000);
		
		assertEquals("value should be correct", 3, counter.get() );
	}
	
	@Test
	public void test00102ConsumeEventOlderThanTest2() throws InterruptedException, IOException
	{
		String channelID = getClass().getCanonicalName() + "." + currentMethodeName();
		
		AtomicLong counter = new AtomicLong(0);
		IDispatcherChannelReference channelReference = MessageDispatcherChannelSetup.create().addFeature
		(
			MessageConsumerFeature.newBuilder()
				.inMessageMonitoringPool().minPoolSize(0)
				.consumeMessage(ExceptionCatchedBiConsumer.wrap( (m,h) -> counter.incrementAndGet()))
				.notBeforeTheConsumeEvent().tookPlace(1).secondsAgo()
			.buildFeature()
		)
		.preparedBuilder().inManagedDispatcher(DispatcherTest.TEST_DISPATCHER_ID).buildChannelWithId(channelID);
		
		Thread.sleep(500);
		
		assertEquals("value should be correct", 0, counter.get() );
		
		Thread.sleep(1000);
		
		assertEquals("value should be correct", 0, counter.get() );
		
		Thread.sleep(1000);
		
		assertEquals("value should be correct", 0, counter.get() );
	}
	
	@Test
	public void test10003CompresserWithHeartbeat() throws IOException, InterruptedException
	{
		String channelID = "test00003CompresserWithHeartbeat";
		
		SnapshotableDeque<Long> payloadMessages = new SnapshotableDeque<>();
		SnapshotableDeque<Long> heartbeatMessages = new SnapshotableDeque<>();
		
		BiConsumer<IMessage<Long>, MessageConsumeHelper<Long, Object>> payloadConsumer = (n,h) -> payloadMessages.add(n.getPayload());
		BiConsumer<IMessage<Long>, MessageConsumeHelper<Long, Object>> heartbeatConsumer = (n,h) -> heartbeatMessages.add(System.currentTimeMillis());
		
		IDispatcherChannelReference channelReference = MessageDispatcherChannelSetup.create()
		.addFeature
		(
			MessageConsumerFeature.newBuilder()
				.inMessageMonitoringPool().minPoolSize(2)
				.consumeMessage(payloadConsumer).memberOfGroup("payload").andMemberOfGroup("heartbeat")
				.notBeforeTheConsumeEvent().ofGroup("payload").eitherTookPlaceNeverOrTookPlace(1).secondsAgo()
			.or()
				.inMessageMonitoringPool().useFilter(m -> false).minPoolSize(0)
				.consumeMessage(heartbeatConsumer).memberOfGroup("heartbeat")
				.notBeforeTheConsumeEvent().ofGroup("heartbeat").eitherTookPlaceNeverOrTookPlace(3).secondsAgo()
			.buildFeature()						
		)
		.preparedBuilder().inManagedDispatcher(DispatcherTest.TEST_DISPATCHER_ID).buildChannelWithId(channelID);
		
		IDispatcherChannel<Long> channel = channelReference.getChannel(Long.class);
		assertNotNull("channel should not be null", channel);
		assertSame("value should be same", channel, IMessageDispatcherManager.get().getOrCreateDispatcher(DispatcherTest.TEST_DISPATCHER_ID).getChannel(channelID));
		
		
		Thread.sleep(500L);
		
		assertEquals("value should be correct", 1, heartbeatMessages.size());
		assertEquals("value should be correct", 0, payloadMessages.size());
		
		channel.sendMessage(1L);
		
		assertEquals("value should be correct", 1, heartbeatMessages.size());
		assertEquals("value should be correct", 0, payloadMessages.size());
		
		Thread.sleep(800L);
		
		channel.sendMessage(2L);
		
		Thread.sleep(500L);
		
		assertEquals("value should be correct", 1, heartbeatMessages.size());
		assertEquals("value should be correct", 2, payloadMessages.size());
		
		Thread.sleep(2000L);
		
		channel.sendMessage(3L);
		
		assertEquals("value should be correct", 1, heartbeatMessages.size());
		assertEquals("value should be correct", 2, payloadMessages.size());
		
		Thread.sleep(1000L);
		
		assertEquals("value should be correct", 2, heartbeatMessages.size());
		assertEquals("value should be correct", 2, payloadMessages.size());
		
		channel.sendMessage(4L);
		
		Thread.sleep(500L);
		
		assertEquals("value should be correct", 2, heartbeatMessages.size());
		assertEquals("value should be correct", 4, payloadMessages.size());
		
		Thread.sleep(3000L);
		
		assertEquals("value should be correct", 3, heartbeatMessages.size());
		assertEquals("value should be correct", 4, payloadMessages.size());
		
		channelReference.close();
		
		assertNull("channel should be null", IMessageDispatcherManager.get().getOrCreateDispatcher(DispatcherTest.TEST_DISPATCHER_ID).getChannel(channelID));
	}
	
	private String currentMethodeName()
	{
		StackTraceElement[] stack =Thread.currentThread().getStackTrace();
		return stack[2].getMethodName();
	}
}
