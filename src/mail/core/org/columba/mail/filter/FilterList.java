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


package org.columba.mail.filter;

import java.util.Vector;

import org.columba.core.config.AdapterNode;
import org.columba.mail.config.FolderItem;
import org.columba.mail.config.MailConfig;
import org.columba.mail.folder.Folder;


public class FilterList
{
    private Vector list;
    private Folder folder;


    public FilterList( Folder folder )
    {
        this.folder = folder;
        folder.setFilterList( this );
        list = new Vector();

        FolderItem item = folder.getFolderItem();
        AdapterNode filterListNode = item.getFilterListNode();

        if ( filterListNode != null )
        {
            AdapterNode child;
            for ( int i=0; i< filterListNode.getChildCount(); i++)
            {
                child = (AdapterNode) filterListNode.getChild( i );
                Filter filter = new Filter( child );
                filter.setFolder( folder );
                add( filter );
            }
        }
    }

    public void removeAllElements()
    {
        list.removeAllElements();
        getFilterListNode().removeChildren();
    }

    public void clear()
    {
        if ( list.size() > 0 )
            list.clear();
    }

    public Folder getFolder()
    {
        return folder;
    }

    public AdapterNode getFilterListNode()
    {
        FolderItem item = getFolder().getFolderItem();
        AdapterNode filterListNode = item.getFilterListNode();

        return filterListNode;
    }


    public Filter addEmtpyFilter()
    {
        //AdapterNode filterListNode = getFilterListNode();

        AdapterNode node = MailConfig.getFolderConfig().addEmptyFilterNode( getFolder().getNode() );
        Filter filter = new Filter( node );

        add( filter );

        return filter;
    }

    public void add( Filter f )
    {
        list.add( f );
    }

    public int count()
    {
        return list.size();

    }

    public Filter get( int index )
    {
        Filter filter = (Filter) list.get(index);

        return filter;
    }

    public void remove( int index )
    {
        Filter filter = (Filter) list.remove( index );
        AdapterNode node = filter.getRootNode();
        getFilterListNode().removeChild( node );
    }


	/*
    public boolean process( int i, Object[] uids ) throws Exception
    {
        Filter filter = (Filter) list.get(i);
        filter.setFolder( getFolder() );
        boolean result = false;

        if ( filter.getNode() != null )
            filter.processFilter( uids );

        return result;
    }
	*/

	/*
    public boolean processAll( Object[] uids) throws Exception
    {
          //System.out.println("processAll listsize: " + list.size() );

        boolean result = false;

        for ( int i=0; i<list.size(); i++)
        {
            if ( result == true )
                break;
            else
                process( i, uids );

        }
        return result;

    }
    */
}





