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
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.columba.api.gui.frame.IContentPane;
import org.columba.core.config.ViewItem;
import org.columba.core.gui.base.UIFSplitPane;
import org.columba.core.gui.selection.ISelectionListener;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.io.DiskIO;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.config.MailConfig;
import org.columba.mail.folder.IMailFolder;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.composer.HeaderController;
import org.columba.mail.gui.infopanel.FolderInfoPanel;
import org.columba.mail.gui.message.action.ViewMessageAction;
import org.columba.mail.gui.table.FilterToolbar;
import org.columba.mail.gui.table.ITableController;
import org.columba.mail.gui.table.TableController;
import org.columba.mail.gui.table.action.DeleteAction;
import org.columba.mail.gui.table.action.OpenMessageWithComposerAction;
import org.columba.mail.gui.table.action.OpenMessageWithMessageFrameAction;
import org.columba.mail.gui.table.action.ViewHeaderListAction;
import org.columba.mail.gui.table.model.HeaderTableModel;
import org.columba.mail.gui.table.model.MessageNode;
import org.columba.mail.gui.table.selection.TableSelectionChangedEvent;
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
		implements TreeViewOwner, TableViewOwner, IContentPane,
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
	 * true, if the messagelist table selection event was triggered by a popup
	 * event. False, otherwise.
	 */
	public boolean isTablePopupEvent;

	/**
	 * true, if the tree selection event was triggered by a popup event. False,
	 * otherwise.
	 */
	public boolean isTreePopupEvent;

	/**
	 * @param container
	 */
	public ThreePaneMailFrameController(ViewItem viewItem) {
		super(viewItem);

		treeController = new TreeController(this, FolderTreeModel.getInstance());
		tableController = new TableController(this);

		// create selection handlers
		TableSelectionHandler tableHandler = new TableSelectionHandler(
				tableController);
		getSelectionManager().addSelectionHandler(tableHandler);
		tableHandler.addSelectionListener(this);

		TreeSelectionHandler treeHandler = new TreeSelectionHandler(
				treeController.getView());
		getSelectionManager().addSelectionHandler(treeHandler);

		// double-click mouse listener
		tableController.getView().addMouseListener(new TableMouseListener());

		treeController.getView().addMouseListener(new TreeMouseListener());

		folderInfoPanel = new FolderInfoPanel(this);

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
		tableController.getView().getActionMap().put("DEL", deleteAction);
		tableController.getView().getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DEL");

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
	 * @see org.columba.api.gui.frame.IContentPane#getComponent()
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

		// TODO implement
		// try {
		// ((MenuPluginHandler) PluginManager.getInstance().getHandler(
		// MenuPluginHandler.NAME)).insertPlugins(getContainer()
		// .getMenu());
		// } catch (PluginHandlerNotFoundException ex) {
		// throw new RuntimeException(ex);
		// }

		try {
			InputStream is = DiskIO
					.getResourceStream("org/columba/mail/action/menu.xml");

			getContainer().extendMenu(this, is);
		} catch (IOException e) {
			e.printStackTrace();
		}

		getContainer().extendToolbar(
				this,
				MailConfig.getInstance().get("main_toolbar").getElement(
						"toolbar"));

		tableController.createPopupMenu();
		treeController.createPopupMenu();
		messageController.createPopupMenu();

		// TODO: fixme
		// JFrame frame = (JFrame) getContainer().getFrame();
		// ColumbaMenu menu = (ColumbaMenu) frame.getJMenuBar();
		// menu.addMenuItem("my_reply_action_id", new ReplyAction(this),
		// ColumbaMenu.MENU_VIEW, ColumbaMenu.PLACEHOLDER_BOTTOM);

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

			if (folder instanceof IMailbox)
				getFolderOptionsController().save((IMailbox) folder);
		}
	}

	/**
	 * @return Returns the folderInfoPanel.
	 */
	public FolderInfoPanel getFolderInfoPanel() {
		return folderInfoPanel;
	}

	/**
	 * @see org.columba.api.gui.frame.IFrameMediator#getString(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public String getString(String sPath, String sName, String sID) {
		return MailResourceLoader.getString(sPath, sName, sID);
	}

	/**
	 * @see org.columba.api.gui.frame.IFrameMediator#getContentPane()
	 */
	public IContentPane getContentPane() {
		return this;
	}

	/**
	 * @see org.columba.core.gui.selection.ISelectionListener#selectionChanged(org.columba.core.gui.selection.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent e) {

		if (e instanceof TreeSelectionChangedEvent) {
			// tree selection event
			TreeSelectionChangedEvent event = (TreeSelectionChangedEvent) e;

			IMailFolder[] selectedFolders = event.getSelected();

			if (isTreePopupEvent == false) {
				// view headerlist in message list viewer
				new ViewHeaderListAction(this).actionPerformed(null);

				getFolderInfoPanel().selectionChanged(e);

				// update frame title
				if (selectedFolders.length == 1 && selectedFolders[0] != null) {
					getContainer().getFrame().setTitle(
							selectedFolders[0].getName());
				} else {
					getContainer().getFrame().setTitle("");
				}
			}

			isTreePopupEvent = false;

		} else if (e instanceof TableSelectionChangedEvent) {
			// messagelist table selection event
			TableSelectionChangedEvent event = (TableSelectionChangedEvent) e;

			if (isTablePopupEvent == false)
				// show message content
				new ViewMessageAction(this).actionPerformed(null);

			isTablePopupEvent = false;
		} else
			throw new IllegalArgumentException(
					"unknown selection changed event");
	}

	/**
	 * Double-click mouse listener for message list table component.
	 * <p>
	 * If message is marked as draft, the composer will be opened to edit the
	 * message. Otherwise, the message will be viewed in the message frame.
	 * 
	 * @author Frederik Dietz
	 */
	class TableMouseListener extends MouseAdapter {

		/**
		 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent event) {
			if (event.isPopupTrigger()) {
				processPopup(event);
			}
		}

		/**
		 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent event) {
			if (event.isPopupTrigger()) {
				processPopup(event);
			}
		}

		/**
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent event) {
			// if mouse button was pressed twice times
			if (event.getClickCount() == 2) {
				// get selected row
				int selectedRow = tableController.getView().getSelectedRow();

				// get message node at selected row
				MessageNode node = (MessageNode) ((HeaderTableModel) tableController
						.getHeaderTableModel())
						.getMessageNodeAtRow(selectedRow);

				// is the message marked as draft ?
				boolean markedAsDraft = node.getHeader().getFlags().getDraft();

				if (markedAsDraft) {
					// edit message in composer
					new OpenMessageWithComposerAction(
							ThreePaneMailFrameController.this)
							.actionPerformed(null);
				} else {
					// open message in new message-frame
					new OpenMessageWithMessageFrameAction(
							ThreePaneMailFrameController.this)
							.actionPerformed(null);
				}
			}
		}

		protected void processPopup(final MouseEvent event) {

			isTablePopupEvent = true;

			JTable table = tableController.getView();

			int selectedRows = table.getSelectedRowCount();

			if (selectedRows <= 1) {
				// select node
				int row = table
						.rowAtPoint(new Point(event.getX(), event.getY()));
				table.setRowSelectionInterval(row, row);
			}

			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					tableController.getPopupMenu().show(event.getComponent(),
							event.getX(), event.getY());
				}
			});
		}
	}

	class TreeMouseListener extends MouseAdapter {

		/**
		 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent event) {
			if (event.isPopupTrigger()) {
				processPopup(event);
			}
		}

		/**
		 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent event) {
			if (event.isPopupTrigger()) {
				processPopup(event);
			}
		}

		/**
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent event) {
			// if mouse button was pressed twice times
			if (event.getClickCount() == 2) {
				// get selected row

			}
		}

		protected void processPopup(final MouseEvent event) {

			isTreePopupEvent = true;

			Point point = event.getPoint();
			TreePath path = treeController.getView().getClosestPathForLocation(
					point.x, point.y);
			treeController.getView().setSelectionPath(path);

			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					treeController.getPopupMenu().show(event.getComponent(),
							event.getX(), event.getY());
				}
			});
		}
	}
}