// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.folder.headercache;

import java.awt.Color;
import java.util.Date;

import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.IColumbaHeader;
import org.columba.ristretto.message.Address;

/**
 * 
 * 
 * Holds a collection of all cached headerfields, which Columba needs to be able
 * to quickly show the message summary, etc. to the user.
 * 
 * @author fdietz
 */
public class CachedHeaderfields {

	// internally used headerfields
	// these are all boolean values, which are saved using
	// a single int value
	public static final String[] INTERNAL_COMPRESSED_HEADERFIELDS = {

	// message flags
			"columba.flags.seen", "columba.flags.recent",
			"columba.flags.answered", "columba.flags.flagged",
			"columba.flags.expunged", "columba.flags.draft",
			//	true, if message has attachments, false otherwise
			"columba.attachment",
			//	true/false
			"columba.spam" };

	// this internally used headerfields can be of every basic
	// type, including String, Integer, Boolean, Date, etc.
	public static final String[] INTERNAL_HEADERFIELDS = {

	// priority as integer value
			"columba.priority",
			// short from, containing only name of person
			"columba.from",
			// host from which this message was downloaded
			"columba.host",
			// date
			"columba.date",
			// size of message
			"columba.size",
			// properly decoded subject
			"columba.subject",
			// message color
			"columba.color",
			// account ID
			"columba.accountuid",
			// to
			"columba.to",
			// Cc
			"columba.cc" };

	public static final Class[] INTERNAL_HEADERFIELDS_TYPE = { Integer.class,
			Address.class, String.class, Date.class, Integer.class,
			String.class, Color.class, Integer.class, Address.class,
			String.class };

	// these are cached by default
	public static final String[] DEFAULT_HEADERFIELDS = { "Subject", "From",
			"To", "Cc", "Date", "Message-ID", "In-Reply-To", "References",
			"Content-Type" };

	public static final String[] POP3_HEADERFIELDS = { "Subject", "From",
			"columba.date", "columba.size",
			// POP3 message UID
			"columba.pop3uid",
			// was this message already fetched from the server?
			"columba.alreadyfetched" };

	public static final Class[] POP3_HEADERFIELDS_TYPE = { String.class,
			String.class, Date.class, Integer.class, String.class,
			Boolean.class };

	/**
	 * No need for creating instances of this class.
	 */
	private CachedHeaderfields() {
	}

	/**
	 * 
	 * create new header which only contains headerfields needed by Columba
	 * (meaning they also get cached)
	 * 
	 * @param h
	 * @return
	 */
	public static IColumbaHeader stripHeaders(IColumbaHeader h) {
		//return h;
		IColumbaHeader strippedHeader = new ColumbaHeader();

		//		copy all internally used headerfields
		for (int i = 0; i < DEFAULT_HEADERFIELDS.length; i++) {
			if (h.get(DEFAULT_HEADERFIELDS[i]) != null) {

				strippedHeader.set(DEFAULT_HEADERFIELDS[i], h
						.get(DEFAULT_HEADERFIELDS[i]));
			}
		}

		for (int i = 0; i < INTERNAL_HEADERFIELDS.length; i++) {
			if (h.get(INTERNAL_HEADERFIELDS[i]) != null) {
				strippedHeader.set(INTERNAL_HEADERFIELDS[i], h
						.get(INTERNAL_HEADERFIELDS[i]));
			}
		}

		for (int i = 0; i < INTERNAL_COMPRESSED_HEADERFIELDS.length; i++) {
			if (h.get(INTERNAL_COMPRESSED_HEADERFIELDS[i]) != null) {
				strippedHeader.set(INTERNAL_COMPRESSED_HEADERFIELDS[i], h
						.get(INTERNAL_COMPRESSED_HEADERFIELDS[i]));
			}
		}

		return strippedHeader;
	}

	public static String[] getDefaultHeaderfields() {
		String[] result = new String[DEFAULT_HEADERFIELDS.length];
		System.arraycopy(DEFAULT_HEADERFIELDS,0, result, 0, DEFAULT_HEADERFIELDS.length);
		return result;
	}
};