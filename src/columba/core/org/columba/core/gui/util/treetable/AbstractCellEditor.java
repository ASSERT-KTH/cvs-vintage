package org.columba.core.gui.util.treetable;

import java.util.EventObject;

import javax.swing.CellEditor;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;

/**
 * 
 * A base class for CellEditors, providing default implementations for all 
 * methods in the CellEditor interface and support for managing a series 
 * of listeners. 
 *
 */

public class AbstractCellEditor implements CellEditor {

	protected EventListenerList listenerList = new EventListenerList();

	public Object getCellEditorValue() {
		return null;
	}
	public boolean isCellEditable(EventObject e) {
		return true;
	}
	public boolean shouldSelectCell(EventObject anEvent) {
		return false;
	}
	public boolean stopCellEditing() {
		return true;
	}
	public void cancelCellEditing() {
	}

	public void addCellEditorListener(CellEditorListener l) {
		listenerList.add(CellEditorListener.class, l);
	}

	public void removeCellEditorListener(CellEditorListener l) {
		listenerList.remove(CellEditorListener.class, l);
	}

	/**
	 * Notify all listeners that have registered interest for
	 * notification on this event type.  
	 * @see EventListenerList
	 */
	protected void fireEditingStopped() {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == CellEditorListener.class) {
				((CellEditorListener) listeners[i + 1]).editingStopped(
					new ChangeEvent(this));
			}
		}
	}

	/**
	 * Notify all listeners that have registered interest for
	 * notification on this event type.  
	 * @see EventListenerList
	 */
	protected void fireEditingCanceled() {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == CellEditorListener.class) {
				((CellEditorListener) listeners[i + 1]).editingCanceled(
					new ChangeEvent(this));
			}
		}
	}
}
