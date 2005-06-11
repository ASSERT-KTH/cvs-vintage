//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.gui.message.command;

import java.net.SocketException;

import javax.swing.JOptionPane;

import org.columba.core.command.Command;
import org.columba.core.command.CommandProcessor;
import org.columba.core.command.ICommandReference;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.Worker;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.folder.FolderInconsistentException;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.frame.MessageViewOwner;
import org.columba.mail.gui.frame.TableViewOwner;
import org.columba.mail.gui.frame.ThreePaneMailFrameController;
import org.columba.mail.gui.message.IMessageController;
import org.columba.mail.gui.table.command.ViewHeaderListCommand;
import org.columba.mail.util.MailResourceLoader;
import org.columba.ristretto.message.Flags;
import org.columba.ristretto.message.MimeTree;

/**
 * @author Timo Stich (tstich@users.sourceforge.net)
 *  
 */
public class ViewMessageCommand extends Command {

	private MimeTree mimePartTree;

	private Flags flags;

	private IMailbox srcFolder;

	private Object uid;

	private static final boolean debug = true;

	/**
	 * Constructor for ViewMessageCommand.
	 * 
	 * @param references
	 */
	public ViewMessageCommand(FrameMediator frame, ICommandReference reference) {
		super(frame, reference);

		priority = Command.REALTIME_PRIORITY;
		commandType = Command.NORMAL_OPERATION;
	}

	/**
	 * @see org.columba.core.command.Command#updateGUI()
	 */
	public void updateGUI() throws Exception {

		IMessageController messageController = ((MessageViewOwner) frameMediator)
				.getMessageController();

		// display changes
		messageController.updateGUI();

	}

	/**
	 * @see org.columba.core.command.Command#execute(Worker)
	 */
	public void execute(WorkerStatusController wsc) throws Exception {
		// get command reference
		MailFolderCommandReference r = (MailFolderCommandReference) getReference();

		// get selected folder
		srcFolder = (IMailbox) r.getSourceFolder();
		
		// register for status events
		((StatusObservableImpl) srcFolder.getObservable()).setWorker(wsc);

		// get selected message UID
		uid = r.getUids()[0];
	
		try {
			// get attachment structure
			mimePartTree = srcFolder.getMimePartTree(uid);
			
			if( debug) throw new SocketException();
			
			if (mimePartTree == null)
				return;

			// get flags
			flags = srcFolder.getFlags(uid);
		} catch (FolderInconsistentException ex) {
			Object[] options = new String[] { MailResourceLoader.getString("",
					"global", "ok").replaceAll("&", ""), };
			int result = JOptionPane.showOptionDialog(null, MailResourceLoader
					.getString("dialog", "error", "message_deleted"), "Error",
					JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
					null, options, options[0]);

			CommandProcessor.getInstance().addOp(
					new ViewHeaderListCommand(getFrameMediator(), r));

			return;
		}

		// get messagecontroller of frame
		IMessageController messageController = ((MessageViewOwner) frameMediator)
				.getMessageController();

		messageController.showMessage(srcFolder, uid);

		restartMarkAsReadTimer(flags);
	}

	private void restartMarkAsReadTimer(Flags flags) throws Exception {

		if (flags == null)
			return;

		// if the message it not yet seen
		if (!flags.getSeen() && !srcFolder.isReadOnly()) {
			// restart timer which marks the message as read
			// after a user configurable time interval
			if ( frameMediator instanceof ThreePaneMailFrameController )
			((TableViewOwner) frameMediator).getTableController()
					.restartMarkAsReadTimer(
							(MailFolderCommandReference) getReference());
		}
	}
}