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
package org.columba.addressbook.gui.table.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;


/**
 * @author fdietz
 */

public class DefaultLabelRenderer extends JLabel implements TableCellRenderer {
    private Border unselectedBorder = null;
    private Border selectedBorder = null;
    private Color background;
    private Color foreground;
    private boolean isBordered = true;

    /**
 * Constructor for DefaultLabelRenderer.
 */
    public DefaultLabelRenderer() {
        super();
    }

    /**
 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
 *      java.lang.Object, boolean, boolean, int, int)
 */
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
        if (isBordered) {
            if (isSelected) {
                if (selectedBorder == null) {
                    selectedBorder = BorderFactory.createMatteBorder(2, 5, 2,
                            5, table.getSelectionBackground());
                }

                //setBorder(selectedBorder);
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                if (unselectedBorder == null) {
                    unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2,
                            5, table.getBackground());
                }

                setBackground(table.getBackground());

                //setBorder(unselectedBorder);
                setForeground(table.getForeground());
            }
        }

        return this;
    }

    public boolean isOpaque() {
        return (background != null);
    }

    /**
 * Returns the background.
 * 
 * @return Color
 */
    public Color getBackground() {
        return background;
    }

    /**
 * Returns the foreground.
 * 
 * @return Color
 */
    public Color getForeground() {
        return foreground;
    }

    /**
 * Sets the background.
 * 
 * @param background
 *            The background to set
 */
    public void setBackground(Color background) {
        this.background = background;
    }

    /**
 * Sets the foreground.
 * 
 * @param foreground
 *            The foreground to set
 */
    public void setForeground(Color foreground) {
        this.foreground = foreground;
    }
}
