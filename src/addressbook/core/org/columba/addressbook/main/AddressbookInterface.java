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

package org.columba.addressbook.main;

import org.columba.addressbook.gui.action.AddressbookActionListener;
import org.columba.addressbook.gui.frame.AddressbookFrameView;
import org.columba.addressbook.gui.menu.AddressbookMenu;
import org.columba.addressbook.gui.table.TableView;
import org.columba.addressbook.gui.tree.TreeView;
import org.columba.addressbook.gui.tree.AddressbookTreeModel;
import org.columba.core.command.TaskManager;
import org.columba.core.gui.statusbar.StatusBar;

public class AddressbookInterface
{
    public AddressbookFrameView frame;
    public AddressbookMenu menu;
    
    public TableView table;
    public TreeView tree;
    
    public AddressbookTreeModel treeModel;
    //public AddressbookConfig config;
    public StatusBar statusbar;
	public TaskManager taskManager;
	public AddressbookActionListener actionListener;

    public AddressbookInterface()
    {
    }

}


