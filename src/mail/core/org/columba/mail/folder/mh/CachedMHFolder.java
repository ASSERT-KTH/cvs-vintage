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
package org.columba.mail.folder.mh;

import java.util.Enumeration;

import org.columba.core.command.WorkerStatusController;
import org.columba.core.config.HeaderItem;
import org.columba.core.config.TableItem;
import org.columba.mail.coder.EncodedWordDecoder;
import org.columba.mail.config.FolderItem;
import org.columba.mail.config.MailConfig;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.mail.folder.headercache.LocalHeaderCache;
import org.columba.mail.message.AbstractMessage;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderInterface;
import org.columba.mail.message.HeaderList;
import org.columba.mail.parser.Rfc822Parser;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class CachedMHFolder extends MHFolder {

	protected LocalHeaderCache cache;

	public CachedMHFolder(FolderItem item) {
		super(item);

		cache = new LocalHeaderCache(this);
	}

	public HeaderList getHeaderList(WorkerStatusController worker)
		throws Exception {
		return cache.getHeaderList(worker);
	}

	public void save() throws Exception {
		cache.save(null);
	}

	public boolean exists(Object uid, WorkerStatusController worker)
		throws Exception {
		return cache.getHeaderList(worker).containsKey(uid);
	}

	public ColumbaHeader getMessageHeader(
		Object uid,
		WorkerStatusController worker)
		throws Exception {

		if ((aktMessage != null) && (aktMessage.getUID().equals(uid))) {
			// message is already cached

			// try to compare the headerfield count of
			// the actually parsed message with the cached
			// headerfield count
			AbstractMessage message = getMessage(uid, worker);
			int size = message.getHeader().count();

			HeaderInterface h =
				(ColumbaHeader) cache.getHeaderList(worker).get(uid);
			if (h == null)
				return null;

			int cachedSize = h.count();

			if (size > cachedSize)
				return (ColumbaHeader) message.getHeader();

			return (ColumbaHeader) h;
		} else
			return (ColumbaHeader) cache.getHeaderList(worker).get(uid);
	}

	public AbstractMessage getMessage(
		Object uid,
		WorkerStatusController worker)
		throws Exception {
		if (aktMessage != null) {
			if (aktMessage.getUID().equals(uid)) {
				// this message is already cached
				//ColumbaLogger.log.info("using already cached message..");

				return aktMessage;
			}
		}

		String source = getMessageSource(uid, worker);
		ColumbaHeader header =
			(ColumbaHeader) cache.getHeaderList(worker).get(uid);

		AbstractMessage message =
			new Rfc822Parser().parse(source, true, header, 0);
		message.setUID(uid);
		message.setSource(source);

		aktMessage = message;

		return message;
	}

	public Object addMessage(
		AbstractMessage message,
		WorkerStatusController worker)
		throws Exception {

		getHeaderList(worker);

		Object newUid = super.addMessage(message, worker);

		ColumbaHeader h =
			(ColumbaHeader) ((ColumbaHeader) message.getHeader()).clone();

		EncodedWordDecoder decoder = new EncodedWordDecoder();
		TableItem v = MailConfig.getMainFrameOptionsConfig().getTableItem();
		String column;
		for (int j = 0; j < v.count(); j++) {
			HeaderItem headerItem = v.getHeaderItem(j);
			column = (String) headerItem.get("name");

			Object item = h.get(column);

			if (item instanceof String) {
				String str = (String) item;
				h.set(column, decoder.decode(str));
			}
		}

		h.set("columba.uid", newUid);

		if (h.get("columba.flags.recent").equals(Boolean.TRUE))
			getMessageFolderInfo().incRecent();
		if (h.get("columba.flags.seen").equals(Boolean.FALSE))
			getMessageFolderInfo().incUnseen();

		cache.add(h);

		return newUid;
	}

	public void expungeFolder(WorkerStatusController worker)
		throws Exception {

		Object[] uids = getUids(worker);
		
		for (int i = 0; i < uids.length; i++) {
			Object uid = uids[i];

			if (exists(uid, worker) == false)
				continue;

			ColumbaHeader h = getMessageHeader(uid, worker);
			Boolean expunged = (Boolean) h.get("columba.flags.expunged");

			//ColumbaLogger.log.debug("expunged=" + expunged);

			if (expunged.equals(Boolean.TRUE)) {
				// move message to trash

				//ColumbaLogger.log.info("moving message with UID " + uid + " to trash");

				// remove message
				removeMessage(uid, worker);

			}
		}
	}

	public void removeMessage(Object uid, WorkerStatusController worker)
		throws Exception {
		ColumbaHeader header = (ColumbaHeader) getMessageHeader(uid, worker);

		if (header.get("columba.flags.seen").equals(Boolean.FALSE))
			getMessageFolderInfo().decUnseen();
		if (header.get("columba.flags.recent").equals(Boolean.TRUE))
			getMessageFolderInfo().decRecent();

		cache.remove(uid);
		super.removeMessage(uid, worker);

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
					h.set("columba.flags.recent", Boolean.FALSE);
					break;
				}
			case MarkMessageCommand.MARK_AS_UNREAD :
				{
					h.set("columba.flags.seen", Boolean.FALSE);
					getMessageFolderInfo().incUnseen();
					break;
				}
			case MarkMessageCommand.MARK_AS_FLAGGED :
				{
					h.set("columba.flags.flagged", Boolean.TRUE);
					break;
				}
			case MarkMessageCommand.MARK_AS_UNFLAGGED :
				{
					h.set("columba.flags.flagged", Boolean.FALSE);
					break;
				}
			case MarkMessageCommand.MARK_AS_EXPUNGED :
				{

					h.set("columba.flags.expunged", Boolean.TRUE);
					break;
				}
			case MarkMessageCommand.MARK_AS_UNEXPUNGED :
				{

					h.set("columba.flags.expunged", Boolean.FALSE);
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
			if (exists(uids[i], worker)) {
				markMessage(uids[i], variant, worker);
			}
		}
	}

	public String getDefaultChild() {
		return "MHFolder";
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

	public void innerCopy(
		Folder destFolder,
		Object[] uids,
		WorkerStatusController worker)
		throws Exception {
		for (int i = 0; i < uids.length; i++) {

			Object uid = uids[i];

			if (exists(uid, worker)) {
				AbstractMessage message = getMessage(uid, worker);

				destFolder.addMessage(message, worker);
			}

			worker.setProgressBarValue(i);
		}
	}

}
