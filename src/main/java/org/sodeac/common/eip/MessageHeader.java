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

import java.util.Map;
import java.util.UUID;

public class MessageHeader
{
	private UUID messageID;
	private UUID correlationID;
	private Integer priority;
	private Boolean guaranteedDelivery;
	
	private Long timestamp;
	private Long expiration;
	
	private String topic;
	private String messageType;
	private String messageFormat;
	
	private String destination;
	private String replyTo;
	private Long deliveryTime;
	private Boolean redelivered;
	
	private Long sequence;
	private Long position;
	private Long size;
	private Boolean end;
	
	private Map<String,Object> properties;
}
