package org.columba.mail.imap.parser;

import org.columba.mail.imap.IMAPResponse;
import org.columba.mail.imap.parser.*;
import org.columba.mail.message.MimePartTree;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MimePartTreeParser {
	public static MimePartTree parse(IMAPResponse[] responses) {
		StringBuffer buf = new StringBuffer();

		for (int i = 0; i < responses.length-1; i++) {
			if (responses[i] == null)
				continue;
				
			buf.append( responses[i].getSource()+ "\n" );			
		}

		MimePartTree mptree = new Imap4Parser().parseBodyStructure(buf.toString());
		
		return mptree;
	}

	
}
