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
