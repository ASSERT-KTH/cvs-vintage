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

class HeaderTokenizer
{

    private String input;
    private int actPos;
    
    public HeaderTokenizer( String input )
        {
            this.input = input;
            actPos = 0;
        }

// This Method frees a given String from Comments defined in Rfc822 

    private String killComments( String commented )
        {
            int end;
            int start = commented.indexOf("(");
            int quoted;

            if( start == -1 ) return commented;

            StringBuffer output = new StringBuffer( commented );
            
            while( start != -1 )
            {                
                end = output.toString().indexOf(")",start);

                if( end == -1 ) return output.toString();

                quoted = commented.indexOf( "?=", start );
                if( (quoted == -1) | (quoted > end) ) {
                    output = output.delete(start,end+1);
                    start = output.toString().indexOf("(");                    
                }
                else {
                    start = output.toString().indexOf("(", end);
                }
            }

            return output.toString();
        }
    
// This Method delivers the next line

    public String nextLine()
        {
            StringBuffer output;
            int crlfPos;

            if(actPos >= input.length()) return null;

            if( (input.charAt(actPos)=='\n') ) return null;
            
            crlfPos = input.indexOf('\n',actPos);
            if( crlfPos == -1 ) return null;

            output = new StringBuffer( killComments( input.substring(actPos,crlfPos)) );
            actPos = crlfPos + 1;
            if(actPos >= input.length()) return output.toString().trim();
            
            while( input.charAt(actPos) == '\t' | input.charAt(actPos) == ' ' )
            {                
                crlfPos = input.indexOf('\n',actPos);
                output.append( killComments( input.substring(actPos,crlfPos)) );
                actPos = crlfPos + 1;
                if( actPos >= input.length() ) return output.toString().trim();
            }
                
            return output.toString().trim();
                
        }
    
}


