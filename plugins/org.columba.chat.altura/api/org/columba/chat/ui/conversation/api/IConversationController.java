package org.columba.chat.ui.conversation.api;

import org.jivesoftware.smack.Chat;



public interface IConversationController {

	public abstract boolean exists(String jabberId);
	
	public abstract IChatMediator addChat(String jabberId, Chat chat);

	public abstract IChatMediator getSelected();

	public abstract IChatMediator get(int index);

	public abstract void closeSelected();

}