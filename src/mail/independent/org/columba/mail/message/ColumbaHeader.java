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

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * represents a Rfc822-compliant header
 * every headeritem is saved in a hashtable-structure
 * generally every headeritem is a string,
 * but for optimization reasons some items
 * are going to change to for example a Date class
 *
 * we added these items:
 *  - a date object
 *  - shortfrom, a parsed from
 *  - alreadyfetched, Boolean
 *  - pop3uid, String
 *  - uid, String
 *  - size, Integer
 *  - attachment, Boolean
 *  - priority, Integer
 */

public class ColumbaHeader extends Rfc822Header implements HeaderInterface{
	//protected Flags flags;

	public ColumbaHeader() {
		super();
		//flags = new Flags( this );

		set("columba.fetchstate", new Boolean(false));

		set("columba.flags.seen", new Boolean(false));
		set("columba.flags.recent", new Boolean(false));
		set("columba.flags.answered", new Boolean(false));
		set("columba.flags.flagged", new Boolean(false));
		set("columba.flags.expunged", new Boolean(false));
		set("columba.flags.draft", new Boolean(false));
	}

	/*
	public ColumbaHeader(Rfc822Header header)
	{
	this.hashTable = (Hashtable) header.hashTable.clone();
	flags = new Flags( this );
	
	    set("columba.fetchstate", new Boolean( false ) );
	
	    set("columba.flags.seen", new Boolean( false ) );
	    set("columba.flags.recent", new Boolean( false ) );
	    set("columba.flags.answered", new Boolean( false ) );
	    set("columba.flags.flagged", new Boolean( false ) );
	    set("columba.flags.expunged", new Boolean( false ) );
	    set("columba.flags.draft", new Boolean( false ) );
	}
	*/

	/*
	public Flags getFlags() {
		return new Flags(this);
	}
	*/
	public Object clone() {

		ColumbaHeader header = new ColumbaHeader();
		
		Hashtable ht = new Hashtable();

		for (Enumeration e = getHashtable().keys(); e.hasMoreElements();) {
			Object o = e.nextElement();
			ht.put(o, getHashtable().get(o));

		}
		header.setHashtable(ht);
		
		//header.setHashtable( (Hashtable) hashTable.clone() );

		

		return header;
	}

	public void copyColumbaKeys(ColumbaHeader header) {
		if (header != null) {
			Hashtable oldTable = header.hashTable;
			Hashtable newTable = this.hashTable;

			for (Enumeration keys = oldTable.keys(); keys.hasMoreElements();) {
				String aktKey = (String) keys.nextElement();
				if (aktKey.startsWith("columba."))
					newTable.put(aktKey, oldTable.get(aktKey));
			}

		}
	}
	
	

}
