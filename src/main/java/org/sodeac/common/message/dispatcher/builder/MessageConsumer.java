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
package org.sodeac.common.message.dispatcher.builder;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.sodeac.common.message.dispatcher.api.IDispatcherChannel;
import org.sodeac.common.message.dispatcher.api.IMessageDispatcher;
import org.sodeac.common.message.dispatcher.builder.MessageDispatcherChannelBuilder.IPreparedChannelFeature;

public class MessageConsumer
{
	private MessageConsumer()
	{
		super();
	}
	
	public static FeatureBuilder newFeatureBuilder()
	{
		return new MessageConsumer().new FeatureBuilder();
	}
	
	public class FeatureBuilder
	{
		
		public BuilderLevelA1 withMessagePool()
		{
			return new BuilderLevelA1();
		}
		
		public BuilderLevelB1.BuilderLevelB2 ifLastConsumeEvent()
		{
			return new BuilderLevelB1(new ConsumerRule()).new BuilderLevelB2();
		}
		
		public class BuilderLevelA1
		{
			private BuilderLevelA1(){super();}
			
			private ConsumerRule ruleData = new ConsumerRule();
			
			public BuilderLevelA2 useFilter(Predicate<IMessageDispatcher> filter)
			{
				BuilderLevelA1.this.ruleData.filter = filter;
				return new BuilderLevelA2();
			}
			
			public BuilderLevelA2.BuilderLevelA3 minPoolSize(int minSize)
			{
				return new BuilderLevelA2().minPoolSize(minSize);
			}
			
			public BuilderLevelB1 maxPoolSize(int maxSize)
			{
				return new BuilderLevelA2().maxPoolSize(maxSize);
			}
	
			
			public class BuilderLevelA2
			{
				private BuilderLevelA2(){super();}
				
				public BuilderLevelA3 minPoolSize(int minSize)
				{
					if(minSize < 0)
					{
						minSize = 1;
					}
					BuilderLevelA1.this.ruleData.minSize = minSize;
					return new BuilderLevelA3();
				}
				
				public BuilderLevelB1 maxPoolSize(int maxSize)
				{
					return new BuilderLevelA3().maxPoolSize(maxSize);
				}
				
				public class BuilderLevelA3
				{
					private BuilderLevelA3(){super();}
					
					public BuilderLevelB1 maxPoolSize(int maxSize)
					{
						if(maxSize < BuilderLevelA1.this.ruleData.minSize)
						{
							maxSize = BuilderLevelA1.this.ruleData.minSize;
						}
						BuilderLevelA1.this.ruleData.maxSize = maxSize;
						
						return new BuilderLevelB1(BuilderLevelA1.this.ruleData);
					}
				}
			}
		}
		
		public class BuilderLevelB1
		{
			private ConsumerRule ruleData;
			
			private BuilderLevelB1(ConsumerRule ruleData)
			{
				super();
				this.ruleData = ruleData;
			}
	
			public BuilderLevelB2 andIfLastConsumeEvent()
			{
				return new BuilderLevelB2();
			}
			
			public class BuilderLevelB2
			{
				private BuilderLevelB2(){super();}
				
				public BuilderLevelB3.BuilderLevelB4 isOlderThan(int olderThan)
				{
					return new BuilderLevelB3().isOlderThan(olderThan);
				}
				
				public BuilderLevelB3 ofGroup(String group)
				{
					BuilderLevelB1.this.ruleData.consumeEventOlderThanGroup = group;
					return new BuilderLevelB3();
				}
				
				public class BuilderLevelB3
				{
					private BuilderLevelB3(){super();}
					
					public BuilderLevelB4 isOlderThan(int olderThan)
					{
						if(olderThan < 0)
						{
							olderThan = 0;
						}
						
						BuilderLevelB1.this.ruleData.consumeEventOlderThan = olderThan;
						
						return new BuilderLevelB4();
					}
					
					public class BuilderLevelB4
					{
						private BuilderLevelB4(){super();}
						
						public BuilderLevelC1 milliSeconds()
						{
							BuilderLevelB1.this.ruleData.consumeEventOlderThanTimeUnit = TimeUnit.MILLISECONDS;
							
							return new BuilderLevelC1(BuilderLevelB1.this.ruleData);
						}
						
						public BuilderLevelC1 seconds()
						{
							BuilderLevelB1.this.ruleData.consumeEventOlderThanTimeUnit = TimeUnit.SECONDS;
							
							return new BuilderLevelC1(BuilderLevelB1.this.ruleData);
						}
						
						public BuilderLevelC1 minutes()
						{
							BuilderLevelB1.this.ruleData.consumeEventOlderThanTimeUnit = TimeUnit.MINUTES;
							
							return new BuilderLevelC1(BuilderLevelB1.this.ruleData);
						}
						
						public BuilderLevelC1 hours()
						{
							BuilderLevelB1.this.ruleData.consumeEventOlderThanTimeUnit = TimeUnit.HOURS;
							
							return new BuilderLevelC1(BuilderLevelB1.this.ruleData);
						}
						
						public BuilderLevelC1 days()
						{
							BuilderLevelB1.this.ruleData.consumeEventOlderThanTimeUnit = TimeUnit.DAYS;
							
							return new BuilderLevelC1(BuilderLevelB1.this.ruleData);
						}
					}
				}
			}
		}
		
		public class BuilderLevelC1
		{
			private ConsumerRule ruleData;
			
			private BuilderLevelC1(ConsumerRule ruleData)
			{
				super();
				this.ruleData = ruleData;
			}
		}
		
	}
	
	protected class ConsumerRule
	{
		private Predicate<IMessageDispatcher> filter = null;
		private int minSize = 1;
		private int maxSize = Short.MAX_VALUE;
		private String consumeEventOlderThanGroup = null;
		private int consumeEventOlderThan = -1;
		private TimeUnit consumeEventOlderThanTimeUnit = TimeUnit.SECONDS;
	}
	
	protected class MessageConsumerFeature implements IPreparedChannelFeature
	{

		@Override
		public void applyToChannel(IDispatcherChannel<?> channel)
		{
			// TODO Auto-generated method stub
			
		}
		
	}
}
