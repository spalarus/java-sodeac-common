package org.sodeac.common.message.service.api;

import org.sodeac.common.message.service.api.IChannel.IChannelPolicy;

public interface ICommonChannelPolicies
{
	// prefetch
	public interface IPreMessageRequest extends IChannelPolicy
	{
		public IPreMessageRequest ifChannelMessageSizeLessThen(int value);
		public IPreMessageRequest thenPreRequestForNext(int value);
		
		/**
		 * dummy method for nicer syntax
		 */
		public void messages();
	}
}
