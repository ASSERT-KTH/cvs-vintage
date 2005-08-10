// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
package org.columba.mail.smtp.command;

import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.JOptionPane;

import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusChangeListener;
import org.columba.api.command.IWorkerStatusController;
import org.columba.api.command.WorkerStatusChangedEvent;
import org.columba.core.command.Command;
import org.columba.core.command.CommandProcessor;
import org.columba.core.command.Worker;
import org.columba.mail.command.ComposerCommandReference;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.composer.MessageBuilderHelper;
import org.columba.mail.composer.MessageComposer;
import org.columba.mail.composer.SendableMessage;
import org.columba.mail.config.AccountItem;
import org.columba.mail.folder.AbstractMessageFolder;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.mail.gui.composer.command.SaveMessageCommand;
import org.columba.mail.gui.tree.FolderTreeModel;
import org.columba.mail.gui.util.SendMessageDialog;
import org.columba.mail.pgp.CancelledException;
import org.columba.mail.smtp.SMTPServer;
import org.columba.mail.util.MailResourceLoader;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.message.Flags;
import org.waffel.jscf.JSCFException;

/**
 * 
 * This command is started when the user sends the message after creating it in
 * the composer window.
 * <p>
 * After closing the compser window, it will open a little dialog showing the
 * progress of sending the message.
 * <p>
 * If the user cancelles sending, the composer window will be opened again.
 * 
 * @author fdietz
 */
public class SendMessageCommand extends Command {
	private SendMessageDialog sendMessageDialog;

	private boolean showComposer = false;

	private ComposerController composerController;

	/**
	 * Constructor for SendMessageCommand.
	 * 
	 * @param frameMediator
	 * @param references
	 */
	public SendMessageCommand(ICommandReference reference) {
		super(reference);
	}

	private void showInvalidRecipientMessage(String recipient) {

		String message = MailResourceLoader.getString("dialog", "error",
				recipient);

		String title = MailResourceLoader.getString("dialog", "error",
				"invalid_recipient_title");
		message = MessageFormat.format(message, new Object[] { recipient });
		JOptionPane.showMessageDialog(null, message, title,
				JOptionPane.ERROR_MESSAGE);
	}

	/*
	 * validate command parameters. At the moment only checks if there are any
	 * invalid email addresses
	 *  
	 */
	private boolean validArguments(ComposerCommandReference reference) {

		String invalidRecipient = null;

		//VALIDATION DISABLE ! Sebastian Witt 25.07.04,
		//"NAME" <email@somewhat.de> isnt true, which should :(
		//root@localhost is valid, but not with this check. :(
		//root is also valid (with local mailserver), but not with this check
		// :(

		//TODO: get the validation working CORRECTLY !

		//		for(int i=0;i<references.length;i++)
		//		{
		//		  
		//			invalidRecipient = references[i].getComposerController().getModel()
		//														.getInvalidRecipients();
		//			
		//		  if (invalidRecipient != null)
		//			{
		//
		//		    //it would be really nice to highlight the invalid recipient
		//				showInvalidRecipientMessage(invalidRecipient);
		//				//AFAIK, there's no need to set showComposer to true because
		//				//composer window is already displayed
		//				// open composer view
		//				//showComposer = true;
		//				
		//				return false;
		//
		//			}
		//			
		//		}
		//		
		return true;

	}

