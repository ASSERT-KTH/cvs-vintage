/*
 * Created on 07.08.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.gui.plugin;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class EnabledEditor
	extends AbstractCellEditor
	implements TableCellEditor {

	JCheckBox component = new JCheckBox();

	/**
	 * 
	 */
	public EnabledEditor() {
		component.setHorizontalAlignment(SwingConstants.CENTER);
		component.setOpaque(true);
	}

	//	This method is called when a cell value is edited by the user.
	public Component getTableCellEditorComponent(
		JTable table,
		Object value,
		boolean isSelected,
		int rowIndex,
		int vColIndex) {

		PluginNode node = (PluginNode) value;

		// Configure the component with the specified value
		 ((JCheckBox) component).setSelected(node.isEnabled());

		// Return the configured component
		return component;
	}

	// This method is called when editing is completed.
	// It must return the new value to be stored in the cell.
	public Object getCellEditorValue() {
		Boolean b = new Boolean(((JCheckBox) component).isSelected());

		return b;
	}
}
