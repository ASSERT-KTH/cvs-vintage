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
import java.util.Enumeration;
import java.util.Vector;

import org.columba.core.command.WorkerStatusController;
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.composer.SendableMessage;
import org.columba.mail.config.FolderItem;
import org.columba.mail.folder.LocalFolder;
import org.columba.mail.folder.LocalHeaderCache;
import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.mail.folder.mh.MHFolder;
import org.columba.mail.message.AbstractMessage;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderInterface;
import org.columba.mail.message.HeaderList;
import org.columba.mail.message.Message;
import org.columba.mail.message.SendableHeader;
import org.columba.mail.parser.Rfc822Parser;

public class OutboxFolder extends MHFolder {

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

	
	// this method needs to be fixed for the sake of completness
	// (we don't use it anyway)
	public Object addMessage(String source, WorkerStatusController worker)
		throws Exception {
			
		getHeaderList(worker);
		
		Object newUid = super.addMessage(source, worker);

		Rfc822Parser parser = new Rfc822Parser();

		ColumbaHeader header = parser.parseHeader(source);

		AbstractMessage m = new Message(header);
		ColumbaHeader h = (ColumbaHeader) m.getHeader();

		parser.addColumbaHeaderFields(h);

		Integer sizeInt = new Integer(source.length());
		int size = Math.round(sizeInt.intValue() / 1024);
		h.set("columba.size", new Integer(size));

		h.set("columba.uid", newUid);
		
		if (h.get("columba.flags.recent").equals(Boolean.TRUE))
			getMessageFolderInfo().incRecent();
		if (h.get("columba.flags.seen").equals(Boolean.FALSE))
			getMessageFolderInfo().incUnseen();

		cache.add(h);

		return newUid;
	}

	public Object addMessage(
		AbstractMessage message,
		WorkerStatusController worker)
		throws Exception {
			
		getHeaderList(worker);
		
		Object newUid = super.addMessage(message, worker);

		SendableHeader h =
			(SendableHeader) ((SendableHeader) message.getHeader());

		h.set("columba.uid", newUid);
		
		if (h.get("columba.flags.recent").equals(Boolean.TRUE))
			getMessageFolderInfo().incRecent();
		if (h.get("columba.flags.seen").equals(Boolean.FALSE))
			getMessageFolderInfo().incUnseen();

		cache.add(h);

		return newUid;
	}
	
	public Object[] getUids(WorkerStatusController worker) throws Exception {
		cache.getHeaderList(worker);
		
		int count = cache.count();
		Object[] uids = new Object[count];

		int i = 0;
		for (Enumeration e = cache.getHeaderList(worker).keys();
			e.hasMoreElements();
			) {
			uids[i++] = e.nextElement();
		}

		return uids;
	}
	
	public void expungeFolder(Object[] uids, WorkerStatusController worker) throws Exception {
	

		for (int i = 0; i < uids.length; i++) {
			Object uid = uids[i];

			HeaderInterface h = getMessageHeader(uid, worker);
			Boolean expunged = (Boolean) h.get("columba.flags.expunged");

			//ColumbaLogger.log.debug("expunged=" + expunged);

			if (expunged.equals(Boolean.TRUE)) {
				// move message to trash

				ColumbaLogger.log.info(
					"moving message with UID " + uid + " to trash");

				// remove message
				removeMessage(uid, worker);

			}
		}
	}

	protected void markMessage(
		Object uid,
		int variant,
		WorkerStatusController worker)
		throws Exception {
		ColumbaHeader h = (ColumbaHeader) cache.getHeaderList(worker).get(uid);

		switch (variant) {
			case MarkMessageCommand.MARK_AS_READ :
				{
					if (h.get("columba.flags.recent").equals(Boolean.TRUE))
						getMessageFolderInfo().decRecent();
						
					if (h.get("columba.flags.seen").equals(Boolean.FALSE))
						getMessageFolderInfo().decUnseen();
						
					h.set("columba.flags.seen", Boolean.TRUE);
					break;
				}
			case MarkMessageCommand.MARK_AS_FLAGGED :
				{
					h.set("columba.flags.flagged", Boolean.TRUE);
					break;
				}
			case MarkMessageCommand.MARK_AS_EXPUNGED :
				{
					h.set("columba.flags.expunged", Boolean.TRUE);
					break;
				}
			case MarkMessageCommand.MARK_AS_ANSWERED :
				{
					h.set("columba.flags.answered", Boolean.TRUE);
					break;
				}
		}
	}

	public void markMessage(
		Object[] uids,
		int variant,
		WorkerStatusController worker)
		throws Exception {

		for (int i = 0; i < uids.length; i++) {
			markMessage(uids[i], variant, worker);
		}
	}
	
	public AbstractMessage getMessage(
		Object uid,
		WorkerStatusController worker)
		throws Exception {
		if (aktMessage != null) {
			if (aktMessage.getUID().equals(uid)) {
				// this message is already cached
				ColumbaLogger.log.info("using already cached message..");

				return aktMessage;
			}
		}

		String source = getMessageSource(uid, worker);

		AbstractMessage message =
			new Rfc822Parser().parse(source, true, null, 0);
		message.setUID(uid);
		
		SendableHeader header = (SendableHeader) getHeaderList(worker).get(uid);
		SendableMessage sendableMessage = new SendableMessage();
		sendableMessage.setHeader( header );
		sendableMessage.setMimePartTree( message.getMimePartTree() );
		sendableMessage.setSource( source );

		aktMessage = sendableMessage;

		return sendableMessage;
	}

	public HeaderList getHeaderList(WorkerStatusController worker)
		throws Exception {
		return cache.getHeaderList(worker);
	}

	public String getDefaultChild() {
		return "MHFolder";
	}

	/*
	public void expungeFolder(WorkerStatusController worker) throws Exception {
		Object[] uids = getUids(worker);

		for (int i = 0; i < uids.length; i++) {
			Object uid = uids[i];

			ColumbaHeader h = getMessageHeader(uid, worker);
			Boolean expunged = (Boolean) h.get("columba.flags.expunged");

			ColumbaLogger.log.debug("expunged=" + expunged);

			if (expunged.equals(Boolean.TRUE)) {
				// move message to trash

				ColumbaLogger.log.info(
					"moving message with UID " + uid + " to trash");

				// remove message
				removeMessage(uid);

			}
		}
	}
	*/
	
	public void removeMessage(Object uid, WorkerStatusController worker) throws Exception {
		cache.remove(uid);
		super.removeMessage(uid, worker);
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
						.getNextUid(),
					null));
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
		cache.save(null);
	}

	class OutboxHeaderCache extends LocalHeaderCache {
		public OutboxHeaderCache(LocalFolder folder) {
			super(folder);
		}
		
		public HeaderInterface createHeaderInstance()
		{
			return new SendableHeader();
		}

		protected void loadHeader(ObjectInputStream p, HeaderInterface h)
			throws Exception {
			super.loadHeader(p, h);

			int accountUid = p.readInt();
			((SendableHeader) h).setAccountUid(accountUid);

			Vector recipients = (Vector) p.readObject();
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