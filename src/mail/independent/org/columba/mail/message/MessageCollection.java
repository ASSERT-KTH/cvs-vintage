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

import java.util.Collection;
import java.util.Enumeration;
import java.util.Vector;

public class MessageCollection
{
    private Vector list;
      //private Hashtable uidTable;


    public MessageCollection()
        {
            list = new Vector();
              //uidTable = new Hashtable();
        }

    public MessageCollection( Collection messages )
        {
            list = new Vector( messages );
              //uidTable = new Hashtable();
        }

	public Enumeration getEnumeration()
	{
		return list.elements();
	}

    public Vector getList()
        {
            return list;

        }

    public void clear()
    {
        list.clear();
    }

    public void uidRemove( Object uid )
    {
        for ( int i=0; i<count(); i++ )
        {
            Message message = get(i);
            ColumbaHeader header = (ColumbaHeader) message.getHeader();
            String str = (String) header.get("columba.pop3uid");
            
            if ( str.equals(uid) )
            {
                remove( i );
            }
        }
    }

    /*
    // !!!!! this  method is only for the
    public HeaderList getHeaderList()
    {
        HeaderList headerList = new HeaderList();

        for ( int i=0; i<count(); i++ )
        {
            Message message = get(i);
            ColumbaHeader header = message.getHeader();
            Object uid = header.get("columba.pop3uid");
            headerList.add( header, uid );
        }

        return headerList;
    }
    */

	public Message pop3UidGet( Object uid )
	{
		for ( int i=0; i<count(); i++ )
        {
            Message message = get(i);
            ColumbaHeader header = (ColumbaHeader) message.getHeader();
            //String str = (String) message.getPop3Uid();
            String str = (String) header.get("columba.pop3uid");
            //System.out.println("local message uid: "+ str );
            if ( uid.equals(str) )
            {
                //System.out.println("popServer uid exists locally");

                return message;
            }
        }
        
        return null;
	}
	
    public Message get(int number)
        {
            if ( (number>list.size()-1) || ( number<0 ) )
                return null;
            else
                return (Message) list.elementAt(number);

        }


    public Message getInstance(int number)
        {
            Message item = new Message();

            if ( (number>list.size()-1) || ( number<0 ) )
                return null;
            else
            {
                item = (Message) list.elementAt(number);
                return item;
            }


        }

    public boolean exists(int number)
        {
            if ( (number>=0) && (number<list.size()) )
            {
                return true;
            }
            else
            {
                return false;
            }
        }

      /*
    public void uidRemove( Object uid )
    {
        Object index = uidTable.get( uid );
    }
      */

    public int indexOf( Message message )
    {
        return list.indexOf( message );
    }
    
    public int indexOf( Object uid )
    {
    	for ( int i=0; i<count(); i++ )
        {
            Message message = get(i);
            ColumbaHeader header = (ColumbaHeader) message.getHeader();
            //String str = (String) message.getPop3Uid();
            String str = (String) header.get("columba.pop3uid");
            //System.out.println("local message uid: "+ str );
            if ( uid.equals(str) )
            {
                //System.out.println("popServer uid exists locally");

                return i;
            }
        }
        
        return -1;
    }

    public boolean remove( Message message )
    {
        boolean b = list.remove( message );

        return b;
    }


    public void remove(int number)
        {            
			if ( ( number>=0 ) && ( number<count() ) )
            	list.remove(number);
        }


    public int add(Message message)
        {
            list.addElement(message);

              /*
                Object uid = message.getUID();

                uidTable.put( uid, list.size()-1 );
              */

            return list.size()-1;
        }

    public void insert(int index, Message message)
        {
            list.insertElementAt(message, index);
        }

    public int count()
        {
            return list.size();
        }

    public void reset()
        {
            list.clear();
              //uidTable.clear();
        }

    public void replace( int index, Message message )
    {
        list.remove( index );
        list.insertElementAt( message, index );
    }


}
