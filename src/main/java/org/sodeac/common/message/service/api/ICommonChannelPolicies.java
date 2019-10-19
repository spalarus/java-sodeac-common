package org.sodeac.common.message.service.api;

import org.sodeac.common.message.service.api.IMessageDrivenService.IChannel.IChannelPolicy;

public interface ICommonChannelPolicies
{
	public interface IPreMessageRequest extends IChannelPolicy
	{
		public IPreMessageRequest ifChannelMessageSizeLessThen(int value);
		public IPreMessageRequest thenPreRequestForNext(int value);
		
		/**
		 * dummy method for nicer fluent api
		 */
		public void messages();
	}
}
