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


package org.columba.mail.folder.outbox;

import java.util.*;
import org.columba.mail.composer.*;


public class SendListManager
{
    private Vector sendAccounts;
    private int counter;

    private boolean mutex;


    public SendListManager()
    {
        sendAccounts = new Vector();
        counter = 0;

        mutex = false;
    }

    private synchronized void getMutex()
    {
        while( mutex ) {
            try {
                wait();
            }
            catch( InterruptedException e )
            {
            }
        }

        mutex = true;
    }

    private synchronized void releaseMutex()
    {
        mutex = false;
        notify();
    }


    public void add( SendableMessage message )
    {
        getMutex();

        SendList actList;
        counter++;

	System.out.println("SMTP_SEND::Adding message in sendlistManager");

        for( int i=0; i<sendAccounts.size(); i++)
        {
            actList = (SendList) sendAccounts.get(i);

            if( message.getAccountUid() == actList.getAccountUid() )
            {
                actList.add( message );
                releaseMutex();
                return;
            }
        }

        sendAccounts.add( new SendList( message ) );

        releaseMutex();
    }

    public boolean hasMoreMessages()
    {
        getMutex();

        boolean output = (counter > 0);

        releaseMutex();

        return output;
    }

    public int count()
    {
        int output;

        System.out.println("DEBUG");

        getMutex();

        output = counter;

        releaseMutex();

        return output;
    }



    public Object getNextUid()
    {
        getMutex();

        SendList actList = (SendList) sendAccounts.get(0);
        Object output = actList.getFirst().getUID();

        counter --;

        if( actList.count() == 0 )
        {
            sendAccounts.remove(0);
        }

        releaseMutex();

        return output;
    }


    public SendableMessage getNextMessage()
    {
        getMutex();

        SendList actList = (SendList) sendAccounts.get(0);
        SendableMessage output = actList.getFirst();

        counter --;

        if( actList.count() == 0 )
        {
            sendAccounts.remove(0);
        }

        releaseMutex();

        return output;
    }


}


class SendList
{
    private Vector messages;
    private int accountUid;


    public SendList( SendableMessage message )
    {
        this.accountUid = message.getAccountUid();

        messages = new Vector();
        messages.add( message );
    }

    public int getAccountUid()
    {
        return accountUid;
    }

    public void add( SendableMessage message )
    {
        messages.add( message );
    }

    public SendableMessage getFirst()
    {
        return (SendableMessage) messages.remove(0);
    }

    public SendableMessage get(int index)
    {
        return (SendableMessage) messages.get(index);
    }

    public int count()
    {
        return messages.size();
    }

}
