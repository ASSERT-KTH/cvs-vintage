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

import java.io.File;

import org.columba.core.util.SwingWorker;
import org.columba.mail.folder.Folder;

public abstract class DefaultMailboxImporter extends SwingWorker
{
	public static int TYPE_FILE = 0;
	public static int TYPE_DIRECTORY = 1;

	protected Folder destinationFolder;
	protected File sourceFile;
	//protected TempFolder tempFolder;
	protected int counter;

	public void init()
	{
		counter = 0;
		//tempFolder = new TempFolder();
	}


	/*********** overwrite the following messages **************************/

	/**
	 * overwrite this method to specify type
	 * the wizard dialog will open the correct file/directory dialog automatically
	 */
	public int getType()
	{
		return TYPE_FILE;
	}


	/**
	 * this method does all the import work
	 */
	public abstract void importMailbox(File file) throws Exception;


	/*********** intern methods (no need to overwrite these) ****************/

	public void setSourceFile(File file)
	{
		this.sourceFile = file;
	}

	/**
	 * set destination folder
	 */
	public void setDestinationFolder(Folder folder)
	{
		destinationFolder = folder;
	}

	/**
	 *  counter for successfully imported messages
	 */
	public int getCount()
	{
		return counter;
	}

	/**
	 *  this method calls your overwritten importMailbox(File)-method
	 *  and handles exceptions
	 */
	public Object construct()
	{
		// FIXME
		
		/*
		setText("Importing messages...");

		startTimer();

		try
		{
			importMailbox(sourceFile);
		}
		catch (Exception ex)
		{
			if (ex instanceof FileNotFoundException)
			{
				NotifyDialog dialog = new NotifyDialog();
				dialog.showDialog("Source File not found!");
			}
			else
			{
				ExceptionDialog dialog = new ExceptionDialog();
				dialog.showDialog(ex);
			}

			NotifyDialog dialog = new NotifyDialog();
			dialog.showDialog(
				"Message import failed! No messages were added to your folder.");

			stopTimer();

			unregister();
			
			
			return null;
		}

		if (getCancel())
		{
			NotifyDialog dialog = new NotifyDialog();
			dialog.showDialog(
				"You cancelled the import operation! No messages were added to your folder.");

			stopTimer();

			unregister();

			return null;
		}

		if (getCount() == 0)
		{
			NotifyDialog dialog = new NotifyDialog();
			dialog.showDialog(
				"Message import failed! No messages were added to your folder.\nThis means that the parser didn't throw any exception even if it didn't recognize the mailbox format or simple the messagebox didn't contain any messages.");

			stopTimer();

			unregister();

			return null;
		}
		else if (getCount() > 0)
		{
			
			JOptionPane.showMessageDialog(null, "Message import was successfull!", "Information",
                                  JOptionPane.INFORMATION_MESSAGE,
                                  ImageLoader.getImageIcon("stock_dialog_info_48.png") );
			
		}

		stopTimer();

		unregister();
		*/
		
		return null;

	}

	/**
	 * use this method to save a message to the specified destination folder
	 */
	protected void saveMessage(String rawString) throws Exception
	{
		// FIXME
		/*
		int index = rawString.indexOf("\n\n");
		if (index == -1)
		{
			System.out.println("non-standard-compliant message:\n" + rawString);
			return;
		}

		String headerString = rawString.substring(0, index);
		Rfc822Parser parser = new Rfc822Parser();

		ColumbaHeader header = parser.parseHeader(rawString);

		Message m = new Message(header);
		ColumbaHeader h = m.getHeader();
		m.setRawString(rawString);

		h.set("columba.flags.recent", Boolean.TRUE);

		int size = rawString.length();

		size = Math.round(size / 1024);

		if (size == 0)
			size = 1;
		//m.setSize( size );
		h.set("columba.size", new Integer(size));

		Date date = DateParser.parseString((String) h.get("date"));
		//System.out.println("date1: "+ h.get("date") );
		//m.setDate( date );
		h.set("columba.date", date);
		Date date2 = (Date) h.get("columba.date");

		//System.out.println("date2: "+ h.get("columba.date") );

		String shortFrom = (String) header.get("From");
		if (shortFrom != null)
		{
			if (shortFrom.indexOf("<") != -1)
			{
				shortFrom = shortFrom.substring(0, shortFrom.indexOf("<"));
				if (shortFrom.length() > 0)
				{
					if (shortFrom.startsWith("\""))
						shortFrom = shortFrom.substring(1, shortFrom.length() - 1);
					if (shortFrom.endsWith("\""))
						shortFrom = shortFrom.substring(0, shortFrom.length() - 1);
				}

			}
			else
				shortFrom = shortFrom;

			//m.setShortFrom( shortFrom );
			h.set("columba.from", shortFrom);
		}
		else
		{
			//m.setShortFrom("");
			h.set("columba.from", new String(""));
		}

		String priority = (String) header.get("X-Priority");
		if (priority != null)
		{

			//m.setPriority( prio );
			h.set("columba.priority", Integer.getInteger(priority));
		}
		else
		{
			//m.setPriority( 3 );
			h.set("columba.priority", new Integer(3));
		}

		String attachment = (String) header.get("Content-Type");
		if (attachment != null)
		{
			attachment = attachment.toLowerCase();

			if (attachment.indexOf("multipart") != -1)
			{
				//m.setAttachment(true);
				h.set("columba.attachment", Boolean.TRUE);
			}
			else
			{
				h.set("columba.attachment", Boolean.FALSE);
				//m.setAttachment(false);
			}
		}
		else
		{
			h.set("columba.attachment", Boolean.FALSE);
			//m.setAttachment(false);
		}

		Object uid = tempFolder.workerAdd(m);

		Object[] uids = new Object[1];
		uids[0] = uid;
		
		FolderOperation op =
			new FolderOperation(Operation.MOVE, 0, uids, tempFolder, destinationFolder);
		MainInterface.crossbar.operate(op);
		
		counter++;

		setText("Importing messages: " + getCount());
		*/
	}

	/*
	protected void finish()
	{
		Object[] uids = tempFolder.getUids();

		FolderOperation op =
			new FolderOperation(Operation.MOVE, 0, uids, tempFolder, destinationFolder);
		MainInterface.crossbar.operate(op);
	}
	*/

}
