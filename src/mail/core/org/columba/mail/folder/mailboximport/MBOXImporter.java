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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.columba.core.command.WorkerStatusController;

public class MBOXImporter extends DefaultMailboxImporter
{

	public int getType()
	{
		return TYPE_FILE;
	}
	
	public void importMailbox(File file, WorkerStatusController worker) throws Exception
	{

		boolean success = false;

		StringBuffer strbuf = new StringBuffer();

		BufferedReader in = new BufferedReader(new FileReader(file));
		String str;

		// parse line by line
		while ((str = in.readLine()) != null)
		{
			// if user cancelled task exit immediately			
			if ( worker.cancelled() )
				return;

			// if line doesn't start with "From" or line length is 0
			//  -> save everything in StringBuffer
			if (!str.startsWith("From ") || (str.length() == 0))
			{
				strbuf.append(str + "\n");
			}
			else
			{

				//  -> import message in Columba

				if (strbuf.length() != 0)
				{
					// found new message

					saveMessage(strbuf.toString());

					success = true;

				}
				strbuf = new StringBuffer();

			}

		}

		// save last message, because while loop aborted before being able to save message
		if (success && (strbuf.length() > 0))
		{
			saveMessage(strbuf.toString());
		}

		in.close();
	}
}
