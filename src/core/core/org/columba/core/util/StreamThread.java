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

package org.columba.core.util;

import java.util.*;
import java.io.*;

public class StreamThread extends Thread
{
    InputStream is;
    String type;

    StringBuffer buf;

    public StreamThread( InputStream is, String type )
    {
        this.is = is;
        this.type = type;

        buf = new StringBuffer();
    }


    public void run()
       {


	   try
	       {
		   InputStreamReader isr = new InputStreamReader(is);
		   BufferedReader br = new BufferedReader(isr);
		   String line=null;
		   while ( (line = br.readLine()) != null)
                   {
		       System.out.println(type + ">" + line);
                       buf.append( line+"\n" );
                   }

	       } catch (IOException ioe)
		   {
		       ioe.printStackTrace();
		   }
       }


    public String getBuffer()
    {
        return buf.toString();
    }

}