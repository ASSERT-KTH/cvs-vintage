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

package org.columba.mail.gui.table;

import java.awt.Rectangle;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;

import org.columba.core.config.HeaderItem;
import org.columba.core.config.TableItem;
import org.columba.core.gui.util.CScrollPane;
import org.columba.core.gui.util.treetable.Tree;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;
import org.columba.core.util.SwingWorker;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.config.MailConfig;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.FolderTreeNode;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.frame.ThreePaneMailFrameController;
import org.columba.mail.gui.table.action.HeaderTableActionListener;
import org.columba.mail.gui.table.selection.TableSelectionManager;
import org.columba.mail.gui.table.util.MarkAsReadTimer;
import org.columba.mail.gui.table.util.MessageNode;
import org.columba.mail.message.HeaderList;

/**
 * This class shows the messageheaderlist
 *
 *
 * @version 0.9.1
 * @author Frederik
 */

public class TableController {

	private TableView headerTable;
	private HeaderTableModel headerTableModel;

	public CScrollPane scrollPane;
	private SwingWorker worker;
	private Folder folder;
	private HeaderList headerList;

	private FilterToolbar filterToolbar;

	private HeaderTableActionListener headerTableActionListener;

	private MessageNode[] messageNodes;

	private MessageNode node;
	private MessageNode oldNode;

	private Object[] selection;

	private HeaderTableMouseListener headerTableMouseListener;
	private HeaderTableDnd headerTableDnd;
	private HeaderTableFocusListener headerTableFocusListener;
	private HeaderItemActionListener headerItemActionListener;
	private FilterActionListener filterActionListener;

	private TableItem headerTableItem;

	private boolean folderChanged = false;

	private int counter = 1;

	//protected SelectionManager selectionManager;
	protected TableView view;
	protected HeaderTableActionListener actionListener;

	protected TableSelectionManager tableSelectionManager;

	protected AbstractMailFrameController mailFrameController;

	protected Object[] newUidList;

	protected MarkAsReadTimer markAsReadTimer;

	protected Vector tableChangedListenerList;

	protected TableMenu menu;
	public TableController(AbstractMailFrameController mailFrameController) {

		this.mailFrameController = mailFrameController;

		headerTableItem =
			(TableItem) MailConfig.getMainFrameOptionsConfig().getTableItem();

		headerTableModel = new HeaderTableModel(headerTableItem);

		view = new TableView(headerTableModel);
		headerTableModel.setTree((Tree) view.getTree());

		tableSelectionManager = new TableSelectionManager();

		tableChangedListenerList = new Vector();

		actionListener = new HeaderTableActionListener(this);

		headerTableMouseListener = new HeaderTableMouseListener(this);
		view.addMouseListener(headerTableMouseListener);

		headerItemActionListener =
			new HeaderItemActionListener(this, headerTableItem);
		filterActionListener = new FilterActionListener(this);

		markAsReadTimer = new MarkAsReadTimer(this);

		getHeaderTableModel().getTableModelSorter().setSortingColumn(
			headerTableItem.get("selected"));
		getHeaderTableModel().getTableModelSorter().setSortingOrder(
			headerTableItem.getBoolean("ascending"));

		getView().setTransferHandler(new MessageTransferHandler(this));

		getView().setDragEnabled(true);

	}

	public boolean isAscending() {
		return getHeaderTableModel().getTableModelSorter().getSortingOrder();
	}

	public void addTableChangedListener(TableChangeListener l) {
		tableChangedListenerList.add(l);
	}

	public void fireTableChangedEvent(TableChangedEvent e) {
		for (Iterator it = tableChangedListenerList.iterator();
			it.hasNext();
			) {
			TableChangeListener l = (TableChangeListener) it.next();
			// for (int i = 0; i < tableChangedListenerList.size(); i++) {
			// TableChangeListener l =
			// (TableChangeListener) tableChangedListenerList.get(i);
			l.tableChanged(e);
		}
	}

	public TableView getView() {

		return view;
	}

