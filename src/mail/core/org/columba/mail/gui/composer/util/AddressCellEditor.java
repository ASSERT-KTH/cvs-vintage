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
package org.columba.mail.gui.composer.util;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;

import org.columba.addressbook.folder.HeaderItem;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class AddressCellEditor
	extends AddressComboBox
	implements TableCellEditor, KeyListener {

	AddressbookTableView table;

	HeaderItem selection;

	protected EventListenerList listenerList = new EventListenerList();
	protected ChangeEvent changeEvent = new ChangeEvent(this);

	boolean editing = false;

	public AddressCellEditor(AddressbookTableView table) {
		super(table);
		this.table = table;

		getEditor().getEditorComponent().addKeyListener(this);
		
		

	}

	public void addCellEditorListener(CellEditorListener listener) {
		listenerList.add(CellEditorListener.class, listener);
	}

	public void removeCellEditorListener(CellEditorListener listener) {
		listenerList.remove(CellEditorListener.class, listener);
	}

	protected void fireEditingStopped() {

		CellEditorListener listener;
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] == CellEditorListener.class) {
				listener = (CellEditorListener) listeners[i + 1];
				listener.editingStopped(changeEvent);
			}
		}

	}

	protected void fireEditingCanceled() {
		CellEditorListener listener;
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] == CellEditorListener.class) {
				listener = (CellEditorListener) listeners[i + 1];
				listener.editingCanceled(changeEvent);
			}
		}
	}

	public void cancelCellEditing() {
		fireEditingCanceled();
	}

	public boolean stopCellEditing() {

		fireEditingStopped();
		return true;
	}

	public boolean isCellEditable(EventObject event) {
		if (event instanceof MouseEvent) { 
		return ((MouseEvent)event).getClickCount() >= 2;
	    }
	    
		return true;
	}

	public boolean shouldSelectCell(EventObject event) {
		return true;
	}

	public Object getCellEditorValue() {

		return ((JTextField) getEditor().getEditorComponent()).getText();

	}

	public Component getTableCellEditorComponent(
		JTable table,
		Object value,
		boolean isSelected,
		int row,
		int column) {

		selection = (HeaderItem) value;

		setSelectedItem(selection.get("displayname"));

		return this;
	}

	/******************* Key Listener **************************/

	public void keyTyped(KeyEvent e) {
		char ch = e.getKeyChar();

		
		if (ch == KeyEvent.VK_BACK_SPACE) {

			int length =
				((JTextField) getEditor().getEditorComponent())
					.getText()
					.length();
			if (length == 0) {
				this.table.removeEditingRow();
			}

		}
		

	}

	public void keyPressed(KeyEvent e) {
		char ch = e.getKeyChar();

	}

	public void keyReleased(KeyEvent e) {

		char ch = e.getKeyChar();

		if (ch == KeyEvent.VK_ENTER) {

			fireEditingStopped();

			table.appendRow();

		}

	}
}