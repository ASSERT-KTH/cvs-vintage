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
package org.columba.mail.gui.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;

import org.columba.core.config.ViewItem;
import org.columba.core.gui.frame.Container;
import org.columba.core.gui.frame.ContentPane;
import org.columba.core.gui.util.UIFSplitPane;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.PluginHandlerNotFoundException;
import org.columba.core.pluginhandler.MenuPluginHandler;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.AbstractFolder;
import org.columba.mail.gui.attachment.selection.AttachmentSelectionHandler;
import org.columba.mail.gui.composer.HeaderController;
import org.columba.mail.gui.infopanel.FolderInfoPanel;
import org.columba.mail.gui.table.FilterToolbar;
import org.columba.mail.gui.table.TableController;
import org.columba.mail.gui.table.selection.TableSelectionHandler;
import org.columba.mail.gui.tree.TreeController;
import org.columba.mail.gui.tree.action.ApplyFilterAction;
import org.columba.mail.gui.tree.action.RenameFolderAction;
import org.columba.mail.gui.tree.selection.TreeSelectionHandler;
import org.columba.mail.main.MailInterface;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author fdietz
 *  
 */
public class ThreePaneMailFrameController extends AbstractMailFrameController
		implements TreeViewOwner, TableViewOwner, ContentPane {

	public TreeController treeController;

	public TableController tableController;

	public HeaderController headerController;

	public FilterToolbar filterToolbar;

	public JSplitPane mainSplitPane;

	public JSplitPane rightSplitPane;

	private JPanel tablePanel;

	private JPanel messagePanel;

	public FolderInfoPanel folderInfoPanel;

	/**
	 * @param container
	 */
	public ThreePaneMailFrameController(Container container, ViewItem viewItem) {
		super(container, viewItem);

		TableUpdater.add(this);

		treeController = new TreeController(this, MailInterface.treeModel);
		tableController = new TableController(this);
		folderInfoPanel = new FolderInfoPanel();

		filterToolbar = new FilterToolbar(tableController);

		// create selection handlers
		TableSelectionHandler tableHandler = new TableSelectionHandler(
				tableController);
		getSelectionManager().addSelectionHandler(tableHandler);

		TreeSelectionHandler treeHandler = new TreeSelectionHandler(
				treeController.getView());
		getSelectionManager().addSelectionHandler(treeHandler);

		// table registers interest in tree selection events
		treeHandler.addSelectionListener(tableHandler);

		getSelectionManager().addSelectionHandler(
				new AttachmentSelectionHandler(attachmentController.getView()));

		container.extendMenuFromFile(this, "org/columba/mail/action/menu.xml");

		try {
			((MenuPluginHandler) MainInterface.pluginManager
					.getHandler("org.columba.mail.menu"))
					.insertPlugins(container.getMenu());
		} catch (PluginHandlerNotFoundException ex) {
			throw new RuntimeException(ex);
		}

		container.extendToolbar(this, MailInterface.config.get("main_toolbar")
				.getElement("toolbar"));

		tableController.createPopupMenu();
		treeController.createPopupMenu();
		messageController.createPopupMenu();
		attachmentController.createPopupMenu();

		//getContainer().setContentPane(this);

		RenameFolderAction renameFolderAction = new RenameFolderAction(this);

		// Register F2 hotkey for renaming folder when the message panel has
		// focus
		tableController.getView().getActionMap().put("F2", renameFolderAction);
		tableController.getView().getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "F2");

		// Register F2 hotkey for renaming folder when the folder tree itself
		// has focus
		treeController.getView().getActionMap().put("F2", renameFolderAction);
		treeController.getView().getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "F2");

		ApplyFilterAction applyFilterAction = new ApplyFilterAction(this);

		// Register ALT-A hotkey for apply filter on folder when the folder tree
		// itself has focus
		treeController.getView().getActionMap().put("ALT_A", applyFilterAction);
		treeController.getView().getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.ALT_DOWN_MASK),
				"ALT_A");
		tableController.getView().getActionMap()
				.put("ALT_A", applyFilterAction);
		tableController.getView().getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.ALT_DOWN_MASK),
				"ALT_A");

		//register the markasread timer as selection listener
		((MailFrameMediator) tableController.getFrameController())
				.registerTableSelectionListener(tableController
						.getMarkAsReadTimer());

		getContainer().setContentPane(this);
	}

	public void enableMessagePreview(boolean enable) {
		getViewItem().set("header_enabled", enable);
		
		if (enable) {
			rightSplitPane = new UIFSplitPane();
			rightSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			rightSplitPane.add(tablePanel, JSplitPane.LEFT);
			rightSplitPane.add(messagePanel, JSplitPane.RIGHT);

			mainSplitPane.add(rightSplitPane, JSplitPane.RIGHT);
		} else {
			rightSplitPane = null;
			
			mainSplitPane.add(tablePanel, JSplitPane.RIGHT);
		}
		
		mainSplitPane.setDividerLocation(viewItem.getInteger("splitpanes",
				"main", 100));

		if (enable)
			rightSplitPane.setDividerLocation(viewItem.getInteger(
					"splitpanes", "header", 100));
		
		getContainer().getFrame().validate();
	}

	/**
	 * @return Returns the filterToolbar.
	 */
	public FilterToolbar getFilterToolbar() {
		return filterToolbar;
	}

	/**
	 * @see org.columba.mail.gui.frame.TreeViewOwner#getTreeController()
	 */
	public TreeController getTreeController() {
		return treeController;
	}

	/**
	 * @see org.columba.mail.gui.frame.TableViewOwner#getTableController()
	 */
	public TableController getTableController() {
		return tableController;
	}

	/**
	 * @see org.columba.core.gui.frame.ContentPane#getComponent()
	 */
	public JComponent getComponent() {
		JPanel panel = new JPanel();

		mainSplitPane = new UIFSplitPane();

		panel.setLayout(new BorderLayout());

		//mainSplitPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		panel.add(mainSplitPane, BorderLayout.CENTER);

		mainSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);

		JScrollPane treeScrollPane = new JScrollPane(treeController.getView());

		//treeScrollPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1,
		// 1));
		mainSplitPane.add(treeScrollPane, JSplitPane.LEFT);

		messagePanel = new JPanel();
		messagePanel.setLayout(new BorderLayout());
		messagePanel.add(messageController.getView(), BorderLayout.CENTER);

		tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());

		ViewItem viewItem = getViewItem();

		tablePanel.add(filterToolbar, BorderLayout.NORTH);

		JScrollPane tableScrollPane = new JScrollPane(tableController.getView());
		tableScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		tableScrollPane.getViewport().setScrollMode(
				JViewport.BACKINGSTORE_SCROLL_MODE);

		tableScrollPane.getViewport().setBackground(Color.white);
		tablePanel.add(tableScrollPane, BorderLayout.CENTER);

		if (viewItem.getBoolean("splitpanes", "header_enabled", true)) {

			rightSplitPane = new UIFSplitPane();
			rightSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			rightSplitPane.add(tablePanel, JSplitPane.LEFT);
			rightSplitPane.add(messagePanel, JSplitPane.RIGHT);

			mainSplitPane.add(rightSplitPane, JSplitPane.RIGHT);
		} else {
			mainSplitPane.add(tablePanel, JSplitPane.RIGHT);
		}

		getContainer().setInfoPanel(folderInfoPanel);
		/*
		 * // same as menu if (((AbstractFrameController) frameController)
		 * .isToolbarEnabled(MailFrameView.FOLDERINFOPANEL) == true) {
		 * toolbarPane.add(folderInfoPanel); }
		 */

		int count = MailInterface.config.getAccountList().count();

		if (count == 0) {
			//pack();
			rightSplitPane.setDividerLocation(150);
		} else {
			mainSplitPane.setDividerLocation(viewItem.getInteger("splitpanes",
					"main", 100));

			if (viewItem.getBoolean("splitpanes", "header_enabled", true))
				rightSplitPane.setDividerLocation(viewItem.getInteger(
						"splitpanes", "header", 100));
		}

		return panel;
	}

	public void showFilterToolbar() {
		tablePanel.add(filterToolbar, BorderLayout.NORTH);
		tablePanel.validate();

	}

	public void hideFilterToolbar() {
		tablePanel.remove(filterToolbar);
		tablePanel.validate();

	}

	public void savePositions(ViewItem viewItem) {
		super.savePositions(viewItem);

		// splitpanes
		viewItem.set("splitpanes", "main", mainSplitPane.getDividerLocation());

		if (rightSplitPane != null)
			viewItem.set("splitpanes", "header", rightSplitPane
					.getDividerLocation());
		viewItem.set("splitpanes", "header_enabled", rightSplitPane != null);

		FolderCommandReference r = getTreeSelection();

		if (r != null) {
			AbstractFolder folder = r.getFolder();

			// folder-based configuration
			getFolderOptionsController().saveGlobalSettings(folder);
		}
	}

	/**
	 * @return Returns the folderInfoPanel.
	 */
	public FolderInfoPanel getFolderInfoPanel() {
		return folderInfoPanel;
	}

	/**
	 * @see org.columba.core.gui.frame.FrameMediator#getString(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public String getString(String sPath, String sName, String sID) {
		return MailResourceLoader.getString(sPath, sName, sID);
	}
}