	/**
	 * @see org.columba.api.command.Command#execute(Worker)
	 */
	public void execute(IWorkerStatusController worker) throws Exception {

		ComposerCommandReference r = (ComposerCommandReference) getReference();

		if (!validArguments(r))
			return;

		//	display status message
		worker.setDisplayText(MailResourceLoader.getString("statusbar",
				"message", "send_message_compose"));

		// get composer controller
		// -> get all the account information from the controller
		composerController = r.getComposerController();

		// close composer view
		if (composerController.getView().getFrame() != null) {
			composerController.getView().getFrame().setVisible(false);
		}

		sendMessageDialog = new SendMessageDialog(worker);

		ComposerModel model = ((ComposerModel) composerController.getModel());

		AccountItem item = model.getAccountItem();

		// sent folder
		AbstractMessageFolder sentFolder = (AbstractMessageFolder) FolderTreeModel.getInstance()
				.getFolder(item.getSpecialFoldersItem().getInteger("sent"));

		// get the SendableMessage object
		SendableMessage message = null;

		try {
			// compose the message suitable for sending
			message = new MessageComposer(model).compose(worker, r.isAppendSignature());

		} catch (JSCFException e1) {
			if (e1 instanceof CancelledException) {
				// user cancelled sending operation
				// open composer view
				showComposer = true;

				return;
			} else {
				JOptionPane.showMessageDialog(null, e1.getMessage());

				//	open composer view
				showComposer = true;

				return;
			}
		}

		// display status message
		worker.setDisplayText(MailResourceLoader.getString("statusbar",
				"message", "send_message_connect"));

		// open connection
		final SMTPServer server = new SMTPServer(item);


		// successfully connected and autenthenticated to SMTP server
		try {
			// display status message
			worker.setDisplayText(MailResourceLoader.getString("statusbar",
					"message", "send_message"));

			IWorkerStatusChangeListener listener = new IWorkerStatusChangeListener() {
				public void workerStatusChanged(WorkerStatusChangedEvent e) {
					if( e.getSource().cancelled() ) {
						try {
							server.dropConnection();
						} catch (IOException e1) {							
						}
					}
					
				}				
			};
			
			// important for cancel
			worker.addWorkerStatusChangeListener(listener);
			
			// send message
			server.sendMessage(message, worker);
			
			// not needed anymore
			worker.removeWorkerStatusChangeListener(listener);
			
			if( worker.cancelled() ) {
				showComposer = true;
				return;
			}
			
			// mark as read
			Flags flags = new Flags();
			flags.setSeen(true);
			message.getHeader().setFlags(flags);

			// save message in Sent folder
			ComposerCommandReference ref = new ComposerCommandReference(
					composerController, sentFolder);
			ref.setMessage(message);

			SaveMessageCommand c = new SaveMessageCommand(ref);

			CommandProcessor.getInstance().addOp(c);

			// -> get source reference of message
			// when replying this is the original sender's message
			// you selected and replied to
			MailFolderCommandReference ref2 = model.getSourceReference();
			if (ref2 != null && ((IMailbox)ref2.getSourceFolder()).exists(ref2.getUids()[0])) {
				// mark message as answered
				ref2.setMarkVariant(MarkMessageCommand.MARK_AS_ANSWERED);
				MarkMessageCommand c1 = new MarkMessageCommand(ref2);
				CommandProcessor.getInstance().addOp(c1);
			}

			//	display status message
			worker.setDisplayText(MailResourceLoader.getString("statusbar",
					"message", "send_message_closing"));

			// close connection to server
			server.closeConnection();

			// display status message
			worker.setDisplayText(MailResourceLoader.getString("statusbar",
					"message", "send_message_success"));
		} /*
		   * catch (SMTPException e) { JOptionPane.showMessageDialog(null,
		   * e.getMessage(), "Error while sending", JOptionPane.ERROR_MESSAGE); //
		   * open composer view showComposer = true; }
		   */catch (Exception e) {
			//e.printStackTrace();

			// open composer view
			showComposer = true;

			throw e;
		}
	}

	public void updateGUI() throws Exception {

		//can no longer assume that sendMessageDialog has been displayed
		if (sendMessageDialog != null) {
			// close send message dialog
			sendMessageDialog.setVisible(false);
		}

		if (showComposer == true
				&& composerController.getView().getFrame() != null) {
			// re-open composer view
			composerController.getView().getFrame().setVisible(true);
			composerController.getView().getFrame().requestFocus();
		} else {
			// do not prompt user if composer should be really closed
			composerController.setPromptOnDialogClosing(false);
			// save composer window state
			composerController.getContainer().close();
		}
	}
}