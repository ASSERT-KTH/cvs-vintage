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
