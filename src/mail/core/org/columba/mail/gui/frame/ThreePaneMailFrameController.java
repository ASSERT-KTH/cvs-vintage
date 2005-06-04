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
import org.columba.core.gui.frame.ContentPane;
import org.columba.core.gui.selection.ISelectionListener;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.gui.util.UIFSplitPane;
import org.columba.core.plugin.PluginHandlerNotFoundException;
import org.columba.core.plugin.PluginManager;
import org.columba.core.pluginhandler.MenuPluginHandler;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.config.MailConfig;
import org.columba.mail.folder.AbstractFolder;
import org.columba.mail.folder.AbstractMessageFolder;
import org.columba.mail.folder.IMailFolder;
import org.columba.mail.gui.composer.HeaderController;
import org.columba.mail.gui.infopanel.FolderInfoPanel;
import org.columba.mail.gui.table.FilterToolbar;
import org.columba.mail.gui.table.ITableController;
import org.columba.mail.gui.table.TableController;
import org.columba.mail.gui.table.action.DeleteAction;
import org.columba.mail.gui.table.selection.TableSelectionHandler;
import org.columba.mail.gui.tree.FolderTreeModel;
import org.columba.mail.gui.tree.ITreeController;
import org.columba.mail.gui.tree.TreeController;
import org.columba.mail.gui.tree.action.MoveDownAction;
import org.columba.mail.gui.tree.action.MoveUpAction;
import org.columba.mail.gui.tree.action.RenameFolderAction;
import org.columba.mail.gui.tree.selection.TreeSelectionChangedEvent;
import org.columba.mail.gui.tree.selection.TreeSelectionHandler;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author fdietz
 * 
 */
