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


package org.columba.mail.folder;

import org.columba.mail.message.*;

public class FolderOp
{
    public static final int COPY = 1;
    public static final int MOVE = 2;
    public static final int REMOVE = 3;
    public static final int CUT = 4;
    public static final int CCOPY = 5;
    public static final int PASTE = 6;
    public static final int MARK = 7;
    public static final int POP3 = 8;
    public static final int SEND = 9;
    
    
    public static final int READ = 0;
    public static final int EXPUNGED = 1;
    public static final int FLAGGED = 2;
    public static final int ANSWERED = 3;
    
    private static final String[] opName = {"Copy", "Move", "Remove",
                                            "Cut", "Copy", "Paste",
                                            "Mark","Apply Filters", "Send"};
    private static final String[] markVariant = {"Mark as Read", "Mark as Expunged",
                                                 "Mark as Flagged", "Mark as Answered"};
    private int mode;
    private int variant;
    private Folder source;
    private Folder target;
    private Object[] op1;
    private int op2;
    private Message message;
    private String rawString;
    
    
    public FolderOp( int mode, Folder source, Folder target,
                     Object[] op1 , int op2 )
    {
        this.mode = mode;
        this.source = source;
        this.target = target;
        this.op1 = op1;
        this.op2 = op2;
    }

    public FolderOp( int mode, Folder source, Folder target,
                     Object[] op1 )
    {
        this.mode = mode;
        this.source = source;
        this.target = target;
        this.op1 = op1;
    }


    public FolderOp( int mode, Folder source, Object[] op1)
    {
        this.mode = mode;
        this.source = source;
        this.target = target;
        this.op1 = op1;
    }

    public FolderOp( int mode, Folder target)
    {
        this.mode = mode;
        this.target = target;
    }

    public FolderOp( int mode, Folder target, Message message, String rawString)
    {
        this.mode = mode;
        this.target = target;
        this.message = message;
        this.rawString = rawString;
    }

    public FolderOp( int mode, Folder target, Message message )
    {
        this.mode = mode;
        this.target = target;
        this.message = message;
    }

    
    public FolderOp( int mode, int variant, Folder source, Object[] op1)
    {
        this.mode = mode;
        this.source = source;
        this.target = target;
        this.op1 = op1;
        this.variant = variant;
    }
    

    
    public int getMode()
    {
        return mode;
    }

    public Folder getSource()
    {
        return source;
    }

    public Folder getTarget()
    {
        return target;
    }

    public Object[] getOp1()
    {
        return op1;
    }

    public int getOp2()
    {
        return op2;
    }

    public void setMode( int mode )
    {
        this.mode = mode;
    }

    public void setSource( Folder source )
    {
        this.source = source;
    }

    public void setTarget( Folder target )
    {
        this.target = target;
    }

    public void setOp1( Object[] op1 )
    {
        this.op1 = op1;
    }

    public void setOp2( int op2 )
    {
        this.op2 = op2;
    }

    public void setVariant( int var )
    {
        this.variant = var;
    }

    public int getVariant()
    {
        return variant;
    }

    public Message getMessage()
    {
        return message;
    }

    public String getRawString()
    {
        return rawString;
    }
    
    
    
    public String toString()
    {
        StringBuffer output = new StringBuffer();

    
        if ( mode == 7 )
        {
              // mark
            output.append( markVariant[ variant ] );
        }
        else
            output.append(opName[mode-1]);

        
        output.append(" ");

        return output.toString();
    }
        
        
}

