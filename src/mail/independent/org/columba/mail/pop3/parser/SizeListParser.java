package org.columba.mail.pop3.parser;

import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class SizeListParser {

	/**
	 * Constructor for SizeListParser.
	 */
	public SizeListParser() {
		super();
	}
	
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

                // message size
            str2 = (String) tok2.nextElement();
            list.add( str2 );
        }
        
        return list;
    }

}
