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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.columba.core.command.Command;
import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.Worker;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.io.StreamUtils;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.xml.XmlElement;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.config.MailConfig;
import org.columba.mail.config.PGPItem;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.attachment.AttachmentSelectionHandler;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.frame.ThreePaneMailFrameController;
import org.columba.mail.gui.message.SecurityIndicator;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.ColumbaMessage;
import org.columba.mail.pgp.PGPController;
import org.columba.ristretto.message.LocalMimePart;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;
import org.columba.ristretto.message.MimeType;
import org.columba.ristretto.message.StreamableMimePart;
import org.columba.ristretto.message.io.CharSequenceSource;
import org.columba.ristretto.parser.MessageParser;
import org.columba.ristretto.parser.ParserException;

/**
 * @author Timo Stich (tstich@users.sourceforge.net)
 *
 */
public class ViewMessageCommand extends FolderCommand {

	StreamableMimePart bodyPart;
	MimeTree mimePartTree;
	ColumbaHeader header;
	Folder srcFolder;
	Object uid;
	Object[] uids;

	String pgpMessage;
	int pgpMode = SecurityIndicator.NOOP;

	// true if we view an encrypted message
	boolean encryptedMessage = false;

	/**
	 * Constructor for ViewMessageCommand.
	 * @param references
	 */
	public ViewMessageCommand(
		AbstractFrameController frame,
		DefaultCommandReference[] references) {
		super(frame, references);

		//priority = Command.REALTIME_PRIORITY;
		commandType = Command.NORMAL_OPERATION;
	}

	protected void decryptEncryptedPart( MimePart encryptedMultipart ) throws Exception {
		encryptedMessage = true;
		
		// the first child must be the control part
		InputStream controlPart =
			srcFolder.getMimePartBodyStream(
				uid,
				encryptedMultipart.getChild(0).getAddress());

		// the second child must be the encrypted message
		InputStream encryptedPart =
			srcFolder.getMimePartBodyStream(
				uid,
				encryptedMultipart.getChild(1).getAddress());
				
		// get PGPItem, use To-headerfield and search through
		// all accounts to find a matching PGP id
		String to = (String) header.get("To");
		PGPItem pgpItem = MailConfig.getAccountList().getPGPItem(to);
				
		// decrypt string
		// getting controller Instance
		PGPController controller = PGPController.getInstance();
		// creating Stream for encrypted Body part and decrypt it
		InputStream decryptedStream =
			controller.decrypt( encryptedPart,
				pgpItem);
		try {
			// TODO should be removed if we only use Streams!
			String decryptedBodyPart =
				StreamUtils.readInString(decryptedStream).toString();
			ColumbaLogger.log.debug(decryptedBodyPart);
			//String decryptedBodyPart = PGPController.getInstance().decrypt(encryptedBodyPart, pgpItem);

			// construct new Message from decrypted string
			ColumbaMessage message;

			message =
				new ColumbaMessage(
					MessageParser.parse(
						new CharSequenceSource(decryptedBodyPart)));
			mimePartTree = message.getMimePartTree();

			//header = (ColumbaHeader) message.getHeaderInterface();
		} catch (ParserException e) {
			e.printStackTrace();
			pgpMode = SecurityIndicator.DECRYPTION_FAILURE;
		} catch (IOException e) {
			e.printStackTrace();

			pgpMode = SecurityIndicator.DECRYPTION_FAILURE;
		}
	}

	protected void verifySignedPart(MimePart signedMultipart)
		throws Exception {

		// the first child must be the signed part
		InputStream signedPart =
			srcFolder.getMimePartSourceStream(
				uid,
				signedMultipart.getChild(0).getAddress());

		// the second child must be the pgp-signature
		InputStream signature =
			srcFolder.getMimePartBodyStream(
				uid,
				signedMultipart.getChild(1).getAddress());

		// get PGPItem, use To-headerfield and search through
		// all accounts to find a matching PGP id
		String to = (String) header.get("To");
		PGPItem pgpItem = MailConfig.getAccountList().getPGPItem(to);

		// getting controller Instance
		PGPController controller = PGPController.getInstance();
		// verify
		boolean ok = controller.verifySignature(signedPart, signature, pgpItem);
		if (ok) {
			pgpMode = SecurityIndicator.VERIFICATION_SUCCESS;
			ColumbaLogger.log.error(controller.getPGPResultStream());
		} else {
			pgpMode = SecurityIndicator.VERIFICATION_FAILURE;
			ColumbaLogger.log.debug(controller.getPGPErrorStream());
		}
	}

