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

package org.columba.mail.composer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.columba.core.io.CloneStreamMaster;
import org.columba.mail.message.ColumbaMessage;
import org.columba.mail.message.SendableHeader;
import org.columba.ristretto.message.io.SourceInputStream;

public class SendableMessage extends ColumbaMessage {

	private CloneStreamMaster sourceStream;

	public SendableMessage() {
		super();
	}

	public SendableMessage(int accountUid, List recipients, String message) {
		super();
		setStringSource(message);

		columbaHeader.getAttributes().put(
			"columba.recipients",
			recipients);
		columbaHeader.getAttributes().put("columba.accountuid", new Integer(accountUid));
	}

	public int getAccountUid() {
		return ((SendableHeader) getHeader()).getAccountUid();
	}

	public List getRecipients() {
		return ((SendableHeader) getHeader()).getRecipients();
	}

	public void setAccountUid(int uid) {
		((SendableHeader) getHeader()).setAccountUid(uid);
	}

	public void setRecipients(List rcpt) {
		((SendableHeader) getHeader()).setRecipients(rcpt);
	}

	/**
	 * @return Returns the sourceStream.
	 */
	public InputStream getSourceStream() {
		if (sourceStream == null) {
			return new SourceInputStream(getSource());
		}

		return sourceStream.getClone();
	}

	/**
	 * @param sourceStream
	 *            The sourceStream to set.
	 */
	public void setSourceStream(InputStream sourceStream) throws IOException {
		this.sourceStream = new CloneStreamMaster(sourceStream);
	}

}
