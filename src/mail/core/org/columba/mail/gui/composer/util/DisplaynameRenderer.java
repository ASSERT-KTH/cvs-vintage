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

import org.columba.addressbook.folder.HeaderItem;

import org.columba.core.gui.util.ImageLoader;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;


/**
 * Renderer for contact column.
 */
public class DisplaynameRenderer extends JLabel implements TableCellRenderer {
    protected Border unselectedBorder = null;
    protected Border selectedBorder = null;
    protected boolean isBordered = true;
    ImageIcon contactIcon = ImageLoader.getSmallImageIcon("contact_small.png");
    ImageIcon groupIcon = ImageLoader.getSmallImageIcon("group_small.png");

    public DisplaynameRenderer() {
        setOpaque(true);

        isBordered = true;
    }

    public Component getTableCellRendererComponent(JTable table, Object object,
        boolean isSelected, boolean hasFocus, int row, int column) {
        if (isBordered) {
            if (isSelected) {
                if (selectedBorder == null) {
                    selectedBorder = BorderFactory.createMatteBorder(2, 5, 2,
                            5, table.getSelectionBackground());
                }

                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                if (unselectedBorder == null) {
                    unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2,
                            5, table.getBackground());
                }

                setBackground(table.getBackground());

                setForeground(table.getForeground());
            }
        }

        HeaderItem item = (HeaderItem) object;

        setText(item.toString());

        if (item.isContact()) {
            String displayname = (String) item.get("displayname");

            StringBuffer buf = new StringBuffer();
            buf.append("<html><body>&nbsp;Name: " + convert(displayname));
            buf.append("<br>&nbsp;eMail: " +
                convert((String) item.get("email;internet")));
            buf.append("</body></html>");
            setToolTipText(buf.toString());
            setIcon(contactIcon);
        } else {
            setIcon(groupIcon);
            setToolTipText("");
        }

        return this;
    }

    private String convert(String str) {
        if (str == null) {
            return "";
        }

        StringBuffer result = new StringBuffer();
        int pos = 0;
        char ch;

        while (pos < str.length()) {
            ch = str.charAt(pos);

            if (ch == '<') {
                result.append("&lt;");
            } else if (ch == '>') {
                result.append("&gt;");
            } else {
                result.append(ch);
            }

            pos++;
        }

        return result.toString();
    }
}
