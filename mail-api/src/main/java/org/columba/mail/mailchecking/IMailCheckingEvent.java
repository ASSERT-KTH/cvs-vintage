package org.columba.mail.mailchecking;

import org.columba.mail.command.IMailFolderCommandReference;

public interface IMailCheckingEvent {

	public Object getSource();
	
	public IMailFolderCommandReference getReference();
}
