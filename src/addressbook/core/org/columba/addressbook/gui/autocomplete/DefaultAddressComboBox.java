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
package org.columba.addressbook.gui.autocomplete;

import org.columba.addressbook.folder.Folder;
import org.columba.addressbook.folder.HeaderItem;
import org.columba.addressbook.folder.HeaderItemList;
import org.columba.addressbook.main.AddressbookInterface;

import org.columba.mail.util.AddressCollector;


/**
 * JCombox includes autocomplete feature.
 * <p>
 * This class automatically initializes the data
 * for the autocomplete feature.
 *
 * @author fdietz
 */
public class DefaultAddressComboBox extends BasicAddressAutocompleteComboBox {
    public DefaultAddressComboBox() {
        super();

        initData();

        // initialize completer
        addCompleter();
    }

    private void addList(HeaderItemList list) {
        for (int i = 0; i < list.count(); i++) {
            HeaderItem item = list.get(i);

            if (item.contains("displayname")) {
                AddressCollector.addAddress((String) item.get("displayname"),
                    item); //$NON-NLS-1$ //$NON-NLS-2$
            }

            if (item.contains("email;internet")) {
                AddressCollector.addAddress((String) item.get("email;internet"),
                    item); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    /**
 * Add data from the Personal Addressbook and Collected Addresses
 *
 */
    private void initData() {
        AddressCollector.clear();

        HeaderItemList list = ((Folder) AddressbookInterface.addressbookTreeModel.getFolder(101)).getHeaderItemList();
        addList(list);
        list = ((Folder) AddressbookInterface.addressbookTreeModel.getFolder(102)).getHeaderItemList();
        addList(list);
    }
}
