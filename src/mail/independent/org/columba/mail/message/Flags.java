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

package org.columba.mail.message;

public class Flags
{
    private HeaderInterface header;
    private Message message;

    /*
    private boolean seen;
    private boolean answered;
    private boolean flagged;
    private boolean deleted;
    private boolean draft;
    private boolean recent;
    */

    public Flags( Message m )
    {
        this.message = m;
        this.header = (HeaderInterface) m.getHeader();

        /*
        seen = false;
        answered = false;
        flagged = false;
        deleted = false;
        draft = false;
	recent = false;
        */
    }

    public Flags( HeaderInterface header )
    {
        this.header = header;

        /*
        seen = false;
        answered = false;
        flagged = false;
        deleted = false;
        draft = false;
	recent = false;
        */
    }


    public boolean getSeen()
    {
        //System.out.println("flags->getSeen(): "+ header.get("columba.flags.seen") );
        Boolean b = (Boolean) header.get("columba.flags.seen");

        return b.booleanValue();
    }

    public boolean getRecent()
    {
        Boolean b = (Boolean) header.get("columba.flags.recent");

        return b.booleanValue();
    }

    public boolean getAnswered()
    {
        Boolean b = (Boolean) header.get("columba.flags.answered");

        return b.booleanValue();
    }

    public boolean getFlagged()
    {
        Boolean b = (Boolean) header.get("columba.flags.flagged");

        return b.booleanValue();
    }

    public boolean getDeleted()
    {
        Boolean b = (Boolean) header.get("columba.flags.expunged");

        return b.booleanValue();
    }

    public boolean getDraft()
    {
        Boolean b = (Boolean) header.get("columba.flags.draft");

        return b.booleanValue();
    }

    public void setSeen( boolean b )
    {
        header.set("columba.flags.seen", new Boolean(b) );
    }

    public void setRecent( boolean b )
    {
        header.set("columba.flags.recent", new Boolean(b) );
    }

    public void setAnswered( boolean b )
    {
        header.set("columba.flags.answered", new Boolean(b) );
    }

    public void setFlagged( boolean b )
    {
        header.set("columba.flags.flagged", new Boolean(b) );
    }

    public void setDeleted( boolean b )
    {
        header.set("columba.flags.expunged", new Boolean(b) );
    }

    public void setDraft( boolean b )
    {
        header.set("columba.flags.draft", new Boolean(b) );
    }


    /*
    public Object clone()
    {
        Flags flags = new Flags( message );

        flags.setAnswered( getAnswered() );
        flags.setFlagged( getFlagged() );
        flags.setDeleted( getDeleted() );
        flags.setDraft( getDraft() );
        flags.setRecent( getRecent() );
        flags.setSeen( getSeen() );

        return flags;

    }
    */

}
