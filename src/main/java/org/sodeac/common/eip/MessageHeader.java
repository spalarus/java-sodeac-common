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
package org.sodeac.common.eip;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MessageHeader implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2987891836819988594L;
	
	public static final String MESSAGE_HEADER_MESSAGE_ID 			= "SDC_MH_MESSAGE_ID"			;
	public static final String MESSAGE_HEADER_CORRELATION_ID 		= "SDC_MH_CORRELATION_ID"		;
	public static final String MESSAGE_HEADER_PRIORITY 				= "SDC_MH_PRIORITY"				;
	public static final String MESSAGE_HEADER_GUARANTEED_DELIVERY 	= "SDC_MH_GUARANTEED_DELIVERY"	;
	public static final String MESSAGE_HEADER_TIMESTAMP 			= "SDC_MH_TIMESTAMP"			;
	public static final String MESSAGE_HEADER_EXPIRATION 			= "SDC_MH_EXPIRATION"			;
	public static final String MESSAGE_HEADER_TOPIC 				= "SDC_MH_TOPIC"				;
	public static final String MESSAGE_HEADER_MESSAGE_TYPE 			= "SDC_MH_MESSAGE_TYPE"			;
	public static final String MESSAGE_HEADER_MESSAGE_FORMAT 		= "SDC_MH_MESSAGE_FORMAT"		;
	public static final String MESSAGE_HEADER_DOMAIN 				= "SDC_MH_DOMAIN"				;
	public static final String MESSAGE_HEADER_BOUNDED_CONTEXT 		= "SDC_MH_BOUNDED_CONTEXT"		;
	public static final String MESSAGE_HEADER_DESTINATION 			= "SDC_MH_DESTINATION"			;
	public static final String MESSAGE_HEADER_SOURCE 				= "SDC_MH_SOURCE"				;
	public static final String MESSAGE_HEADER_REPLY_TO 				= "SDC_MH_REPLY_TO"				;
	public static final String MESSAGE_HEADER_DELIVERY_TIME 		= "SDC_MH_REPLY_DELIVERY_TIME"	;
	public static final String MESSAGE_HEADER_REDELIVERED 			= "SDC_MH_REPLY_REDELIVERED"	;
	public static final String MESSAGE_HEADER_SEQUENCE 				= "SDC_MH_SEQUENCE"				;
	public static final String MESSAGE_HEADER_POSITION 				= "SDC_MH_POSITION"				;
	public static final String MESSAGE_HEADER_SIZE 					= "SDC_MH_SIZE"					;
	public static final String MESSAGE_HEADER_END 					= "SDC_MH_END"					;
	public static final String MESSAGE_HEADER_PROPERTIES 			= "SDC_MH_PROPERTIES"			;
	
	public static MessageHeader newInstance()
	{
		return new MessageHeader();
	}
	
	private MessageHeader()
	{
		super();
	}
	
	private volatile UUID messageID = null;
	private volatile boolean messageIDLocked = false;
	
	private UUID correlationID = null;
	private volatile boolean correlationIDLocked = false;
	
	private Integer priority = null;
	private volatile boolean priorityLocked = false;
	
	private Boolean guaranteedDelivery = null;
	private volatile boolean guaranteedDeliveryLocked = false;
	
	private Long timestamp = null;
	private volatile boolean timestampLocked = false;
	
	private Long expiration = null;
	private volatile boolean expirationLocked = false;
	
	private String topic = null;
	private volatile boolean topicLocked = false;
	
	private String messageType = null;
	private volatile boolean messageTypeLocked = false;
	
	private String messageFormat = null;
	private volatile boolean messageFormatLocked = false;
	
	private String domain =  null;
	private volatile boolean domainLocked = false;
	
	private String boundedContext = null;
	private volatile boolean boundedContextLocked = false;
	
	private String destination = null;
	private volatile boolean destinationLocked = false;
	
	private String source = null;
	private volatile boolean sourceLocked = false;
	
	private String replyTo = null;
	private volatile boolean replyToLocked = false;
	
	private Long deliveryTime = null;
	private volatile boolean deliveryTimeLocked = false;
	
	private Boolean redelivered = null;
	private volatile boolean redeliveredLocked = false;
	
	private Long sequence = null;
	private volatile boolean sequenceLocked = false;
	
	private Long position = null;
	private volatile boolean positionLocked = false;
	
	private Long size = null;
	private volatile boolean sizeLocked = false;
	
	private Boolean end = null;
	private volatile boolean endLocked = false;
	
	private Map<String,Object> properties;
	private volatile boolean propertiesLocked = false;
	
	public MessageHeader lockAllHeader()
	{
		this.messageIDLocked = true;
		this.correlationIDLocked = true;
		this.priorityLocked = true;
		this.guaranteedDeliveryLocked = true;
		this.timestampLocked = true;
		this.expirationLocked = true;
		this.topicLocked = true;
		this.messageTypeLocked = true;
		this.messageFormatLocked = true;
		this.domainLocked = true;
		this.boundedContextLocked = true;
		this.destinationLocked = true;
		this.sourceLocked = true;
		this.replyToLocked = true;
		this.deliveryTimeLocked = true;
		this.redeliveredLocked = true;
		this.sequenceLocked = true;
		this.positionLocked = true;
		this.sizeLocked = true;
		this.endLocked = true;
		this.propertiesLocked = true;
		
		return this;
	}
	
	public MessageHeader lockHeader(String messageHeader)
	{
		switch (messageHeader) 
		{
			case MESSAGE_HEADER_MESSAGE_ID:
			
				this.messageIDLocked = true;
				break;
			
			case MESSAGE_HEADER_CORRELATION_ID:
				
				this.correlationIDLocked = true;
				break;
			
			case MESSAGE_HEADER_PRIORITY:
				
				this.priorityLocked = true;
				break;

			case MESSAGE_HEADER_GUARANTEED_DELIVERY:
				
				this.guaranteedDeliveryLocked = true;
				break;
			
			case MESSAGE_HEADER_TIMESTAMP:
				
				this.timestampLocked = true;
				break;
				
			case MESSAGE_HEADER_EXPIRATION:
				
				this.expirationLocked = true;
				break;
			
			case MESSAGE_HEADER_TOPIC:
				
				this.topicLocked = true;
				break;
		
			case MESSAGE_HEADER_MESSAGE_TYPE:
				
				this.messageTypeLocked = true;
				break;
			
			case MESSAGE_HEADER_MESSAGE_FORMAT:
				
				this.messageFormatLocked = true;
				break;
				
			case MESSAGE_HEADER_DOMAIN:
				
				this.domainLocked = true;
				break;
				
			case MESSAGE_HEADER_BOUNDED_CONTEXT:
				
				this.boundedContextLocked = true;
				break;
				
			case MESSAGE_HEADER_DESTINATION:
				
				this.destinationLocked = true;
				break;
				
			case MESSAGE_HEADER_SOURCE:
				
				this.sourceLocked = true;
				break;
				
			case MESSAGE_HEADER_REPLY_TO:
				
				this.replyToLocked = true;
				break;
				
			case MESSAGE_HEADER_DELIVERY_TIME:
				
				this.deliveryTimeLocked = true;
				break;
				
			case MESSAGE_HEADER_REDELIVERED:
				
				this.redeliveredLocked = true;
				break;
				
			case MESSAGE_HEADER_SEQUENCE:
				
				this.sequenceLocked = true;
				break;
				
			case MESSAGE_HEADER_POSITION:
				
				this.positionLocked = true;
				break;
				
			case MESSAGE_HEADER_SIZE:
				
				this.sizeLocked = true;
				break;
				
			case MESSAGE_HEADER_END:
				
				this.endLocked = true;
				break;
				
			case MESSAGE_HEADER_PROPERTIES:
				
				this.propertiesLocked = true;
				break;
				
			default:
				break;
		}
		
		return this;
	}

	public UUID getMessageID()
	{
		return messageID;
	}

	public MessageHeader setMessageID(UUID messageID)
	{
		if(! this.messageIDLocked)
		{
			this.messageID = messageID;
		}
		return this;
	}
	
	public MessageHeader generateMessageID()
	{
		if(! this.messageIDLocked)
		{
			this.messageID = UUID.randomUUID();
		}
		return this;
	}

	public UUID getCorrelationID()
	{
		return correlationID;
	}

	public MessageHeader setCorrelationID(UUID correlationID)
	{
		if(! this.correlationIDLocked)
		{
			this.correlationID = correlationID;
		}
		return this;
	}

	public Integer getPriority()
	{
		return priority;
	}

	public MessageHeader setPriority(Integer priority)
	{
		if(! this.priorityLocked)
		{
			this.priority = priority;
		}
		return this;
	}

	public Boolean getGuaranteedDelivery()
	{
		return guaranteedDelivery;
	}

	public MessageHeader setGuaranteedDelivery(Boolean guaranteedDelivery)
	{
		if(! this.guaranteedDeliveryLocked)
		{
			this.guaranteedDelivery = guaranteedDelivery;
		}
		return this;
	}

	public Long getTimestamp()
	{
		return timestamp;
	}

	public MessageHeader setTimestamp(Long timestamp)
	{
		if(! this.timestampLocked)
		{
			this.timestamp = timestamp;
		}
		return this;
	}

	public Long getExpiration()
	{
		return expiration;
	}

	public MessageHeader setExpiration(Long expiration)
	{
		if(! this.expirationLocked)
		{
			this.expiration = expiration;
		}
		return this;
	}

	public String getTopic()
	{
		return topic;
	}

	public MessageHeader setTopic(String topic)
	{
		if(! this.topicLocked)
		{
			this.topic = topic;
		}
		return this;
	}

	public String getMessageType()
	{
		return messageType;
	}

	public MessageHeader setMessageType(String messageType)
	{
		if(! this.messageTypeLocked)
		{
			this.messageType = messageType;
		}
		return this;
	}

	public String getMessageFormat()
	{
		return messageFormat;
	}

	public MessageHeader setMessageFormat(String messageFormat)
	{
		if(! this.messageFormatLocked)
		{
			this.messageFormat = messageFormat;
		}
		return this;
	}

	public String getDomain()
	{
		return domain;
	}

	public MessageHeader setDomain(String domain)
	{
		if(! this.domainLocked)
		{
			this.domain = domain;
		}
		return this;
	}

	public String getBoundedContext()
	{
		return boundedContext;
	}

	public MessageHeader setBoundedContext(String boundedContext)
	{
		if(! this.boundedContextLocked)
		{
			this.boundedContext = boundedContext;
		}
		return this;
	}

	public String getDestination()
	{
		return destination;
	}

	public MessageHeader setDestination(String destination)
	{
		if(! this.destinationLocked)
		{
			this.destination = destination;
		}
		return this;
	}

	public String getSource()
	{
		return source;
	}

	public MessageHeader setSource(String source)
	{
		if(! this.sourceLocked)
		{
			this.source = source;
		}
		return this;
	}

	public String getReplyTo()
	{
		return replyTo;
	}

	public MessageHeader setReplyTo(String replyTo)
	{
		if(! this.replyToLocked)
		{
			this.replyTo = replyTo;
		}
		return this;
	}

	public Long getDeliveryTime()
	{
		return deliveryTime;
	}

	public MessageHeader setDeliveryTime(Long deliveryTime)
	{
		if(! this.deliveryTimeLocked)
		{
			this.deliveryTime = deliveryTime;
		}
		return this;
	}

	public Boolean getRedelivered()
	{
		return redelivered;
	}

	public MessageHeader setRedelivered(Boolean redelivered)
	{
		if(! this.redeliveredLocked)
		{
			this.redelivered = redelivered;
		}
		return this;
	}

	public Long getSequence()
	{
		return sequence;
	}

	public MessageHeader setSequence(Long sequence)
	{
		if(! this.sequenceLocked)
		{
			this.sequence = sequence;
		}
		return this;
	}

	public Long getPosition()
	{
		return position;
	}

	public MessageHeader setPosition(Long position)
	{
		if(! this.positionLocked)
		{
			this.position = position;
		}
		return this;
	}

	public Long getSize()
	{
		return size;
	}

	public MessageHeader setSize(Long size)
	{
		if(! this.sizeLocked)
		{
			this.size = size;
		}
		return this;
	}

	public Boolean getEnd()
	{
		return end;
	}

	public MessageHeader setEnd(Boolean end)
	{
		if(! this.endLocked)
		{
			this.end = end;
		}
		return this;
	}
	
	public Set<String> getPropertyKeySet()
	{
		return  this.properties == null  ? Collections.emptySet() : this.properties.keySet();
	}
	
	public MessageHeader addProperty(String key, Object value)
	{
		if( this.propertiesLocked)
		{
			return this;
		}
		
		if(this.properties == null)
		{
			this.properties = new HashMap<String,Object>();
		}
		
		this.properties.put(key, value);
		
		return this;
	}
	
	public MessageHeader removeProperty(String key)
	{
		if( this.propertiesLocked)
		{
			return this;
		}
		
		if(this.properties == null)
		{
			return this;
		}
		
		this.properties.remove(key);
		
		return this;
	}
	
	public MessageHeader clearProperties()
	{
		if( this.propertiesLocked)
		{
			return this;
		}
		
		if(this.properties == null)
		{
			return this;
		}
		
		this.properties.clear();
		
		return this;
	}
	
	public Object getPropertyValue(String key)
	{
		if(this.properties == null)
		{
			return null;
		}
		
		return this.properties.get(key);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getPropertyValue(String key, Class<T> type)
	{
		if(this.properties == null)
		{
			return null;
		}
		
		return (T)this.properties.get(key);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((boundedContext == null) ? 0 : boundedContext.hashCode());
		result = prime * result + (boundedContextLocked ? 1231 : 1237);
		result = prime * result + ((correlationID == null) ? 0 : correlationID.hashCode());
		result = prime * result + (correlationIDLocked ? 1231 : 1237);
		result = prime * result + ((deliveryTime == null) ? 0 : deliveryTime.hashCode());
		result = prime * result + (deliveryTimeLocked ? 1231 : 1237);
		result = prime * result + ((destination == null) ? 0 : destination.hashCode());
		result = prime * result + (destinationLocked ? 1231 : 1237);
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime * result + (domainLocked ? 1231 : 1237);
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + (endLocked ? 1231 : 1237);
		result = prime * result + ((expiration == null) ? 0 : expiration.hashCode());
		result = prime * result + (expirationLocked ? 1231 : 1237);
		result = prime * result + ((guaranteedDelivery == null) ? 0 : guaranteedDelivery.hashCode());
		result = prime * result + (guaranteedDeliveryLocked ? 1231 : 1237);
		result = prime * result + ((messageFormat == null) ? 0 : messageFormat.hashCode());
		result = prime * result + (messageFormatLocked ? 1231 : 1237);
		result = prime * result + ((messageID == null) ? 0 : messageID.hashCode());
		result = prime * result + (messageIDLocked ? 1231 : 1237);
		result = prime * result + ((messageType == null) ? 0 : messageType.hashCode());
		result = prime * result + (messageTypeLocked ? 1231 : 1237);
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		result = prime * result + (positionLocked ? 1231 : 1237);
		result = prime * result + ((priority == null) ? 0 : priority.hashCode());
		result = prime * result + (priorityLocked ? 1231 : 1237);
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
		result = prime * result + (propertiesLocked ? 1231 : 1237);
		result = prime * result + ((redelivered == null) ? 0 : redelivered.hashCode());
		result = prime * result + (redeliveredLocked ? 1231 : 1237);
		result = prime * result + ((replyTo == null) ? 0 : replyTo.hashCode());
		result = prime * result + (replyToLocked ? 1231 : 1237);
		result = prime * result + ((sequence == null) ? 0 : sequence.hashCode());
		result = prime * result + (sequenceLocked ? 1231 : 1237);
		result = prime * result + ((size == null) ? 0 : size.hashCode());
		result = prime * result + (sizeLocked ? 1231 : 1237);
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + (sourceLocked ? 1231 : 1237);
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		result = prime * result + (timestampLocked ? 1231 : 1237);
		result = prime * result + ((topic == null) ? 0 : topic.hashCode());
		result = prime * result + (topicLocked ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MessageHeader other = (MessageHeader) obj;
		if (boundedContext == null)
		{
			if (other.boundedContext != null)
				return false;
		} else if (!boundedContext.equals(other.boundedContext))
			return false;
		if (boundedContextLocked != other.boundedContextLocked)
			return false;
		if (correlationID == null)
		{
			if (other.correlationID != null)
				return false;
		} else if (!correlationID.equals(other.correlationID))
			return false;
		if (correlationIDLocked != other.correlationIDLocked)
			return false;
		if (deliveryTime == null)
		{
			if (other.deliveryTime != null)
				return false;
		} else if (!deliveryTime.equals(other.deliveryTime))
			return false;
		if (deliveryTimeLocked != other.deliveryTimeLocked)
			return false;
		if (destination == null)
		{
			if (other.destination != null)
				return false;
		} else if (!destination.equals(other.destination))
			return false;
		if (destinationLocked != other.destinationLocked)
			return false;
		if (domain == null)
		{
			if (other.domain != null)
				return false;
		} else if (!domain.equals(other.domain))
			return false;
		if (domainLocked != other.domainLocked)
			return false;
		if (end == null)
		{
			if (other.end != null)
				return false;
		} else if (!end.equals(other.end))
			return false;
		if (endLocked != other.endLocked)
			return false;
		if (expiration == null)
		{
			if (other.expiration != null)
				return false;
		} else if (!expiration.equals(other.expiration))
			return false;
		if (expirationLocked != other.expirationLocked)
			return false;
		if (guaranteedDelivery == null)
		{
			if (other.guaranteedDelivery != null)
				return false;
		} else if (!guaranteedDelivery.equals(other.guaranteedDelivery))
			return false;
		if (guaranteedDeliveryLocked != other.guaranteedDeliveryLocked)
			return false;
		if (messageFormat == null)
		{
			if (other.messageFormat != null)
				return false;
		} else if (!messageFormat.equals(other.messageFormat))
			return false;
		if (messageFormatLocked != other.messageFormatLocked)
			return false;
		if (messageID == null)
		{
			if (other.messageID != null)
				return false;
		} else if (!messageID.equals(other.messageID))
			return false;
		if (messageIDLocked != other.messageIDLocked)
			return false;
		if (messageType == null)
		{
			if (other.messageType != null)
				return false;
		} else if (!messageType.equals(other.messageType))
			return false;
		if (messageTypeLocked != other.messageTypeLocked)
			return false;
		if (position == null)
		{
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		if (positionLocked != other.positionLocked)
			return false;
		if (priority == null)
		{
			if (other.priority != null)
				return false;
		} else if (!priority.equals(other.priority))
			return false;
		if (priorityLocked != other.priorityLocked)
			return false;
		if (properties == null)
		{
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		if (propertiesLocked != other.propertiesLocked)
			return false;
		if (redelivered == null)
		{
			if (other.redelivered != null)
				return false;
		} else if (!redelivered.equals(other.redelivered))
			return false;
		if (redeliveredLocked != other.redeliveredLocked)
			return false;
		if (replyTo == null)
		{
			if (other.replyTo != null)
				return false;
		} else if (!replyTo.equals(other.replyTo))
			return false;
		if (replyToLocked != other.replyToLocked)
			return false;
		if (sequence == null)
		{
			if (other.sequence != null)
				return false;
		} else if (!sequence.equals(other.sequence))
			return false;
		if (sequenceLocked != other.sequenceLocked)
			return false;
		if (size == null)
		{
			if (other.size != null)
				return false;
		} else if (!size.equals(other.size))
			return false;
		if (sizeLocked != other.sizeLocked)
			return false;
		if (source == null)
		{
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (sourceLocked != other.sourceLocked)
			return false;
		if (timestamp == null)
		{
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		if (timestampLocked != other.timestampLocked)
			return false;
		if (topic == null)
		{
			if (other.topic != null)
				return false;
		} else if (!topic.equals(other.topic))
			return false;
		if (topicLocked != other.topicLocked)
			return false;
		return true;
	}
	
	
}
