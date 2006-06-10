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

public class EnabledEditor extends AbstractCellEditor implements TableCellEditor {
    protected JCheckBox component = new JCheckBox();
    protected PluginNode currentNode;

    /**
 *
 */
    public EnabledEditor() {
        component.setHorizontalAlignment(SwingConstants.CENTER);
    }

    public int getClickCountToStart() {
        return 1;
    }

    //	This method is called when a cell value is edited by the user.
    public Component getTableCellEditorComponent(JTable table, Object value,
        boolean isSelected, int rowIndex, int vColIndex) {
        currentNode = (PluginNode) value;

        // Configure the component with the specified value
        ((JCheckBox) component).setSelected(currentNode.isEnabled());

        if (isSelected) {
            ((JCheckBox) component).setBackground(table.getSelectionBackground());
        } else {
            ((JCheckBox) component).setBackground(table.getBackground());
        }

        // Return the configured component
        return component;
    }

    // This method is called when editing is completed.
    // It must return the new value to be stored in the cell.
    public Object getCellEditorValue() {
        Boolean b = Boolean.valueOf(((JCheckBox) component).isSelected());

        // enable/disable tree node
        currentNode.setEnabled(b.booleanValue());

        /*
// enable/disable plugin
String id = currentNode.getId();

MainInterface.pluginManager.setEnabled(id, b.booleanValue());
*/
        return b;
    }

    public Component getComponent() {
        return component;
    }
}
