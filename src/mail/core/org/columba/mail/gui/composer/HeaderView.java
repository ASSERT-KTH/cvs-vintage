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

package org.columba.mail.gui.composer;

import java.awt.Color;

import javax.swing.JScrollPane;

import org.columba.addressbook.folder.HeaderItem;
import org.columba.addressbook.gui.table.AddressbookTableModel;
import org.columba.mail.gui.composer.util.AddressbookTableView;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class HeaderView extends JScrollPane {
	
	ComposerModel model;

	AddressbookTableView table;
	
	

	public HeaderView(ComposerModel model) {
		super();

		this.model = model;
		
		table = new AddressbookTableView();

		getViewport().setBackground(Color.white);	
		
		
		
		setViewportView( table );
		
		
	}

	public AddressbookTableView getTable()
	{
		return table;
	}
	
	public AddressbookTableModel getAddressbookTableModel()
	{
		return table.getAddressbookTableModel();
	}
	
	public int count()
	{
		return getTable().getRowCount();
	}
	
	public void removeSelected()
	{
		int[] indices = getTable().getSelectedRows();
		HeaderItem[] items = new HeaderItem[indices.length];
		
		for ( int i=0; i<indices.length; i++ )
		{
			items[i] = getAddressbookTableModel().getHeaderItem(indices[i]);
		}
		
		getAddressbookTableModel().removeItem(items);
	}
	
	
	
	
	
	
	
	
	

}