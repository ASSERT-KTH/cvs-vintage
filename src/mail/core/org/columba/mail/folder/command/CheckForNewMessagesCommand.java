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
import org.columba.mail.gui.frame.MailFrameController;
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

        // we only check inbox
		inboxFolder = (IMAPFolder) srcFolder.getChild("Inbox");

        // Find old numbers
		int total  = inboxFolder.getMessageFolderInfo().getExists();
		int recent = inboxFolder.getMessageFolderInfo().getRecent();
        int unseen = inboxFolder.getMessageFolderInfo().getUnseen();

        // check for new headers
		inboxFolder.getHeaderList(worker);

        // Get the new numbers
		int newTotal  = inboxFolder.getMessageFolderInfo().getExists();
		int newRecent = inboxFolder.getMessageFolderInfo().getRecent();
        int newUnseen = inboxFolder.getMessageFolderInfo().getUnseen();

        // ALP 04/29/03
        // Call updageGUI() if anything has changed
		if (newRecent != recent || newTotal != total || newUnseen != unseen){
          updateGUI();
          ImapItem item = srcFolder.getAccountItem().getImapItem();
          if((newRecent != recent) && (item.getBoolean("enable_sound"))){
            // the number of "recent" messages has changed, so play a sound
            // of told to for new messages on server
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

            } //  END else
          } //  END if((newRecent != recent) && (item.getBoolean...
		} //  END if (newRecent != recent || newTotal != total ...
	} //  END public void execute(Worker worker) throws Exception

	/* (non-Javadoc)
	 * @see org.columba.core.command.Command#updateGUI()
	 */
	public void updateGUI() throws Exception {
		// send update event to table
		TableChangedEvent ev =
			new TableChangedEvent(TableChangedEvent.UPDATE, inboxFolder);
        MainInterface.treeModel.nodeChanged(inboxFolder);
        MailFrameController.tableChanged(ev);
	}

}
