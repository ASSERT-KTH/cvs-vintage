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
