package org.columba.mail.composer.mimepartrenderers;

import org.columba.mail.coder.CoderRouter;
import org.columba.mail.coder.Encoder;
import org.columba.mail.composer.MimePartRenderer;
import org.columba.mail.composer.MimeTreeRenderer;
import org.columba.mail.message.MimeHeader;
import org.columba.mail.message.MimePart;
import org.columba.mail.message.PgpMimePart;
import org.columba.mail.pgp.PGPController;

/**
 * @author timo
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MultipartSignedRenderer extends MimePartRenderer {

	/**
	 * @see org.columba.modules.mail.composer.MimePartRenderer#getRegisterString()
	 */
	public String getRegisterString() {
		return "multipart/signed";
	}

	/**
	 * @see org.columba.modules.mail.composer.MimePartRenderer#render(MimePart)
	 */
	public String render(MimePart part) {

		StringBuffer result = new StringBuffer();
		MimeHeader header = part.getHeader();
		String boundary = createUniqueBoundary();
		PGPController controller = PGPController.getInstance();

		// Store boundary in header
		header.putContentParameter("boundary", boundary);

		// Determine the Encoder needed				
		Encoder encoder =
			CoderRouter.getEncoder(header.getContentTransferEncoding());

		// First Render Header		
		appendHeader(result, header);

		result.append("\n\n");

		result.append(
			"   MIME-Multipart/Signed composed by Columba - visit http://columba.sourceforge.net\n\n");

		// This MimePart has exactly two Childs as defined by RFC1847
		// The Signed part is created on the fly by this renderer

		// First render mail
		String protectedBodyPart =
			MimeTreeRenderer.getInstance().renderMimePart(
				(MimePart) part.getChild(0));

		// Next create Signature		
		PgpMimePart pgpMimePart = (PgpMimePart) part;

		MimeHeader signatureHeader =
			new MimeHeader("application", "pgp-signature");
		MimePart signature = new MimePart(signatureHeader);
		signature.setBody(
			controller.sign(protectedBodyPart, pgpMimePart.getPgpItem()));

		pgpMimePart.addChild(signature);

		// Render
		result.append("\n--");
		result.append(boundary);
		result.append("\n");


		// Protected MimePart
		result.append(protectedBodyPart);

		result.append("\n--");
		result.append(boundary);
		result.append("\n");

		// Signature
		result
			.append(
				MimeTreeRenderer.getInstance().renderMimePart( signature ));

		result.append("\n--");
		result.append(boundary);
		result.append("--\n");

		return result.toString();
	}

}
