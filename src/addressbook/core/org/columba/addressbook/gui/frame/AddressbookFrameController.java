// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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

package org.columba.addressbook.gui.frame;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionListener;

import org.columba.addressbook.gui.table.TableController;
import org.columba.addressbook.gui.tree.TreeController;
import org.columba.addressbook.main.AddressbookInterface;
import org.columba.addressbook.util.AddressbookResourceLoader;
import org.columba.core.config.ViewItem;
import org.columba.core.gui.frame.Container;
import org.columba.core.gui.frame.ContainerInfoPanel;
import org.columba.core.gui.frame.ContentPane;
import org.columba.core.gui.frame.DefaultFrameController;
import org.columba.core.gui.util.UIFSplitPane;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.PluginHandlerNotFoundException;
import org.columba.core.pluginhandler.MenuPluginHandler;

/**
 * 
 * 
 * @author fdietz
 */
public class AddressbookFrameController extends DefaultFrameController
		implements ContentPane, AddressbookFrameMediator {

	protected TreeController tree;

	protected TableController table;

	/**
	 * Constructor for AddressbookController.
	 */
	public AddressbookFrameController(Container container, ViewItem viewItem) {
		super(container, viewItem);

		tree = new TreeController(this);
		table = new TableController(this);

		// table should be updated when tree selection changes
		tree.getView().addTreeSelectionListener(table);

		getContainer().setContentPane(this);
	}

	/**
	 * @return AddressbookTableController
	 */
	public TableController getTable() {
		return table;
	}

	/**
	 * @return AddressbookTreeController
	 */
	public TreeController getTree() {
		return tree;
	}

	/**
	 * @see org.columba.addressbook.gui.frame.AddressbookFrameMediator#addTableSelectionListener(javax.swing.event.ListSelectionListener)
	 */
	public void addTableSelectionListener(ListSelectionListener listener) {
		getTable().getView().getSelectionModel().addListSelectionListener(
				listener);
	}

	/**
	 * @see org.columba.addressbook.gui.frame.AddressbookFrameMediator#addTreeSelectionListener(javax.swing.event.TreeSelectionListener)
	 */
	public void addTreeSelectionListener(TreeSelectionListener listener) {
		getTree().getView().addTreeSelectionListener(listener);
	}

	/**
	 * @see org.columba.core.gui.frame.ContentPane#getComponent()
	 */
	public JComponent getComponent() {
		JScrollPane treeScrollPane = new JScrollPane(tree.getView());
		treeScrollPane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		JScrollPane tableScrollPane = new JScrollPane(table.getView());

		JSplitPane splitPane = new UIFSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				treeScrollPane, tableScrollPane);
		splitPane.setBorder(null);

		getContainer().extendMenuFromFile(this,
				"org/columba/addressbook/action/menu.xml");
		try {
			((MenuPluginHandler) MainInterface.pluginManager
					.getHandler("org.columba.addressbook.menu"))
					.insertPlugins(getContainer().getMenu());
		} catch (PluginHandlerNotFoundException ex) {
			throw new RuntimeException(ex);
		}

		getContainer().extendToolbar(this, AddressbookInterface.config.get(
				"main_toolbar").getElement("toolbar"));

		getContainer().setInfoPanel(new ContainerInfoPanel());

		return splitPane;
	}

	/**
	 * @see org.columba.core.gui.frame.FrameMediator#getString(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public String getString(String sPath, String sName, String sID) {
		return AddressbookResourceLoader.getString(sPath, sName, sID);
	}
}