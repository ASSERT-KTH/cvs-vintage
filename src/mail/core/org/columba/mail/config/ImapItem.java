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

import org.columba.core.config.*;
import org.w3c.dom.*;

public class ImapItem extends DefaultItem implements MailCheckInterface
{
	public AdapterNode user;
	public AdapterNode password;
	public AdapterNode host;
	public AdapterNode port;
	public AdapterNode savePassword;
	public AdapterNode automaticallyApplyFilter;
	
	private AdapterNode useDefaultAccount;
	
	private AdapterNode rootNode;
	
	private AdapterNode mailCheck;
	private AdapterNode interval;
	private AdapterNode soundfile;
    private AdapterNode playsound;
    private AdapterNode autodownload;
	

	public ImapItem(AdapterNode rootNode, Document doc)
	{
		super(doc);

		this.rootNode = rootNode;

		parse();
		
		createMissingElements();
	}

	protected void parse()
	{
		int count= getRootNode().getChildCount();
		for (int i = 0; i < count; i++)
		{
			AdapterNode child = getRootNode().getChildAt(i);
			String str = child.getName();
			
			if (str.equals("user"))
			{
				user = child;
			}
			else if (str.equals("password"))
			{
				password = child;
			}
			else if (str.equals("host"))
			{
				host = child;
			}
			else if (str.equals("port"))
			{
				port = child;
			}
			else if (str.equals("savepassword"))
			{
				savePassword = child;
			}
			else if (str.equals("automaticallyapplyfilter"))
			{
				automaticallyApplyFilter = child;
			}
			 else if ( str.equals("usedefaultaccount") )
                {
                	useDefaultAccount = child;
                }
             else  if ( str.equals("mailcheck") )
                {
                    mailCheck = child;
                }
                else  if ( str.equals("interval") )
                {
                    interval = child;
                }
               else  if ( str.equals("playsound") )
                {
                    playsound = child;
                }
                else  if ( str.equals("soundfile") )
                {
                    soundfile = child;
                }
                else  if ( str.equals("autodownload") )
                {
                    autodownload = child;
                }
		}
	}

	protected void createMissingElements()
    {
		if ( automaticallyApplyFilter == null ) automaticallyApplyFilter = addKey( rootNode, "automaticallyapplyfilter", "false");
		if ( useDefaultAccount == null ) useDefaultAccount= addKey( rootNode, "usedefaultaccount", "false" );
		if ( playsound == null ) playsound = addKey( rootNode, "playsound", "false");
		if ( soundfile == null ) soundfile = addKey( rootNode, "soundfile", "default");
		if ( autodownload == null ) autodownload = addKey( rootNode, "autodownload", "true" );
		if ( mailCheck == null ) mailCheck = addKey( rootNode, "mailcheck", "false" );		
		if ( interval == null ) interval = addKey( rootNode, "interval", "5" );
    }
	
	public AdapterNode getRootNode()
	{
		return rootNode;
	}

	/*
	public void setUserNode(AdapterNode node)
	{
		user = node;
	}

	public void setPortNode(AdapterNode node)
	{
		port = node;
	}

	public void setHostNode(AdapterNode node)
	{
		host = node;
	}

	public void setSavePasswordNode(AdapterNode node)
	{
		savePassword = node;
	}

	public void setPasswordNode(AdapterNode node)
	{
		password = node;
	}
	*/

	/*************************************************************************/
	
	public void setAutodownload( boolean b )
	{
		setTextValue( autodownload, (new Boolean(b)).toString() );
	}

	public void setPlaysound( boolean b )
	{
		setTextValue( playsound, (new Boolean(b)).toString() );
	}
	
	public void setSoundfile( String s )
	{
		setTextValue( soundfile, s );
	}
	
	public void setInterval( String str )
    {
        setTextValue( interval, str );
    }

    public void setMailCheck( String str )
    {
        setTextValue( mailCheck, str );
    }
    
	public void setAutomaticallyApplyFilter( boolean b )
	{
			Boolean bool = new Boolean(b);

		setTextValue(automaticallyApplyFilter, bool.toString());	
	}

	public void setUser(String str)
	{
		setCDATAValue(user, str);
	}

	public void setPort(String str)
	{
		setTextValue(port, str);
	}

	public void setPassword(String str)
	{
		setCDATAValue(password, str);
	}

	public void setHost(String str)
	{
		setTextValue(host, str);
	}

	public void setSavePassword(String str)
	{
		setTextValue(savePassword, str);
	}

	public void setSavePassword(boolean b)
	{
		Boolean bool = new Boolean(b);

		setTextValue(savePassword, bool.toString());
	}

	public void setUseDefaultAccount( boolean b )
	{
		Boolean bool = new Boolean(b);
		setTextValue( useDefaultAccount, bool.toString() );	
	}
	
	/**********************************************************************/

	public boolean isAutomaticallyApplyFilterEnabled()
	{
		if (getAutomaticallyApplyFilter().equals("true"))
			return true;
		else
			return false;
	}
	
	public String getAutomaticallyApplyFilter()
	{
		return getTextValue( automaticallyApplyFilter );
	}
		
	public String getUser()
	{
		return getCDATAValue(user);
	}

	public String getPort()
	{
		return getTextValue(port);
	}

	public String getPassword()
	{
		return getCDATAValue(password);
	}

	public String getHost()
	{
		return getTextValue(host);
	}

	public String getSavePassword()
	{
		return getTextValue(savePassword);
	}

	public boolean isSavePassword()
	{
		if (getSavePassword().equals("true"))
			return true;
		else
			return false;
	}
	
	public boolean isUseDefaultAccount()
	{
		if ( getTextValue(useDefaultAccount).equals("true") )
			return true;
		else
			return false;
	}
	
	public boolean isAutoDownload()
	{
		Boolean bool = new Boolean( getTextValue( autodownload ) );
		
		return bool.booleanValue();
	}

	public boolean isPlaysound()
	{
		Boolean bool = new Boolean( getTextValue( playsound ) );
		
		return bool.booleanValue();
	}
	
	public String getSoundfile()
	{
		return getTextValue( soundfile );
	}
	
	public String getMailCheck()
    {
        return getTextValue( mailCheck );
    }
    
    public String getInterval()
    {
        return getTextValue( interval );
    }
    
     public boolean isMailCheck()
    {
        String str = getMailCheck();

        if ( str.equals("true") ) return true;
           else return false;
    }

}