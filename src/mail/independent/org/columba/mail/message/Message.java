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


public class Message extends AbstractMessage {
	protected ColumbaHeader header;

	public Message() {
		super();

		header = new ColumbaHeader();

	}

	public Message(ColumbaHeader header) {
		super();
		this.header = header;

	}

	public HeaderInterface getHeader() {
		return header;
	}

	public void setHeader(HeaderInterface h) {
		this.header = (ColumbaHeader) h;
	}

	public Object clone() {
		Message message = new Message();

		if (header != null)
			message.setHeader((ColumbaHeader) header.clone());

		if (getSource() != null)
			message.setSource(new String(getSource()));

		return message;
	}

}