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



package org.columba.mail.config;

import org.columba.core.config.AdapterNode;
import org.columba.core.config.DefaultItem;
import org.w3c.dom.Document;


public class FolderItem extends DefaultItem
{
    private AdapterNode name, accessRights, messageFolder, type, search, uid, accountUid;
    private AdapterNode subfolder, add, remove;
    //private Vector filterList;
    private AdapterNode filterListNode;
    private AdapterNode accountName;

    public FolderItem( Document doc )
    {
        super( doc );
        //filterList = new Vector();
    }



    public void setAccountNameNode( AdapterNode node )
    {
        accountName = node;
    }

    public void setUidNode( AdapterNode node )
    {
        uid= node;
    }

    public void setAccountUidNode( AdapterNode node )
    {
        accountUid= node;
    }


    public void setNameNode( AdapterNode node )
    {
        name = node;
    }

    public void setSearchNode( AdapterNode node )
    {
        search = node;
    }

    public void setAccessRightsNode( AdapterNode node )
    {
        accessRights = node;
    }

    public void setMessageFolderNode( AdapterNode node )
    {
        messageFolder = node;
    }

    public void setTypeNode( AdapterNode node )
    {
        type = node;
    }

    public void setSubfolderNode( AdapterNode node )
    {
        subfolder = node;
    }

    public void setAddNode( AdapterNode node )
    {
        add = node;
    }

    public void setRemoveNode( AdapterNode node )
    {
        remove = node;
    }


    public AdapterNode getSearchNode()
    {
        return search;
    }



      /******************************************** set ***************************************/


    public void setUid( int i )
    {
        Integer h = new Integer( i );

        setTextValue( uid, h.toString() );
    }

    public void setAccountUid( int i )
    {
        Integer h = new Integer( i );

        setTextValue( accountUid, h.toString() );
    }


    public void setAccountName( String str )
    {
        setTextValue( accountName, str );
    }

    public void setAccessRights( String str )
    {
        setTextValue( accessRights, str );
    }

    public void setMessageFolder( String str )
    {
        setTextValue( messageFolder, str );

    }

    public void setName( String str )
    {
        setTextValue( name, str );
    }


      /**************************************************** get *********************************/







    public String getAccountName()
    {
        return getTextValue( accountName );
    }


    public int getUid()
    {
        Integer i = new Integer(  getTextValue( uid ) );

        return i.intValue();
    }

    public int getAccountUid()
    {
        Integer i = new Integer(  getTextValue( accountUid ) );

        return i.intValue();
    }


    public String getMessageFolder()
    {
        return getTextValue( messageFolder );
    }

    public String getType()
    {
        return getTextValue( type );
    }

    public String getSubfolder()
    {
        return getTextValue( subfolder );
    }

    public String getAdd()
    {
        return getTextValue( add );
    }

    public String getRemove()
    {
        return getTextValue( remove );
    }

    public String getName()
    {
        return getTextValue( name );
    }


    public String getAccessRights()
    {
        return getTextValue( accessRights );
    }




    public boolean isMessageFolder()
    {
        if ( getMessageFolder().equals("true") ) return true;
        else return false;
    }

    public boolean isSubfolderAllowed()
    {
        if ( getSubfolder().equals("true") ) return true;
        else return false;
    }

    public boolean isAddAllowed()
    {
        if ( getAdd().equals("true") ) return true;
        else return false;
    }

    public boolean isRemoveAllowed()
    {
        return true;

          /*
        if ( getRemove().equals("true") ) return true;
        else return false;
          */
    }


      /*************************** filter *************************************/


      /*
    public Vector getFilterList()
    {
        return filterList;
    }
    */

    /*
    public void addFilterNode( AdapterNode node )
    {
        filterList.add( node );
    }
    */


    public void setFilterListNode( AdapterNode node )
    {
        filterListNode = node;
    }

    public AdapterNode getFilterListNode()
    {
        return filterListNode;
    }





}




