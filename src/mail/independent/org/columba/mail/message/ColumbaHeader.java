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

import org.columba.ristretto.message.Address;
import org.columba.ristretto.message.Attributes;
import org.columba.ristretto.message.BasicHeader;
import org.columba.ristretto.message.Flags;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.message.HeaderInterface;
import org.columba.ristretto.parser.HeaderParser;

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

public class ColumbaHeader implements HeaderInterface {

	protected Header header;
	protected Attributes attributes;
	protected Flags flags;

	public ColumbaHeader( ColumbaHeader header ) {
		this.header = header.getHeader();
		this.attributes = header.getAttributes();
		this.flags = header.getFlags();
	}

	public ColumbaHeader() {
		this(new Header());
		
	}

	public ColumbaHeader(Header header) {
		this.header = header;
		flags = new Flags();
		attributes = new Attributes();

		BasicHeader basicHeader = new BasicHeader( header );

		attributes.put("columba.fetchstate", new Boolean(false));
		attributes.put("columba.priority", new Integer( basicHeader.getPriority()));
		Address from = basicHeader.getFrom();
		if ( from != null)
			attributes.put("columba.from", from);
		else 
		attributes.put("columba.from", new Address(""));
		/*
		if( from != null ) {			
			attributes.put("columba.from", from.toString());
		} else {
			attributes.put("columba.from", "");
		}
		*/
		
		attributes.put("columba.host", new String());
		attributes.put("columba.date", basicHeader.getDate());
		String subject = basicHeader.getSubject();
		if( subject != null) {
			attributes.put("columba.subject", subject);
		} else {
			attributes.put("columba.subject", "");			
		}
		attributes.put("columba.attachment", new Boolean(false));
		attributes.put("columba.size", new Integer(0));
	}


	public Object clone() {
		ColumbaHeader clone = new ColumbaHeader();
		clone.attributes = (Attributes) this.attributes.clone();
		clone.flags = (Flags) this.flags.clone();
		clone.header = (Header) this.header.clone();
		
		return clone;
	}

	public void copyColumbaKeys(ColumbaHeader header) {
		header.flags = (Flags) flags.clone();
		header.attributes = (Attributes) attributes.clone();
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.message.HeaderInterface#count()
	 */
	public int count() {
		return attributes.count() + header.count() + 5;
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.message.HeaderInterface#getFlags()
	 */
	public Flags getFlags() {
		return flags;
	}


	/* (non-Javadoc)
	 * @see org.columba.mail.message.HeaderInterface#get(java.lang.String)
	 */
	public Object get(String s) {
		if( s.startsWith("columba.flags.")) {
			String flag = s.substring("columba.flags.".length());
			if( flag.equals("seen")) {
				return new Boolean( flags.get(Flags.SEEN) ); 
			}
			if( flag.equals("recent")) {
				return new Boolean( flags.get(Flags.RECENT) ); 
			}
			if( flag.equals("answered")) {
				return new Boolean( flags.get(Flags.ANSWERED) ); 
			}
			if( flag.equals("draft")) {
				return new Boolean( flags.get(Flags.DRAFT) ); 
			}
			if( flag.equals("flagged")) {
				return new Boolean( flags.get(Flags.FLAGGED) ); 
			}
			if( flag.equals("expunged")) {
				return new Boolean( flags.get(Flags.EXPUNGED) ); 
			}
		}
		
		if( s.startsWith("columba.")) {
			return attributes.get(s);
		}
		
		return header.get(HeaderParser.normalizeKey(s));
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.message.HeaderInterface#set(java.lang.String, java.lang.Object)
	 */
	public void set(String s, Object o) {
		if( s.startsWith("columba.flags")) {
			String flag = s.substring("columba.flags.".length());
			boolean value = ((Boolean) o).booleanValue();
			if( flag.equals("seen")) {				
				flags.set(Flags.SEEN, value);
				return; 
			}
			if( flag.equals("recent")) {
				flags.set(Flags.RECENT, value);
				return; 
			}
			if( flag.equals("answered")) {
				flags.set(Flags.ANSWERED, value );
				return; 
			}
			if( flag.equals("expunged")) {
				flags.set(Flags.EXPUNGED, value);
				return; 
			}
			if( flag.equals("draft")) {
				flags.set(Flags.DRAFT, value );
				return; 
			}
			if( flag.equals("flagged")) {
				flags.set(Flags.FLAGGED,value ); 
			}
		}
		
		if( s.startsWith("columba.")) {
			attributes.put(s, o);
		} else {
			header.set(HeaderParser.normalizeKey(s), (String) o);
		}
	}

	/**
	 * @return
	 */
	public Header getHeader() {
		return header;
	}

	/**
	 * @return
	 */
	public Attributes getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes
	 */
	public void setAttributes(Attributes attributes) {
		this.attributes = attributes;
	}

	/**
	 * @param flags
	 */
	public void setFlags(Flags flags) {
		this.flags = flags;
	}

	/**
	 * @param header
	 */
	public void setHeader(Header header) {
		this.header = header;
	}

}
