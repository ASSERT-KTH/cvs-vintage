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
