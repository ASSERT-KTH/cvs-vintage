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

package org.columba.addressbook.folder.importfilter;

import java.io.File;

import org.columba.addressbook.folder.ContactCard;
import org.columba.addressbook.folder.Folder;
import org.columba.addressbook.main.AddressbookInterface;
import org.columba.core.util.SwingWorker;

/**
 * @version 	1.0
 * @author
 */
public abstract class DefaultAddressbookImporter extends SwingWorker
{
	public static int TYPE_FILE = 0;
	public static int TYPE_DIRECTORY = 1;

	protected Folder destinationFolder;

	protected File sourceFile;

	protected AddressbookInterface addressbookInterface;

	//protected AddressbookFolder tempFolder;

	protected int counter;


	public void init()
	{
		counter = 0;

		//tempFolder = new AddressbookFolder(null,addressbookInterface);
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
	public abstract void importAddressbook(File file) throws Exception;


	/*********** intern methods (no need to overwrite these) ****************/

	public void setAddressbookInterface( AddressbookInterface i )
	{
		this.addressbookInterface = i;
	}

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
		/*
		setText("Importing contacts...");

		startTimer();

		try
		{
			importAddressbook(sourceFile);
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
				"Addressbook import failed! No contacts were added to your folder.");

			stopTimer();

			unregister();
			
			return null;
		}
		*/
		/*
		if (getCancel() == true)
		{
			NotifyDialog dialog = new NotifyDialog();
			dialog.showDialog(
				"You cancelled the import operation! No contacts were added to your folder.");

			stopTimer();

			unregister();

			return null;
		}
		
		
		if (getCount() == 0)
		{
			NotifyDialog dialog = new NotifyDialog();
			dialog.showDialog(
				"Addressbook import failed! No contacts were added to your folder.\nThis means that the parser didn't throw any exception even if it didn't recognize the mailbox format or simple the messagebox didn't contain any messages.");

			stopTimer();

			unregister();

			return null;
		}

		if (getCount() > 0)
		{
			NotifyDialog dialog = new NotifyDialog();
			dialog.showDialog("Addressbook import was successful!");

			finish();

			addressbookInterface.table.setFolder( destinationFolder );
		}

		stopTimer();

		unregister();
		*/
		return null;

	}

	/**
	 * use this method to save a message to the specified destination folder
	 */
	protected void saveContact(ContactCard card) throws Exception
	{
		/*
		destinationFolder.add( card );

		counter++;

		setText("Importing messages: " + getCount());
		*/
	}

	protected void finish()
	{
		/*
		Object[] uids = tempFolder.getUids();

		FolderOperation op =
			new FolderOperation(Operation.MOVE, 0, uids, tempFolder, destinationFolder);
		mainInterface.crossbar.operate(op);
		*/
	}

}