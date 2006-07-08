package org.columba.mail.gui.message;

import java.util.EventObject;

import org.columba.mail.folder.IMailbox;

public class MessageSelectionEvent extends EventObject implements
		IMessageSelectionEvent {

	private IMailbox folder;

	private String messageId;

	public MessageSelectionEvent(Object source, IMailbox folder,
			String messageId) {
		super(source);
		this.folder = folder;
		this.messageId = messageId;

	}

	public IMailbox getFolder() {
		return folder;
	}

	public String getMessageId() {
		return messageId;
	}

}
