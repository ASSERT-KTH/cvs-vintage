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
package org.columba.core.gui.checkablelist;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JList;


/**
 * JList with an additional JCheckBox beside the JLabel.
 * <p>
 * 
 * @author fdietz
 */
public class CheckableList extends JList {
    public CheckableList() {
        super();

        setCellRenderer(new CheckListRenderer());

        addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int index = locationToIndex(e.getPoint());

                    CheckableItem item = (CheckableItem) getModel()
                                                             .getElementAt(index);
                    item.setSelected(!item.isSelected());

                    Rectangle rect = getCellBounds(index, index);
                    repaint(rect);
                }
            });
    }
}
