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

package org.columba.addressbook.folder;

import javax.swing.ImageIcon;

import org.columba.addressbook.config.FolderItem;
import org.columba.core.gui.util.ImageLoader;


/**
 * 
 * LocalFolder-class gives as an additional abstraction-layer:
 *  --> DataStorageInterface
 * 
 * this makes it very easy to add other folder-formats
 * 
 * the important methods from Folder are just mapped to
 * the corresponding methods from DataStorageInterface
 * 
 * 
 */
public abstract class LocalFolder extends Folder
{
	
	
	protected DataStorage dataStorage;
	
	/**
	 * 
	 * unique identification number for the list of HeaderItem's
	 * 
	 */
	protected int nextUid;
	

	public LocalFolder(FolderItem item)
	{
		super(item);
		nextUid = 0;
	}
	
	protected Object generateNextUid()
	{
		return new Integer(nextUid++);
	}
	

	public abstract DataStorage getDataStorageInstance();

	
	/*
	public void add(ContactCard item)
	{
		Object newUid = generateNextUid();
		
		getDataStorageInstance().saveDefaultCard(item, newUid);
	}
	*/
	
	public void add(DefaultCard item)
	{
		Object newUid = generateNextUid();
		
		getDataStorageInstance().saveDefaultCard(item, newUid);
	}

	public void remove(Object uid)
	{
		getDataStorageInstance().removeCard(uid);
	}
	
	/*
	public void removeFolder()
	{
		super.removeFolder();
		
		// remove folder from disc
		directoryFile.delete();
	}
	*/
	
	public DefaultCard get(Object uid)
	{
		return getDataStorageInstance().loadDefaultCard(uid);
	}
	
	public void modify( DefaultCard card, Object uid )
	{
		getDataStorageInstance().modifyCard(card, uid);
	}

}