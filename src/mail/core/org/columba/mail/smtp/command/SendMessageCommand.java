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
import org.columba.mail.gui.util.SendMessageDialog;
import org.columba.mail.main.MailInterface;
import org.columba.mail.pgp.CancelledException;
import org.columba.mail.pgp.PGPException;
import org.columba.mail.smtp.SMTPServer;
import org.columba.mail.util.MailResourceLoader;

import org.columba.ristretto.smtp.SMTPException;

import javax.swing.JOptionPane;


/**
 *
 * This command is started when the user sends the message after creating it in
 * the composer window.
 * <p>
 * After closing the compser window, it will open a little dialog showing
 * the progress of sending the message.
 * <p>
 * If the user cancelles sending, the composer window will be opened again.
 *
 * @author fdietz
 */
public class SendMessageCommand extends FolderCommand {
    private SendMessageDialog sendMessageDialog;
    private boolean showComposer = false;
    private ComposerController composerController;

    /**
     * Constructor for SendMessageCommand.
     *
     * @param frameMediator
     * @param references
     */
    public SendMessageCommand(DefaultCommandReference[] references) {
        super(references);
    }

    /**
     * @see org.columba.core.command.Command#execute(Worker)
     */
    public void execute(Worker worker) throws Exception {
        ComposerCommandReference[] r = (ComposerCommandReference[]) getReferences();

        //	display status message
        worker.setDisplayText(MailResourceLoader.getString("statusbar",
                "message", "send_message_compose"));

        // get composer controller
        // -> get all the account information from the controller
        composerController = r[0].getComposerController();

        // close composer view
        composerController.getView().setVisible(false);

        sendMessageDialog = new SendMessageDialog(worker);

        AccountItem item = ((ComposerModel) composerController.getModel()).getAccountItem();

        // sent folder
        Folder sentFolder = (Folder) MailInterface.treeModel.getFolder(item.getSpecialFoldersItem()
                                                                           .getInteger("sent"));

        // get the SendableMessage object
        SendableMessage message = null;

        try {
            // compose the message suitable for sending
            message = new MessageComposer(((ComposerModel) composerController.getModel())).compose(worker);
        } catch (PGPException e1) {
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
        SMTPServer server = new SMTPServer(item);
        boolean open = server.openConnection();

        // show interest on status information
        ((StatusObservableImpl) server.getObservable()).setWorker(worker);

        if (open) {
            // successfully connected and autenthenticated to SMTP server
            try {
                // display status message
                worker.setDisplayText(MailResourceLoader.getString(
                        "statusbar", "message", "send_message"));

                // send message
                server.sendMessage(message, worker);

                // close composer frame
                composerController.close();

                // save message in Sent folder
                ComposerCommandReference[] ref = new ComposerCommandReference[1];
                ref[0] = new ComposerCommandReference(composerController,
                        sentFolder);
                ref[0].setMessage(message);

                SaveMessageCommand c = new SaveMessageCommand(ref);

                MainInterface.processor.addOp(c);

                //	display status message
                worker.setDisplayText(MailResourceLoader.getString(
                        "statusbar", "message", "send_message_closing"));

                // close connection to server
                server.closeConnection();

                // display status message
                worker.setDisplayText(MailResourceLoader.getString(
                        "statusbar", "message", "send_message_success"));
            } catch (SMTPException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(),
                    "Error while sending", JOptionPane.ERROR_MESSAGE);

                // open composer view
                showComposer = true;
            } catch (Exception e) {
                e.printStackTrace();

                // open composer view
                showComposer = true;
            }
        } else {
            // open == false
            // -> user cancelled sending 
            // open composer view
            showComposer = true;
        }
    }

    public void updateGUI() throws Exception {
        // close send message dialog
        sendMessageDialog.setVisible(false);

        if (showComposer == true) {
            // re-open composer view
            composerController.getView().setVisible(true);
            composerController.getView().requestFocus();
        }
    }
}
