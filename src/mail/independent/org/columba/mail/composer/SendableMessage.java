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

package org.columba.mail.composer;

import java.util.Vector;

import org.columba.mail.message.AbstractMessage;
import org.columba.mail.message.HeaderInterface;
import org.columba.mail.message.SendableHeader;

public class SendableMessage extends AbstractMessage {
	SendableHeader header;

	public SendableMessage() {
		super();

		header = new SendableHeader();

	}

	public SendableMessage(int accountUid, Vector recipients, String message) {
		super();
		setSource(message);

		header = new SendableHeader();

		header.setRecipients(recipients);
		header.setAccountUid(accountUid);
	}

	public HeaderInterface getHeader() {
		return header;
	}

	public void setHeader(HeaderInterface h) {
		this.header = (SendableHeader) h;
	}

	public int getAccountUid() {
		return ((SendableHeader) getHeader()).getAccountUid();
	}

	public Vector getRecipients() {
		return ((SendableHeader) getHeader()).getRecipients();
	}

	public void setAccountUid(int uid) {
		((SendableHeader) getHeader()).setAccountUid(uid);
	}

	public void setRecipients(Vector rcpt) {
		((SendableHeader) getHeader()).setRecipients(rcpt);
	}

	public Object clone() {
		SendableMessage message = new SendableMessage();

		message.setHeader((SendableHeader) header.clone());

		message.setSource(new String(getSource()));

		message.setRecipients((Vector) getRecipients().clone());

		message.setAccountUid(getAccountUid());

		return message;
	}

}
