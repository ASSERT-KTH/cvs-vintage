package org.columba.chat.api;


public interface IConversationController {

	public abstract IChatMediator addChat(String jabberId);

	public abstract IChatMediator getSelected();

	public abstract IChatMediator get(int index);

	public abstract void closeSelected();

}