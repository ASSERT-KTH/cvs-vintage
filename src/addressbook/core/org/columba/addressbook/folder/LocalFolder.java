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