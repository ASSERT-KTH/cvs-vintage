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

package org.columba.mail.parser.mimetypeparsers;

import org.columba.mail.message.MimeHeader;
import org.columba.mail.message.MimePart;
import org.columba.mail.parser.MimeParser;
import org.columba.mail.parser.MimeTypeParser;

public class MimeMultipartParser extends MimeTypeParser
{
	public String getRegisterString() {
		return "multipart";	
	}
    
    public synchronized MimePart parse(MimeHeader header, String input)
    {
        MimeParser mimeParser = new MimeParser();
        int startPos, endPos;
        String bound = (String) header.contentParameter.get("boundary");

		MimePart multiPartNode = new MimePart( header );		

		
		// Create the boundaries to search for
		
        String startBound = new String("--"+bound+"\n");
        String endBound = new String("--"+bound+"--");

        // Divide Message into single parts and parse each

        startPos = input.indexOf(startBound)+startBound.length();
        endPos = input.indexOf(startBound,startPos);

        while( endPos != -1 ) {
            multiPartNode.addChild( mimeParser.parse( input.substring( startPos, endPos ) ));
            startPos = endPos + startBound.length();
            endPos = input.indexOf(startBound,startPos);
        }

        endPos = input.indexOf(endBound,startPos);
        if( endPos == -1 ) endPos = input.length()-1;
        
	    multiPartNode.addChild( mimeParser.parse( input.substring( startPos, endPos ) ));
        
        return multiPartNode;
    }
}

