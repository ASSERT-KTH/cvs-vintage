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
package org.columba.mail.gui.contact.list;

import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JList;

import org.columba.addressbook.facade.IHeaderItem;



public class ContactList extends JList {
    private ContactListModel model;

    public ContactList(ContactListModel model) {
        super(model);
        this.model = model;

        setCellRenderer(new ContactListRenderer());

        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    }

    public ContactList() {
        super();

        model = new ContactListModel();
        setModel(model);

        setCellRenderer(new ContactListRenderer());

        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    }

    public void setHeaderItemList(List<IHeaderItem> list) {
        removeAll();

        model.setHeaderItemList(list);
    }

    public void setModel(ContactListModel model) {
        this.model = model;
        super.setModel(model);
    }

    public void addElement(IHeaderItem item) {
        model.addElement(item);
    }

    public IHeaderItem get(int index) {
        IHeaderItem item = (IHeaderItem) model.get(index);

        return item;
    }
}
