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

package org.columba.mail.gui.config.account;

import javax.swing.table.AbstractTableModel;

import org.columba.mail.config.AccountItem;
import org.columba.mail.config.AccountList;
import org.columba.mail.util.MailResourceLoader;

class AccountListDataModel extends AbstractTableModel
{
    final String[] columnNames = {MailResourceLoader.getString("dialog","account", "accountname"), //$NON-NLS-1$
                                  MailResourceLoader.getString("dialog","account", "type"), //$NON-NLS-1$
                                  };

    private AccountList accountList;

    public AccountListDataModel( AccountList list )
    {
        super();
        this.accountList = list;
    }

    public int getColumnCount()
    {
        return columnNames.length;
    }

    public int getRowCount()
    {
        return accountList.count();
    }

    public String getColumnName(int col)
    {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col)
    {
        AccountItem item = accountList.get(row);
        if ( item == null ) return new String();
        if ( col == 0 )
        {
            /*
            String description = item.getName();
            if ( description == null ) return new String();
            return description;
            */
            return item;
        }
        else
        {
            if (item.isPopAccount()) return MailResourceLoader.getString("dialog","account", "pop3"); //$NON-NLS-1$
            else return MailResourceLoader.getString("dialog","account", "imap4"); //$NON-NLS-1$
        }
    }

    public Class getColumnClass(int c)
    {
        if ( c==0 )
           return AccountItem.class;
        else
           return String.class;
    }

    /*
    public boolean isCellEditable(int row, int col)
    {
        if ( col == 1 )
           return true;
        else
           return false;
    }

    public void setValueAt(Object value, int row, int col)
    {
        Filter filter = filterList.get(row);
        filter.setEnabled( (Boolean) value );
    }
    */
}
