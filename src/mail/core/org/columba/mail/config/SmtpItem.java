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

public class SmtpItem extends DefaultItem
{

	private AdapterNode user;
	private AdapterNode password;
	private AdapterNode savepassword;
	private AdapterNode host;
	private AdapterNode port;
	private AdapterNode esmtp;
	private AdapterNode loginMethod;
	private AdapterNode popbeforesmtp;
	private AdapterNode useDefaultAccount;

	// this is a uid representing the sent folder for this account
	//private AdapterNode uid;

	private AdapterNode bccYourself;
	private AdapterNode bccAnother;

	private AdapterNode rootNode;

	public SmtpItem(AdapterNode rootNode, Document doc)
	{
		super(doc);

		this.rootNode = rootNode;

		parse();

		createMissingElements();
	}

	protected void parse()
	{
		int count = getRootNode().getChildCount();
		
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
			else if (str.equals("esmtp"))
			{
				esmtp = child;
			}
			else if (str.equals("savepassword"))
			{
				savepassword = child;
			}
			/*
			else  if ( str.equals("uid") )
			{
			    uid = child;
			}
			*/
			else if (str.equals("bccyourself"))
			{
				bccYourself = child;
			}
			else if (str.equals("bccanother"))
			{
				bccAnother = child;
			}
			else if (str.equals("popbeforesmtp"))
			{
				popbeforesmtp = child;
			}
			else if (str.equals("loginmethod"))
			{
				loginMethod = child;
			}
			else if ( str.equals("usedefaultaccount") )
                {
                	useDefaultAccount = child;
                }

		}
	}

	protected void createMissingElements()
	{
		if (popbeforesmtp == null)
			popbeforesmtp = addKey(rootNode, "popbeforesmtp", "false");
		if (loginMethod == null)
			loginMethod = addKey(rootNode, "loginmethod", "LOGIN");
		if ( useDefaultAccount == null ) useDefaultAccount= addKey( rootNode, "usedefaultaccount", "false" );
	}
	public AdapterNode getRootNode()
	{
		return rootNode;
	}
	/*
	public void setUidNode( AdapterNode node )
	{
	    uid= node;
	}
	*/

	/*
		public void setPortNode( AdapterNode node )
		{
		    port = node;
		}
	
	
		public void setHostNode( AdapterNode node )
		{
		    host = node;
		}
	
		public void setUserNode( AdapterNode node )
		{
		    user = node;
		}
	
	        public void setPasswordNode( AdapterNode node )
		{
		    password = node;
		}
	
	        public void setSavePasswordNode( AdapterNode node )
		{
		    savepassword = node;
		}
	
	
		public void setESmtpNode( AdapterNode node )
		{
		    esmtp = node;
		}
	
		public void setBccYourselfNode( AdapterNode node )
		{
		    bccYourself = node;
		}
		public void setBccAnotherNode( AdapterNode node )
		{
		    bccAnother = node;
		}
	*/

	/*********************************** set *********************************/

	public void setPopBeforeSmtp(boolean b)
	{
		Boolean bool = new Boolean(b);

		setTextValue(popbeforesmtp, bool.toString());
	}

	public void setUser(String str)
	{
		setCDATAValue(user, str);
	}

	public void setPassword(String str)
	{
		
		setCDATAValue(password, str);
	}

	public void setSavePassword(String str)
	{
		setTextValue(savepassword, str);
	}

	public void setHost(String str)
	{
		setTextValue(host, str);
	}

	public void setPort(String str)
	{
		setTextValue(port, str);
	}

	public void setESmtp(String str)
	{
		setTextValue(esmtp, str);
	}

	public void setLoginMethod(String str)
	{
		setTextValue(loginMethod, str);
	}
	/*
	public void setUid( int i )
	{
	    Integer h = new Integer( i );
	
	    setTextValue( uid, h.toString() );
	}
	*/

	public void setBccYourself(String str)
	{
		setTextValue(bccYourself, str);

	}
	public void setBccAnother(String str)
	{
		setTextValue(bccAnother, str);
	}
	
	public void setUseDefaultAccount( boolean b )
	{
		Boolean bool = new Boolean(b);
		setTextValue( useDefaultAccount, bool.toString() );	
	}

	/******************************************** get ************************/

	public boolean getPopBeforeSmtp()
	{
		String str = getTextValue(popbeforesmtp);
		Boolean bool = new Boolean(str);

		return bool.booleanValue();
	}

	public String getLoginMethod()
	{
		return getTextValue(loginMethod);
	}

	public String getUser()
	{
		return getCDATAValue(user);
	}

	public String getPassword()
	{
		return getCDATAValue(password);
	}

	public String getSavePassword()
	{
		return getTextValue(savepassword);
	}

	public String getHost()
	{
		return getTextValue(host);
	}

	public int getPort()
	{
		int output;

		try
		{
			output = new Integer(getTextValue(port)).intValue();
		}
		catch (NumberFormatException e)
		{
			System.out.println(e);

			return -1;
		}

		return output;
	}

	public String getESmtp()
	{
		return getTextValue(esmtp);
	}

	/*
	public int getUid()
	{
	    Integer i = new Integer(  getTextValue( uid ) );
	
	    return i.intValue();
	}
	*/

	public String getBccYourself()
	{
		return getTextValue(bccYourself);
	}

	public String getBccAnother()
	{
		return getTextValue(bccAnother);
	}

	public boolean isESmtp()
	{
		if (getESmtp().equals("true"))
			return true;
		else
			return false;
	}

	public boolean isBccYourself()
	{
		if (getBccYourself().equals("true"))
			return true;
		else
			return false;
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
}