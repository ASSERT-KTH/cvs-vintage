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
package org.columba.mail.gui.table;

import java.util.Observable;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreePath;

import org.columba.mail.folder.IMailbox;
import org.columba.mail.message.IHeaderList;

/**
 * @author fdietz
 *
 */
public interface ITableController {
	/**
	 * Get view of table controller
	 * 
	 * @return table view
	 */
	//TableView getView();

	/**
	 * Get table model
	 * 
	 * @return table model
	 */
	IHeaderTableModel getHeaderTableModel();

	/**
	 * Show the headerlist of currently selected folder.
	 * <p>
	 * Additionally, implements folderoptions plugin entrypoint.
	 * 
	 * @see org.columba.mail.folder.folderoptions
	 * @see org.columba.mail.gui.frame.ViewHeaderListInterface#showHeaderList(org.columba.mail.folder.Folder,
	 *      org.columba.mail.message.HeaderList)
	 */
	void showHeaderList(IMailbox folder, IHeaderList headerList)
			throws Exception;
	
	
	IMessageNode[] getSelectedNodes();
	void setSelected(Object[] uids);
	int[] getSelectedRows();
	TreePath getPathForRow(int row);
	
	int getRowCount();
	
	Object selectFirstRow();
	Object selectLastRow();
	void selectRow(int row);
	void clearSelection();
	void makeSelectedRowVisible();
	
	IMessageNode getMessageNode(Object uid);
	
	void enableThreadedView(boolean enableThreadedMode, boolean updateModel);
	boolean isThreadedViewEnabled();
	
	Observable getSortingStateObservable();
	void setSortingOrder(boolean order);
	void setSortingColumn(String column);	
	String getSortingColumn();
	boolean getSortingOrder();
	
	void clear();
	
	TableColumnModel getColumnModel();
	void resetColumnModel();
	TableColumn createTableColumn(String  name, int size);
	void addColumn(TableColumn column);
	
	ListSelectionModel getListSelectionModel();
	
	public void addMessageListSelectionListener(IMessageListSelectionListener l);

	public void removeMessageListSelectionListener(IMessageListSelectionListener l);
}