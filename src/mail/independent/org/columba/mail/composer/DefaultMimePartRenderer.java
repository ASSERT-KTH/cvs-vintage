// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

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
