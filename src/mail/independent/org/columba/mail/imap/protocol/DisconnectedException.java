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

package org.columba.mail.imap.protocol;

import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.imap.IMAPResponse;

/**
 * @version 	1.0
 * @author
 */
public class DisconnectedException extends IMAPException
{
	public DisconnectedException() {
        super();
    }

    public DisconnectedException( String s )
    {
        super( "IMAP Server disconnected:\n"+s );
    }
    
    public DisconnectedException( IMAPResponse response )
    {
    	super( response );
    	
    	ColumbaLogger.log.debug("disconnect -> reconnecting...");
    }
}
