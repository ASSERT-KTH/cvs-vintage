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


public class SpecialFoldersItem extends DefaultItem
{
    private AdapterNode trash, drafts, templates, sent, inbox;

    private AdapterNode rootNode;
    
    private AdapterNode useDefaultAccount;

    public SpecialFoldersItem( Document doc, AdapterNode rootNode )
    {
        super( doc );
        this.rootNode = rootNode;

        parse();

        createMissingElements();
    }

    protected void parse()
    {
    	int count = rootNode.getChildCount();
    	
        for ( int i=0; i<count; i++ )
        {
            AdapterNode child = rootNode.getChildAt(i);
			String str = child.getName();
            
            if ( str.equals("trash") )
            {
                trash = child;
            }            
            else if ( str.equals("drafts") )
            {
                drafts = child;
            }
            else if ( str.equals("templates") )
            {
                templates = child;
            }
            else if ( str.equals("sent") )
            {
                sent = child;
            }
            else if ( str.equals("inbox") )
            {
                inbox = child;
            }
             else if ( str.equals("usedefaultaccount") )
                {
                	useDefaultAccount = child;
                }
            else
            {
                // found obsolete item
            }
        }
    }

    protected void createMissingElements()
    {
        
        if ( trash == null ) trash = addKey( rootNode, "trash", "105" );
        

        if ( drafts == null ) drafts = addKey( rootNode, "drafts", "102" );
        if ( templates == null ) templates = addKey( rootNode, "templates", "107" );
        if ( sent == null ) sent = addKey( rootNode, "sent", "104" );
        if ( inbox == null ) inbox = addKey( rootNode, "inbox", "101" );
        if ( useDefaultAccount == null ) useDefaultAccount= addKey( rootNode, "usedefaultaccount", "false" );

    }


      /******************************************** set ***************************************/



      
    public void setTrash( String str )
    {
        setTextValue( trash, str );
    }
    

    public void setDrafts( String str )
    {
        setTextValue( drafts, str );
    }

    public void setTemplates( String str )
    {
        setTextValue( templates, str );
    }

    public void setSent( String str )
    {
        setTextValue( sent, str );
    }


    public void setInbox( String str )
    {
        setTextValue( inbox, str );
    }

	public void setUseDefaultAccount( boolean b )
	{
		Boolean bool = new Boolean(b);
		setTextValue( useDefaultAccount, bool.toString() );	
	}
	
      /**************************************************** get *********************************/

    public String getInbox()
    {
        return getTextValue( inbox );
    }


    
    public String getTrash()
    {
        return getTextValue( trash );
    }
    

    public String getDrafts()
    {
        return getTextValue( drafts );
    }

    public String getTemplates()
    {
        return getTextValue( templates );
    }

    public String getSent()
    {
        return getTextValue( sent );
    }


	public boolean isUseDefaultAccount()
	{
		if ( getTextValue(useDefaultAccount).equals("true") )
			return true;
		else
			return false;
	}
	
}




