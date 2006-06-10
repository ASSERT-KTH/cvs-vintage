/*
 * Created on 06.08.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.gui.plugin;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;


/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

public class EnabledRenderer extends DefaultTableCellRenderer {
    JCheckBox checkBox = new JCheckBox();

    public EnabledRenderer() {
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    /* (non-Javadoc)
 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
 */
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
        PluginNode node = (PluginNode) value;

        if (node.isCategory()) {
            // this node is category folder
            // -> don't make it editable
            return super.getTableCellRendererComponent(table, "", isSelected,
                hasFocus, row, column);
        } else {
            boolean b = node.isEnabled();

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
