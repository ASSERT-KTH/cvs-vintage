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

package org.columba.mail.parser;


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


