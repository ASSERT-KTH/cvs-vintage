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
package org.columba.mail.gui.message.filter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.io.StreamUtils;
import org.columba.core.main.MainInterface;
import org.columba.mail.config.PGPItem;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.gui.message.viewer.SecurityInformationController;
import org.columba.mail.main.MailInterface;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.ColumbaMessage;
import org.columba.mail.pgp.JSCFController;
import org.columba.mail.pgp.PGPPassChecker;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.message.BasicHeader;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;
import org.columba.ristretto.message.MimeType;
import org.columba.ristretto.message.io.CharSequenceSource;
import org.columba.ristretto.parser.MessageParser;
import org.columba.ristretto.parser.ParserException;
import org.waffel.jscf.JSCFConnection;
import org.waffel.jscf.JSCFException;
import org.waffel.jscf.JSCFResultSet;
import org.waffel.jscf.JSCFStatement;

/**
 * Filter decrypting and verifying messages.
 * <p>
 * A {@link SecurityStatusEvent}is used to notify all listeners.
 * <p>
 * {@link SecurityInformationController}is currently the only listener. In the
 * future a status icon will be added to the message header, too.
 * <p>
 * 
 * @author fdietz
 *  
 */
public class PGPMessageFilter extends AbstractFilter {

    private static final java.util.logging.Logger LOG = 
      java.util.logging.Logger.getLogger("org.columba.mail.gui.message.filter");

    private ColumbaHeader header;

    private MimeTree mimePartTree;

    private int pgpMode = SecurityInformationController.NOOP;

    // true if we view an encrypted message
    private boolean encryptedMessage = false;

    private String pgpMessage = "";

    private InputStream decryptedStream;

    private ColumbaMessage message;

    private List listeners;

    public PGPMessageFilter(FrameMediator mediator) {
        super(mediator);

        listeners = new ArrayList();
    }

    public void addSecurityStatusListener(SecurityStatusListener l) {
        listeners.add(l);
    }

