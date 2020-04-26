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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.sodeac.common.message.dispatcher.api.IDispatcherChannel;
import org.sodeac.common.message.dispatcher.api.IMessage;
import org.sodeac.common.message.dispatcher.api.ISubChannel;
import org.sodeac.common.message.dispatcher.setup.MessageConsumerFeature.ConsumerRule.TriggerByMessageAgeMode;
import org.sodeac.common.message.dispatcher.setup.MessageDispatcherChannelSetup.IChannelFeature;
import org.sodeac.common.message.dispatcher.setup.MessageDispatcherChannelSetup.IPreparedChannelFeature;
import org.sodeac.common.message.dispatcher.setup.MessageDispatcherChannelSetup.MessageConsumeHelper;

public class MessageConsumerFeature
{
	private MessageConsumerFeature()
	{
		super();
	}
	
	public static FeatureBuilder newBuilder()
	{
		return new MessageConsumerFeature().new FeatureBuilder();
	}
	
	public class FeatureBuilder
	{
		private MessageConsumerFeatureConfiguration feature = new MessageConsumerFeatureConfiguration();
		
		public BuilderPhaseA1 inMessageMonitoringPool()
		{
			return new BuilderPhaseA1();
		}
		
		public <T,H> BuilderPhaseB1.BuilderPhaseB2 consumeMessage(BiConsumer<IMessage<T>, MessageConsumeHelper<T,H>> messageConsumer)
		{
			return new BuilderPhaseB1().consumeMessage(messageConsumer);
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <T,H> BuilderPhaseB1.BuilderPhaseB2 consumeMessage(BiConsumer<IMessage<T>, MessageConsumeHelper<T,H>> messageConsumer, Class<T> messageType)
		{
			FeatureBuilder.this.feature.currentConsumerRule.messageConsumer = (BiConsumer)messageConsumer;
			return new BuilderPhaseB1().consumeMessage(messageConsumer);
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <T,H> BuilderPhaseB1.BuilderPhaseB2 consumeMessage(BiConsumer<IMessage<T>, MessageConsumeHelper<T,H>> messageConsumer, Class<T> messageType, Class<H> helperType)
		{
			FeatureBuilder.this.feature.currentConsumerRule.messageConsumer = (BiConsumer)messageConsumer;
			return new BuilderPhaseB1().consumeMessage(messageConsumer);
		}
		
		public class BuilderPhaseA1
		{
			private BuilderPhaseA1(){super();}
			
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public <T> BuilderPhaseA2 useFilter(Predicate<IMessage<T>> filter)
			{
				FeatureBuilder.this.feature.currentConsumerRule.poolFilter = (Predicate)filter;
				return new BuilderPhaseA2();
			}
			
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public <T> BuilderPhaseA2 useFilter(Predicate<IMessage<T>> filter, Class<T> messageType)
			{
				FeatureBuilder.this.feature.currentConsumerRule.poolFilter = (Predicate)filter;
				return new BuilderPhaseA2();
			}
			
			public BuilderPhaseA2.BuilderPhaseA3 minPoolSize(int minSize)
			{
				return new BuilderPhaseA2().minPoolSize(minSize);
			}
			
			public BuilderPhaseB1 maxPoolSize(int maxSize)
			{
				return new BuilderPhaseA2().maxPoolSize(maxSize);
			}
			
			public class BuilderPhaseA2
			{
				private BuilderPhaseA2(){super();}
				
				public BuilderPhaseA3 minPoolSize(int minSize)
				{
					if(minSize < 0)
					{
						minSize = 0;
					}
					FeatureBuilder.this.feature.currentConsumerRule.poolMinSize = minSize;
					return new BuilderPhaseA3();
				}
				
				public BuilderPhaseB1 maxPoolSize(int maxSize)
				{
					return new BuilderPhaseA3().maxPoolSize(maxSize);
				}
				
				public <T,H> BuilderPhaseB1.BuilderPhaseB2 consumeMessage(BiConsumer<IMessage<T>, MessageConsumeHelper<T,H>> messageConsumer)
				{
					return new BuilderPhaseB1().consumeMessage(messageConsumer);
				}
				
				@SuppressWarnings({ "unchecked", "rawtypes" })
				public <T,H> BuilderPhaseB1.BuilderPhaseB2 consumeMessage(BiConsumer<IMessage<T>, MessageConsumeHelper<T,H>> messageConsumer, Class<T> messageType)
				{
					FeatureBuilder.this.feature.currentConsumerRule.messageConsumer = (BiConsumer)messageConsumer;
					return new BuilderPhaseB1().consumeMessage(messageConsumer);
				}
				
				@SuppressWarnings({ "unchecked", "rawtypes" })
				public <T,H> BuilderPhaseB1.BuilderPhaseB2 consumeMessage(BiConsumer<IMessage<T>, MessageConsumeHelper<T,H>> messageConsumer, Class<T> messageType, Class<H> helperType)
				{
					FeatureBuilder.this.feature.currentConsumerRule.messageConsumer = (BiConsumer)messageConsumer;
					return new BuilderPhaseB1().consumeMessage(messageConsumer);
				}
				
				public class BuilderPhaseA3
				{
					private BuilderPhaseA3(){super();}
					
					public BuilderPhaseB1 maxPoolSize(int maxSize)
					{
						if(maxSize < 0)
						{
							maxSize = 0;
						}
						if(maxSize < FeatureBuilder.this.feature.currentConsumerRule.poolMinSize)
						{
							maxSize = FeatureBuilder.this.feature.currentConsumerRule.poolMinSize;
						}
						FeatureBuilder.this.feature.currentConsumerRule.poolMaxSize = maxSize;
						
						return new BuilderPhaseB1();
					}
					
					public <T,H> BuilderPhaseB1.BuilderPhaseB2 consumeMessage(BiConsumer<IMessage<T>, MessageConsumeHelper<T,H>> messageConsumer)
					{
						return new BuilderPhaseB1().consumeMessage(messageConsumer);
					}
					
					@SuppressWarnings({ "unchecked", "rawtypes" })
					public <T,H> BuilderPhaseB1.BuilderPhaseB2 consumeMessage(BiConsumer<IMessage<T>, MessageConsumeHelper<T,H>> messageConsumer, Class<T> messageType)
					{
						FeatureBuilder.this.feature.currentConsumerRule.messageConsumer = (BiConsumer)messageConsumer;
						return new BuilderPhaseB1().consumeMessage(messageConsumer);
					}
					
					@SuppressWarnings({ "unchecked", "rawtypes" })
					public <T,H> BuilderPhaseB1.BuilderPhaseB2 consumeMessage(BiConsumer<IMessage<T>, MessageConsumeHelper<T,H>> messageConsumer, Class<T> messageType, Class<H> helperType)
					{
						FeatureBuilder.this.feature.currentConsumerRule.messageConsumer = (BiConsumer)messageConsumer;
						return new BuilderPhaseB1().consumeMessage(messageConsumer);
					}
				}
			}
		}
		
		public class BuilderPhaseB1
		{
			private BuilderPhaseB1()
			{
				super();
			}
			
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public <T,H> BuilderPhaseB2 consumeMessage(BiConsumer<IMessage<T>, MessageConsumeHelper<T,H>> messageConsumer)
			{
				FeatureBuilder.this.feature.currentConsumerRule.messageConsumer = (BiConsumer)messageConsumer;
				return new BuilderPhaseB2();
			}
			
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public <T,H> BuilderPhaseB2 consumeMessage(BiConsumer<IMessage<T>, MessageConsumeHelper<T,H>> messageConsumer, Class<T> messageType)
			{
				FeatureBuilder.this.feature.currentConsumerRule.messageConsumer = (BiConsumer)messageConsumer;
				return new BuilderPhaseB2();
			}
			
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public <T,H> BuilderPhaseB2 consumeMessage(BiConsumer<IMessage<T>, MessageConsumeHelper<T,H>> messageConsumer, Class<T> messageType, Class<H> helperType)
			{
				FeatureBuilder.this.feature.currentConsumerRule.messageConsumer = (BiConsumer)messageConsumer;
				return new BuilderPhaseB2();
			}
			
			public class BuilderPhaseB2 extends BuilderPhaseC1
			{
				private BuilderPhaseB2()
				{
					super();
				}
				
				public BuilderPhaseB3 memberOfGroup(String group)
				{
					FeatureBuilder.this.feature.currentConsumerRule.groupMembers.add(group);
					return new BuilderPhaseB3();
				}
				
				public class BuilderPhaseB3 extends BuilderPhaseC1
				{
					private BuilderPhaseB3()
					{
						super();
					}
					
					public BuilderPhaseB3 andMemberOfGroup(String group)
					{
						FeatureBuilder.this.feature.currentConsumerRule.groupMembers.add(group);
						return this;
					}
				}
				
			}
		}
		
		public class BuilderPhaseC1
		{	
			private BuilderPhaseC1()
			{
				super();
			}
			
			public BuilderPhaseC11 withTimeoutForEachMessage(int timeOut)
			{
				FeatureBuilder.this.feature.currentConsumerRule.timeOut = timeOut;
				return new BuilderPhaseC11();
			}
			
			public class BuilderPhaseC11
			{
				private BuilderPhaseC11(){super();}
				
				public BuilderPhaseC12 milliSeconds()
				{
					FeatureBuilder.this.feature.currentConsumerRule.timeOutUnit = TimeUnit.MILLISECONDS;
					
					return new BuilderPhaseC12();
				}
				
				public BuilderPhaseC12 seconds()
				{
					FeatureBuilder.this.feature.currentConsumerRule.timeOutUnit = TimeUnit.SECONDS;
					
					return new BuilderPhaseC12();
				}
				
				public BuilderPhaseC12 minutes()
				{
					FeatureBuilder.this.feature.currentConsumerRule.timeOutUnit = TimeUnit.MINUTES;
					
					return new BuilderPhaseC12();
				}
				
				public BuilderPhaseC12 hours2()
				{
					FeatureBuilder.this.feature.currentConsumerRule.timeOutUnit = TimeUnit.HOURS;
					
					return new BuilderPhaseC12();
				}
				
				public BuilderPhaseC12 days()
				{
					FeatureBuilder.this.feature.currentConsumerRule.timeOutUnit = TimeUnit.DAYS;
					
					return new BuilderPhaseC12();
				}
				
				public class BuilderPhaseC12 
				{
					private BuilderPhaseC12(){super();}
					
					public BuilderPhaseX1 immediately()
					{
						return new BuilderPhaseX1();
					}
					
					public BuilderPhaseD1.BuilderPhaseD2 ifLastConsumeEvent()
					{
						return new BuilderPhaseD1().new BuilderPhaseD2();
					}
					
					public BuilderPhaseC2 ifAllMessagesAreOlderThan(int olderThan)
					{
						FeatureBuilder.this.feature.currentConsumerRule.messageAgeTriggerMode = TriggerByMessageAgeMode.ALL;
						FeatureBuilder.this.feature.currentConsumerRule.messageAgeTriggerAge = olderThan;
						return new BuilderPhaseC2();
					}
					
					public BuilderPhaseC2 ifAtLeastOneOfMessagesIsOlderThan(int olderThan)
					{
						FeatureBuilder.this.feature.currentConsumerRule.messageAgeTriggerMode = TriggerByMessageAgeMode.LEAST_ONE;
						FeatureBuilder.this.feature.currentConsumerRule.messageAgeTriggerAge = olderThan;
						FeatureBuilder.this.feature.currentConsumerRule.messageAgeTriggerCount = 1;
						return new BuilderPhaseC2();
					}
					
					public BuilderPhaseC3 ifAtLeast(int messgeSize)
					{
						FeatureBuilder.this.feature.currentConsumerRule.messageAgeTriggerCount = messgeSize;
						return new BuilderPhaseC3();
					}
				}
			}
			
			public BuilderPhaseX1 immediately()
			{
				return new BuilderPhaseX1();
			}
			
			public BuilderPhaseD1.BuilderPhaseD2 ifLastConsumeEvent()
			{
				return new BuilderPhaseD1().new BuilderPhaseD2();
			}
			
			public BuilderPhaseC2 ifAllMessagesAreOlderThan(int olderThan)
			{
				FeatureBuilder.this.feature.currentConsumerRule.messageAgeTriggerMode = TriggerByMessageAgeMode.ALL;
				FeatureBuilder.this.feature.currentConsumerRule.messageAgeTriggerAge = olderThan;
				return new BuilderPhaseC2();
			}
			
			public BuilderPhaseC2 ifAtLeastOneOfMessagesIsOlderThan(int olderThan)
			{
				FeatureBuilder.this.feature.currentConsumerRule.messageAgeTriggerMode = TriggerByMessageAgeMode.LEAST_ONE;
				FeatureBuilder.this.feature.currentConsumerRule.messageAgeTriggerAge = olderThan;
				FeatureBuilder.this.feature.currentConsumerRule.messageAgeTriggerCount = 1;
				return new BuilderPhaseC2();
			}
			
			public BuilderPhaseC3 ifAtLeast(int messgeSize)
			{
				FeatureBuilder.this.feature.currentConsumerRule.messageAgeTriggerCount = messgeSize;
				return new BuilderPhaseC3();
			}
			
			public class BuilderPhaseC2
			{
				private BuilderPhaseC2()
				{
					super();
				}
				
				public BuilderPhaseD1 milliSeconds()
				{
					FeatureBuilder.this.feature.currentConsumerRule.messageAgeTriggerUnit = TimeUnit.MILLISECONDS;
					
					return new BuilderPhaseD1();
				}
				
				public BuilderPhaseD1 seconds()
				{
					FeatureBuilder.this.feature.currentConsumerRule.messageAgeTriggerUnit = TimeUnit.SECONDS;
					
					return new BuilderPhaseD1();
				}
				
				public BuilderPhaseD1 minutes()
				{
					FeatureBuilder.this.feature.currentConsumerRule.messageAgeTriggerUnit = TimeUnit.MINUTES;
					
					return new BuilderPhaseD1();
				}
				
				public BuilderPhaseD1 hours()
				{
					FeatureBuilder.this.feature.currentConsumerRule.messageAgeTriggerUnit = TimeUnit.HOURS;
					
					return new BuilderPhaseD1();
				}
				
				public BuilderPhaseD1 days()
				{
					FeatureBuilder.this.feature.currentConsumerRule.messageAgeTriggerUnit = TimeUnit.DAYS;
					
					return new BuilderPhaseD1();
				}
			}
			
			public class BuilderPhaseC3
			{
				private BuilderPhaseC3()
				{
					super();
				}
				
				public BuilderPhaseC2 messagesAreOlderThan(int olderThan)
				{
					FeatureBuilder.this.feature.currentConsumerRule.messageAgeTriggerMode = TriggerByMessageAgeMode.LEAST_X;
					FeatureBuilder.this.feature.currentConsumerRule.messageAgeTriggerAge = olderThan;
					return new BuilderPhaseC2();
				}
			}
		}
		
		public class BuilderPhaseD1 extends BuilderPhaseX1
		{	
			private BuilderPhaseD1()
			{
				super();
			}
	
			public BuilderPhaseD2 andIfLastConsumeEvent()
			{
				return new BuilderPhaseD2();
			}
			
			public class BuilderPhaseD2
			{
				private BuilderPhaseD2(){super();}
				
				public BuilderPhaseD3.BuilderPhaseD4 isOlderThan(int olderThan)
				{
					return new BuilderPhaseD3().isOlderThan(olderThan);
				}
				
				public BuilderPhaseD3 ofGroup(String group)
				{
					FeatureBuilder.this.feature.currentConsumerRule.consumeEventAgeTriggerGroup = group;
					return new BuilderPhaseD3();
				}
				
				public class BuilderPhaseD3
				{
					private BuilderPhaseD3(){super();}
					
					public BuilderPhaseD4 isOlderThan(int olderThan)
					{
						if(olderThan < 0)
						{
							olderThan = 0;
						}
						
						FeatureBuilder.this.feature.currentConsumerRule.consumeEventAgeTriggerAge = olderThan;
						
						return new BuilderPhaseD4();
					}
					
					public class BuilderPhaseD4
					{
						private BuilderPhaseD4(){super();}
						
						public BuilderPhaseX1 milliSeconds()
						{
							FeatureBuilder.this.feature.currentConsumerRule.consumeEventAgeTriggerUnit = TimeUnit.MILLISECONDS;
							
							return new BuilderPhaseX1();
						}
						
						public BuilderPhaseX1 seconds()
						{
							FeatureBuilder.this.feature.currentConsumerRule.consumeEventAgeTriggerUnit = TimeUnit.SECONDS;
							
							return new BuilderPhaseX1();
						}
						
						public BuilderPhaseX1 minutes()
						{
							FeatureBuilder.this.feature.currentConsumerRule.consumeEventAgeTriggerUnit = TimeUnit.MINUTES;
							
							return new BuilderPhaseX1();
						}
						
						public BuilderPhaseX1 hours()
						{
							FeatureBuilder.this.feature.currentConsumerRule.consumeEventAgeTriggerUnit = TimeUnit.HOURS;
							
							return new BuilderPhaseX1();
						}
						
						public BuilderPhaseX1 days()
						{
							FeatureBuilder.this.feature.currentConsumerRule.consumeEventAgeTriggerUnit = TimeUnit.DAYS;
							
							return new BuilderPhaseX1();
						}
					}
				}
			}
		}
		
		public class BuilderPhaseX1 extends BuilderPhaseZ1
		{
			
			private BuilderPhaseX1(){super();}
			
			public <E extends Exception,T,H> BuilderPhaseX1 onError(Class<E> clazz,BiConsumer<E, MessageConsumeHelper<T,H>> errorConsumer)
			{
				FeatureBuilder.this.feature.currentConsumerRule.specialErrorHandler.put(clazz, (BiConsumer)errorConsumer);
				return this;
			}
			
			public <E extends Exception,T,H> BuilderPhaseX1 onError(Class<E> clazz,BiConsumer<E, MessageConsumeHelper<T,H>> errorConsumer, Class<T> messageType)
			{
				FeatureBuilder.this.feature.currentConsumerRule.specialErrorHandler.put(clazz, (BiConsumer)errorConsumer);
				return this;
			}
			
			public <E extends Exception,T,H> BuilderPhaseX1 onError(Class<E> clazz,BiConsumer<E, MessageConsumeHelper<T,H>> errorConsumer, Class<T> messageType, Class<H> helperType)
			{
				FeatureBuilder.this.feature.currentConsumerRule.specialErrorHandler.put(clazz, (BiConsumer)errorConsumer);
				return this;
			}
			
			public <T,H> BuilderPhaseX2 onError(BiConsumer<Exception, MessageConsumeHelper<T,H>> errorConsumer)
			{
				FeatureBuilder.this.feature.currentConsumerRule.defaultErrorHandler = (BiConsumer)errorConsumer;
				return new BuilderPhaseX2();
			}
			
			public <T,H> BuilderPhaseX2 onError(BiConsumer<Exception, MessageConsumeHelper<T,H>> errorConsumer, Class<T> messageType)
			{
				FeatureBuilder.this.feature.currentConsumerRule.defaultErrorHandler = (BiConsumer)errorConsumer;
				return new BuilderPhaseX2();
			}
			
			public <T,H> BuilderPhaseX2 onError(BiConsumer<Exception, MessageConsumeHelper<T,H>> errorConsumer, Class<T> messageType , Class<H> helperType)
			{
				FeatureBuilder.this.feature.currentConsumerRule.defaultErrorHandler = (BiConsumer)errorConsumer;
				return new BuilderPhaseX2();
			}
			
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public <T,H> BuilderPhaseZ1 onTimeout(BiConsumer<IMessage<T>, MessageConsumeHelper<T,H>> messageConsumer)
			{
				FeatureBuilder.this.feature.currentConsumerRule.timeOutHandler = (BiConsumer)messageConsumer;
				return new BuilderPhaseZ1();
			}
			
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public <T,H> BuilderPhaseZ1 onTimeout(BiConsumer<IMessage<T>, MessageConsumeHelper<T,H>> messageConsumer, Class<T> messageType)
			{
				FeatureBuilder.this.feature.currentConsumerRule.timeOutHandler = (BiConsumer)messageConsumer;
				return new BuilderPhaseZ1();
			}
			
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public <T,H> BuilderPhaseZ1 onTimeout(BiConsumer<IMessage<T>, MessageConsumeHelper<T,H>> messageConsumer, Class<T> messageType, Class<H> helperType)
			{
				FeatureBuilder.this.feature.currentConsumerRule.timeOutHandler = (BiConsumer)messageConsumer;
				return new BuilderPhaseZ1();
			}
			
			public class BuilderPhaseX2 extends BuilderPhaseZ1
			{
				private BuilderPhaseX2(){super();}
				
				@SuppressWarnings({ "unchecked", "rawtypes" })
				public <T,H> BuilderPhaseZ1 onTimeout(BiConsumer<IMessage<T>, MessageConsumeHelper<T,H>> messageConsumer)
				{
					FeatureBuilder.this.feature.currentConsumerRule.timeOutHandler = (BiConsumer)messageConsumer;
					return new BuilderPhaseZ1();
				}
				
				@SuppressWarnings({ "unchecked", "rawtypes" })
				public <T,H> BuilderPhaseZ1 onTimeout(BiConsumer<IMessage<T>, MessageConsumeHelper<T,H>> messageConsumer, Class<T> messageType)
				{
					FeatureBuilder.this.feature.currentConsumerRule.timeOutHandler = (BiConsumer)messageConsumer;
					return new BuilderPhaseZ1();
				}
				
				@SuppressWarnings({ "unchecked", "rawtypes" })
				public <T,H> BuilderPhaseZ1 onTimeout(BiConsumer<IMessage<T>, MessageConsumeHelper<T,H>> messageConsumer, Class<T> messageType, Class<H> helperType)
				{
					FeatureBuilder.this.feature.currentConsumerRule.timeOutHandler = (BiConsumer)messageConsumer;
					return new BuilderPhaseZ1();
				}
			}
		}
		
		public class BuilderPhaseZ1
		{
			
			private BuilderPhaseZ1()
			{
				super();
			}
			
			public BuilderPhaseZ2 butKeepMessagesInChannel()
			{
				return new BuilderPhaseZ2();
			}
			
			public class BuilderPhaseZ2
			{
				
				private BuilderPhaseZ2()
				{
					super();
				}
				
				public BuilderPhaseZ3 andYesIKnowWhatThisMeans()
				{
					FeatureBuilder.this.feature.currentConsumerRule.keepMessages = true;
					return new BuilderPhaseZ3();
				}
				
				public class BuilderPhaseZ3
				{
					
					private BuilderPhaseZ3()
					{
						super();
					}
					
					public IChannelFeature buildFeature()
					{
						return FeatureBuilder.this.feature.copy(true);
					}
				}
			}
			
			public IChannelFeature buildFeature()
			{
				return FeatureBuilder.this.feature.copy(true);
			}
			
			public FeatureBuilder or()
			{
				FeatureBuilder.this.feature.currentConsumerRule = new ConsumerRule();
				FeatureBuilder.this.feature.consumerRuleList.add(FeatureBuilder.this.feature.currentConsumerRule);
				
				return FeatureBuilder.this;
			}
		}
	}
	
	public static class ConsumerRule
	{
		public enum TriggerByMessageAgeMode {NONE, ALL, LEAST_ONE, LEAST_X};
		public static final String ALL_GROUP = "CONSUMER_ALL_GROUP";
		
		private UUID id = UUID.randomUUID();
		
		// pool
		private Predicate<IMessage> poolFilter = null;
		private int poolMinSize = 1;
		private int poolMaxSize = Short.MAX_VALUE;
		
		// consume event trigger
		private String consumeEventAgeTriggerGroup = null;
		private int consumeEventAgeTriggerAge = -1;
		private TimeUnit consumeEventAgeTriggerUnit = TimeUnit.SECONDS;
		
		// message age trigger
		private TriggerByMessageAgeMode messageAgeTriggerMode = TriggerByMessageAgeMode.NONE;
		private int messageAgeTriggerCount = -1;
		private int messageAgeTriggerAge = -1;
		private TimeUnit messageAgeTriggerUnit = TimeUnit.SECONDS;
		
		// consumer
		private BiConsumer<IMessage<?>, MessageConsumeHelper<?,?>> messageConsumer = null;
		private Set<String> groupMembers = new HashSet<String>();
				
		// timeout
		private int timeOut = -1;
		private TimeUnit timeOutUnit = TimeUnit.SECONDS;
		private BiConsumer<IMessage<?>, MessageConsumeHelper<?,?>> timeOutHandler = null;
		
		// error
		private BiConsumer<Exception, MessageConsumeHelper> defaultErrorHandler = null;
		private Map<Class,BiConsumer<Exception, MessageConsumeHelper>> specialErrorHandler = new HashMap<>();
		
		// keep messages in channel
		private boolean keepMessages = false;
		
		private ConsumerRule copy()
		{
			ConsumerRule consumerRule = new ConsumerRule();
			consumerRule.poolFilter = this.poolFilter;
			consumerRule.poolMinSize = this.poolMinSize;
			consumerRule.poolMaxSize = this.poolMaxSize;
			consumerRule.consumeEventAgeTriggerGroup = this.consumeEventAgeTriggerGroup;
			consumerRule.consumeEventAgeTriggerAge = this.consumeEventAgeTriggerAge;
			consumerRule.consumeEventAgeTriggerUnit = this.consumeEventAgeTriggerUnit;
			consumerRule.messageConsumer = this.messageConsumer;
			consumerRule.timeOut = this.timeOut;
			consumerRule.timeOutUnit = this.timeOutUnit;
			consumerRule.groupMembers = new HashSet<String>(this.groupMembers);
			consumerRule.messageAgeTriggerMode = this.messageAgeTriggerMode;
			consumerRule.messageAgeTriggerCount = this.messageAgeTriggerCount;
			consumerRule.messageAgeTriggerAge = this.messageAgeTriggerAge;
			consumerRule.messageAgeTriggerUnit = this.messageAgeTriggerUnit;
			consumerRule.defaultErrorHandler = this.defaultErrorHandler;
			consumerRule.specialErrorHandler = new HashMap<>(this.specialErrorHandler);
			consumerRule.timeOutHandler = this.timeOutHandler;
			consumerRule.keepMessages = this.keepMessages;
			return consumerRule;
			
		}

		public UUID getId()
		{
			return id;
		}

		public Predicate<IMessage> getPoolFilter()
		{
			return poolFilter;
		}

		public int getPoolMinSize()
		{
			return poolMinSize;
		}

		public int getPoolMaxSize()
		{
			return poolMaxSize;
		}

		public String getConsumeEventAgeTriggerGroup()
		{
			return consumeEventAgeTriggerGroup;
		}

		public int getConsumeEventAgeTriggerAge()
		{
			return consumeEventAgeTriggerAge;
		}

		public TimeUnit getConsumeEventAgeTriggerUnit()
		{
			return consumeEventAgeTriggerUnit;
		}

		public BiConsumer<IMessage<?>, MessageConsumeHelper<?, ?>> getMessageConsumer()
		{
			return messageConsumer;
		}

		public Set<String> getGroupMembers()
		{
			return groupMembers;
		}

		public TriggerByMessageAgeMode getMessageAgeTriggerMode()
		{
			return messageAgeTriggerMode;
		}

		protected int getMessageAgeTriggerCount()
		{
			return messageAgeTriggerCount;
		}

		protected int getMessageAgeTriggerAge()
		{
			return messageAgeTriggerAge;
		}

		protected TimeUnit getMessageAgeTriggerUnit()
		{
			return messageAgeTriggerUnit;
		}

		protected int getTimeOut()
		{
			return timeOut;
		}

		protected TimeUnit getTimeOutUnit()
		{
			return timeOutUnit;
		}

		protected BiConsumer<Exception, MessageConsumeHelper> getDefaultErrorHandler()
		{
			return defaultErrorHandler;
		}

		protected Map<Class, BiConsumer<Exception, MessageConsumeHelper>> getSpecialErrorHandler()
		{
			return specialErrorHandler;
		}

		protected BiConsumer<IMessage<?>, MessageConsumeHelper<?, ?>> getTimeOutHandler()
		{
			return timeOutHandler;
		}

		protected boolean isKeepMessages()
		{
			return keepMessages;
		}
	}
	
	public class MessageConsumerFeatureConfiguration implements IPreparedChannelFeature
	{
		private List<ConsumerRule> consumerRuleList = null;
		private ConsumerRule currentConsumerRule = null;
		
		private MessageConsumerFeatureConfiguration()
		{
			super();
			this.consumerRuleList = new ArrayList<MessageConsumerFeature.ConsumerRule>();
			this.currentConsumerRule = new ConsumerRule();
			this.consumerRuleList.add(currentConsumerRule);
		}
		
		private MessageConsumerFeatureConfiguration(List<ConsumerRule> consumerRuleList, boolean immutable)
		{
			super();
			this.consumerRuleList = new ArrayList<MessageConsumerFeature.ConsumerRule>();
			for(ConsumerRule consumerRule : consumerRuleList)
			{
				this.consumerRuleList.add(consumerRule.copy());
			}
			if(immutable)
			{
				for(ConsumerRule consumerRule : this.consumerRuleList)
				{
					String privateGroup = "CONSUMER_PRIVATE_GROUP_" + consumerRule.id.toString();
					if((consumerRule.consumeEventAgeTriggerAge > -1) && (consumerRule.consumeEventAgeTriggerGroup == null))
					{
						consumerRule.consumeEventAgeTriggerGroup = privateGroup;
					}
					consumerRule.groupMembers.add(privateGroup);
					consumerRule.groupMembers.add(ConsumerRule.ALL_GROUP);
					
					consumerRule.groupMembers = Collections.unmodifiableSet(consumerRule.groupMembers);
					consumerRule.specialErrorHandler = Collections.unmodifiableMap(consumerRule.specialErrorHandler);
				}
				this.consumerRuleList = Collections.unmodifiableList(this.consumerRuleList);
			}
		}
		
		@Override
		public void applyToChannel(IDispatcherChannel<?> channel)
		{
			Map<String,Object> configurationProperties = new HashMap<>();
			configurationProperties.put(MessageConsumerFeatureConfiguration.class.getCanonicalName(), this);
			UUID uuid = UUID.randomUUID();
			channel.createChildScope(uuid,"Message Consumer Planner Scope " + uuid.toString(), configurationProperties, null);
		}
		
		private MessageConsumerFeatureConfiguration copy(boolean immutable)
		{
			return new MessageConsumerFeatureConfiguration(this.consumerRuleList,immutable);
		}

		public List<ConsumerRule> getConsumerRuleList()
		{
			return consumerRuleList;
		}
		
		
	}
}
