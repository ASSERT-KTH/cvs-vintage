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

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * represents a Rfc822-compliant header
 * every headeritem is saved in a hashtable-structure
 * generally every headeritem is a string,
 * but for optimization reasons some items
 * are going to change to for example a Date class
 *
 * we added these items:
 *  - a date object
 *  - shortfrom, a parsed from
 *  - alreadyfetched, Boolean
 *  - pop3uid, String
 *  - uid, String
 *  - size, Integer
 *  - attachment, Boolean
 *  - priority, Integer
 */

public class Rfc822Header implements HeaderInterface
{

    public static final int ALL = 0;
    public static final int NORMAL = 1;
    public static final int BRIEF = 2;
    public static final int USER_DEF = 3;

        // this hashtable stores all headeritems
    protected Hashtable hashTable;

        // do we need this variables anymore ??
    private String mimeVer = new String();

        // standard constructor
    public Rfc822Header()
    {
        hashTable = new Hashtable();


    }
    
    public Flags getFlags() {
		return new Flags(this);
	}

        // returns the header as given from mailserver
        // the items are split by \n
    public String getHeader()
    {
        Enumeration keys;
        StringBuffer output = new StringBuffer();
        String aktKey;

        keys = hashTable.keys();

        while( keys.hasMoreElements() ) {
            aktKey = (String) keys.nextElement();

            if ( aktKey.indexOf("columba") == -1 )
            {
                output.append( aktKey );
                output.append(": ");

		output.append( hashTable.get( aktKey ) );

                output.append("\n");
            }
        }

        return output.toString();
    }


    public void setHashtable( Hashtable h )
    {
        hashTable = h;
    }

    public Hashtable getHashtable()
    {
        return hashTable;
    }

        // return headeritem
    public Object get( String s )
    {
        Object result = null;

        s = s.toLowerCase();

            // does this item exist?
        if ( hashTable.containsKey( s ) == true )
        {
            result = hashTable.get( s );
        }

        return result;
    }

        // set headeritem
    public void set( String s, Object o)
    {
        s = s.toLowerCase();

        if ( ( s != null ) && ( o != null ) )
        hashTable.put( s, o );
    }



    public void setMimeVer( String par )
    {
	mimeVer = par;
    }

    public String getMimeVer()
    {
	return mimeVer;
    }

    public String getHeaderEntry( String type )
        {
            StringBuffer output = new StringBuffer( type );

            if( type.equals("from") ) {
                output.append(" : ");
                output.append( get("From") );
                output.append('\n');
                return output.toString();
            }
            if( type.equals("to") ) {
                output.append(" : ");
                output.append( get("To") );
                output.append('\n');
                return output.toString();
            }
            if( type.equals("cc") ) {
                output.append(" : ");
                output.append( get("Cc"));
                output.append('\n');
                return output.toString();
            }
            if( type.equals("bcc") ) {
                output.append(" : ");
                output.append( get("Bcc"));
                output.append('\n');
                return output.toString();
            }
            if( type.equals("date") ) {
                output.append(" : ");
                output.append( get("Date"));
                output.append('\n');
                return output.toString();
            }
            if( type.equals("subject") ) {
                output.append(" : ");
                output.append( get("Subject") );
                output.append('\n');
                return output.toString();
            }

            return null;
        }

    public String getHeaderInfo(int mode)
    {
        return null;
    }

    public Object clone()
    {
        Rfc822Header header = new Rfc822Header();
        header.setHashtable( (Hashtable) hashTable.clone() );

        return header;
    }
    
	public void printDebug()
		{
			for (Enumeration keys = hashTable.keys(); keys.hasMoreElements();) {
				String key = (String) keys.nextElement();
				
				Object value = hashTable.get(key);
				
				System.out.println("key="+key+" value="+value.toString());
			}	
	
		}



}
