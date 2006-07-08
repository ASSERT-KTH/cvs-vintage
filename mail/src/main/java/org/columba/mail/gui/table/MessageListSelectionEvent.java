package org.columba.mail.gui.table;

import java.util.EventObject;
import java.util.List;

public class MessageListSelectionEvent extends EventObject implements
		IMessageListSelectionEvent {

	private List<String> messageIds;
	
	public MessageListSelectionEvent(Object source, List<String> messageIds) {
		super(source);
	}

	public List<String> getMessageIds() {
		return messageIds;
	}

}
