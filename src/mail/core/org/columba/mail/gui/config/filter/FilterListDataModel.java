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


package org.columba.mail.gui.config.filter;

import javax.swing.*;
import javax.swing.table.*;

import org.columba.main.MainInterface;
import org.columba.mail.config.*;
import org.columba.mail.filter.*;


class FilterListDataModel extends AbstractTableModel
{

    final String[] columnNames = {"Description",
                                  "Enabled",
                                  };

    private FilterList filterList;

    public FilterListDataModel( FilterList list )
    {
        super();
        this.filterList = list;

    }



    public int getColumnCount()
    {
        return columnNames.length;
    }

    public int getRowCount()
    {
        return filterList.count();
    }

    public String getColumnName(int col)
    {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col)
    {
        Filter filter = filterList.get(row);
        if ( filter == null ) return new String();

        if ( col == 0 )
        {
            // description
            String description = filter.getName();
            if ( description == null ) return new String();

            return description;
        }
        else
        {
            // enabled/disabled
            Boolean enabled = filter.getEnabled();
            if ( enabled == null ) return new String();

            return enabled;
        }
    }


    public Class getColumnClass(int c)
    {
        if ( c==0 )
           return String.class;
        else
           return Boolean.class;
    }


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

}

