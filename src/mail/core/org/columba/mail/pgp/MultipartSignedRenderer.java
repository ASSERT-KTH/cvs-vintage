package org.columba.mail.pgp;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Vector;

import org.columba.mail.config.PGPItem;
import org.columba.mail.message.PGPMimePart;
import org.columba.ristretto.composer.MimePartRenderer;
import org.columba.ristretto.composer.MimeTreeRenderer;
import org.columba.ristretto.message.InputStreamMimePart;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.StreamableMimePart;

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

public class MultipartSignedRenderer extends MimePartRenderer {

	private MimeHeader signatureHeader;

	public MultipartSignedRenderer() {
		signatureHeader = new MimeHeader("application", "pgp-signature");
	}

	/* (non-Javadoc)
	 * @see org.columba.ristretto.composer.MimePartRenderer#getRegisterString()
	 */
	public String getRegisterString() {
		return "multipart/signed";
	}

	/* (non-Javadoc)
	 * @see org.columba.ristretto.composer.MimePartRenderer#render(org.columba.ristretto.message.StreamableMimePart)
	 */
	public InputStream render(MimePart part) throws IOException {
		Vector streams = new Vector(2 * 2 + 3);

		MimeHeader header = part.getHeader();

		// Create boundary to separate the mime-parts
		String boundary = createUniqueBoundary().toString();
		header.putContentParameter("boundary", boundary);
		byte[] startBoundary = ("\r\n--" + boundary + "\r\n").getBytes();
		byte[] endBoundary = ("\r\n--" + boundary + "--\r\n").getBytes();

		// Add pgp-specific content-parameters
		// we take as default hash-algo SHA1
		header.putContentParameter("micalg", "pgp-sha1");
		header.putContentParameter("protocol", "application/pgp-signature");

		// Create the header and body of the multipart
		streams.add(header.getHeader().getInputStream());
		PGPItem pgpItem = ((PGPMimePart) part).getPgpItem();

		// Add the MimePart that will be signed
		streams.add(new ByteArrayInputStream(startBoundary));
		streams.add(
			MimeTreeRenderer.getInstance().renderMimePart(
				(StreamableMimePart) part.getChild(0)));

		// Add the signature
		streams.add(new ByteArrayInputStream(startBoundary));
		StreamableMimePart signatureMimePart;
		
		PGPController controller = PGPController.getInstance();
		
		signatureMimePart =
			new InputStreamMimePart(
				signatureHeader,
				controller.sign(
					MimeTreeRenderer.getInstance().renderMimePart(
						(StreamableMimePart) part.getChild(0)),
					pgpItem));

		streams.add(
			MimeTreeRenderer.getInstance().renderMimePart(signatureMimePart));

		// Create the closing boundary
		streams.add(new ByteArrayInputStream(endBoundary));

		return new SequenceInputStream(streams.elements());
	}

}
