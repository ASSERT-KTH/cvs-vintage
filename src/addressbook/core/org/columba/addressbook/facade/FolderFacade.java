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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.addressbook.facade;

import org.columba.addressbook.folder.AbstractFolder;
import org.columba.addressbook.folder.IContactFolder;
import org.columba.addressbook.folder.IFolder;
import org.columba.addressbook.gui.tree.AddressbookTreeModel;

/**
 * Provides access to contact and group folders.
 * 
 * @author fdietz
 */
public class FolderFacade implements IFolderFacade {

	/**
	 * @see org.columba.addressbook.facade.IFolderFacade#getFolder(int)
	 */
	public IFolder getFolder(int uid) {
		return (IFolder) AddressbookTreeModel.getInstance().getFolder(uid);
	}

	/**
	 * @see org.columba.addressbook.facade.IFolderFacade#getCollectedAddresses()
	 */
	public IContactFolder getCollectedAddresses() {
		AddressbookTreeModel model = AddressbookTreeModel.getInstance();
		if (model != null)
			return (AbstractFolder) model.getFolder(102);

		return null;
	}

	/**
	 * @see org.columba.addressbook.facade.IFolderFacade#getLocalAddressbook()
	 */
	public IContactFolder getLocalAddressbook() {
		AddressbookTreeModel model = AddressbookTreeModel.getInstance();
		if (model != null)
			return (AbstractFolder) model.getFolder(101);

		return null;
	}

	/**
	 * @see org.columba.addressbook.facade.IFolderFacade#getFolder(java.lang.String)
	 */
	public IFolder getFolder(String name) {
		AddressbookTreeModel model = AddressbookTreeModel.getInstance();
		if (model != null)
			return (AbstractFolder) model.getFolder(name);

		return null;
	}
}