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
import org.columba.core.command.Worker;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.ComposerCommandReference;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.composer.SendableMessage;
import org.columba.mail.config.AccountItem;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.command.AddMessageCommand;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.mail.smtp.SMTPException;
import org.columba.mail.smtp.SMTPServer;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
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

		ComposerController composerController = r[0].getComposerController();

		AccountItem item =
			((ComposerModel) composerController.getModel()).getAccountItem();
		Folder sentFolder =
			(Folder) MainInterface.treeModel.getFolder(
				item.getSpecialFoldersItem().getInteger("sent"));
		SendableMessage message =
			composerController.getMessageComposer().compose(worker);

		SMTPServer server = new SMTPServer(item);
		boolean open = server.openConnection();

		if (open) {

			try {
				server.sendMessage(message, worker);

				composerController.close();

				FolderCommandReference[] ref = new FolderCommandReference[1];
				ref[0] = new FolderCommandReference(sentFolder);
				ref[0].setMessage(message);

				AddMessageCommand c = new AddMessageCommand(ref);

				MainInterface.processor.addOp(c);

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
