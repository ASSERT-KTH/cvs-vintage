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

package org.columba.addressbook.gui.frame;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JSplitPane;

import org.columba.addressbook.gui.menu.AddressbookMenu;
import org.columba.addressbook.gui.table.AddressbookTable;
import org.columba.addressbook.gui.tree.AddressbookTree;
import org.columba.addressbook.main.AddressbookInterface;
import org.columba.core.gui.FrameController;
import org.columba.core.gui.FrameView;
import org.columba.core.gui.ToolBar;
import org.columba.core.gui.menu.Menu;
import org.columba.core.main.MainInterface;

public class AddressbookView extends FrameView {
	private AddressbookInterface addressbookInterface;
	private AddressbookTree tree;
	private AddressbookTable table;

	public AddressbookView(FrameController frameController) {
		super(frameController);

		//super("Columba v" + MainInterface.version + " - Addressbook");
		/*
		this.setIconImage(
			ImageLoader.getImageIcon("ColumbaIcon.png").getImage());
		*/
		addressbookInterface = MainInterface.addressbookInterface;
		addressbookInterface.frame = this;
		// FIXME

		/*
		addressbookInterface.actionListener =
			new AddressbookActionListener(addressbookInterface);
		addressbookInterface.menu = new AddressbookMenu(addressbookInterface);
		*/
		init();
	}

	/* (non-Javadoc)
		 * @see org.columba.core.gui.FrameView#createMenu(org.columba.core.gui.FrameController)
		 */
	protected Menu createMenu(FrameController controller) {
		Menu menu = new AddressbookMenu("org/columba/core/action/menu.xml", controller);
		menu.extendMenuFromFile("org/columba/addressbook/action/menu.xml");

		return menu;
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.FrameView#createToolbar(org.columba.core.gui.FrameController)
	 */
	protected ToolBar createToolbar(FrameController controller) {
		return new ToolBar(
			"org/columba/addressbook/action/main_toolbar.xml",
			controller);
	}

	public void init() {
		super.init();
		
		Container c = getContentPane();

		//setJMenuBar(addressbookInterface.menu.getMenuBar());

		/*
		AddressbookToolBar toolbar =
			new AddressbookToolBar(addressbookInterface);
		
		c.add(toolbar, BorderLayout.NORTH);
		*/

		tree = createTree(addressbookInterface);

		addressbookInterface.tree = tree;
		table = new AddressbookTable(addressbookInterface);
		table.setupRenderer();
		addressbookInterface.table = table;

		JSplitPane splitPane =
			new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tree.scrollPane, table);
		splitPane.setBorder(null);

		c.add(splitPane, BorderLayout.CENTER);

		/*
		StatusBar statusbar = new StatusBar(addressbookInterface.taskManager);
		addressbookInterface.statusbar = statusbar;

		c.add(statusbar, BorderLayout.SOUTH);
		*/
		
		//pack();

		/*
		Dimension size = getSize();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(
			(screenSize.width - size.width) / 2,
			(screenSize.height - size.height) / 2);
		*/
		//setVisible(true);
	}

	protected AddressbookTree createTree(AddressbookInterface addressbookInterface) {
		AddressbookTree tree = new AddressbookTree(addressbookInterface);
		return tree;
	}
}
