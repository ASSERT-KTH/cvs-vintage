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

package org.columba.addressbook.folder;

import java.io.File;

import org.columba.core.command.WorkerStatusController;
import org.columba.core.config.AdapterNode;
import org.columba.core.config.ConfigPath;
import org.columba.addressbook.config.FolderItem;
import org.columba.addressbook.gui.tree.AddressbookTreeNode;
import org.columba.addressbook.main.AddressbookInterface;

public abstract class Folder extends AddressbookTreeNode
{

	/**
	 *  unique identification number
	 */
	private int uid;

	/**
	 * folder where we save everything
	 * name of folder is usually the UID-number
	 */
	protected File directoryFile;

	/**
	 * FolderItem keeps information about the folder
	 * for example: name, accessrights, type
	 */
	protected FolderItem folderItem;

	protected AddressbookInterface addressbookInterface;

	public Folder(FolderItem item, AddressbookInterface addressbookInterface)
	{
		super(item.getName());

		this.folderItem = item;
		this.uid = folderItem.getUid();
		this.addressbookInterface = addressbookInterface;

		String dir =
			ConfigPath.getConfigDirectory() + "/addressbook" + "/" + new Integer(uid).toString();

		directoryFile = new File(dir);
		if (directoryFile.exists() == false)
		{
			directoryFile.mkdir();
		}
	}

	/**
	 * return FolderItem 
	 */
	public FolderItem getFolderItem()
	{
		return folderItem;
	}

	/**
	 * return HeaderItemList
	 * 
	 * List of HeaderItem-Objects is needed for the table
	 * and therefore only keeps table-relevant information
	 * 
	 * to get *all* the data of a contact use the uidGet-method
	 */
	public abstract HeaderItemList getHeaderItemList();
	
	/**
	 * this method it ATM only needed for group-support
	 *  -> get the uid-list (the grouplist-members!) from the
	 *     grouplistcard-object
	 * 
	 *  -> this method return a HeaderItemList for use in a JList-object, etc.
	 */
	public abstract HeaderItemList getHeaderItemList( Object[] uids );

	/** 
	 * add ContactCard to Folder
	 */
	public abstract void add(DefaultCard item);

	public abstract void modify(DefaultCard item, Object uid );
	
	//public abstract void add(GroupListCard item);

	/**
	 * remove ContactCard/GroupList
	 */
	public abstract void remove(Object uid);

	/**
	 * this is for address-completion
	 */
	public abstract HeaderItemList searchPattern( String pattern );

	/**
	 * return ContactCard containing every information 
	 * we have,
	 * use this method for the EditContact-Dialog
	 */
	public abstract DefaultCard get(Object uid);
	
	public abstract boolean exists( String email );

	/**
	 * remove folder from parent
	 */
	public void removeFolder()
	{
		// remove treenode from xml-config-file
		FolderItem item = getFolderItem();
		AdapterNode rootNode = item.getRootNode();
		rootNode.remove();
		
		// remove treenode from JTree
		removeFromParent();
	}
	
	/**
	 * save header-cache (HeaderItemList)
	 */
	public abstract void save(WorkerStatusController worker) throws Exception;

	/**
	 * load header-cache (HeaderItemList)
	 */
	public abstract void load(WorkerStatusController worker) throws Exception;

}
