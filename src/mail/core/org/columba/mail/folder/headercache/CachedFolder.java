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
package org.columba.mail.folder.headercache;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.columba.core.logging.ColumbaLogger;
import org.columba.core.util.Mutex;
import org.columba.mail.coder.EncodedWordDecoder;
import org.columba.mail.config.FolderItem;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.LocalFolder;
import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.mail.message.AbstractMessage;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderInterface;
import org.columba.mail.message.HeaderList;
import org.columba.mail.parser.Rfc822Parser;

/**
 * @author fdietz
 *
 * <class>CachedFolder</class> adds a header-cache
 * facility to <class>LocalFolder</class> to be 
 * able to quickly show a message summary, etc.
 */
public abstract class CachedFolder extends LocalFolder {

	// header-cache implementation
	protected AbstractHeaderCache headerCache;
	protected Mutex mutex;

	/**
	 * @param item	<class>FolderItem</class> contains xml configuration of this folder
	 */
	public CachedFolder(FolderItem item) {
		super(item);

		mutex = new Mutex(getName());
	}

	/**
	 * @see org.columba.mail.folder.Folder#addMessage(org.columba.mail.message.AbstractMessage, org.columba.core.command.WorkerStatusController)
	 */
	public Object addMessage(
		AbstractMessage message)
		throws Exception {

		if ( message == null ) return null;
		
		// get headerlist before adding a message
		getHeaderList();

		// call addMessage of superclass LocalFolder
		// to do the dirty work
		Object newUid = super.addMessage(message);
		if ( newUid == null ) return null;
		
		// this message was already parsed and so we
		// re-use the header to save us some cpu time
		ColumbaHeader h =
			(ColumbaHeader) ((ColumbaHeader) message.getHeader()).clone();

		// decode all headerfields:

		// init encoded word decoder
		EncodedWordDecoder decoder = new EncodedWordDecoder();

		// get list of used-defined headerfields
		String[] list = CachedHeaderfieldOwner.getCachedHeaderfieldArray();

		//TableItem v = MailConfig.getMainFrameOptionsConfig().getTableItem();
		String column;
		for (int j = 0; j < list.length; j++) {

			column = (String) list[j];

			Object item = h.get(column);

			// only decode strings
			if (item instanceof String) {
				String str = (String) item;
				h.set(column, decoder.decode(str));
			}
		}

		// remove all unnecessary headerfields which doesn't
		// need to be cached
		// -> saves much memory
		ColumbaHeader strippedHeader = CachedHeaderfieldOwner.stripHeaders(h);

		// free memory
		h = null;

		// set UID for new message
		strippedHeader.set("columba.uid", newUid);

		// increment recent count of messages if appropriate
		if (strippedHeader.get("columba.flags.recent").equals(Boolean.TRUE))
			getMessageFolderInfo().incRecent();

		// increment unseen count of messages if appropriate
		if (strippedHeader.get("columba.flags.seen").equals(Boolean.FALSE))
			getMessageFolderInfo().incUnseen();

		// add header to header-cache list
		getHeaderCacheInstance().add(strippedHeader);

		return newUid;
	}

	/**
	 * @see org.columba.mail.folder.Folder#exists(java.lang.Object, org.columba.core.command.WorkerStatusController)
	 */
	public boolean exists(Object uid)
		throws Exception {

		// check if message with UID exists
		return getCachedHeaderList().containsKey(uid);
	}

	/**
	 * @see org.columba.mail.folder.Folder#expungeFolder(org.columba.core.command.WorkerStatusController)
	 */
	public void expungeFolder() throws Exception {

		// get list of all uids 
		Object[] uids = getUids();

		for (int i = 0; i < uids.length; i++) {
			Object uid = uids[i];
			
			if ( uid == null ) continue;
			
			// if message with uid doesn't exist -> skip
			if (exists(uid) == false) {
				ColumbaLogger.log.debug("uid " + uid + " doesn't exist");

				continue;
			}

			// retrieve header of messages
			ColumbaHeader h = getMessageHeader(uid);
			Boolean expunged = (Boolean) h.get("columba.flags.expunged");

			if (expunged.equals(Boolean.TRUE)) {
				// move message to trash if marked as expunged

				ColumbaLogger.log.debug("removing uid=" + uid);

				// remove message
				removeMessage(uid);

			}
		}

		// folder was modified
		changed = true;
	}

	/**
	 * 
	 * Return headerlist from cache
	 * 
	 * This method is just another layer for getHeaderList() which
	 * adds a mutex to it.
	 * 
	 * We lock folders to be sure that only one <code>Command</code>
	 * at a time can modify the folder.
	 * 
	 * But we also allow to add messages at any time, because that 
	 * doesn't interfere or causes problems ;-)
	 * 
	 * Adding the headercache here, makes it necessary to load
	 * the headercache, for the first time before we do any 
	 * operation.
	 * 
	 * This is a speciality of the headercache implementation which
	 * has nothing to do with our Folder locking system and is
	 * put here for this reason.
	 * 
	 * 
	 * @return				<class>HeaderList</class>
	 * @throws Exception	<class>Exception</class>
	 */
	protected HeaderList getCachedHeaderList()
		throws Exception {
		HeaderList result;

		try {
			mutex.getMutex();
			result = getHeaderCacheInstance().getHeaderList();
		} finally {
			mutex.releaseMutex();
		}

		return result;
	}

	/**
	 * @see org.columba.mail.folder.Folder#getHeaderList(org.columba.core.command.WorkerStatusController)
	 */
	public HeaderList getHeaderList()
		throws Exception {
		return getCachedHeaderList();
	}

