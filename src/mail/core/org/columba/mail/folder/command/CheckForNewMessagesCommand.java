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
import org.columba.mail.gui.table.TableChangedEvent;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CheckForNewMessagesCommand extends FolderCommand {

	FolderCommandAdapter adapter;
	IMAPFolder inboxFolder;

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
		
		// get IMAP rootfolder 
		IMAPRootFolder srcFolder = (IMAPRootFolder) r[0].getFolder();

		boolean newMessages = false;

		// we only check inbox
		inboxFolder = (IMAPFolder) srcFolder.getChild("Inbox");
		
		// number of recent messages
		int recent = inboxFolder.getMessageFolderInfo().getRecent();
		
		// fetch headerlist
		inboxFolder.getHeaderList(worker);

		// new recent message count
		int newRecent = inboxFolder.getMessageFolderInfo().getRecent();

		// update tree information
		MainInterface.treeModel.nodeChanged(inboxFolder);

		// if recent message count changed...
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

	/* (non-Javadoc)
	 * @see org.columba.core.command.Command#updateGUI()
	 */
	public void updateGUI() throws Exception {
		// send update event to table
		TableChangedEvent ev =
			new TableChangedEvent(TableChangedEvent.UPDATE, inboxFolder);
	}

}
