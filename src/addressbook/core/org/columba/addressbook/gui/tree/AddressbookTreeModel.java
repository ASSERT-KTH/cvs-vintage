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

import javax.swing.*;
import javax.swing.tree.*;

import org.columba.main.MainInterface;
import org.columba.mail.config.*;

class AddressbookTreeModel extends DefaultTreeModel
{
    private AddressbookTreeNode rootNode;

    public AddressbookTreeModel( AddressbookTreeNode root )
    {
        super( root );
        rootNode = root;
    }
    
      /* ===================================================================== */
      // methods for TreeModel implementation

    
    public Object getRoot() 
    {
	return rootNode;
    }
    
    public boolean isLeaf(Object aNode) 
    {
	AddressbookTreeNode node = (AddressbookTreeNode) aNode;
	if (node.getChildCount() > 0) return false;
	return true;
    }
    
    public int getChildCount(Object parent) 
    {
        AddressbookTreeNode node = (AddressbookTreeNode) parent;
        return node.getChildCount();
    }
    
    public Object getChild(Object parent, int index) 
    {
        AddressbookTreeNode node = (AddressbookTreeNode) parent;
        return node.getChildAt(index);
    }

    public int getIndexOfChild(Object parent, Object child) 
    {
        AddressbookTreeNode node = (AddressbookTreeNode) parent;
        return node.getIndex((AddressbookTreeNode) child);
    }
    
}

