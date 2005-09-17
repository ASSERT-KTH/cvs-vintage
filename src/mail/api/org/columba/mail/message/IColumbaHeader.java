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
package org.columba.mail.message;

import org.columba.ristretto.message.Attributes;
import org.columba.ristretto.message.Flags;
import org.columba.ristretto.message.Header;

/**
 * @author fdietz
 *
 */
public interface IColumbaHeader {
	void copyColumbaKeys(IColumbaHeader header);

	/* (non-Javadoc)
	 * @see org.columba.mail.message.HeaderInterface#count()
	 */int count();

	/* (non-Javadoc)
	 * @see org.columba.mail.message.HeaderInterface#getFlags()
	 */Flags getFlags();

	/**
	 * Note: Don't use this method anymore when accessing
	 * attributes like "columba.size", use getAttribute() instead
	 *
	 */
	Object get(String s);

	/* (non-Javadoc)
	 * @see org.columba.mail.message.HeaderInterface#set(java.lang.String, java.lang.Object)
	 */void set(String s, Object o);

	/**
	 * @return
	 */
	Header getHeader();

	/**
	 * @return
	 */
	Attributes getAttributes();

	/**
	 * @param attributes
	 */
	void setAttributes(Attributes attributes);

	/**
	 * @param flags
	 */
	void setFlags(Flags flags);

	/**
	 * @param header
	 */
	void setHeader(Header header);

	Boolean hasAttachments();
	
	public Object clone();
}