	/**
	 * @see org.columba.mail.folder.LocalFolder#getMessage(java.lang.Object, org.columba.core.command.WorkerStatusController)
	 */
	public AbstractMessage getMessage(
		Object uid)
		throws Exception {

		// check if message was already parsed before
		if (aktMessage != null) {
			if (aktMessage.getUID().equals(uid)) {
				// this message is already cached
				// -> no need to parse it again

				//return (AbstractMessage) aktMessage.clone();
				return (AbstractMessage) aktMessage;
			}
		}

		// get source of message as string
		String source = getMessageSource(uid);

		// get header from cache
		ColumbaHeader header =
			(ColumbaHeader) getCachedHeaderList().get(uid);

		// generate message object from source
		AbstractMessage message = new Rfc822Parser().parse(source, header);

		// set message uid
		message.setUID(uid);

		// set message source
		message.setSource(source);
		if (source == null) {
			source = new String();
		}

		// this is the new cached message
		// which should be re-used if possible
		aktMessage = message;

		// there's no need to clone() here
		
		//return (AbstractMessage) message.clone();
		return (AbstractMessage) message;
	}

	/**
	 * @see org.columba.mail.folder.Folder#getMessageHeader(java.lang.Object, org.columba.core.command.WorkerStatusController)
	 */
	public ColumbaHeader getMessageHeader(
		Object uid)
		throws Exception {

		if ((aktMessage != null) && (aktMessage.getUID().equals(uid))) {
			// message is already cached

			// try to compare the headerfield count of
			// the actually parsed message with the cached
			// headerfield count
			AbstractMessage message = getMessage(uid);

			// number of headerfields
			int size = message.getHeader().count();

			// get header from cache
			HeaderInterface h =
				(ColumbaHeader) getCachedHeaderList().get(uid);

			// message doesn't exist (this shouldn't happen here)
			if (h == null)
				return null;

			// number of headerfields
			int cachedSize = h.count();

			// if header contains from fields than the cached header
			if (size > cachedSize)
				return (ColumbaHeader) message.getHeader();

			return (ColumbaHeader) h;
		} else
			// message isn't cached
			// -> just return header from cache
			return (ColumbaHeader) getCachedHeaderList().get(uid);
	}

	/**
	 * @see org.columba.mail.folder.Folder#getUids(org.columba.core.command.WorkerStatusController)
	 */
	public Object[] getUids() throws Exception {

		int count = getCachedHeaderList().count();
		//Object[] uids = new Object[count];
		List list = new Vector(count);
		int i = 0;
		for (Enumeration e = getCachedHeaderList().keys();
			e.hasMoreElements();
			) {
			//uids[i++] = e.nextElement();
			list.add( e.nextElement() );
		}

		Object[] uids = new Object[list.size()];
		((Vector)list).copyInto(uids);
		
		return uids;
	}

	/**
	 * @see org.columba.mail.folder.Folder#innerCopy(org.columba.mail.folder.Folder, java.lang.Object[], org.columba.core.command.WorkerStatusController)
	 */
	public void innerCopy(
		Folder destFolder,
		Object[] uids)
		throws Exception {
		for (int i = 0; i < uids.length; i++) {

			Object uid = uids[i];

			if (exists(uid)) {
				AbstractMessage message = getMessage(uid);
				if ( message != null )
					destFolder.addMessage(message);
			}

			if ( getObservable() != null ) 
				getObservable().setCurrent(i);
			//worker.setProgressBarValue(i);
		}
	}

	/**
	 * @param uid
	 * @param variant
	 * @param worker
	 * @throws Exception
	 */
	protected void markMessage(
		Object uid,
		int variant)
		throws Exception {
		ColumbaHeader h = (ColumbaHeader) getCachedHeaderList().get(uid);

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
					if (h.get("columba.flags.seen").equals(Boolean.FALSE))
						getMessageFolderInfo().decUnseen();

					h.set("columba.flags.seen", Boolean.TRUE);
					h.set("columba.flags.recent", Boolean.FALSE);
					
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

		changed = true;
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.Folder#markMessage(java.lang.Object[], int, org.columba.core.command.WorkerStatusController)
	 */
	public void markMessage(
		Object[] uids,
		int variant)
		throws Exception {

		for (int i = 0; i < uids.length; i++) {
			if (exists(uids[i])) {
				markMessage(uids[i], variant);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.Folder#removeMessage(java.lang.Object, org.columba.core.command.WorkerStatusController)
	 */
	public void removeMessage(Object uid)
		throws Exception {
		ColumbaHeader header = (ColumbaHeader) getMessageHeader(uid);
		if ( header == null ) return;
		
		if (header.get("columba.flags.seen").equals(Boolean.FALSE))
			getMessageFolderInfo().decUnseen();
		if (header.get("columba.flags.recent").equals(Boolean.TRUE))
			getMessageFolderInfo().decRecent();

		getHeaderCacheInstance().remove(uid);
		super.removeMessage(uid);
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.Folder#save(org.columba.core.command.WorkerStatusController)
	 */
	public void save() throws Exception {
		// only save header-cache if folder data changed
		if (hasChanged() == true) {

			getHeaderCacheInstance().save();
			setChanged(false);
		}

		// call Folder.save() to be sure that messagefolderinfo is saved
		super.save();
	}

	/**
	 * @return
	 */
	public AbstractHeaderCache getHeaderCacheInstance() {
		if (headerCache == null) {
			headerCache = new LocalHeaderCache(this);
		}
		return headerCache;
	}

}
