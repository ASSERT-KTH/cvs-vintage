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


package org.columba.addressbook.gui.tree;


import javax.swing.tree.*;

import org.columba.core.config.*;
import org.columba.mail.config.*;
import org.columba.mail.message.*;
import org.columba.mail.folder.*;




public class AddressbookTreeNode extends DefaultMutableTreeNode
{
    private String name;
    
    
    public AddressbookTreeNode( String name )
    {
        super( name );
       
        this.name = name;
    }

    
    public void setName( String s )
    {
        name = s;
    }
    
        
    public String getName()
    {
        return name;
    }

    
   

    
}






