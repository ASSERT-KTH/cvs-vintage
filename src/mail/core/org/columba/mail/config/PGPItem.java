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


import java.util.Vector;
import java.awt.*;

import org.columba.mail.folder.*;

import org.w3c.dom.*;

import org.columba.core.config.*;
import org.columba.core.gui.themes.*;


public class PGPItem extends DefaultItem
{
    private AdapterNode type, path, id, alwaysSign, alwaysEncrypt, enable;

    private AdapterNode rootNode;

    private String passphrase;

    public PGPItem( Document doc, AdapterNode rootNode )
    {
        super( doc );
        this.rootNode = rootNode;

        parse();

        createMissingElements();

	passphrase = new String();
    }

    public String getPassphrase()
    {
	return passphrase;
    }

    public void clearPassphrase()
    {
	passphrase = new String();
    }

    public void setPassphrase( String s )
    {
	passphrase = s;
    }

    protected void parse()
    {
    	int count= rootNode.getChildCount();
    	
        for ( int i=0; i<count; i++ )
        {
            AdapterNode child = rootNode.getChildAt(i);
			String str = child.getName();

            if ( str.equals("id") )
            {
                id = child;
            }
            else if ( str.equals("type") )
            {
                type = child;
            }
            else if ( str.equals("path") )
            {
                path = child;
            }
            else if ( str.equals("alwayssign") )
            {
                alwaysSign = child;
            }
            else if ( str.equals("alwaysencrypt") )
            {
                alwaysEncrypt = child;
            }
            else if ( str.equals("enable") )
            {
                enable = child;
            }
            else
            {
                // found obsolete item
            }
        }
    }

    protected void createMissingElements()
    {
        if ( id == null ) id = addKey( rootNode, "id", "user-id" );
        if ( type == null ) type = addKey( rootNode, "type", "0" );
        if ( path == null ) path = addKey( rootNode, "path", "/usr/bin/gpg" );
        if ( alwaysSign == null ) alwaysSign = addKey( rootNode, "alwayssign", "false" );
        if ( alwaysEncrypt == null ) alwaysEncrypt = addKey( rootNode, "alwaysencrypt", "false" );
        if ( enable == null ) enable = addKey( rootNode, "enable", "false" );

    }


      /******************************************** set ***************************************/



    public void setId( String str )
    {
        setTextValue( id, str );
    }

    public void setPath( String str )
    {
        setTextValue( path, str );
    }

    public void setType( int i )
    {
        String str = ( new Integer(i) ).toString();
        setTextValue( type, str );
    }

    public void setEnabled( boolean b  )
    {
        Boolean bool = new Boolean(b);
        setTextValue( enable, bool.toString() );
    }

    public void setAlwaysSign( boolean b  )
    {
        Boolean bool = new Boolean(b);
        setTextValue( alwaysSign, bool.toString() );
    }

    public void setAlwaysEncrypt( boolean b  )
    {
        Boolean bool = new Boolean(b);
        setTextValue( alwaysEncrypt, bool.toString() );
    }


      /**************************************************** get *********************************/

    public boolean getEnabled()
    {
        Boolean bool = new Boolean(  getTextValue( enable ) );

        return bool.booleanValue();
    }

    public boolean getAlwaysSign()
    {
        Boolean bool = new Boolean(  getTextValue( alwaysSign ) );

        return bool.booleanValue();
    }

    public boolean getAlwaysEncrypt()
    {
        Boolean bool = new Boolean(  getTextValue( alwaysEncrypt ) );

        return bool.booleanValue();
    }

    public int getType()
    {
        String str = getTextValue( type );
        int i = Integer.parseInt( str );

        return i;
    }

    public String getId()
    {
        return getTextValue( id );
    }

    public String getPath()
    {
        return getTextValue( path );
    }



}