public class ThreePaneMailFrameController extends AbstractMailFrameController
		implements TreeViewOwner, TableViewOwner, ContentPane,
		ISelectionListener {

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
	public ThreePaneMailFrameController(ViewItem viewItem) {
		super(viewItem);

		treeController = new TreeController(this, FolderTreeModel.getInstance());
		tableController = new TableController(this);
		folderInfoPanel = new FolderInfoPanel();

		// create selection handlers
		TableSelectionHandler tableHandler = new TableSelectionHandler(
				tableController);
		getSelectionManager().addSelectionHandler(tableHandler);

		TreeSelectionHandler treeHandler = new TreeSelectionHandler(
				treeController.getView());
		getSelectionManager().addSelectionHandler(treeHandler);

		// table registers interest in tree selection events
		treeHandler.addSelectionListener(tableHandler);

		// also register interest in tree seleciton events
		// for updating the title
		treeHandler.addSelectionListener(this);

		filterToolbar = new FilterToolbar(tableController);

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

		// Register Alt-Up hotkey for moving up folder when folder tree or
		// table have focus
		MoveUpAction moveUpAction = new MoveUpAction(this);
		tableController.getView().getActionMap().put("ALT_UP", moveUpAction);
		tableController.getView().getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.ALT_MASK),
				"ALT_UP");

		treeController.getView().getActionMap().put("ALT_UP", moveUpAction);
		treeController.getView().getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.ALT_MASK),
				"ALT_UP");

		// Register Alt-Down hotkey for moving up folder when folder tree or
		// table have focus
		MoveDownAction moveDownAction = new MoveDownAction(this);
		tableController.getView().getActionMap()
				.put("ALT_DOWN", moveDownAction);
		tableController.getView().getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.ALT_MASK),
				"ALT_DOWN");

		treeController.getView().getActionMap().put("ALT_DOWN", moveDownAction);
		treeController.getView().getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.ALT_MASK),
				"ALT_DOWN");

		DeleteAction deleteAction = new DeleteAction(this);
		tableController.getView().getActionMap()
				.put("DEL", deleteAction);
		tableController.getView().getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
				"DEL");

		// register the markasread timer as selection listener
		((MailFrameMediator) tableController.getFrameController())
				.registerTableSelectionListener(tableController
						.getMarkAsReadTimer());

	}

	public void enableMessagePreview(boolean enable) {
		getViewItem().setBoolean("header_enabled", enable);

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

		mainSplitPane.setDividerLocation(viewItem.getIntegerWithDefault(
				"splitpanes", "main", 100));

		if (enable)
			rightSplitPane.setDividerLocation(viewItem.getIntegerWithDefault(
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
	public ITreeController getTreeController() {
		return treeController;
	}

	/**
	 * @see org.columba.mail.gui.frame.TableViewOwner#getTableController()
	 */
	public ITableController getTableController() {
		return tableController;
	}

	/**
	 * @see org.columba.core.gui.frame.ContentPane#getComponent()
	 */
	public JComponent getComponent() {
		JPanel panel = new JPanel();

		mainSplitPane = new UIFSplitPane();
		mainSplitPane.setBorder(null);

		panel.setLayout(new BorderLayout());

		panel.add(mainSplitPane, BorderLayout.CENTER);

		mainSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);

		JScrollPane treeScrollPane = new JScrollPane(treeController.getView());

		// treeScrollPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1,
		// 1));
		mainSplitPane.add(treeScrollPane, JSplitPane.LEFT);

		messagePanel = new JPanel();
		messagePanel.setLayout(new BorderLayout());
		messagePanel.add(messageController, BorderLayout.CENTER);

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

		if (viewItem
				.getBooleanWithDefault("splitpanes", "header_enabled", true)) {

			rightSplitPane = new UIFSplitPane();
			rightSplitPane.setBorder(null);
			rightSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			rightSplitPane.add(tablePanel, JSplitPane.LEFT);
			rightSplitPane.add(messagePanel, JSplitPane.RIGHT);

			mainSplitPane.add(rightSplitPane, JSplitPane.RIGHT);
		} else {
			mainSplitPane.add(tablePanel, JSplitPane.RIGHT);
		}

		getContainer().setInfoPanel(folderInfoPanel);

		int count = MailConfig.getInstance().getAccountList().count();

		if (count == 0) {
			// pack();
			rightSplitPane.setDividerLocation(150);
		} else {
			mainSplitPane.setDividerLocation(viewItem.getIntegerWithDefault(
					"splitpanes", "main", 100));

			if (viewItem.getBooleanWithDefault("splitpanes", "header_enabled",
					true))
				rightSplitPane.setDividerLocation(viewItem
						.getIntegerWithDefault("splitpanes", "header", 100));
		}

		try {
			((MenuPluginHandler) PluginManager.getInstance().getHandler(
					"org.columba.mail.menu")).insertPlugins(getContainer()
					.getMenu());
		} catch (PluginHandlerNotFoundException ex) {
			throw new RuntimeException(ex);
		}

		getContainer().extendMenuFromFile(this,
				"org/columba/mail/action/menu.xml");

		getContainer().extendToolbar(
				this,
				MailConfig.getInstance().get("main_toolbar").getElement(
						"toolbar"));

		tableController.createPopupMenu();
		treeController.createPopupMenu();
		messageController.createPopupMenu();

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
		viewItem.setInteger("splitpanes", "main", mainSplitPane
				.getDividerLocation());

		if (rightSplitPane != null)
			viewItem.setInteger("splitpanes", "header", rightSplitPane
					.getDividerLocation());
		viewItem.setBoolean("splitpanes", "header_enabled",
				rightSplitPane != null);

		IMailFolderCommandReference r = getTreeSelection();

		if (r != null) {
			IMailFolder folder = (IMailFolder) r.getSourceFolder();

			// folder-based configuration

			if (folder instanceof AbstractMessageFolder)
				getFolderOptionsController().save(
						(AbstractMessageFolder) folder);
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

	/**
	 * @see org.columba.core.gui.frame.FrameMediator#getContentPane()
	 */
	public ContentPane getContentPane() {
		return this;
	}

	/**
	 * @see org.columba.core.gui.selection.ISelectionListener#selectionChanged(org.columba.core.gui.selection.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent e) {
		TreeSelectionChangedEvent event = (TreeSelectionChangedEvent) e;

		AbstractFolder[] selectedFolders = event.getSelected();
		if (selectedFolders.length == 1 && selectedFolders[0] != null) {
			getContainer().getFrame().setTitle(selectedFolders[0].getName());
		}
	}
}