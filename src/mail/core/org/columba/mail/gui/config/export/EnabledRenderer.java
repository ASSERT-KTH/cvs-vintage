/*
 * Created on 06.08.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.config.export;

import java.awt.Component;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;

import org.columba.mail.folder.virtual.VirtualFolder;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class EnabledRenderer extends DefaultTableCellRenderer {

	JCheckBox checkBox = new JCheckBox();

	Map map;

	public EnabledRenderer(Map map) {
		this.map = map;

		setHorizontalAlignment(SwingConstants.CENTER);

	}
	/* (non-Javadoc)
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(
		JTable table,
		Object value,
		boolean isSelected,
		boolean hasFocus,
		int row,
		int column) {

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

		if (node instanceof VirtualFolder) {
			// this node is category folder
			// -> don't make it editable

			return super.getTableCellRendererComponent(
				table,
				"",
				isSelected,
				hasFocus,
				row,
				column);
		} else {
			// first time initalization
			if (!map.containsKey(node)) {
				map.put(node, Boolean.FALSE);
			}

			Boolean bool = (Boolean) map.get(node);
			
			boolean b = bool.booleanValue();
			

			checkBox.setSelected(b);
			checkBox.setHorizontalAlignment(JLabel.CENTER);

			if (isSelected) {

				checkBox.setBackground(table.getSelectionBackground());
			} else {

				checkBox.setBackground(table.getBackground());

			}

			return checkBox;
		}

	}

}
