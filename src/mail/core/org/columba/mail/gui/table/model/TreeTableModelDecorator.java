/*
 * Created on 31.07.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.table.model;

import java.util.Map;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultTreeModel;

import org.columba.mail.message.HeaderList;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class TreeTableModelDecorator
	implements TableModel, TreeTableModelInterface {

	private TreeTableModelInterface realModel; // We're decorating this model

	public TreeTableModelDecorator(TreeTableModelInterface model) {
		this.realModel = model;

	}

	//	The following 9 methods are defined by the TableModel
	// interface; all of those methods forward to the real model.

	public void addTableModelListener(TableModelListener l) {
		realModel.addTableModelListener(l);
	}

	public void removeTableModelListener(TableModelListener l) {
		realModel.removeTableModelListener(l);
	}

	public Class getColumnClass(int columnIndex) {
		return realModel.getColumnClass(columnIndex);
	}
	public int getColumnCount() {
		return realModel.getColumnCount();
	}
	public String getColumnName(int columnIndex) {
		return realModel.getColumnName(columnIndex);
	}
	public int getRowCount() {
		return realModel.getRowCount();
	}
	public Object getValueAt(int rowIndex, int columnIndex) {
		return realModel.getValueAt(rowIndex, columnIndex);
	}
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return realModel.isCellEditable(rowIndex, columnIndex);
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		realModel.setValueAt(aValue, rowIndex, columnIndex);
	}

	/**************************** treetable model features *******************/

	public MessageNode getRootNode() {
		return realModel.getRootNode();
	}

	public Map getMap() {
		return realModel.getMap();
	}

	public HeaderList getHeaderList() {
		return realModel.getHeaderList();
	}

	public int getColumnNumber(String s) {
		return realModel.getColumnNumber(s);
	}

	public DefaultTreeModel getTreeModel() {
		return realModel.getTreeModel();
	}

	public void fireTableDataChanged() {
		realModel.fireTableDataChanged();
	}

	/****************************** overwrite *****************************/

	// overwrite the following methods to provide custom implementations

	// The getRealModel method is used by subclasses to
	// access the real model.
	protected TreeTableModelInterface getRealModel() {
		return realModel;
	}

	

	/* (non-Javadoc)
	 * @see org.columba.mail.gui.table.model.TableModelModifier#modify(java.lang.Object[])
	 */
	public void modify(Object[] uids) {
		getRealModel().modify(uids);

	}

	/* (non-Javadoc)
	 * @see org.columba.mail.gui.table.model.TableModelModifier#remove(java.lang.Object[])
	 */
	public void remove(Object[] uids) {
		getRealModel().remove(uids);

	}

	/* (non-Javadoc)
	 * @see org.columba.mail.gui.table.model.TableModelModifier#set(org.columba.mail.message.HeaderList)
	 */
	public void set(HeaderList headerList) {
		getRealModel().set(headerList);

	}

	/* (non-Javadoc)
	 * @see org.columba.mail.gui.table.model.TableModelModifier#update()
	 */
	public void update() {
		getRealModel().update();

	}

}
