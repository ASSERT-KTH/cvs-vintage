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
package org.columba.mail.pop3.parser;

import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

public class UIDListParser
{
    

    public static List parse( String s )
    {
        List list = new Vector();

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



