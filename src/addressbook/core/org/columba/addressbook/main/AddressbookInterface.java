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

package org.columba.addressbook.main;

import org.columba.addressbook.gui.action.AddressbookActionListener;
import org.columba.addressbook.gui.frame.AddressbookView;
import org.columba.addressbook.gui.menu.AddressbookMenu;
import org.columba.addressbook.gui.table.AddressbookTable;
import org.columba.addressbook.gui.tree.AddressbookTree;
import org.columba.addressbook.gui.tree.AddressbookTreeModel;
import org.columba.core.command.TaskManager;
import org.columba.core.gui.statusbar.StatusBar;

public class AddressbookInterface
{
    public AddressbookView frame;
    public AddressbookMenu menu;
    
    public AddressbookTable table;
    public AddressbookTree tree;
    
    public AddressbookTreeModel treeModel;
    //public AddressbookConfig config;
    public StatusBar statusbar;
	public TaskManager taskManager;
	public AddressbookActionListener actionListener;

    public AddressbookInterface()
    {
    }

}


