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

package org.columba.mail.composer;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.columba.core.command.WorkerStatusController;
import org.columba.mail.coder.CoderRouter;
import org.columba.mail.coder.Encoder;
import org.columba.mail.message.ComposerAttachment;
import org.columba.mail.message.MimeHeader;
import org.columba.mail.message.MimePart;

/**
 * @author timo
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DefaultMimePartRenderer extends MimePartRenderer {

	public String getRegisterString() {
		return null;
	}

	/**
	 * @see AbstractMimePartRenderer#render(MimePart)
	 */
	public String render(MimePart part, WorkerStatusController workerStatusController) {
		StringBuffer result = new StringBuffer();
		MimeHeader header = part.getHeader();

		// Determine the Encoder needed				
		Encoder encoder =
			CoderRouter.getEncoder(header.getContentTransferEncoding());

		// First Render Header		
		appendHeader(result, header);

		result.append("\n");

		// If this is a ComposerAttchment it can be a FILE -> use streams to encode

		if (part instanceof ComposerAttachment) {

			ComposerAttachment attachment = (ComposerAttachment) part;
			if (attachment.getType() == ComposerAttachment.FILE) {
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				FileInputStream input;
				
				try {
					input = new FileInputStream( attachment.getFileAttachment() );
					workerStatusController.setProgressBarMaximum((int)(attachment.getFileAttachment().length() / 1024));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return result.toString();
				}

				try {
					encoder.encode( input, output, workerStatusController );					
					result.append( output.toString("US-ASCII"));
				} catch (IOException e) {
					e.printStackTrace();
				}				
			}

		} else {

			try {
				result.append(
					encoder.encode(
						(String) part.getContent(),
						header.getContentParameter("charset")));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		return result.toString();
	}

}
