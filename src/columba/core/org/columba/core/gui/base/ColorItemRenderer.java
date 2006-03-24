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
package org.columba.core.gui.base;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


/**
 * A <code>JComboBox</code> item renderer.
 *
 * @author redsolo
 */

public class ColorItemRenderer extends JLabel implements ListCellRenderer {
    /**
 * Creates a <code>ColorItemRenderer</code>.
 */
    public ColorItemRenderer() {
        /*setIconTextGap(5);
setVerticalAlignment(JLabel.CENTER);*/
    }

    /** {@inheritDoc} */
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
        if ((value != null) && (value instanceof ColorItem)) {
            ColorItem color = (ColorItem) value;
            setText(color.getName());
            setIcon(color.getIcon());
        } else {
            setText("Internal error");
            setIcon(null);
        }

        return this;
    }
}
