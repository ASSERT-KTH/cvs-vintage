package org.columba.mail.imap.parser;

import org.columba.mail.imap.IMAPResponse;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MimePartParser {
	
	public static String parse(IMAPResponse[] responses) {
		StringBuffer buf = new StringBuffer();
		//System.out.println("linecount="+responses.length);
		
		String source = responses[0].getSource();
		//System.out.println("source="+source);
		
		int leftIndex = source.indexOf('}');
		int rightIndex = source.length()-3;
				
		
		return source.substring( leftIndex+3, rightIndex );
	}

}
