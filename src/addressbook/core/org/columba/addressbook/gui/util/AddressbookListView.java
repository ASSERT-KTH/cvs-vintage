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

package org.columba.addressbook.gui.util;

import javax.swing.BorderFactory;
import javax.swing.JList;

import org.columba.addressbook.folder.HeaderItem;
import org.columba.addressbook.folder.HeaderItemList;

public class AddressbookListView extends JList
{
	private AddressbookListModel model;

	public AddressbookListView(AddressbookListModel model)
	{
		super(model);
		this.model = model;

		setCellRenderer(new AddressbookListRenderer());

		

		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

	}

	public AddressbookListView()
	{
		super();

		model = new AddressbookListModel();
		setModel(model);

		

		setCellRenderer(new AddressbookListRenderer());

		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

	}

	public void setHeaderList(HeaderItemList list)
	{
		removeAll();

		model.setHeaderList(list);
		
		

	}

	public void setModel(AddressbookListModel model)
	{
		this.model = model;
		super.setModel(model);
		
		
		
		
	}

	public void addElement(HeaderItem item)
	{
		model.addElement(item);
	}

	public HeaderItem get(int index)
	{
		HeaderItem item = (HeaderItem) model.get(index);

		return item;
	}

}