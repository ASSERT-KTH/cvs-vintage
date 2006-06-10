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

import org.columba.ristretto.io.Source;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;

/**
 * @author fdietz
 *
 */
public interface IColumbaMessage {
	void setBodyPart(MimePart body);

	void setUID(Object o);

	Object getUID();

	MimeTree getMimePartTree();

	void setMimePartTree(MimeTree ac);

	void freeMemory();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.ristretto.message.Message#getHeader()
	 */IColumbaHeader getHeader();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.ristretto.message.Message#setHeader(org.columba.ristretto.message.Header)
	 */void setHeader(IColumbaHeader h);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.ristretto.message.Message#getBodyPart()
	 */MimePart getBodyPart();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.ristretto.message.Message#getMimePart(int)
	 */MimePart getMimePart(int number);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.ristretto.message.Message#getMimePartCount()
	 */int getMimePartCount();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.ristretto.message.Message#getSource()
	 */Source getSource();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.ristretto.message.Message#setHeader(org.columba.ristretto.message.Header)
	 */void setHeader(Header h);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.ristretto.message.Message#setSource(org.columba.ristretto.message.io.Source)
	 */void setSource(Source source);

	void close();
}