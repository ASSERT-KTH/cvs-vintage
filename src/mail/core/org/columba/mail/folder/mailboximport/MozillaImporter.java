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

package org.columba.mail.folder.mailboximport;

import org.columba.main.*;
import org.columba.mail.folder.*;
import org.columba.mail.gui.util.*;

import java.io.*;


/**
 * @version 	1.0
 * @author
 */
public class MozillaImporter extends DefaultMailboxImporter
{
	

	public int getType()
	{
		return TYPE_FILE;
	}

	
	
	public void importMailbox(File file) throws Exception
	{

		int count = 0;
		boolean sucess = false;

		StringBuffer strbuf = new StringBuffer();

		BufferedReader in = new BufferedReader(new FileReader(file));
		String str;

		// parse line by line
		while ((str = in.readLine()) != null)
		{
			// if user cancelled task exit immediately			
			if (getCancel() == true)
				return;

			// if line doesn't start with "From" or line length is 0
			//  -> save everything in StringBuffer
			if ((str.startsWith("From ") == false) || (str.length() == 0))
			{
				strbuf.append(str + "\n");
			}
			else
			{
				
				// line contains "-" (mozilla mbox style)
				//  -> import message in Columba
				if ( str.indexOf("-") != -1) 
				{
					if (strbuf.length() != 0)
					{
						// found new message

						saveMessage(strbuf.toString());

						count++;

						sucess = true;

					}
					strbuf = new StringBuffer();
				}
				else
				{
					strbuf.append(str + "\n");
				}
			}

		}

		// save last message, because while loop aborted before being able to save message
		if ((sucess == true) && (strbuf.length() > 0))
		{
			saveMessage(strbuf.toString());
		}

		in.close();

	}
}