	public HeaderTableMouseListener getHeaderTableMouseListener() {
		return headerTableMouseListener;
	}

	public boolean hasFocus() {
		return headerTableFocusListener.hasFocus();
	}

	/**
	 * return FilterToolbar
	 */

	public FilterToolbar getFilterToolbar() {
		return filterToolbar;
	}

	/**
	 * return HeaderTableItem
	 */
	public TableItem getHeaderTableItem() {
		return headerTableItem;
	}

	/**
	 * set the render for each column
	 */

	/**
	 * return the ActionListener
	 *
	 */
	public HeaderTableActionListener getActionListener() {
		return actionListener;
	}

	/**
	 * save the column state:
	 *  - position
	 *  - size
	 *  - appearance
	 * of every column
	 */

	public void saveColumnConfig() {
		TableItem tableItem =
			(TableItem) MailConfig.getMainFrameOptionsConfig().getTableItem();

		boolean ascending =
			getHeaderTableModel().getTableModelSorter().getSortingOrder();
		String sortingColumn =
			getHeaderTableModel().getTableModelSorter().getSortingColumn();

		tableItem.set("ascending", ascending);
		tableItem.set("selected", sortingColumn);

		if (MainInterface.DEBUG) {
			ColumbaLogger.log.info("save table column config");
		}

		//.clone();
		//v.removeEnabledItem();

		for (int i = 0; i < tableItem.getChildCount(); i++) {
			HeaderItem v = tableItem.getHeaderItem(i);
			boolean enabled = v.getBoolean("enabled");
			if (enabled == false)
				continue;

			String c = v.get("name");
			ColumbaLogger.log.debug("name=" + c);

			TableColumn tc = getView().getColumn(c);

			v.set("size", tc.getWidth());
			if (MainInterface.DEBUG) {
				ColumbaLogger.log.debug("size" + tc.getWidth());
			}
			try {
				int index = getView().getColumnModel().getColumnIndex(c);
				v.set("position", index);
			} catch (IllegalArgumentException ex) {
				ex.printStackTrace();
			}

		}

	}

	/**
	 * set folder to show
	 */
	public void setFolder(Folder f) {
		this.folder = f;
	}

	/**
	 * return currently showed folder
	 */
	public Folder getFolder() {
		return folder;
	}

	/**
	 * return HeaderTable widget which does all the dirty work
	 */
	public TableView getHeaderTable() {
		return headerTable;
	}

	/**
	 * return the Model which contains a HeaderList
	 */
	public HeaderTableModel getHeaderTableModel() {
		return headerTableModel;

	}

	public void clearMessageNodeList() {
		messageNodes = null;
	}

	/**
	 * return ActionListener for the headeritem sorting
	 */
	public HeaderItemActionListener getHeaderItemActionListener() {
		return headerItemActionListener;
	}

	/**
	 * return ActionListener for FilterToolbar
	 */
	public FilterActionListener getFilterActionListener() {
		return filterActionListener;
	}

	public void setSelected(Object[] uids) {
		MessageNode[] nodes = new MessageNode[uids.length];

		for (int i = 0; i < uids.length; i++) {
			nodes[i] = getHeaderTableModel().getMessageNode(uids[i]);
		}

		TreePath[] paths = new TreePath[nodes.length];

		for (int i = 0; i < nodes.length; i++) {
			paths[i] = new TreePath(nodes[i].getPath());

		}

		view.getTree().setSelectionPaths(paths);

		getTableSelectionManager().fireMessageSelectionEvent(null, uids);
	}

	public void createPopupMenu() {
		menu = new TableMenu(mailFrameController);
	}

	/**
	 * return the PopupMenu for the table
	 */
	public JPopupMenu getPopupMenu() {

		return menu;
	}

	/************************** actions ********************************/

	/**
	 * create the PopupMenu
	 */

	/**
	 * MouseListener sorts table when clicking on a column header
	 */

	// method is called when folder data changed
	// the method updates the model