	/**
	 * @see org.columba.core.command.Command#updateGUI()
	 */
	public void updateGUI() throws Exception {

		AttachmentSelectionHandler h =
			((AttachmentSelectionHandler) frameController
				.getSelectionManager()
				.getHandler("mail.attachment"));

		// show headerfields
		if (h != null)
			h.setMessage(srcFolder, uid);

		if (header != null && bodyPart != null) {
			if (pgpMode != SecurityIndicator.NOOP) {
				// update pgp security indicator
				(
					(
						AbstractMailFrameController) frameController)
							.messageController
							.setPGPMessage(
					pgpMode,
					pgpMessage);
			} else {
				// TODO: Hide SecurityIndicator
			}

			// show message in gui component
			(
				(
					AbstractMailFrameController) frameController)
						.messageController
						.showMessage(
				header,
				bodyPart,
				mimePartTree);

			// security check, i dont know if we need this (waffel)
			if (frameController instanceof ThreePaneMailFrameController) {
				// if the message it not yet seen
				if (!((ColumbaHeader) header).getFlags().getSeen()) {
					// restart timer which marks the message as read
					// after a user configurable time interval
					((ThreePaneMailFrameController) frameController)
						.getTableController()
						.getMarkAsReadTimer()
						.restart((FolderCommandReference) getReferences()[0]);
				}
			}
		}
	}

	/**
	 * @see org.columba.core.command.Command#execute(Worker)
	 */
	public void execute(Worker wsc) throws Exception {
		FolderCommandReference[] r = (FolderCommandReference[]) getReferences();
		srcFolder = (Folder) r[0].getFolder();
		//		register for status events
		 ((StatusObservableImpl) srcFolder.getObservable()).setWorker(wsc);

		uid = r[0].getUids()[0];

		bodyPart = null;

		// get attachment structure
		try {
			mimePartTree = srcFolder.getMimePartTree(uid);
		} catch (FileNotFoundException ex) {
			// message doesn't exist anymore
			return;
		}

		//	get RFC822-header
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
		MimeType firstPartMimeType =
			mimePartTree.getRootMimeNode().getHeader().getMimeType();

		String contentType = (String) header.get("Content-Type");
		ColumbaLogger.log.debug("contentType=" + contentType);

		if (firstPartMimeType.getSubtype().equals("signed")) {
			verifySignedPart(mimePartTree.getRootMimeNode());
		}

		if (firstPartMimeType.getSubtype().equals("encrypted")) {
			decryptEncryptedPart(mimePartTree.getRootMimeNode());
		}
		/*	
			if (((contentType.equals("multipart/encrypted"))
				|| (contentType.equals("multipart/signature"))))
				handlePGPMessage(header, wsc);
		*/
		if (mimePartTree != null) {

			// user prefers html/text messages
			XmlElement html =
				MailConfig.getMainFrameOptionsConfig().getRoot().getElement(
					"/options/html");
			boolean viewhtml =
				new Boolean(html.getAttribute("prefer")).booleanValue();

			// Which Bodypart shall be shown? (html/plain)

			if (viewhtml)
				bodyPart =
					(StreamableMimePart) mimePartTree.getFirstTextPart("html");
			else
				bodyPart =
					(StreamableMimePart) mimePartTree.getFirstTextPart("plain");

			if (bodyPart == null) {
				bodyPart = new LocalMimePart(new MimeHeader());
				((LocalMimePart) bodyPart).setBody(
					new CharSequenceSource("<No Message-Text>"));
			} else if (encryptedMessage == true) {

				// meaning, bodyPart already contains the correct
				// message bodytext

			} else {

				bodyPart =
					(StreamableMimePart) srcFolder.getMimePart(
						uid,
						bodyPart.getAddress());
			}
		}
	}

}
