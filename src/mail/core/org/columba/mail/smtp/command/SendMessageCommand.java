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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.mail.smtp.command;

import javax.swing.JOptionPane;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.Worker;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.ComposerCommandReference;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.composer.MessageComposer;
import org.columba.mail.composer.SendableMessage;
import org.columba.mail.config.AccountItem;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.mail.gui.composer.command.SaveMessageCommand;
import org.columba.mail.smtp.SMTPServer;
import org.columba.mail.util.MailResourceLoader;
import org.columba.ristretto.smtp.SMTPException;

/**
 * @author fdietz
 *
 * This command is started when the user sends the message
 * after creating it in the composer window.
 * 
 * 
 */
public class SendMessageCommand extends FolderCommand {

	/**
	 * Constructor for SendMessageCommand.
	 * @param frameController
	 * @param references
	 */
	public SendMessageCommand(DefaultCommandReference[] references) {
		super(references);
	}

	/**
	 * @see org.columba.core.command.Command#execute(Worker)
	 */
	public void execute(Worker worker) throws Exception {
		ComposerCommandReference[] r =
			(ComposerCommandReference[]) getReferences();

		//		display status message
		worker.setDisplayText(
			MailResourceLoader.getString(
				"statusbar",
				"message",
				"send_message"));
				
				
		// get composer controller
		// -> get all the account information from the controller 
		ComposerController composerController = r[0].getComposerController();

		AccountItem item =
			((ComposerModel) composerController.getModel()).getAccountItem();

		// sent folder
		Folder sentFolder =
			(Folder) MainInterface.treeModel.getFolder(
				item.getSpecialFoldersItem().getInteger("sent"));

		// get the SendableMessage object
		SendableMessage message =
			new MessageComposer(
				((ComposerModel) composerController.getModel())).compose(
				worker);

		// open connection
		SMTPServer server = new SMTPServer(item);
		boolean open = server.openConnection();
		
		// show interest on status information
		((StatusObservableImpl)server.getObservable()).setWorker(worker);

		if (open) {

			try {
				// send message
				server.sendMessage(message, worker);

				// close composer frame
				composerController.close();

				// save message in Sent folder
				ComposerCommandReference[] ref =
					new ComposerCommandReference[1];
				ref[0] =
					new ComposerCommandReference(
						composerController,
						sentFolder);
				ref[0].setMessage(message);

				SaveMessageCommand c = new SaveMessageCommand(ref);

				MainInterface.processor.addOp(c);

				// close connection to server
				server.closeConnection();
			} catch (SMTPException e) {
				JOptionPane.showMessageDialog(
					null,
					e.getMessage(),
					"Error while sending",
					JOptionPane.ERROR_MESSAGE);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

}
