// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
package org.columba.mail.gui.config.accountlist;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import org.columba.mail.config.AccountItem;
import org.columba.mail.main.MailInterface;
import org.columba.mail.util.MailResourceLoader;


/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class NameRenderer extends JLabel implements TableCellRenderer {
    Border unselectedBorder = null;
    Border selectedBorder = null;
    boolean isBordered = true;

    public NameRenderer() {
        super();
        this.isBordered = true;
        setOpaque(true); //MUST do this for background to show up.

        //setBorder( BorderFactory.createEmptyBorder(0,1,0,0) );
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
        if (isBordered) {
            if (isSelected) {
                if (selectedBorder == null) {
                    selectedBorder = BorderFactory.createMatteBorder(2, 5, 2,
                            5, table.getSelectionBackground());
                }

                setBorder(selectedBorder);
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                if (unselectedBorder == null) {
                    unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2,
                            5, table.getBackground());
                }

                setBackground(table.getBackground());
                setBorder(unselectedBorder);
                setForeground(table.getForeground());
            }
        }

        StringBuffer buf = new StringBuffer();

        AccountItem item = (AccountItem) value;

        buf.append(item.getName());

        if (MailInterface.config.getAccountList().getDefaultAccountUid() == item.getUid()) {
            buf.append(" (" +
                MailResourceLoader.getString("dialog", "account", "standard") +
                ")");
        }

        setText(buf.toString());

        return this;
    }
}
