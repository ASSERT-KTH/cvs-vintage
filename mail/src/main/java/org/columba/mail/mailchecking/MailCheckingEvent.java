package org.columba.mail.mailchecking;

import java.util.EventObject;

import org.columba.mail.command.IMailFolderCommandReference;

public class MailCheckingEvent extends EventObject implements IMailCheckingEvent {

	private IMailFolderCommandReference ref;
	
	public MailCheckingEvent(Object source, IMailFolderCommandReference ref) {
		super(source);
		
		this.ref = ref;
	}

	public IMailFolderCommandReference getReference() {
		return ref;
	}

}
