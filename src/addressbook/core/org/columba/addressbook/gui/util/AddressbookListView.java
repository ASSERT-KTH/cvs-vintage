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