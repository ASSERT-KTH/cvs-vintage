//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.

package org.columba.mail.folder;

import org.columba.mail.message.Flags;


public class MessageFolderInfo
{
    private Flags flags;
    private Flags permanentFlags;
    
    private int exists; // total number of messages
    
    private int recent; //  new messages

    private int uidValidity;
    private int uidNext;
    
    private int unseen;

    private boolean readWrite;
    
    
    public MessageFolderInfo()
    {}

	public void clear()
	{
		exists = 0;
		recent = 0;
		unseen = 0;
	}
	
    public void setFlags( Flags f )
    {
        flags = f;
    }

    public void setPermanentFlags( Flags f )
    {
        permanentFlags = f;
    }

    public void setExists( int i )
    {
        exists = i;
    }

    public void setRecent( int i )
    {
        recent = i;
    }

    public void setUnseen( int i )
    {
        unseen = i;
    }

    public void setUidValidity( int i )
    {
        uidValidity = i;
    }

    public void setUidNext( int i )
    {
        uidNext = i;
    }

    public void setReadWrite( boolean b )
    {
        readWrite = b;
    }
    
    

    public void incExists()
    {
	exists++;
    }
    public void incExists(int value)
    {
	exists+=value;
    }    
    public void decExists()
    {
	exists--;
    }
    public void decExists(int value)
    {
	exists-=value;
    }
    
    public void incRecent()
    {
	recent++;
    }
    public void decRecent()
    {
	recent--;
    }
    public void incUnseen()
    {
	unseen++;
    }
    public void decUnseen()
    {
	unseen--;
    }
    

    

    
    public Flags getFlags()
    {
        return flags;
    }

    public Flags getPermanentFlags()
    {
        return permanentFlags;
    }

    public int getExists()
    {
        return exists;
    }

    public int getRecent()
    {
        return recent;
    }

    public int getUidValidity()
    {
        return uidValidity;
    }

    public int getUidNext()
    {
        return uidNext;
    }

    public int getUnseen()
    {
        return unseen;
    }

    public boolean getReadWrite()
    {
        return readWrite;
    }
    
    
    
}
