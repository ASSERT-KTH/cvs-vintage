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
import org.columba.addressbook.gui.tree.AddressbookTreeModel;
import org.columba.addressbook.main.AddressbookInterface;

/**
 * Contact folder-related functionality.
 * 
 * @author fdietz
 */
public class FolderFacade {

    /**
     * 
     * Return a Folder object
     * 
     * @param uid
     *            id of folder
     * @return Folder
     */
    public static AbstractFolder getAddressbook(int uid) {
        return (AbstractFolder) AddressbookInterface.addressbookTreeModel
                .getFolder(uid);
    }

    /**
     * 
     * Returns a reference to the system addressbook which automatically
     * collects addresses
     * 
     * @return Folder
     */
    public static AbstractFolder getCollectedAddresses() {
        AddressbookTreeModel model = AddressbookInterface.addressbookTreeModel;
        if (model != null) return (AbstractFolder) model.getFolder(102);

        return null;
    }
}
