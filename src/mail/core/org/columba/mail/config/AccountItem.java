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
import org.columba.main.MainInterface;
import org.w3c.dom.Document;


public class AccountItem extends DefaultItem
{
    private AdapterNode name;
    private AdapterNode uid;
    private AdapterNode defaultAccount;
    private boolean pop3;

    private IdentityItem identity;
    private PopItem pop;
    private ImapItem imap;
    private SmtpItem smtp;
    private PGPItem pgp;
    private SpecialFoldersItem folder;

    private AdapterNode rootNode;

    public AccountItem( AdapterNode rootNode, Document doc )
    {
        super( doc );

        this.rootNode = rootNode;

        pop3=false;

        identity = null;
        pop = null;
        imap = null;
        identity = null;
        smtp = null;

        if ( rootNode != null ) parse();


		createMissingElements();
    }

    public AdapterNode getRootNode()
    {
        return rootNode;
    }

    protected void parse()
    {
        //System.out.println("parseing accountitem");

		int count = getRootNode().getChildCount();
        for ( int i=0; i<count; i++ )
        {
            AdapterNode child = getRootNode().getChildAt(i);
			String str = child.getName();
			
            if ( str.equals("name") )
            {
                name = child;
            }
            else  if ( str.equals("uid") )
            {
                uid = child;
            }
            else  if ( str.equals("identity") )
            {
                identity = new IdentityItem( child, getDocument() );
            }
            else  if ( str.equals("popserver") )
            {
                pop3 = true;
                pop = new PopItem( child, getDocument() );
            }
            else  if ( str.equals("imapserver") )
            {
                pop3 = false;
                imap = new ImapItem( child, getDocument() );
            }
            else  if ( str.equals("smtpserver") )
            {
                smtp = new SmtpItem( child, getDocument() );
            }
            else  if ( str.equals("pgp") )
            {
                pgp = new PGPItem( getDocument(), child );
            }
            else  if ( str.equals("specialfolders") )
            {
                folder = new SpecialFoldersItem( getDocument(), child );
            }
        }
    }

	protected void createMissingElements() {
		/*
		if (defaultAccount == null)
			defaultAccount = addKey(rootNode, "defaultaccount","false");
		*/
	}
	
   

    public boolean isPopAccount()
    {
        return pop3;
    }

    public void remove()
    {
        AdapterNode parent = new AdapterNode( name.domNode.getParentNode() );
        AdapterNode top = new AdapterNode( parent.domNode.getParentNode() );

	top.removeChild( parent );
    }



    public void setNameNode( AdapterNode node )
    {
        name = node;
    }

    public void setUidNode( AdapterNode node )
    {
        uid = node;
    }


    public void setPopItem( PopItem item )
    {
        pop = item;
    }

    public void setImapItem( ImapItem item )
    {
        imap = item;
    }

    public void setIdentityItem( IdentityItem item )
    {
        identity = item;
    }

    public void setSmtpItem( SmtpItem item )
    {
        smtp = item;
    }

    public SpecialFoldersItem getSpecialFoldersItem()
    {
    	if ( isDefault() ) return folder;
    	
    	if ( folder.isUseDefaultAccount() ) 
    	{
    		// return default-account ImapItem instead 
    		
    		SpecialFoldersItem item = MailConfig.getAccountList().getDefaultAccount().getSpecialFoldersItem();
    		return item;
    	}
    	
        return folder;
    }

    public PopItem getPopItem()
    {
    	if ( isDefault() ) return pop;
    	
    	if ( pop.isUseDefaultAccount() ) 
    	{
    		// return default-account ImapItem instead 
    		
    		PopItem item = MailConfig.getAccountList().getDefaultAccount().getPopItem();
    		return item;
    	}
    	
        return pop;
    }

    public SmtpItem getSmtpItem()
    {
    	if ( isDefault() ) return smtp;
    	
    	if ( smtp.isUseDefaultAccount() ) 
    	{
    		// return default-account ImapItem instead 
    		
    		SmtpItem item = MailConfig.getAccountList().getDefaultAccount().getSmtpItem();
    		return item;
    	}
    	
        return smtp;
    }

    public PGPItem getPGPItem()
    {
    	// uncomment this to make default account support working
    	
    	/*
    	if ( isDefault() ) return pgp;
    	
    	if ( pgp.isUseDefaultAccount() ) 
    	{
    		// return default-account ImapItem instead 
    		
    		PGPItem item = MailConfig.getAccountList().getDefaultAccount().getPGPItem();
    		return item;
    	}
    	*/
    	
        return pgp;
    }

    public ImapItem getImapItem()
    {
    	if ( isDefault() ) return imap;
    	
    	if ( imap.isUseDefaultAccount() ) 
    	{
    		// return default-account ImapItem instead 
    		
    		ImapItem item = MailConfig.getAccountList().getDefaultAccount().getImapItem();
    		return item;
    	}
    	
        return imap;
    }

    public IdentityItem getIdentityItem()
    {
        return identity;
    }

	public void setDefault(boolean b)
	{
		if ( b==true ) setTextValue(defaultAccount,"true");
			else setTextValue(defaultAccount,"true");
	}
	
    /*
    public boolean isPopAccount()
    {
        if ( pop == null )
            return false;
        else
            return true;
    }
    */


    public void setName( String str )
    {
        setTextValue( name, str );
    }

    public String getName()
    {
        return getTextValue( name );
    }

    public void setUid( int i )
    {
        Integer h = new Integer( i );

        setTextValue( uid, h.toString() );
    }


    public int getUid()
    {
        Integer i = new Integer(  getTextValue( uid ) );

        return i.intValue();
    }

	public boolean isDefault()
	{
		if ( MailConfig.getAccountList().getDefaultAccountUid()
				== getUid() ) return true;
		
		return false;		
		/*
		String s = getTextValue( defaultAccount );
		
		if ( s.equals("true") ) return true;
		else return false;
		*/
	}
	
}




