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
package org.columba.mail.imap.protocol;

import java.util.Vector;

/**
 * @author freddy
 *
 * Class encapsulates CLIENT arguments, which can be literal or
 * quoted strings
 * 
 * 
 * 
 *   Following a short paragraph of RFC2060 IMAP:
 * 
 *
 *   A string is in one of two forms: literal and quoted string.  The
 *   literal form is the general form of string.  The quoted string form
 *   is an alternative that avoids the overhead of processing a literal at
 *   the cost of limitations of characters that can be used in a quoted
 *   string.
 *
 *   A literal is a sequence of zero or more octets (including CR and LF),
 *   prefix-quoted with an octet count in the form of an open brace ("{"),
 *   the number of octets, close brace ("}"), and CRLF.  In the case of
 *   literals transmitted from server to client, the CRLF is immediately
 *   followed by the octet data.  In the case of literals transmitted from
 *   client to server, the client MUST wait to receive a command
 *   continuation request (described later in this document) before
 *   sending the octet data (and the remainder of the command).
 *
 *   A quoted string is a sequence of zero or more 7-bit characters,
 *   excluding CR and LF, with double quote (<">) characters at each end.
 *
 *   The empty string is represented as either "" (a quoted string with
 *   zero characters between double quotes) or as {0} followed by CRLF (a
 *   literal with an octet count of 0).
 *
 *      Note: Even if the octet count is 0, a client transmitting a
 *      literal MUST wait to receive a command continuation request.
 * 
 */
public class Arguments {

	Vector v;

	public Arguments() {
		v = new Vector();
	}

	public Arguments(String[] strings) {
		v = new Vector();

		for (int i = 0; i < strings.length; i++) {
			v.add(strings[i]);
		}
	}

	public int count() {
		return v.size();
	}

	public void add(String argument) {
		v.add( new ByteString(argument) );
	}

	public void add(byte[] argument) {
		v.add(argument);
	}

	public Object get(int i) {
		return v.get(i);
	}

	

	

}
