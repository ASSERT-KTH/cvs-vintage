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

package org.columba.core.config;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.columba.addressbook.config.*;
import org.w3c.dom.Document;

public class DomToTreeModelAdapter implements TreeModel 
{
    Document document;
    
    public DomToTreeModelAdapter(Document document)
    {
	
	this.document = document;
	
    }

        /* ===================================================================== */
        // methods for TreeModel implementation

    
    public Object  getRoot() 
    {
	
	return new AdapterNode(document);
    }
    
    public boolean isLeaf(Object aNode) 
    {
	// Determines whether the icon shows up to the left.
	// Return true for any node with no children
	AdapterNode node = (AdapterNode) aNode;
	if (node.getChildCount() > 0) return false;
	return true;
    }
    
    public int getChildCount(Object parent) 
    {
        AdapterNode node = (AdapterNode) parent;
        return node.getChildCount();
    }
    
    public Object getChild(Object parent, int index) 
    {
        AdapterNode node = (AdapterNode) parent;
        return node.getChild(index);
    }

    public int getIndexOfChild(Object parent, Object child) 
    {
        AdapterNode node = (AdapterNode) parent;
        return node.getIndex((AdapterNode) child);
    }
    
    public void valueForPathChanged(TreePath path, Object newValue) 
    {
    }

	/*
	 * Use these methods to add and remove event listeners.
	 * (Needed to satisfy TreeModel interface, but not used.)
	 */
    private Vector listenerList = new Vector();

    public void addTreeModelListener(TreeModelListener listener) 
    {
        if ( listener != null 
             && ! listenerList.contains( listener ) ) 
        {
            listenerList.addElement( listener );
        }
    }
    
    public void removeTreeModelListener(TreeModelListener listener) 
    {
        if ( listener != null ) 
        {
            listenerList.removeElement( listener );
        }
    }


        /* ==================================================================================== */
        // TreeModelEvents

      public void fireTreeNodesChanged( TreeModelEvent e ) {
        Enumeration listeners = listenerList.elements();
        while ( listeners.hasMoreElements() ) {
          TreeModelListener listener = 
            (TreeModelListener)listeners.nextElement();
          listener.treeNodesChanged( e );
        }
      } 
      public void fireTreeNodesInserted( TreeModelEvent e ) {
        Enumeration listeners = listenerList.elements();
        while ( listeners.hasMoreElements() ) {
           TreeModelListener listener =
             (TreeModelListener)listeners.nextElement();
           listener.treeNodesInserted( e );
        }
      }   
      public void fireTreeNodesRemoved( TreeModelEvent e ) {
        Enumeration listeners = listenerList.elements();
        while ( listeners.hasMoreElements() ) {
          TreeModelListener listener = 
            (TreeModelListener)listeners.nextElement();
          listener.treeNodesRemoved( e );
        }
      }   
      public void fireTreeStructureChanged( TreeModelEvent e ) {
        Enumeration listeners = listenerList.elements();
        while ( listeners.hasMoreElements() ) {
          TreeModelListener listener =
            (TreeModelListener)listeners.nextElement();
          listener.treeStructureChanged( e );
        }
      }
    }
