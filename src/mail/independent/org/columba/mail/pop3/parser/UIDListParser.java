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

package org.columba.mail.pop3.parser;

import java.util.*;

public class UIDListParser
{
    

    public static Vector parse( String s )
    {
        Vector list = new Vector();

        String str,str2;
        StringTokenizer tok = new StringTokenizer( s, "\n" );
        StringTokenizer tok2;

        while ( tok.hasMoreElements() )
        {
            str = (String) tok.nextElement();
            tok2 = new StringTokenizer( str, " " );

                // message number
            str2 = (String) tok2.nextElement();
            Integer i = new Integer( str2 );
            int number = i.intValue();

                // message uid
            str2 = (String) tok2.nextElement();
            list.add( str2 );
        }
        
        return list;
    }

  

  

}



