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
package org.columba.mail.folder.mailboximport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.columba.core.command.WorkerStatusController;


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

	
	
	public void importMailbox(File file, WorkerStatusController worker) throws Exception
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
			if ( worker.cancelled() == true)
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
