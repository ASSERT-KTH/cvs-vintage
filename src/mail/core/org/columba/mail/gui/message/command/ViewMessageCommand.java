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
package org.columba.mail.gui.message.command;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.columba.core.command.Command;
import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.io.StreamUtils;
import org.columba.core.main.MainInterface;
import org.columba.core.xml.XmlElement;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.config.PGPItem;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.FolderInconsistentException;
import org.columba.mail.folder.temp.TempFolder;
import org.columba.mail.gui.attachment.AttachmentSelectionHandler;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.frame.ThreePaneMailFrameController;
import org.columba.mail.gui.message.MessageController;
import org.columba.mail.gui.message.SecurityIndicator;
import org.columba.mail.gui.table.command.ViewHeaderListCommand;
import org.columba.mail.gui.table.selection.TableSelectionHandler;
import org.columba.mail.main.MailInterface;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.ColumbaMessage;
import org.columba.mail.pgp.JSCFController;
import org.columba.mail.pgp.PGPPassChecker;
import org.columba.mail.util.MailResourceLoader;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.message.BasicHeader;
import org.columba.ristretto.message.LocalMimePart;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;
import org.columba.ristretto.message.MimeType;
import org.columba.ristretto.message.StreamableMimePart;
import org.columba.ristretto.message.io.CharSequenceSource;
import org.columba.ristretto.parser.MessageParser;
import org.columba.ristretto.parser.ParserException;
import org.waffel.jscf.JSCFConnection;
import org.waffel.jscf.JSCFException;
import org.waffel.jscf.JSCFResultSet;
import org.waffel.jscf.JSCFStatement;

/**
 * @author Timo Stich (tstich@users.sourceforge.net)
 *
 */
public class ViewMessageCommand extends FolderCommand {

    /** JDK 1.4+ logging framework logger, used for logging. */
    private static final Logger LOG = Logger.getLogger("org.columba.mail.gui.message.command");

    private StreamableMimePart bodyPart;

    private MimeTree mimePartTree;

    private ColumbaHeader header;

    private Folder srcFolder;

    private Object uid;

    private Object[] uids;

    private String pgpMessage = "";

    private int pgpMode = SecurityIndicator.NOOP;

    // true if we view an encrypted message
    private boolean encryptedMessage = false;

    private InputStream decryptedStream;

    /**
     * Constructor for ViewMessageCommand.
     *
     * @param references
     */
    public ViewMessageCommand(FrameMediator frame, DefaultCommandReference[] references) {
        super(frame, references);

        //priority = Command.REALTIME_PRIORITY;
        commandType = Command.NORMAL_OPERATION;
    }