    public void fireSecurityStatusEvent(SecurityStatusEvent ev) {
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            SecurityStatusListener l = (SecurityStatusListener) it.next();
            l.statusUpdate(ev);
        }
    }

    /**
     * @see org.columba.mail.gui.message.filter.Filter#filter(org.columba.mail.folder.Folder,
     *      java.lang.Object)
     */
    public void filter(MessageFolder folder, Object uid) throws Exception {

        mimePartTree = folder.getMimePartTree(uid);

        header = folder.getMessageHeader(uid);

        // TODO encrypt AND sign dosN#t work. The message is always only
        // encrypted. We need a function that knows, here
        // is an encrypted AND signed Message. Thus first encyrpt and then
        // verifySign the message
        MimeType firstPartMimeType = mimePartTree.getRootMimeNode().getHeader()
                .getMimeType();
        //      if this message is signed/encrypted we have to use
        // GnuPG to extract the decrypted bodypart
        // - multipart/encrypted
        // - multipart/signed
        String contentType = (String) header.get("Content-Type");

        if (firstPartMimeType.getSubtype().equals("signed")) {
            verify(folder, uid);

        } else if (firstPartMimeType.getSubtype().equals("encrypted")) {
            LOG.fine("Mimepart type encrypted found");
            decrypt(folder, uid);

        } else {
            pgpMode = SecurityInformationController.NOOP;
        }

        //notify listeners
        fireSecurityStatusEvent(new SecurityStatusEvent(this, pgpMessage,
                pgpMode));

    }

    /**
     * Decrypt message.
     * 
     * @param folder
     *            selected folder
     * @param uid
     *            selected message UID
     * @throws Exception
     * @throws IOException
     */
    private void decrypt(MessageFolder folder, Object uid) throws Exception,
            IOException {
                
        LOG.fine("start decrypting");
        PGPItem pgpItem = null;
        // we need the pgpItem, to extract the path to gpg
        pgpItem = MailInterface.config.getAccountList().getDefaultAccount()
                .getPGPItem();
         // this is wrong! we need the default id.
        //pgpItem.set("id", new BasicHeader(header.getHeader()).getTo()[0]
        //        .getMailAddress());

        MimePart encryptedMultipart = mimePartTree.getRootMimeNode();

        encryptedMessage = true;

        // the first child must be the control part
        InputStream controlPart = folder.getMimePartBodyStream(uid,
                encryptedMultipart.getChild(0).getAddress());

        // the second child must be the encrypted message
        InputStream encryptedPart = folder.getMimePartBodyStream(uid,
                encryptedMultipart.getChild(1).getAddress());

        InputStream decryptedStream = null;

        try {
            JSCFController controller = JSCFController.getInstance();
            JSCFConnection con = controller.getConnection();
            LOG.fine("new JSCConnection");
            JSCFStatement stmt = con.createStatement();
            LOG.fine("new Statement");
            PGPPassChecker passCheck = PGPPassChecker.getInstance();
            boolean check = passCheck.checkPassphrase(con);
            LOG.fine("after pass check, check is "+check);
            if (!check) {
                pgpMode = SecurityInformationController.DECRYPTION_FAILURE;
                // TODO make i18n!
                pgpMessage = "wrong passphrase";
                return;
            }
            LOG.fine("encrypted is != null?: "+(encryptedPart != null));
            JSCFResultSet res = stmt.executeDecrypt(encryptedPart);
            LOG.fine("after calling decrypting");
            if (res.isError()) {
                LOG.fine("the result set contains errors ");
                pgpMode = SecurityInformationController.DECRYPTION_FAILURE;
                pgpMessage = StreamUtils.readInString(res.getErrorStream())
                        .toString();
                LOG.fine("error message: "+pgpMessage);
                return;
            } else {
                decryptedStream = res.getResultStream();
                pgpMode = SecurityInformationController.DECRYPTION_SUCCESS;
            }
        } catch (JSCFException e) {
            e.printStackTrace();
            LOG.severe(e.getMessage());
            pgpMode = SecurityInformationController.DECRYPTION_FAILURE;
            pgpMessage = e.getMessage();

            // just show the encrypted raw message
            decryptedStream = encryptedPart;
        }
        try {
            // TODO should be removed if we only use Streams!
            String decryptedBodyPart = StreamUtils
                    .readInString(decryptedStream).toString();
            LOG.fine("the decrypted Body part: "+decryptedBodyPart);
            // construct new Message from decrypted string
            message = new ColumbaMessage(MessageParser
                    .parse(new CharSequenceSource(decryptedBodyPart)));

            InputStream messageSourceStream = folder.getMessageSourceStream(uid);
            message.setSource(new CharSequenceSource(StreamUtils
                    .readInString(messageSourceStream)));
            messageSourceStream.close();

            mimePartTree = message.getMimePartTree();

            encryptedMessage = true;

            // call AbstractFilter to do the tricky part
            filter(folder, uid, message);
            //header = (ColumbaHeader) message.getHeaderInterface();
        } catch (ParserException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        }

        controlPart.close();
        encryptedPart.close();
        if (decryptedStream != null) {
            decryptedStream.close();
        }
    }

    /**
     * Verify message.
     * 
     * @param folder
     *            selected folder
     * @param uid
     *            selected message UID
     * @throws Exception
     * @throws IOException
     */
    private void verify(MessageFolder folder, Object uid) throws Exception,
            IOException {
        MimePart signedMultipart = mimePartTree.getRootMimeNode();

        //          the first child must be the signed part
        InputStream signedPart = folder.getMimePartSourceStream(uid,
                signedMultipart.getChild(0).getAddress());

        // the second child must be the pgp-signature
        InputStream signature = folder.getMimePartBodyStream(uid,
                signedMultipart.getChild(1).getAddress());

        // Get the mailaddress and use it as the id
        Address fromAddress = new BasicHeader(header.getHeader()).getFrom();
        try {
            JSCFController controller = JSCFController.getInstance();
            JSCFConnection con = controller.getConnection();
            JSCFStatement stmt = con.createStatement();
            String micalg = signedMultipart.getHeader().getContentParameter(
                    "micalg").substring(4);
            JSCFResultSet res = stmt.executeVerify(signedPart, signature,
                    micalg);
            if (res.isError()) {
                pgpMode = SecurityInformationController.VERIFICATION_FAILURE;
                pgpMessage = StreamUtils.readInString(res.getErrorStream())
                        .toString();
            } else {
                pgpMode = SecurityInformationController.VERIFICATION_SUCCESS;
                pgpMessage = StreamUtils.readInString(res.getResultStream())
                        .toString();
            }

        } catch (JSCFException e) {

        	if ( MainInterface.DEBUG)
        		e.printStackTrace();

        	pgpMode = SecurityInformationController.VERIFICATION_FAILURE;
            pgpMessage = e.getMessage();
            // something really got wrong here -> show error dialog
            //JOptionPane.showMessageDialog(null, e.getMessage());

            pgpMode = SecurityInformationController.VERIFICATION_FAILURE;
        }

        signedPart.close();
        signature.close();
    }

}
