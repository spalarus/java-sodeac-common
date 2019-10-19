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

import java.io.Serializable;

public abstract class DispatcherChannelSetup implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6854196332026025401L;
	
	/**
	 * specified scopes allowed to apply this configuration type
	 * 
	 * @return array of allowed scope
	 */
	public abstract Class<? extends IChannelComponent>[] getScopes();
	public abstract DispatcherChannelSetup copy(); 
	
	private DispatcherChannelSetup()
	{
		super();
	}
	
	private String dispatcherId = null;
	private String name =  null;
	private Long maxMessageSize = null;
	private PrivateChannelWorkerRequirement privateChannelWorkerRequirement = PrivateChannelWorkerRequirement.NoPreferenceOrRequirement;
	private boolean optional = false;
	
	/**
	 * getter for dispatcherId
	 * 
	 * @return id of dispatcher (NULL for all dispatchers ) managed the channel
	 */
	protected String getDispatcherId()
	{
		return dispatcherId;
	}
	
	/**
	 * setter for dispatcher id
	 * 
	 * @param dispatcherId id of dispatcher (NULL for all dispatchers ) managed the channel
	 * 
	 * @return channel component configuration
	 */
	protected DispatcherChannelSetup setDispatcherId(String dispatcherId)
	{
		this.dispatcherId = dispatcherId;
		return this;
	}
	
	/**
	 * getter for name of channel component
	 * 
	 * @return name of channel component
	 */
	protected String getName()
	{
		return name;
	}
	
	/**
	 * setter for name of channel component
	 * 
	 * @param name name of channel component
	 * @return component configuration
	 */
	protected DispatcherChannelSetup setName(String name)
	{
		this.name = name;
		return this;
	}
	
	/**
	 * getter for max channel size
	 * 
	 * @return max channel size or null, if property not set
	 */
	protected Long getMaxMessageSize() 
	{
		return maxMessageSize;
	}
	
	/**
	 * setter for max channel size
	 * 
	 * @param maxMessageSize max size of messages 
	 * @return channel component configuration
	 */
	protected DispatcherChannelSetup setMaxMessageSize(Long maxMessageSize) 
	{
		this.maxMessageSize = maxMessageSize;
		return this;
	}
		
	/**
	 * getter for  private channel worker requirement
	 * 
	 * @return necessity to use same channel worker thread for synchronized channel activities at all times
	 */
	protected PrivateChannelWorkerRequirement getPrivateChannelWorkerRequirement()
	{
		return privateChannelWorkerRequirement;
	}

	/**
	 * setter for  private channel worker requirement
	 * 
	 * @param privateChannelWorkerRequirement declares necessity to use same channel worker thread for synchronized channel activities at all times
	 * @return channel component configuration
	 */
	protected DispatcherChannelSetup setPrivateChannelWorkerRequirement(PrivateChannelWorkerRequirement privateChannelWorkerRequirement)
	{
		this.privateChannelWorkerRequirement = privateChannelWorkerRequirement;
		return this;
	}
	
	/**
	 * getter for optional flag
	 * 
	 * @return true, if setup item is processed by setup provider, otherwise false
	 */
	public boolean isOptional() 
	{
		return optional;
	}
	
	/**
	 * setter for optional flag
	 * 
	 * @param optional defines that the setup item has to be processed by setup provider, or not
	 * @return channel component configuration
	 */
	public DispatcherChannelSetup setOptional(boolean optional) 
	{
		this.optional = optional;
		return this;
	}

	/**
	 * Configuration to bind a {@link IChannelComponent} (a {@link IChannelManager} or a {@link IChannelService}) to the {@link IChannel} 
	 * with specified channelId managed by {@link IMessageDispatcher} with specified id. 
	 * 
	 * 
	 * @author Sebastian Palarus
	 *
	 */
	public final static class BoundedByChannelId extends DispatcherChannelSetup
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 6587259399825230943L;
		
		private String channelId;
		private boolean autoCreateChannel = true;
		
		/**
		 * Constructor to  bind a {@link IChannelComponent} to the {@link IChannel} with specified channelId.
		 * 
		 * @param channelId id of channel
		 */
		public BoundedByChannelId(String channelId)
		{
			super();
			this.channelId = channelId;
		}
		
		/**
		 * getter for channel id
		 * 
		 * @return id of channel
		 */
		public String getChannelId()
		{
			return channelId;
		}
		
		@Override
		public String getName()
		{
			return super.getName();
		}

		@Override
		public BoundedByChannelId setName(String name)
		{
			return (BoundedByChannelId)super.setName(name);
		}

		/**
		 * setter for auto create channel mode
		 * 
		 * @param autoCreateChannel if true, dispatcher creates automatically a channel with {@code channelId} if it still does not exist
		 * @return channel component configuration
		 */
		public BoundedByChannelId setAutoCreateChannel(boolean autoCreateChannel)
		{
			this.autoCreateChannel = autoCreateChannel;
			return this;
		}
		

		/**
		 * getter for auto create channel mode
		 * 
		 * @return true, if channel should automatically create, if not exists. return false, if not
		 */
		public boolean isAutoCreateChannel()
		{
			return autoCreateChannel;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends IChannelComponent>[] getScopes()
		{
			return new Class[] {IChannelManager.class,IChannelService.class};
		}

		@Override
		public BoundedByChannelId setDispatcherId(String dispatcherId)
		{
			return (BoundedByChannelId)super.setDispatcherId(dispatcherId);
		}

		@Override
		protected BoundedByChannelId setMaxMessageSize(Long maxMessageSize) 
		{
			return (BoundedByChannelId)super.setMaxMessageSize(maxMessageSize);
		}

		@Override
		public BoundedByChannelId setPrivateChannelWorkerRequirement(PrivateChannelWorkerRequirement privateChannelWorkerRequirement)
		{
			return (BoundedByChannelId)super.setPrivateChannelWorkerRequirement(privateChannelWorkerRequirement);
		}

		@Override
		public String getDispatcherId()
		{
			return super.getDispatcherId();
		}

		@Override
		protected Long getMaxMessageSize() 
		{
			return super.getMaxMessageSize();
		}

		@Override
		public PrivateChannelWorkerRequirement getPrivateChannelWorkerRequirement()
		{
			return super.getPrivateChannelWorkerRequirement();
		}

		@Override
		public BoundedByChannelId copy() 
		{
			return new BoundedByChannelId(this.channelId)
					.setDispatcherId(super.dispatcherId)
					.setName(super.name)
					.setAutoCreateChannel(this.autoCreateChannel)
					.setPrivateChannelWorkerRequirement(super.privateChannelWorkerRequirement)
					.setMaxMessageSize(super.maxMessageSize);
		}
		
		
	}
	
	/**
	 * Configuration to bind a {@link IChannelComponent} (a {@link IChannelManager} or a {@link IChannelService}) to existing {@link IChannel}s 
	 * whose properties match a ldapfilter.
	 * 
	 * @author Sebastian Palarus
	 *
	 */
	public final static class BoundedByChannelConfiguration extends DispatcherChannelSetup
	{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 7663354400942715419L;
		
		private String ldapFilter;
		
		/**
		 * Constructor to  bind a {@link IChannelComponent} to existing {@link IChannel}s whose properties match the specified ldap-filter.
		 * 
		 */
		public BoundedByChannelConfiguration(String ldapFilter)
		{
			super();
			this.ldapFilter = ldapFilter;
		}
		
		/**
		 * getter for ldap filter
		 * 
		 * @return ldap filter
		 */
		public String getLdapFilter()
		{
			return ldapFilter;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends IChannelComponent>[] getScopes()
		{
			return new Class[] {IChannelManager.class,IChannelService.class};
		}
		
		@Override
		public BoundedByChannelConfiguration setDispatcherId(String dispatcherId)
		{
			return (BoundedByChannelConfiguration)super.setDispatcherId(dispatcherId);
		}

		@Override
		protected BoundedByChannelConfiguration setMaxMessageSize(Long maxMessageSize) 
		{
			return (BoundedByChannelConfiguration)super.setMaxMessageSize(maxMessageSize);
		}

		@Override
		public BoundedByChannelConfiguration setPrivateChannelWorkerRequirement(PrivateChannelWorkerRequirement privateChannelWorkerRequirement)
		{
			return (BoundedByChannelConfiguration)super.setPrivateChannelWorkerRequirement(privateChannelWorkerRequirement);
		}
		
		@Override
		public String getDispatcherId()
		{
			return super.getDispatcherId();
		}
		
		@Override
		public String getName()
		{
			return super.getName();
		}

		@Override
		public BoundedByChannelConfiguration setName(String name)
		{
			return (BoundedByChannelConfiguration)super.setName(name);
		}

		@Override
		protected Long getMaxMessageSize() 
		{
			return super.getMaxMessageSize();
		}

		@Override
		public PrivateChannelWorkerRequirement getPrivateChannelWorkerRequirement()
		{
			return super.getPrivateChannelWorkerRequirement();
		}
		
		@Override
		public BoundedByChannelConfiguration copy() 
		{
			return new BoundedByChannelConfiguration(this.ldapFilter)
					.setDispatcherId(super.dispatcherId)
					.setName(super.name)
					.setPrivateChannelWorkerRequirement(super.privateChannelWorkerRequirement)
					.setMaxMessageSize(super.maxMessageSize);
		}
	}
	
	/**
	 * Configuration for Channel Services
	 * 
	 * @author Sebastian Palarus
	 *
	 */
	public static class ChannelServiceConfiguration extends DispatcherChannelSetup
	{

		public ChannelServiceConfiguration(String serviceId)
		{
			super();
			this.serviceId = serviceId;
		}
		
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 7301276962907883651L;
		
		private String serviceId;
		private long timeOutInMS = -1;
		private long heartbeatTimeOutInMS = -1;
		private long startDelayInMS = 0;
		private long periodicRepetitionIntervalMS = -1;
		
		@Override
		public String getDispatcherId()
		{
			return super.getDispatcherId();
		}
		
		@Override
		public String getName()
		{
			return super.getName();
		}

		@Override
		public ChannelServiceConfiguration setName(String name)
		{
			return (ChannelServiceConfiguration)super.setName(name);
		}
		
		/**
		 * getter for service timeout in ms
		 * 
		 * @return service timeout in ms
		 */
		public long getTimeOutInMS()
		{
			return timeOutInMS;
		}
		

		/**
		 * 
		 * setter for service timeout in ms
		 * 
		 * @param timeOutInMS service timeout in ms
		 * @return channel service configuration
		 */
		public ChannelServiceConfiguration setTimeOutInMS(long timeOutInMS)
		{
			this.timeOutInMS = timeOutInMS;
			return this;
		}

		/**
		 * getter for service heartbeat timeout in ms
		 * 
		 * @return service heartbeat timeout
		 */
		public long getHeartbeatTimeOutInMS()
		{
			return heartbeatTimeOutInMS;
		}

		/**
		 * setter for service heartbeat timeout in ms
		 * 
		 * @param heartbeatTimeOutInMS service heartbeat timeout in ms
		 * @return channel service configuration
		 */
		public ChannelServiceConfiguration setHeartbeatTimeOutInMS(long heartbeatTimeOutInMS)
		{
			this.heartbeatTimeOutInMS = heartbeatTimeOutInMS;
			return this;
		}

		/**
		 * getter for start delay of service in ms
		 * 
		 * @return start delay of service in ms
		 */
		public long getStartDelayInMS()
		{
			return startDelayInMS;
		}

		/**
		 * setter for start delay of service in ms
		 * 
		 * @param startDelayInMS start delay of service in ms
		 * @return channel service configuration
		 */
		public ChannelServiceConfiguration setStartDelayInMS(long startDelayInMS)
		{
			this.startDelayInMS = startDelayInMS;
			return this;
		}

		/**
		 * getter for periodic repetition interval of service in ms
		 * @return periodic repetition interval of service in ms
		 */
		public long getPeriodicRepetitionIntervalMS()
		{
			return periodicRepetitionIntervalMS;
		}

		/**
		 * setter for periodic repetition interval of service in ms
		 * @param periodicRepetitionIntervalMS repetition interval of service in ms
		 * @return channel service configuration
		 */
		public ChannelServiceConfiguration setPeriodicRepetitionIntervalMS(long periodicRepetitionIntervalMS)
		{
			this.periodicRepetitionIntervalMS = periodicRepetitionIntervalMS;
			return this;
		}
		
		@Override
		protected ChannelServiceConfiguration setMaxMessageSize(Long maxMessageSize) 
		{
			return (ChannelServiceConfiguration)super.setMaxMessageSize(maxMessageSize);
		}

		@Override
		public ChannelServiceConfiguration setPrivateChannelWorkerRequirement(PrivateChannelWorkerRequirement privateChannelWorkerRequirement)
		{
			return (ChannelServiceConfiguration)super.setPrivateChannelWorkerRequirement(privateChannelWorkerRequirement);
		}

		@Override
		protected Long getMaxMessageSize() 
		{
			return super.getMaxMessageSize();
		}

		@Override
		public PrivateChannelWorkerRequirement getPrivateChannelWorkerRequirement()
		{
			return super.getPrivateChannelWorkerRequirement();
		}

		/**
		 * getter for service id
		 * 
		 * @return id of service
		 */
		public String getServiceId()
		{
			return serviceId;
		}


		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends IChannelComponent>[] getScopes()
		{
			return new Class[] {IChannelService.class};
		}
		
		@Override
		public ChannelServiceConfiguration copy() 
		{
			return new ChannelServiceConfiguration(this.serviceId)
					.setName(super.name)
					.setTimeOutInMS(this.timeOutInMS)
					.setHeartbeatTimeOutInMS(this.heartbeatTimeOutInMS)
					.setStartDelayInMS(this.startDelayInMS)
					.setPeriodicRepetitionIntervalMS(this.periodicRepetitionIntervalMS);
		}
	}
}
