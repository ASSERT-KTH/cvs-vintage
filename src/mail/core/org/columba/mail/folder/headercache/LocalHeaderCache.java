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
import java.util.Enumeration;

import org.columba.core.command.WorkerStatusController;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;
import org.columba.mail.folder.DataStorageInterface;
import org.columba.mail.folder.FolderInconsistentException;
import org.columba.mail.folder.LocalFolder;
import org.columba.mail.folder.MessageFolderInfo;
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
public class LocalHeaderCache extends AbstractFolderHeaderCache {

	public LocalHeaderCache(CachedFolder folder) {
		super(folder);

	}

	public HeaderList getHeaderList(WorkerStatusController worker)
		throws Exception {
		boolean needToRelease = false;
		// if there exists a ".header" cache-file
		//  try to load the cache	
		if (!isHeaderCacheLoaded()) {

			if (headerFile.exists()) {
				try {
					load(worker);
				} catch (Exception e) {
					sync(worker);
				}
			} else {
				sync(worker);
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
	public void load(WorkerStatusController worker) throws Exception {

		if (MainInterface.DEBUG) {
			ColumbaLogger.log.info("loading header-cache=" + headerFile);
		}

		ObjectInputStream ois = openInputStream();

		int capacity = ois.readInt();
		if (MainInterface.DEBUG) {
			ColumbaLogger.log.info("capacity=" + capacity);
		}
		boolean needToRelease = false;

		if (needToSync(capacity)) {
			if (MainInterface.DEBUG) {
				ColumbaLogger.log.info(
					"need to recreateHeaderList() because capacity is not matching");
			}

			throw new FolderInconsistentException();
		}

		headerList = new HeaderList(capacity);

		//System.out.println("Number of Messages : " + capacity);

		if (worker != null)
			worker.setDisplayText(folder.getName()+": Loading headers from cache...");

		if (worker != null)
			worker.setProgressBarMaximum(capacity);

		worker.setProgressBarValue(0);

		int nextUid = -1;

		// exists/unread/recent should be set to 0
		folder.setMessageFolderInfo(new MessageFolderInfo());

		for (int i = 0; i < capacity; i++) {

			if ((worker != null) && (i % 100 == 0))
				worker.setProgressBarValue(i);

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
		if (MainInterface.DEBUG) {
			ColumbaLogger.log.debug("next UID for new messages =" + nextUid);
		}
		((LocalFolder) folder).setNextMessageUid(nextUid);
		//worker.setDisplayText(null);

		worker.setProgressBarValue(capacity);

		closeInputStream();

	}

	/**
	 * @param worker
	 * @throws Exception
	 */
	public void save(WorkerStatusController worker) throws Exception {

		// we didn't load any header to save
		if (!isHeaderCacheLoaded())
			return;

		if (MainInterface.DEBUG) {
			ColumbaLogger.log.info("saveing header-cache=" + headerFile);
		}
		// this has to called only if the uid becomes higher than Integer allows
		//cleanUpIndex();

		//System.out.println("saving headerfile: "+ headerFile.toString() );

		ObjectOutputStream p = openOutputStream();

		//int count = getMessageFileCount();
		int count = headerList.count();
		if (MainInterface.DEBUG) {
			ColumbaLogger.log.info("capacity=" + count);
		}
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
	public void sync(WorkerStatusController worker) throws Exception {
		if (worker != null) {
			worker.setDisplayText(folder.getName()+": Syncing headercache...");
		}
		DataStorageInterface ds =
			((LocalFolder) folder).getDataStorageInstance();

		Object[] uids = ds.getMessageUids();

		headerList = new HeaderList(uids.length);

		// parse all message files to recreate the header cache

		Rfc822Parser parser = new Rfc822Parser();
		ColumbaHeader header;
		MessageFolderInfo messageFolderInfo = folder.getMessageFolderInfo();

		folder.setChanged(true);

		if (worker != null)
			worker.setProgressBarMaximum(uids.length);

		for (int i = 0; i < uids.length; i++) {
			try {
				String source = ds.loadMessage(uids[i]);
				if (source.length() == 0) {
					ds.removeMessage(uids[i]);
					continue;
				}

				header = parser.parseHeader(source);

				ColumbaHeader h = CachedHeaderfieldOwner.stripHeaders(header);

				parser.addColumbaHeaderFields(h);

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

				if (worker != null && i % 100 == 0) {
					worker.setProgressBarValue(i);
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

}
