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

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.folder.DataStorageInterface;
import org.columba.mail.folder.FolderInconsistentException;
import org.columba.mail.folder.LocalFolder;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderList;
import org.columba.ristretto.message.Flags;
import org.columba.ristretto.message.HeaderInterface;
import org.columba.ristretto.message.MessageFolderInfo;
import org.columba.ristretto.message.io.CharSequenceSource;
import org.columba.ristretto.parser.HeaderParser;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class LocalHeaderCache extends AbstractFolderHeaderCache {
	
	private final int WEEK = 1000 * 60 * 60 * 24 * 7; 

	public LocalHeaderCache(CachedFolder folder) {
		super(folder);

	}

	public HeaderList getHeaderList() throws Exception {
		boolean needToRelease = false;
		// if there exists a ".header" cache-file
		//  try to load the cache	
		if (!isHeaderCacheLoaded()) {

			if (headerFile.exists()) {
				try {
					load();
				} catch (Exception e) {
					sync();
				}
			} else {
				sync();
			}
			setHeaderCacheLoaded(true);
		}

		return headerList;
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.headercache.AbstractHeaderCache#needToSync(int)
	 */
	public boolean needToSync(int capacity) {
		int mcount =
			((LocalFolder) folder).getDataStorageInstance().getMessageCount();
		if (capacity != mcount)
			return true;

		return false;
	}

	/**
		 * @param worker
		 * @throws Exception
		 */
	public void load() throws Exception {

		ColumbaLogger.log.info("loading header-cache=" + headerFile);

		ObjectInputStream ois = openInputStream();

		int capacity = ois.readInt();
		ColumbaLogger.log.info("capacity=" + capacity);
		boolean needToRelease = false;

		if (needToSync(capacity)) {
			ColumbaLogger.log.info(
				"need to recreateHeaderList() because capacity is not matching");

			throw new FolderInconsistentException();
		}

		headerList = new HeaderList(capacity);

		//System.out.println("Number of Messages : " + capacity);

		if (getObservable() != null)
			getObservable().setMessage(
				folder.getName() + ": Loading headers from cache...");

		if (getObservable() != null)
			getObservable().setMax(capacity);

		getObservable().setCurrent(0);

		int nextUid = -1;

		// exists/unread/recent should be set to 0
		folder.setMessageFolderInfo(new MessageFolderInfo());

		for (int i = 0; i < capacity; i++) {

			if ((getObservable() != null) && (i % 100 == 0))
				getObservable().setCurrent(i);

			//ColumbaHeader h = message.getHeader();
			HeaderInterface h = createHeaderInstance();

			/*
			// read current number of message
			ois.readInt();
			*/

			loadHeader(ois, h);

			//System.out.println("message=" + h.get("subject"));

			headerList.add(h, (Integer) h.get("columba.uid"));

			if (h.get("columba.flags.recent").equals(Boolean.TRUE))
				folder.getMessageFolderInfo().incRecent();
			if (h.get("columba.flags.seen").equals(Boolean.FALSE))
				folder.getMessageFolderInfo().incUnseen();
			folder.getMessageFolderInfo().incExists();

			int aktUid = ((Integer) h.get("columba.uid")).intValue();
			if (nextUid < aktUid)
				nextUid = aktUid;

		}

		nextUid++;
		ColumbaLogger.log.debug("next UID for new messages =" + nextUid);
		((LocalFolder) folder).setNextMessageUid(nextUid);
		//worker.setDisplayText(null);

		getObservable().setCurrent(capacity);

		closeInputStream();

	}

	/**
	 * @param worker
	 * @throws Exception
	 */
	public void save() throws Exception {

		// we didn't load any header to save
		if (!isHeaderCacheLoaded())
			return;

		ColumbaLogger.log.info("saving header-cache=" + headerFile);
		// this has to called only if the uid becomes higher than Integer allows
		//cleanUpIndex();

		//System.out.println("saving headerfile: "+ headerFile.toString() );

		ObjectOutputStream p = openOutputStream();

		//int count = getMessageFileCount();
		int count = headerList.count();
		ColumbaLogger.log.info("capacity=" + count);
		p.writeInt(count);

		ColumbaHeader h;
		//Message message;

		for (Enumeration e = headerList.keys(); e.hasMoreElements();) {
			Object uid = e.nextElement();

			h = (ColumbaHeader) headerList.getHeader(uid);

			saveHeader(p, h);
		}

		closeOutputStream();
	}

	/**
		 * @param worker
		 * @throws Exception
		 */
	public void sync() throws Exception {
		if (getObservable() != null) {
			getObservable().setMessage(
				folder.getName() + ": Syncing headercache...");
		}
		DataStorageInterface ds =
			((LocalFolder) folder).getDataStorageInstance();

		Object[] uids = ds.getMessageUids();

		headerList = new HeaderList(uids.length);
		
		Date today = Calendar.getInstance().getTime();

		// parse all message files to recreate the header cache

		ColumbaHeader header;
		MessageFolderInfo messageFolderInfo = folder.getMessageFolderInfo();
		messageFolderInfo.setExists(0);
		messageFolderInfo.setRecent(0);
		messageFolderInfo.setUnseen(0);

		folder.setChanged(true);

		if (getObservable() != null)
			getObservable().setMax(uids.length);

		for (int i = 0; i < uids.length; i++) {
			try {
				String source = ds.loadMessage(uids[i]);
				if (source.length() == 0) {
					ds.removeMessage(uids[i]);
					continue;
				}

				header = new ColumbaHeader( HeaderParser.parse(new CharSequenceSource(source)) );
				ColumbaHeader h = CachedHeaderfieldOwner.stripHeaders(header);

				if( isOlderThanOneWeek(today, ((Date)header.getAttributes().get("columba.date"))) ) {
					header.getFlags().set(Flags.SEEN);
				}

				int size = source.length() >> 10; // Size in KB
				h.set("columba.size", new Integer(size));

				h.set("columba.uid", uids[i]);

				if (h.get("columba.flags.recent").equals(Boolean.TRUE))
					messageFolderInfo.incRecent();
				if (h.get("columba.flags.seen").equals(Boolean.FALSE))
					messageFolderInfo.incUnseen();

				messageFolderInfo.incExists();

				headerList.add(h, uids[i]);

				header = null;
				source = null;

				if (getObservable() != null && i % 100 == 0) {
					getObservable().setCurrent(i);
				}

			} catch (Exception ex) {
				ex.printStackTrace();
				ColumbaLogger.log.error(
					"Error syncing HeaderCache :" + ex.getLocalizedMessage());
			}

		}
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.headercache.AbstractHeaderCache#loadHeader(java.io.ObjectInputStream, org.columba.mail.message.HeaderInterface)
	 */
	protected void loadHeader(ObjectInputStream p, HeaderInterface h)
		throws Exception {
		Integer uid = new Integer(p.readInt());
		h.set("columba.uid", uid);

		super.loadHeader(p, h);
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.headercache.AbstractHeaderCache#saveHeader(java.io.ObjectOutputStream, org.columba.mail.message.HeaderInterface)
	 */
	protected void saveHeader(ObjectOutputStream p, HeaderInterface h)
		throws Exception {

		p.writeInt(((Integer) h.get("columba.uid")).intValue());

		super.saveHeader(p, h);
	}

	public boolean isOlderThanOneWeek(Date arg0, Date arg1) {
		return arg0.getTime() - WEEK > arg1.getTime();
	}


}
