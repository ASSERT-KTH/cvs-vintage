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


