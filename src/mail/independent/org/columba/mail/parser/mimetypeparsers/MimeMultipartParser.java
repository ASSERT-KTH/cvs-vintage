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

