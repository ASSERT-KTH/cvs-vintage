package org.columba.mail.composer.mimepartrenderers;

import org.columba.mail.coder.CoderRouter;
import org.columba.mail.coder.Encoder;
import org.columba.mail.composer.MimePartRenderer;
import org.columba.mail.composer.MimeTreeRenderer;
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
public class MultipartRenderer extends MimePartRenderer {


	/**
	 * @see org.columba.modules.mail.composer.MimePartRenderer#getRegisterString()
	 */
	public String getRegisterString() {
		return "multipart";
	}

	/**
	 * @see org.columba.modules.mail.composer.MimePartRenderer#render(MimePart)
	 */
	public String render(MimePart part) {
		StringBuffer result = new StringBuffer();
		MimeHeader header = part.getHeader();
		String boundary = createUniqueBoundary();
		
		// Store boundary in header
		header.putContentParameter("boundary", boundary);
		
		// Determine the Encoder needed				
		Encoder encoder = CoderRouter.getEncoder( header.getContentTransferEncoding() );
		
		// First Render Header		
		appendHeader( result, header );
		
		result.append( "\n" );
		
		result.append( "\tMIME-Multipart Message composed with Columba - visit columba.sourceforge.net\n");
		
		for( int i=0; i<part.countChilds(); i++ ) {		
			result.append( "\n--" );
			result.append( boundary );
			result.append( "\n" );
			
			result.append( MimeTreeRenderer.getInstance().renderMimePart( (MimePart) part.getChild(i) ) );
		}
		
		result.append( "\n--" );
		result.append( boundary );
		result.append( "--\n" );
		
		return result.toString();
	}

	

}