    protected void decryptEncryptedPart(PGPItem pgpItem, MimePart encryptedMultipart) throws Exception {
        encryptedMessage = true;

        // the first child must be the control part
        InputStream controlPart = srcFolder.getMimePartBodyStream(uid, encryptedMultipart.getChild(0).getAddress());

        // the second child must be the encrypted message
        InputStream encryptedPart = srcFolder.getMimePartBodyStream(uid, encryptedMultipart.getChild(1).getAddress());

        InputStream decryptedStream = null;

        try {
            /*
             * JSCFDriverManager.registerJSCFDriver(new GPGDriver()); JSCFConnection con =
             * JSCFDriverManager.getConnection("jscf:gpg:"+(String)pgpItem.get("path"));
             */
            JSCFController controller = JSCFController.getInstance();
            JSCFConnection con = controller.getConnection();
            //con.getProperties().put("USERID", pgpItem.get("id"));
            JSCFStatement stmt = con.createStatement();
            PGPPassChecker passCheck = PGPPassChecker.getInstance();
            boolean check = passCheck.checkPassphrase(con);
            if (!check) {
                pgpMode = SecurityIndicator.DECRYPTION_FAILURE;
                // TODO make i18n!
                pgpMessage = "wrong passphrase";
                return;
            }
            JSCFResultSet res = stmt.executeDecrypt(encryptedPart);
            if (res.isError()) {
                pgpMode = SecurityIndicator.DECRYPTION_FAILURE;
                pgpMessage = StreamUtils.readInString(res.getErrorStream()).toString();
                return;
            } else {
                decryptedStream = res.getResultStream();
                pgpMode = SecurityIndicator.DECRYPTION_SUCCESS;
            }
        } catch (JSCFException e) {
            e.printStackTrace();

            pgpMode = SecurityIndicator.DECRYPTION_FAILURE;
            pgpMessage = e.getMessage();

            // just show the encrypted raw message
            decryptedStream = encryptedPart;
        }
        try {
            // TODO should be removed if we only use Streams!
            String decryptedBodyPart = StreamUtils.readInString(decryptedStream).toString();

            // construct new Message from decrypted string
            ColumbaMessage message;

            message = new ColumbaMessage(MessageParser.parse(new CharSequenceSource(decryptedBodyPart)));
            message.setSource(new CharSequenceSource(StreamUtils.readInString(srcFolder.getMessageSourceStream(uid))));

            mimePartTree = message.getMimePartTree();

            //map selection to this temporary message
            TempFolder tempFolder = MailInterface.treeModel.getTempFolder();

            // add message to temporary folder
            Object uid = tempFolder.addMessage(message);

            // create reference to this message
            FolderCommandReference[] local = new FolderCommandReference[1];
            local[0] = new FolderCommandReference(tempFolder, new Object[] {uid});

            // if we don't use this here - actions like reply would only work on the
            // the encrypted message
            TableSelectionHandler h1 = ((TableSelectionHandler) frameMediator.getSelectionManager().getHandler("mail.table"));

            h1.setLocalReference(local);

            // this is needed to be able to open attachments of the decrypted message
            AttachmentSelectionHandler h = ((AttachmentSelectionHandler) frameMediator.getSelectionManager().getHandler("mail.attachment"));
            h.setLocalReference(local);

            encryptedMessage = true;

            //header = (ColumbaHeader) message.getHeaderInterface();
        } catch (ParserException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    protected void verifySignedPart(MimePart signedMultipart) throws Exception {

        // the first child must be the signed part
        InputStream signedPart = srcFolder.getMimePartSourceStream(uid, signedMultipart.getChild(0).getAddress());

        // the second child must be the pgp-signature
        InputStream signature = srcFolder.getMimePartBodyStream(uid, signedMultipart.getChild(1).getAddress());

        // Get the mailaddress and use it as the id
        Address fromAddress = new BasicHeader(header.getHeader()).getFrom();
        /*
         * PGPItem pgpItem =null; // we need the pgpItem, to extract the path to gpg pgpItem =
         * MailInterface.config.getAccountList().getDefaultAccount().getPGPItem();
         */
        try {
            /*
             * // TODO this should be only once, after starting columba! JSCFDriverManager.registerJSCFDriver(new GPGDriver()); JSCFConnection con =
             * JSCFDriverManager.getConnection("jscf:gpg:"+(String)pgpItem.get("path"));
             */
            JSCFController controller = JSCFController.getInstance();
            JSCFConnection con = controller.getConnection();
            JSCFStatement stmt = con.createStatement();
            String micalg = signedMultipart.getHeader().getContentParameter("micalg").substring(4);
            JSCFResultSet res = stmt.executeVerify(signedPart, signature, micalg);
            if (res.isError()) {
                pgpMode = SecurityIndicator.VERIFICATION_FAILURE;
                pgpMessage = StreamUtils.readInString(res.getErrorStream()).toString();
            } else {
                pgpMode = SecurityIndicator.VERIFICATION_SUCCESS;
                pgpMessage = StreamUtils.readInString(res.getResultStream()).toString();
            }
        } catch (JSCFException e) {

            e.printStackTrace();

            // something really got wrong here -> show error dialog
            JOptionPane.showMessageDialog(null, e.getMessage());

            pgpMode = SecurityIndicator.VERIFICATION_FAILURE;
        }

    }

    /**
     * @see org.columba.core.command.Command#updateGUI()
     */
    public void updateGUI() throws Exception {

        AttachmentSelectionHandler h = ((AttachmentSelectionHandler) frameMediator.getSelectionManager().getHandler("mail.attachment"));

        if (!encryptedMessage) {
            // show headerfields
            if (h != null) {
                h.setMessage(srcFolder, uid);
            }
        }

        MessageController messageController = ((AbstractMailFrameController) frameMediator).messageController;
        if (header != null && bodyPart != null) {
            // update pgp security indicator
            messageController.setPGPMessage(pgpMode, pgpMessage);
            // show message in gui component
            messageController.showMessage(header, bodyPart, mimePartTree);
            // security check, i dont know if we need this (waffel)
            if (frameMediator instanceof ThreePaneMailFrameController) {
                // if the message it not yet seen
                if (!((ColumbaHeader) header).getFlags().getSeen()) {
                    // restart timer which marks the message as read
                    // after a user configurable time interval
                    ((ThreePaneMailFrameController) frameMediator).getTableController().getMarkAsReadTimer().restart(
                            (FolderCommandReference) getReferences()[0]);
                }
            }
        }
    }

    /**
     * @see org.columba.core.command.Command#execute(Worker)
     */
    public void execute(WorkerStatusController wsc) throws Exception {
        FolderCommandReference[] r = (FolderCommandReference[]) getReferences();
        srcFolder = (Folder) r[0].getFolder();
        //register for status events
        ((StatusObservableImpl) srcFolder.getObservable()).setWorker(wsc);
        uid = r[0].getUids()[0];
        bodyPart = null;
        // get attachment structure
        try {
            mimePartTree = srcFolder.getMimePartTree(uid);
        } catch (FolderInconsistentException ex) {
            Object[] options = new String[] {MailResourceLoader.getString("", "global", "ok").replaceAll("&", ""),};
            int result = JOptionPane.showOptionDialog(null, MailResourceLoader.getString("dialog", "error", "message_deleted"), "Error",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);

            MainInterface.processor.addOp(new ViewHeaderListCommand(getFrameMediator(), r));
            return;
        } //get RFC822-header
        header = srcFolder.getMessageHeader(uid);
        // if this message is signed/encrypted we have to use
        // GnuPG to extract the decrypted bodypart
        //
        // we basically replace the Message object we
        // just got from Folder with the decrypted
        // Message object, this includes header, bodyPart,
        // and mimePartTree
        // interesting for the PGP stuff are:
        // - multipart/encrypted
        // - multipart/signed

        // TODO encrypt AND sign dosN#t work. The message is always only encrypted. We need a function that knows, here
        // is an encrypted AND signed Message. Thus first encyrpt and then verifySign the message
        MimeType firstPartMimeType = mimePartTree.getRootMimeNode().getHeader().getMimeType();
        String contentType = (String) header.get("Content-Type");
        LOG.info("contentType=" + contentType);

        if (firstPartMimeType.getSubtype().equals("signed")) {
            verifySignedPart(mimePartTree.getRootMimeNode());
        }

        if (firstPartMimeType.getSubtype().equals("encrypted")) {

            PGPItem pgpItem = null;
            // we need the pgpItem, to extract the path to gpg
            pgpItem = MailInterface.config.getAccountList().getDefaultAccount().getPGPItem();
            pgpItem.set("id", new BasicHeader(header.getHeader()).getTo()[0].getMailAddress());
            decryptEncryptedPart(pgpItem, mimePartTree.getRootMimeNode());
        }

        if (mimePartTree != null) { // user prefers html/text messages
            XmlElement html = MailInterface.config.getMainFrameOptionsConfig().getRoot().getElement("/options/html");
            // Which Bodypart shall be shown? (html/plain)
            if (Boolean.valueOf(html.getAttribute("prefer")).booleanValue()) {
                bodyPart = (StreamableMimePart) mimePartTree.getFirstTextPart("html");
            } else {
                bodyPart = (StreamableMimePart) mimePartTree.getFirstTextPart("plain");
            }
            if (bodyPart == null) {
                bodyPart = new LocalMimePart(new MimeHeader());
                ((LocalMimePart) bodyPart).setBody(new CharSequenceSource("<No Message-Text>"));
            } else if (encryptedMessage) {
                // meaning, bodyPart already contains the correct
                // message bodytext

            } else {

                bodyPart = (StreamableMimePart) srcFolder.getMimePart(uid, bodyPart.getAddress());
            }
        }
    }
}
