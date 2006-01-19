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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusController;
import org.columba.core.command.Command;
import org.columba.core.command.ProgressObservedInputStream;
import org.columba.core.command.Worker;
import org.columba.core.io.StreamUtils;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.folder.IMailbox;
import org.columba.ristretto.coder.Base64DecoderInputStream;
import org.columba.ristretto.coder.EncodedWord;
import org.columba.ristretto.coder.QuotedPrintableDecoderInputStream;
import org.columba.ristretto.message.MimeHeader;

/**
 * @author freddy
 */
public abstract class SaveAttachmentCommand extends Command {
	protected static File lastDir = null;

	private static final Logger LOG = Logger
			.getLogger("org.columba.mail.gui.message.attachment.command");

	/**
	 * Constructor for SaveAttachmentCommand.
	 * 
	 * @param reference
	 *            the reference for the command.
	 */
	public SaveAttachmentCommand(ICommandReference reference) {
		super(reference);
	}

	/**
	 * @see org.columba.api.command.Command#execute(Worker)
	 */
	public void execute(IWorkerStatusController worker) throws Exception {
		IMailFolderCommandReference r = (IMailFolderCommandReference) getReference();
		IMailbox folder = (IMailbox) r.getSourceFolder();
		Object[] uids = r.getUids();

		Integer[] address = r.getAddress();

		MimeHeader header = folder.getMimePartTree(uids[0]).getFromAddress(
				address).getHeader();

		InputStream bodyStream = folder.getMimePartBodyStream(uids[0], address);

		// wrap with observable stream for progress bar updates
		bodyStream = new ProgressObservedInputStream(bodyStream, worker);

		File destFile = getDestinationFile(header);
		if ( destFile == null ) return;
		
		worker.setDisplayText("Saving " + destFile.getName());

		// write to temporary file
		File tempFile = new File(destFile.getAbsoluteFile() + ".part");

		if (tempFile == null)
			return;

		int encoding = header.getContentTransferEncoding();

		switch (encoding) {
		case MimeHeader.QUOTED_PRINTABLE:
			bodyStream = new QuotedPrintableDecoderInputStream(bodyStream);
			break;

		case MimeHeader.BASE64:
			bodyStream = new Base64DecoderInputStream(bodyStream);
			break;
		default:
		}

		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Storing the attachment to :" + tempFile);
		}

		FileOutputStream fileStream = new FileOutputStream(tempFile);
		StreamUtils.streamCopy(bodyStream, fileStream);
		fileStream.close();
		bodyStream.close();

		// rename "*.part" file to destination file
		tempFile.renameTo(destFile);

		// reset progress bar
		worker.setProgressBarValue(0);

		// We are done - clear the status message with a delay
		worker.clearDisplayTextWithDelay();
	}

	/**
	 * Returns the filename of the attachment.
	 * 
	 * @param mimepart
	 *            the mime part containing the attachment.
	 * @return the filename for the attachment.
	 */
	protected String getFilename(MimeHeader header) {
		String fileName = header.getContentParameter("name");

		if (fileName == null) {
			fileName = header.getDispositionParameter("filename");
		}

		// decode filename
		if (fileName != null) {
			StringBuffer buf = EncodedWord.decode(fileName);
			fileName = buf.toString();
		}
		return fileName;
	}

	/**
	 * Returns the destination file for the attachment.
	 * 
	 * @param mimepart
	 *            the mime part containing the attachment.
	 * @return a File path; null if the saving should be cancelled.
	 */
	protected abstract File getDestinationFile(MimeHeader header);
}