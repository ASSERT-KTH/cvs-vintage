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
package org.columba.mail.folder.outbox;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Vector;

import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.composer.SendableMessage;
import org.columba.mail.config.FolderItem;
import org.columba.mail.folder.headercache.CachedFolder;
import org.columba.mail.folder.headercache.LocalHeaderCache;
import org.columba.mail.folder.mh.CachedMHFolder;
import org.columba.mail.message.ColumbaMessage;
import org.columba.mail.message.SendableHeader;
import org.columba.ristretto.message.HeaderInterface;
import org.columba.ristretto.message.io.CharSequenceSource;
import org.columba.ristretto.parser.MessageParser;

public class OutboxFolder extends CachedMHFolder {

	private SendListManager[] sendListManager = new SendListManager[2];
	private int actSender;
	private boolean isSending;

	protected OutboxHeaderCache cache;

	public OutboxFolder(FolderItem item) {
		super(item);

		sendListManager[0] = new SendListManager();
		sendListManager[1] = new SendListManager();
		actSender = 0;

		isSending = false;

		cache = new OutboxHeaderCache(this);

	}

	public ColumbaMessage getMessage(
		Object uid)
		throws Exception {
		if (aktMessage != null) {
			if (aktMessage.getUID().equals(uid)) {
				// this message is already cached
				ColumbaLogger.log.info("using already cached message..");

				return aktMessage;
			}
		}

		String source = getMessageSource(uid);

		ColumbaMessage message = new ColumbaMessage( MessageParser.parse( new CharSequenceSource(source)));			
		message.setUID(uid);

		SendableHeader header = (SendableHeader) getHeaderList().get(uid);
		SendableMessage sendableMessage = new SendableMessage();
		sendableMessage.setHeader(header);
		sendableMessage.setMimePartTree(message.getMimePartTree());
		sendableMessage.setStringSource(source);

		aktMessage = sendableMessage;

		return sendableMessage;
	}

	public String getDefaultChild() {
		return "MHFolder";
	}

	private void swapListManagers() throws Exception {
		// copy lost Messages
		System.out.println(
			"Sizes : "
				+ sendListManager[actSender].count()
				+ " - "
				+ sendListManager[1
				- actSender].count());

		while (sendListManager[actSender].hasMoreMessages()) {
			sendListManager[1
				- actSender].add(
					(SendableMessage) getMessage(sendListManager[actSender]
						.getNextUid()));
		}

		// swap
		actSender = 1 - actSender;

		System.out.println(
			"Sizes : "
				+ sendListManager[actSender].count()
				+ " - "
				+ sendListManager[1
				- actSender].count());

	}

	public void stoppedSending() {
		isSending = false;
	}

	public void save() throws Exception {
		// only save header-cache if folder data changed
		if (hasChanged() == true) {

			getHeaderCacheInstance().save();
			setChanged(false);
		}
	}

	class OutboxHeaderCache extends LocalHeaderCache {
		public OutboxHeaderCache(CachedFolder folder) {
			super(folder);
		}

		public HeaderInterface createHeaderInstance() {
			return new SendableHeader();
		}

		protected void loadHeader(ObjectInputStream p, HeaderInterface h)
			throws Exception {
			super.loadHeader(p, h);

			int accountUid = p.readInt();
			((SendableHeader) h).setAccountUid(accountUid);

			List recipients = (Vector) p.readObject();
			((SendableHeader) h).setRecipients(recipients);

		}

		protected void saveHeader(ObjectOutputStream p, HeaderInterface h)
			throws Exception {
			super.saveHeader(p, h);

			p.writeInt(((SendableHeader) h).getAccountUid());

			p.writeObject(((SendableHeader) h).getRecipients());

		}
	}
}
