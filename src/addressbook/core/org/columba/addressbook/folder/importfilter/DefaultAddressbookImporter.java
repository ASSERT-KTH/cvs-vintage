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

package org.columba.addressbook.folder.importfilter;

import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JOptionPane;

import org.columba.addressbook.folder.ContactCard;
import org.columba.addressbook.folder.Folder;
import org.columba.addressbook.main.AddressbookInterface;
import org.columba.core.gui.util.ExceptionDialog;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.gui.util.NotifyDialog;
import org.columba.core.plugin.PluginInterface;

/**
 * @version 	1.0
 * @author
 */
public abstract class DefaultAddressbookImporter implements PluginInterface {
	public static int TYPE_FILE = 0;
	public static int TYPE_DIRECTORY = 1;

	protected Folder destinationFolder;

	protected File sourceFile;

	protected AddressbookInterface addressbookInterface;

	//protected AddressbookFolder tempFolder;

	protected int counter;

	public DefaultAddressbookImporter(
		File sourceFile,
		Folder destinationFolder) {
		this.sourceFile = sourceFile;
		this.destinationFolder = destinationFolder;

	}

	public void init() {
		counter = 0;

		//tempFolder = new AddressbookFolder(null,addressbookInterface);
	}

	/*********** overwrite the following messages **************************/

	/**
	 * overwrite this method to specify type
	 * the wizard dialog will open the correct file/directory dialog automatically
	 */
	public int getType() {
		return TYPE_FILE;
	}

	/**
	 * this method does all the import work
	 */
	public abstract void importAddressbook(File file) throws Exception;

	/*********** intern methods (no need to overwrite these) ****************/

	public void setAddressbookInterface(AddressbookInterface i) {
		this.addressbookInterface = i;
	}

	public void setSourceFile(File file) {
		this.sourceFile = file;
	}

	/**
	 * set destination folder
	 */
	public void setDestinationFolder(Folder folder) {
		destinationFolder = folder;
	}

	/**
	 *  counter for successfully imported messages
	 */
	public int getCount() {
		return counter;
	}

	/**
	 *  this method calls your overwritten importMailbox(File)-method
	 *  and handles exceptions
	 */
	public void run() {

		try {
			importAddressbook(sourceFile);
		} catch (Exception ex) {
			if (ex instanceof FileNotFoundException) {
				NotifyDialog dialog = new NotifyDialog();
				dialog.showDialog("Source File not found!");
			} else {
				ExceptionDialog dialog = new ExceptionDialog();
				dialog.showDialog(ex);
			}

			NotifyDialog dialog = new NotifyDialog();
			dialog.showDialog(
				"Addressbook import failed! No contacts were added to your folder.");

			return;
		}

		if (getCount() == 0) {
			NotifyDialog dialog = new NotifyDialog();
			dialog.showDialog(
				"Addressbook import failed! No contacts were added to your folder.\nThis means that the parser didn't throw any exception even if it didn't recognize the mailbox format or simple the messagebox didn't contain any messages.");

			return;
		}

		if (getCount() > 0) {
			JOptionPane.showMessageDialog(
				null,
				"Addressbook import was successfull!",
				"Information",
				JOptionPane.INFORMATION_MESSAGE,
				ImageLoader.getImageIcon("stock_dialog_info_48.png"));

			addressbookInterface.table.setFolder(destinationFolder);
		}

		return;

	}

	/**
	 * use this method to save a message to the specified destination folder
	 */
	protected void saveContact(ContactCard card) throws Exception {

		destinationFolder.add(card);

		counter++;

	}

}