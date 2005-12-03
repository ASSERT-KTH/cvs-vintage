package org.columba.mail.folder.imap;

import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusController;
import org.columba.core.command.Command;
import org.columba.core.command.CommandProcessor;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.command.MailFolderCommandReference;

public class FetchMessagesCommand extends Command {

	private int newMessages;
	private int fetchedMessages;
	private int offset;
	
	public FetchMessagesCommand(ICommandReference reference,int newMessages, int offset, int fetchedMessages) {
		super(reference);
		
		this.newMessages = newMessages;
		this.fetchedMessages = fetchedMessages;
		this.offset = offset;
	}

	public void execute(IWorkerStatusController worker) throws Exception {
		// get references
		IMailFolderCommandReference r = (IMailFolderCommandReference) getReference();

		IMAPFolder imapFolder = (IMAPFolder) r.getSourceFolder();
		
		worker.setProgressBarMaximum(newMessages);
		worker.setProgressBarValue(fetchedMessages);
		
		int fetched = imapFolder.fetchNewMessages(offset + fetchedMessages).size();
		
		fetchedMessages += fetched;
		worker.setProgressBarValue(fetchedMessages);
		
		if( worker.cancelled()) {
			return;
		}
		
		if(fetched > 0 && fetchedMessages < newMessages) {
			CommandProcessor.getInstance().addOp(
					new FetchMessagesCommand(
							new MailFolderCommandReference(imapFolder),
							newMessages, offset, fetchedMessages));
		} else {
			// Trigger any pending Flag syncs
			imapFolder.fetchDone();

		}
	}

}
