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


/* -*-mode: java; compile-command:"javac PopItem.java -classpath ../"; -*- */
package org.columba.mail.config;

import org.columba.core.config.AdapterNode;
import org.columba.core.config.DefaultItem;
import org.w3c.dom.Document;


public class PopItem extends DefaultItem implements MailCheckInterface
{
    private AdapterNode user;
    private AdapterNode password;
    private AdapterNode host;
    private AdapterNode port;
    private AdapterNode leaveMessage;
    private AdapterNode savePassword;
    private AdapterNode uid;
    private AdapterNode exclude;
    private AdapterNode mailCheck;
    private AdapterNode downloadLimit;
    private AdapterNode interval;
    private AdapterNode limit;
    private AdapterNode loginMethod;
    private AdapterNode soundfile;
    private AdapterNode playsound;
    private AdapterNode autodownload;
    private AdapterNode useDefaultAccount;

    private AdapterNode rootNode;

    private String folderName;


    public PopItem( AdapterNode rootNode, Document doc )
    {
        super( doc );
        this.rootNode = rootNode;

        parse();
        
        createMissingElements();
            
    }


    protected void parse()
    {
    	int count = getRootNode().getChildCount();
    	
        for ( int i=0; i<count; i++ )
            {
                AdapterNode child = getRootNode().getChildAt(i);
				String str = child.getName();
				
                if ( str.equals("user") )
                {
                    user = child;
                }
                else  if ( str.equals("password") )
                {
                    password = child;
                }
                else  if ( str.equals("host") )
                {
                    host = child;
                }
                else  if ( str.equals("port") )
                {
                    port = child;
                }
                else  if ( str.equals("leave") )
                {
                    leaveMessage = child;
                }
                else  if ( str.equals("savepassword") )
                {
                    savePassword = child;
                }
                else  if ( str.equals("exclude") )
                {
                    exclude = child;
                }
                else  if ( str.equals("uid") )
                {
                    uid = child;
                }
                else  if ( str.equals("mailcheck") )
                {
                    mailCheck = child;
                }
                else  if ( str.equals("interval") )
                {
                    interval = child;
                }
                else  if ( str.equals("downloadlimit") )
                {
                    downloadLimit = child;
                }
                else  if ( str.equals("limit") )
                {
                    limit = child;
                }
                else  if ( str.equals("loginmethod") )
                {
                    loginMethod = child;
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
                else if ( str.equals("usedefaultaccount") )
                {
                	useDefaultAccount = child;
                }
            }
    }


	protected void createMissingElements()
    {
		if ( loginMethod == null ) loginMethod = addKey( rootNode, "loginmethod", "USER");
		if ( playsound == null ) playsound = addKey( rootNode, "playsound", "false");
		if ( soundfile == null ) soundfile = addKey( rootNode, "soundfile", "default");
		if ( autodownload == null ) autodownload = addKey( rootNode, "autodownload", "true" );
		if ( useDefaultAccount == null ) useDefaultAccount= addKey( rootNode, "usedefaultaccount", "false" );
    }

    public AdapterNode getRootNode()
        {
            return rootNode;
        }

	


      /*************************************** set *********************************/


	public void setAutodownload( boolean b )
	{
		setTextValue( autodownload, (new Boolean(b)).toString() );
	}

	public void setPlaysound( boolean b )
	{
		System.out.println("setPlaysound="+b);
		setTextValue( playsound, (new Boolean(b)).toString() );
	}
	
	public void setSoundfile( String s )
	{
		setTextValue( soundfile, s );
	}

        public void setFolderName( String s )
        {
	folderName = s;
        }




    public void setInterval( String str )
    {
        setTextValue( interval, str );
    }

    public void setMailCheck( String str )
    {
        setTextValue( mailCheck, str );
    }

    public void setDownloadLimit( String str )
    {
        setTextValue( downloadLimit, str );
    }

    public void setLimit( String str )
    {
        setTextValue( limit, str );
    }

    public void setExclude( String str )
    {
        setTextValue( exclude, str );
    }

    public void setPort( String str )
    {
        setTextValue( port, str );
    }


    public void setSavePassword( String str )
    {
        setTextValue( savePassword, str );
    }



    public void setLeaveMessage( String str )
    {
        setTextValue( leaveMessage, str );
    }


    public void setUser( String str )
    {
        setCDATAValue( user, str );
    }

    public void setPassword( String str )
    {
        setCDATAValue( password, str );
    }

    public void setSavePassword(boolean b )
    {
        Boolean bool = new Boolean(b);

        setTextValue( savePassword, bool.toString() );
    }

	public void setUseDefaultAccount( boolean b )
	{
		Boolean bool = new Boolean(b);
		setTextValue( useDefaultAccount, bool.toString() );	
	}
	
	
		

    public void setHost( String str )
    {
        setTextValue( host, str );
    }

    public void setUid( int i )
    {
        Integer h = new Integer( i );

        setTextValue( uid, h.toString() );
    }


	public void setLoginMethod( String str )
	{	
		setTextValue( loginMethod, str );
	}
	

      /*********************************** get ********************************/

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
	
	public String getLoginMethod()
	{
		return getTextValue( loginMethod );
	}
	

    public String getFolderName()
    {
		return folderName;
    }




    public String getMailCheck()
    {
        return getTextValue( mailCheck );
    }

    public String getDownloadLimit()
    {
        return getTextValue( downloadLimit );
    }

    public String getLimit()
    {
        return getTextValue( limit );
    }

    public String getInterval()
    {
        return getTextValue( interval );
    }

    public String getLeaveMessage()
    {
        return getTextValue( leaveMessage );
    }

    public String getSavePassword()
    {
        return getTextValue( savePassword );
    }

    public String getUser()
    {
        return getCDATAValue( user );
    }


    public String getPassword()
    {
        return getCDATAValue( password );
    }

    public String getHost()
    {
        return getTextValue( host );
    }



    public int getUid()
    {
        Integer i = new Integer(  getTextValue( uid ) );


        return i.intValue();
    }


    public String getPort()
    {
        return getTextValue( port );
    }



    public String getExclude()
    {
        return getTextValue( exclude );
    }


    public boolean isLeaveMessage()
    {
	if ( getLeaveMessage().equals("true") )
	    return true;
	else
	    return false;
    }

    public boolean isSavePassword()
    {
	if ( getSavePassword().equals("true") )
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

    public boolean isLimit()
    {
        if ( getLimit().equals("true") )
           return true;
        else
            return false;
    }

    public int getMailCheckInterval()
    {
        String str = getInterval();
        Integer i = new Integer( str );

        return i.intValue();
    }

    public boolean isMailCheck()
    {
        String str = getMailCheck();

        if ( str.equals("true") ) return true;
           else return false;
    }


}

