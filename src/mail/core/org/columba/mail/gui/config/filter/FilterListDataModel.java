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

package org.columba.mail.gui.config.filter;

import javax.swing.table.AbstractTableModel;

import org.columba.mail.filter.Filter;
import org.columba.mail.filter.FilterList;
import org.columba.mail.util.MailResourceLoader;

class FilterListDataModel extends AbstractTableModel
{

    final String[] columnNames = {MailResourceLoader.getString(
                                        "dialog",
                                        "filter",
                                        "description_tableheader"),
                                  MailResourceLoader.getString(
                                        "dialog",
                                        "filter",
                                        "enabled_tableheader")
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
            String description = filter.get("description");
            if ( description == null ) return new String();

            return description;
        }
        else
        {
            // enabled/disabled
            boolean enabled = filter.getBoolean("enabled");
            return enabled ? Boolean.TRUE : Boolean.FALSE;
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
        return col == 1;
    }

    public void setValueAt(Object value, int row, int col)
    {
        Filter filter = filterList.get(row);
        filter.getEnabled();
    }
}
