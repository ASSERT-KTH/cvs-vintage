/*
 * Created on 14.04.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.folder.command;

import java.net.URL;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;
import org.columba.core.util.PlaySound;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandAdapter;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.config.ImapItem;
import org.columba.mail.folder.imap.IMAPFolder;
import org.columba.mail.folder.imap.IMAPRootFolder;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CheckForNewMessagesCommand extends FolderCommand {

	FolderCommandAdapter adapter;

	/**
	 * @param references
	 */
	public CheckForNewMessagesCommand(DefaultCommandReference[] references) {
		super(references);

	}

	/**
	 * @param frame
	 * @param references
	 */
	public CheckForNewMessagesCommand(
		AbstractFrameController frame,
		DefaultCommandReference[] references) {
		super(frame, references);

	}

	/* (non-Javadoc)
	 * @see org.columba.core.command.Command#execute(org.columba.core.command.Worker)
	 */
	public void execute(Worker worker) throws Exception {
		FolderCommandReference[] references =
			(FolderCommandReference[]) getReferences();
		adapter = new FolderCommandAdapter(references);

		FolderCommandReference[] r = adapter.getSourceFolderReferences();
		IMAPRootFolder srcFolder = (IMAPRootFolder) r[0].getFolder();

		boolean newMessages = false;

		IMAPFolder inboxFolder = (IMAPFolder) srcFolder.getChild("Inbox");
		
		int recent = inboxFolder.getMessageFolderInfo().getRecent();
		
		inboxFolder.getHeaderList(worker);

		int newRecent = inboxFolder.getMessageFolderInfo().getRecent();

		MainInterface.treeModel.nodeChanged(inboxFolder);

		if (newRecent != recent)
			newMessages = true;

		if (newMessages == true) {
			ImapItem item = srcFolder.getAccountItem().getImapItem();
			// new messages on server
			if (item.getBoolean("enable_sound")) {
				String file = item.get("sound_file");

				ColumbaLogger.log.info("playing sound file=" + file);

				if (file.equalsIgnoreCase("default")) {
					PlaySound.play("newmail.wav");
				} else {
					try {
						PlaySound.play(new URL(file));
					} catch (Exception ex) {
						ex.printStackTrace();
					}

				}
			}
		}

	}

}
