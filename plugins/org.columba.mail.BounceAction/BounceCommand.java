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

import java.io.InputStream;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.composer.MessageComposer;
import org.columba.mail.composer.SendableMessage;
import org.columba.mail.config.AccountItem;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.folder.command.AddMessageCommand;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.mail.main.MailInterface;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.ColumbaMessage;
import org.columba.mail.smtp.SMTPServer;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.message.MimeTree;
import org.columba.ristretto.message.io.ByteBufferSource;
import org.columba.ristretto.smtp.SMTPException;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class BounceCommand extends FolderCommand {
    /**
     * @param frame
     * @param references
     */
    public BounceCommand(AbstractFrameController frame,
        DefaultCommandReference[] references) {
        super(frame, references);
    }

    public BounceCommand(DefaultCommandReference[] references) {
        super(references);
    }

    /* (non-Javadoc)
     * @see org.columba.core.command.Command#execute(org.columba.core.command.Worker)
     */
    public void execute(WorkerStatusController worker) throws Exception {
        MessageFolder folder = (MessageFolder)
                ((FolderCommandReference) getReferences()[0]).getFolder();
        Object[] uids = ((FolderCommandReference) getReferences()[0]).getUids();

        // create new message
        ColumbaMessage message = new ColumbaMessage();

        // copy header of message we want to bounce
        ColumbaHeader header = (ColumbaHeader) folder.getMessageHeader(uids[0]);
        message.setHeader(header);

        // copy mimeparts of bounce message
        MimeTree mimePartTree = folder.getMimePartTree(uids[0]);
        message.setMimePartTree(mimePartTree);

        // copy message-source of bounce message
        InputStream sourceStream = folder.getMessageSourceStream(uids[0]);
        // TODO: how do I create a Source from an InputStream?
        //message.setSource();

        // create composer-model
        // this encapsulates the data we need to
        // create a new message
        // and keeps the gui separated from the data
        ComposerModel model = new ComposerModel();

        // use bounce message to pass the values
        // to the model
        bounceMessage(message, model);

        // create new message from model
        SendableMessage sendableMessage = new MessageComposer(model).compose(worker);

        // get user-configurable Sent-Folder
        AccountItem item = model.getAccountItem();
        MessageFolder sentFolder = (MessageFolder) 
                MailInterface.treeModel.getFolder(
                        item.getSpecialFoldersItem().getInteger("sent"));

        // the following code should be better put somewhere else
        // because it could be shared with SendMessageCommand
        // open connection to smtp-server
        SMTPServer server = new SMTPServer(item);
        boolean open = server.openConnection();

        if (open) {
            try {
                // send message
                server.sendMessage(sendableMessage, worker);

                FolderCommandReference[] ref = new FolderCommandReference[1];
                ref[0] = new FolderCommandReference(sentFolder);
                ref[0].setMessage(sendableMessage);

                // add message to Sent-Folder
                AddMessageCommand c = new AddMessageCommand(ref);

                MainInterface.processor.addOp(c);

                server.closeConnection();
            } catch (SMTPException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(),
                    "Error while sending", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
             *
             * Fill the <code>ComposerModel</code> with headerfields,
             * bodytext and mimeparts.
             *
             * This is a special method for the "Bounce Message"
             * action.
             *
             * @param message   The <code>Message</code> we want to edit
             *                  as new message.
             *
             * @param model     The <code>ComposerModel</code> we want to
             *                  pass the information to.
             *
             */
    public static void bounceMessage(ColumbaMessage message, ComposerModel model) {
        //ColumbaHeader header = (ColumbaHeader) message.getHeader();
        model.setHeader((Header) message.getHeader().clone());

        // copy every headerfield the original message contains
        /*
        Hashtable hashtable = header.getHashtable();
        for (Enumeration e = hashtable.keys(); e.hasMoreElements();) {
                Object key = e.nextElement();

                try {
                        model.setHeaderField(
                                (String) key,
                                (String) header.get((String) key));
                } catch (ClassCastException ex) {
                        System.out.println("skipping header item");
                }

        }
        */
        model.setSubject("Undelivered Mail Returned to Sender");

        String host = "";
        String sender = (String) message.getHeader().get("Reply-To");

        if (sender == null) {
            sender = (String) message.getHeader().get("From");
        }

        System.out.println("[MESSAGE!!!] sender:" + sender);

        // break the sender string to recive the hostname
        // we break on @
        StringTokenizer strToken = new StringTokenizer(sender, "@");

        // the second token is it
        if (strToken.countTokens() == 2) {
            // get the first token (before @) and lett it fall
            strToken.nextToken();

            // get the nextToken (after @) and remember
            host = strToken.nextToken();
            System.out.println("[DEBUG!!!!] host: " + host);

            // here we can start parsing all valid characters for an hostname
            // RFC0288 describes, that we can until we have found an > character, so let use do this
            strToken = new StringTokenizer(host, ">");

            if (strToken.countTokens() > 0) {
                host = strToken.nextToken();
                System.out.println("[DEBUG!!!!] host: " + host);
            } else {
                // there is an Bug and at this time we send this to the commandline
                System.out.println(
                    "BUG found: ComposerWorker.Operation.COMPOSER_BOUNCE number 1");
            }
        } else {
            // there is an Bug and at this time we send this to the commandline
            System.out.println(
                "BUG found: ComposerWorker.Operation.COMPOSER_BOUNCE number 2");
        }

        System.out.println("[DEBUG!!!!!] host: " + host);

        // setting the from field (this is one of the real hack for bounce)
        model.setHeaderField("From", "MAILER-DAEMON@" + host);

        model.setTo(sender);

        model.setHeaderField("References", "");

        // empty bodytext is not allowed
        model.setBodyText("<no bodytext>");
    }
}
