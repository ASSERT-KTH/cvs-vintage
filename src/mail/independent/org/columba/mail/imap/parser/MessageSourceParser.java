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
public class MessageSourceParser {

	
	public static String parse(IMAPResponse[] responses) {
		StringBuffer buf = new StringBuffer();
		//System.out.println("linecount="+responses.length);
		
		String source = responses[0].getSource();
		
		
		int leftIndex = source.indexOf('}');
		int rightIndex = source.length()-3;
				
		//System.out.println("messag-srouce="+source.substring( leftIndex+3, rightIndex ));
		
		return source.substring( leftIndex+3, rightIndex );
	}

}
