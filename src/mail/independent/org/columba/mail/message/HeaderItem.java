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

package org.columba.mail.message;

import javax.swing.table.DefaultTableCellRenderer;

public class HeaderItem
{
    private Object sortValue;
    private Object viewValue;
    private Object header;
    

        //private DefaultTableCellRenderer renderer;

    public HeaderItem()
    {}

    public HeaderItem( Object s, Object v, Object h, DefaultTableCellRenderer r )
    {
        sortValue = s;
        viewValue = v;
        header = h;
            //renderer = r;
    }

    public Object getSortValue()
    {
        return sortValue;
    }

    public Object getViewValue()
    {
        return viewValue;
    }

    public Object getHeader()
    {
        return header;
    }

    public void setSortValue( Object s )
    {
        sortValue = s;
    }

    public void setViewValue( Object s )
    {
        viewValue = s;
    }

    public void setHeader( Object s )
    {
        header = s;
    }

        /*
          public void setRenderer( DefaultTableCellRenderer r )
          {
          renderer = r;
          }
          
    
          public DefaultTableCellRenderer getRenderer()
          {
          return renderer;
          }
        */

    
    

    
}
