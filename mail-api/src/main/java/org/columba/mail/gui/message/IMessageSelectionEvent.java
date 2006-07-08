package org.columba.mail.gui.message;

import org.columba.mail.folder.IMailbox;

public interface IMessageSelectionEvent {

	public Object getSource();
	public IMailbox getFolder();
	public String getMessageId();
}
