/*
 * Created on 07.08.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.config.export;

import java.awt.Component;
import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.DefaultMutableTreeNode;

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

	DefaultMutableTreeNode currentNode;

	Map map;

	/**
	 * 
	 */
	public EnabledEditor(Map map) {
		this.map = map;

		component.setHorizontalAlignment(SwingConstants.CENTER);

	}

	public int getClickCountToStart() {
		return 1;
	}

	//	This method is called when a cell value is edited by the user.
	public Component getTableCellEditorComponent(
		JTable table,
		Object value,
		boolean isSelected,
		int rowIndex,
		int vColIndex) {

		currentNode = (DefaultMutableTreeNode) value;

		// Configure the component with the specified value
		
		// first time initalization
		if (!map.containsKey(currentNode)) {
			map.put(currentNode, Boolean.FALSE);
		}

		Boolean b = (Boolean) map.get(currentNode);

		((JCheckBox) component).setSelected(b.booleanValue());

		if (isSelected) {

			((JCheckBox) component).setBackground(
				table.getSelectionBackground());
		} else {

			((JCheckBox) component).setBackground(table.getBackground());

		}

		// Return the configured component
		return component;
	}

	// This method is called when editing is completed.
	// It must return the new value to be stored in the cell.
	public Object getCellEditorValue() {

		Boolean b = new Boolean(((JCheckBox) component).isSelected());

		// enable/disable tree node
		map.put(currentNode, b);

		//currentNode.setEnabled(b.booleanValue());

		/*
		// enable/disable plugin
		String id = currentNode.getId();
		
		MainInterface.pluginManager.setEnabled(id, b.booleanValue());
		*/

		System.out.println("cell-editor=" + b);

		return b;
	}

	public Component getComponent() {
		return component;
	}

}
