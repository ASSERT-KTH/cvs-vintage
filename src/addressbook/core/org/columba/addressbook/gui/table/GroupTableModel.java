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

package org.columba.addressbook.gui.table;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.columba.addressbook.config.AdapterNode;
import org.columba.addressbook.config.GroupItem;
import org.columba.core.config.DefaultXmlConfig;


public class GroupTableModel extends AbstractTableModel
{
    private AdapterNode node;
    
    private Vector groupList;
    
    private String[] columnNames = {"Display Name",
                                    "Address",
                                    "First Name",
                                    "Last Name"
                                    };
    

    private int count;

    //private AddressbookXmlConfig config;
    
    protected DefaultXmlConfig config;
    
    public GroupTableModel( DefaultXmlConfig config)
    {
        super();
        //this.config = config;
        
        count=0;
        groupList = new Vector();
        
        
    }

    public void setNode( AdapterNode n )
    {
        node = n;
                
        update();
        
    }

    public void update()
    {
    	/*
        AdapterNode listNode = node.getChild("grouplist");
        count = listNode.getChildCount();

        GroupItem item = config.getGroupItem( node );
        if ( item != null ) System.out.println("item found") ;
        
        Vector ve = item.getListNodes();
        int uid;
        AdapterNode n, itemNode;
        
        groupList.clear();
        
        for ( int i=0; i<ve.size(); i++ )
        {
            n = ( AdapterNode ) ve.get(i);
            uid = (new Integer( n.getValue() ) ).intValue();
            itemNode = config.getNode( uid );
            groupList.addElement( itemNode );
        }
        
        fireTableDataChanged();
        */
    }
    
    public int getColumnCount()
    {
        return columnNames.length;
    }
    
    public int getRowCount()
    {        
        return count;
    }
    
    public String getColumnName(int col)
    {
        String s = (String) columnNames[col];
            
        return s;
    }
    
    protected int getColumnNumber(String str)
    {
        for ( int i=0; i<getColumnCount(); i++)
        {
            if ( str.equals( getColumnName(i) ) )
            {
                return i;
            }
        }
        return -1;
    }
    
    
    public Object getValueAt(int row, int col)
    {
        AdapterNode contact;

        contact = (AdapterNode) groupList.get(row);
        if ( contact == null ) return "";

        AdapterNode child = null;        
        String str = new String("");

        if ( contact.getName().equals("contact") )
        {
            if ( col == 0 )
            {
                child = contact.getChild("displayname");
                
                str = child.getValue();
            }
            else if ( col == 1 )
            {
                child = contact.getChild("address");
                
                str = child.getCDATAValue();
            } 
            else if ( col == 2)
            {
                child = contact.getChild("firstname");
                str = child.getValue();
            }
            if ( col == 3 )
            {
                child = contact.getChild("lastname");
                str = child.getValue();
            }
              /*
                
            if ( col == 1 )
            {
                child = contact.getChild("firstname");
                
                str = child.getValue();
            } 
            else if ( col == 0 )
            {
                child = contact.getChild("lastname");
                str = child.getValue();
            }
            if ( col == 2 )
            {
                child = contact.getChild("address");
                str = child.getCDATAValue();
            }
              */
        }
                
        return str;
    }

    
    public Class getColumnClass(int c)
        {
            return getValueAt(0, c).getClass();
        }
        
}







