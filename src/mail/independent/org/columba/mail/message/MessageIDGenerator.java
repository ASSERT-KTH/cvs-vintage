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
