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

import org.columba.addressbook.config.FolderItem;


/**
 * Complete contact folder using the contact item cache storage
 * and the xml based data storgae.
 * 
 * @author fdietz
 *
 */
public class AddressbookFolder extends LocalFolder {

    public AddressbookFolder(String name, String path) {
    	super(name, path);
    }
    
    public AddressbookFolder(FolderItem folderItem) {
        super(folderItem);

    }

    public DataStorage getDataStorageInstance() {
        if (dataStorage == null) {
            dataStorage = new XmlDataStorage(this);
        }

        return dataStorage;
    }
}
