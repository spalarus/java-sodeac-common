package org.sodeac.common.message.dispatcher.builder;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sodeac.common.message.dispatcher.builder.MessageConsumer;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MessageConsumerFeatureBuilderTest
{
	@Test
	public void xxx()
	{
		MessageConsumer.newFeatureBuilder()
			.withMessagePool().maxPoolSize(12);//.andIfLastConsumeEvent().ofGroup("acv").isOlderThan(12).seconds();
		
		// immediately
		// at least
	}
}