	public void tableChanged(TableChangedEvent event) throws Exception {
		if (MainInterface.DEBUG) {
			ColumbaLogger.log.info("event=" + event);
		}

		FolderTreeNode folder = event.getSrcFolder();

		if (folder == null) {
			if (event.getEventType() == TableChangedEvent.UPDATE)
				getHeaderTableModel().update();

			fireTableChangedEvent(event);
			return;
		}

		FolderCommandReference[] r =
			(FolderCommandReference[]) mailFrameController
				.getSelectionManager()
				.getSelection("mail.table");
		Folder srcFolder = (Folder) r[0].getFolder();

		if (!folder.equals(srcFolder))
			return;
		//System.out.println("headertableviewer->folderChanged");

		switch (event.getEventType()) {
			case TableChangedEvent.UPDATE :
				{
					getHeaderTableModel().update();

					/*
					HeaderInterface[] headerList = event.getHeaderList();
					
					getHeaderTableModel()
								.setHeaderList(headerList);
					*/
					break;
				}
			case TableChangedEvent.ADD :
				{
					getHeaderTableModel().addHeaderList(event.getHeaderList());

					break;
				}
			case TableChangedEvent.REMOVE :
				{
					getHeaderTableModel().removeHeaderList(event.getUids());
					break;
				}
			case TableChangedEvent.MARK :
				{

					getHeaderTableModel().markHeader(
						event.getUids(),
						event.getMarkVariant());

					break;
				}
		}

		// TODO: fix folderInfoPanel

		if (getMailFrameController() instanceof ThreePaneMailFrameController)
			((ThreePaneMailFrameController) getMailFrameController())
				.folderInfoPanel
				.setFolder(srcFolder);

		fireTableChangedEvent(event);
	}

	/**
	 * Returns the tableSelectionManager.
	 * @return TableSelectionManager
	 */
	public TableSelectionManager getTableSelectionManager() {
		return tableSelectionManager;
	}

	/**
	 * Returns the mailFrameController.
	 * @return MailFrameController
	 */
	public AbstractMailFrameController getMailFrameController() {
		return mailFrameController;
	}

	/**
	 * Returns the markAsReadTimer.
	 * @return MarkAsReadTimer
	 */
	public MarkAsReadTimer getMarkAsReadTimer() {
		return markAsReadTimer;
	}

	/* (non-Javadoc)
		 * @see org.columba.mail.gui.frame.ViewHeaderListInterface#showHeaderList(org.columba.mail.folder.Folder, org.columba.mail.message.HeaderList)
		 */
	public void showHeaderList(Folder folder, HeaderList headerList)
		throws Exception {

		getHeaderTableModel().setHeaderList(headerList);

		boolean enableThreadedView =
			folder.getFolderItem().getBoolean(
				"property",
				"enable_threaded_view",
				false);

		getView().enableThreadedView(enableThreadedView);

		getView().getTableModelThreadedView().toggleView(enableThreadedView);

		TableChangedEvent ev =
			new TableChangedEvent(TableChangedEvent.UPDATE, folder);

		tableChanged(ev);

		boolean ascending = isAscending();
		int row = getView().getTree().getRowCount();

		getView().clearSelection();

		JViewport viewport =
			((JScrollPane) getView().getParent().getParent()).getViewport();

		//getView().scrollRectToVisible(new Rectangle(0, 0, 0, 0));

		getView().revalidate();

		viewport.validate();

		System.out.println("table: " + getView().getRowCount());
		System.out.println("height: " + getView().getHeight());

		System.out.println("tree: " + getView().getTree().getRowCount());
		System.out.println("height: " + getView().getTree().getHeight());
		System.out.println(
			"row*rowheight: "
				+ (getView().getRowCount() * getView().getRowHeight()));

		Rectangle rect =
			new Rectangle(
				getView().getSize().width,
				getView().getSize().height,
				5,
				40);
		if (!ascending)
			getView().scrollRectToVisible(new Rectangle(0, 0, 0, 0));
		else
			getView().scrollRectToVisible(rect);
		//getView().getCellRect(row - 1, 0, false));

	}
}
