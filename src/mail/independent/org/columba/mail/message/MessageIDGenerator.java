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

package org.columba.mail.message;

import java.net.InetAddress;

public class MessageIDGenerator
{
    public MessageIDGenerator()
    {}


    /**
     *
     * generate a unique Message-ID
     *
     *
     **/
    public static String generate()
    {
	String hostname=null;
	try
	{
	    hostname = InetAddress.getLocalHost().getHostName();
	}
	catch ( Exception ex )
	{
	    ex.printStackTrace();
	}

	if ( hostname.length()==0 ) hostname = null;	

	long currentTime = System.currentTimeMillis();
	long randomNumber = Double.doubleToLongBits( Math.random() );

	StringBuffer result = new StringBuffer();

	result.append("<");
	result.append( Long.toString( Math.abs(currentTime), 36 ) );
	result.append(".");
	result.append( Long.toString( Math.abs(randomNumber), 36 ) );
	result.append("@");

	if (hostname != null) result.append(hostname);
	else 
        {
	    result.append("a");  
	    randomNumber = ( Double.doubleToLongBits( Math.random() ) & 0xFFFFFFFFL );
	    result.append( Long.toString( Math.abs(randomNumber), 36 ) );
	}

	result.append(">");
	
	return result.toString();
    }

    protected static void validateHostName( String hostname )
    {
    }

}
