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

package org.columba.mail.composer;

import org.columba.mail.message.*;

import java.util.*;

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
