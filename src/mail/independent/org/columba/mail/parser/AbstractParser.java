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

package org.columba.mail.parser;

import java.io.*;
import org.columba.mail.message.*;

abstract class AbstractParser {

    protected String[] divideMessage( String input )
        {
            String[] output = new String[2];
            int emptyLinePos;

            if ( input.length() == 0 ) 
            	return null;

            if( input.charAt(0) == '\n' ) {
				output[0] = new String();
                output[1] = input;
                return output;
            }

            emptyLinePos = input.indexOf("\n\n");

            if( input.indexOf("\n\n") != -1 ) {
                output[0] = input.substring( 0, emptyLinePos + 1 );
                output[1] = input.substring( emptyLinePos + 2 );
            }
	    	else {
				output[0] = input;
				output[1] = new String();
		    }

            return output;
        }


    protected String removeQuotes()
    {

        return new String();
    }



}


