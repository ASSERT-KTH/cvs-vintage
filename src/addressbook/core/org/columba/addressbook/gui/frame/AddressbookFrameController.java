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

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.columba.addressbook.config.AddressbookConfig;
import org.columba.addressbook.folder.AddressbookTreeNode;
import org.columba.addressbook.gui.table.FilterToolbar;
import org.columba.addressbook.gui.table.TableController;
import org.columba.addressbook.gui.tree.TreeController;
import org.columba.addressbook.util.AddressbookResourceLoader;
import org.columba.api.gui.frame.IContainer;
import org.columba.core.config.ViewItem;
import org.columba.core.gui.docking.DockableView;
import org.columba.core.gui.frame.DockFrameController;
import org.columba.core.io.DiskIO;
import org.flexdock.docking.DockingConstants;
import org.flexdock.perspective.LayoutSequence;
import org.flexdock.perspective.Perspective;

/**
 * 
 * 
 * @author fdietz
 */
public class AddressbookFrameController extends DockFrameController implements
		AddressbookFrameMediator, TreeSelectionListener {

	protected TreeController tree;

	protected TableController table;

	protected FilterToolbar filterToolbar;

	private DockableView contactListPanel;

	private DockableView treePanel;

	/**
	 * Constructor for AddressbookController.
	 */
	public AddressbookFrameController(ViewItem viewItem) {
		super(viewItem);

		tree = new TreeController(this);
		table = new TableController(this);
		filterToolbar = new FilterToolbar(table);

		// table should be updated when tree selection changes
		tree.getView().addTreeSelectionListener(table);

		// this is needed to update the titlebar
		tree.getView().addTreeSelectionListener(this);

		// getContainer().setContentPane(this);

		registerDockables();

		//initPerspective(this.perspective);
	}

	public void registerDockables() {
		treePanel = new DockableView("addressbook_foldertree", "Folder Tree");
		JScrollPane treeScrollPane = new JScrollPane(tree.getView());
		treePanel.setContentPane(treeScrollPane);

		contactListPanel = new DockableView("addressbook_contactlist",
				"Contact List");
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(new JScrollPane(table.getView()), BorderLayout.CENTER);
		p.add(filterToolbar, BorderLayout.NORTH);
		contactListPanel.setContentPane(p);

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
	 * @see org.columba.api.gui.frame.IFrameMediator#getString(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public String getString(String sPath, String sName, String sID) {
		return AddressbookResourceLoader.getString(sPath, sName, sID);
	}

	/**
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent arg0) {
		AddressbookTreeNode selectedFolder = (AddressbookTreeNode) arg0
				.getPath().getLastPathComponent();

		if (selectedFolder != null) {
			fireTitleChanged(selectedFolder.getName());
		}
	}

	public void loadDefaultPosition() {
		 super.dock(contactListPanel, DockingConstants.CENTER_REGION);
		 contactListPanel.dock(treePanel, DockingConstants.WEST_REGION, 0.3f);
		
		 super.setSplitProportion(treePanel, 0.3f);
		 super.setSplitProportion(contactListPanel, 0.35f);
	}

//	public void initPerspective(Perspective p) {
//		LayoutSequence sequence = p.getInitialSequence(true);
//		sequence.add(treePanel);
//		sequence.add(contactListPanel, treePanel, DockingConstants.EAST_REGION,
//				0.7f);
//	}

	/** *********************** container callbacks ************* */

	public void extendMenu(IContainer container) {
		try {
			InputStream is = DiskIO
					.getResourceStream("org/columba/addressbook/action/menu.xml");
			container.extendMenu(this, is);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void extendToolBar(IContainer container) {
		try {
			File configDirectory = AddressbookConfig.getInstance()
					.getConfigDirectory();
			InputStream is2 = new FileInputStream(new File(configDirectory,
					"main_toolbar.xml"));
			container.extendToolbar(this, is2);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}