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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;

import org.columba.core.command.WorkerStatusController;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;
import org.columba.mail.folder.DataStorageInterface;
import org.columba.mail.folder.FolderInconsistentException;
import org.columba.mail.folder.MessageFolderInfo;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderInterface;
import org.columba.mail.message.HeaderList;
import org.columba.mail.parser.Rfc822Parser;

/**
 * @author fdietz
 *
 * 
 */
public abstract class AbstractHeaderCache {

	protected HeaderList headerList;

	protected File headerFile;

	private boolean headerCacheLoaded;

	protected CachedFolder folder;
	

	/**
	 * @param folder
	 */
	public AbstractHeaderCache(CachedFolder folder) {
		this.folder = folder;

		headerFile = new File(folder.getDirectoryFile(), ".header");

		

		headerList = new HeaderList();

		headerCacheLoaded = false;
	}

	

	/**
	 * @return
	 */
	public HeaderInterface createHeaderInstance() {
		return new ColumbaHeader();
	}

	/**
	 * @return
	 */
	public boolean isHeaderCacheLoaded() {
		return headerCacheLoaded;
	}

	/**
	 * @param uid
	 * @return
	 * @throws Exception
	 */
	public boolean exists(Object uid) throws Exception {
		return headerList.contains(uid);
	}

	/**
	 * @return
	 */
	public int count() {
		return headerList.size();
	}

	/**
	 * @param uid
	 * @throws Exception
	 */
	public void remove(Object uid) throws Exception {
		if (MainInterface.DEBUG) {
			ColumbaLogger.log.debug("trying to remove message UID=" + uid);
		}

		if (headerList.containsKey(uid)) {
			if (MainInterface.DEBUG) {
				ColumbaLogger.log.debug("remove UID=" + uid);
			}

			headerList.remove(uid);
		}
	}

	/**
	 * @param header
	 * @throws Exception
	 */
	public void add(HeaderInterface header) throws Exception {
		headerList.add(header, header.get("columba.uid"));
	}

	
	/** Get or (re)create the header cache file.
	 *
	 * @param worker
	 * @return the HeaderList
	 * @throws Exception
	 */
	public HeaderList getHeaderList(WorkerStatusController worker)
		throws Exception {
		boolean needToRelease = false;
		// if there exists a ".header" cache-file
		//  try to load the cache	
		if (!headerCacheLoaded) {
			

			if (headerFile.exists()) {
				try {
					load(worker);
				} catch (Exception e) {
					sync(worker);
				}
			} else {
				sync(worker);
			}
			headerCacheLoaded = true;
		}

		return headerList;
	}

	/**
	 * @param worker
	 * @throws Exception
	 */
	public void load(WorkerStatusController worker) throws Exception {

		if (MainInterface.DEBUG) {
			ColumbaLogger.log.info("loading header-cache=" + headerFile);
		}

		FileInputStream istream = new FileInputStream(headerFile.getPath());
		ObjectInputStream ois =
			new ObjectInputStream(new BufferedInputStream(istream));

		int capacity = ois.readInt();
		if (MainInterface.DEBUG) {
			ColumbaLogger.log.info("capacity=" + capacity);
		}
		boolean needToRelease = false;
		int mcount = folder.getDataStorageInstance().getMessageCount();
		if (capacity != mcount) {
			if (MainInterface.DEBUG) {
				ColumbaLogger.log.info(
					"need to recreateHeaderList() because capacity="
						+ capacity
						+ " and getMessageCount()="
						+ mcount);
			}

			throw new FolderInconsistentException();
		}

		headerList = new HeaderList(capacity);

		//System.out.println("Number of Messages : " + capacity);

		if (worker != null)
			worker.setDisplayText("Loading headers from cache...");

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
		folder.setNextMessageUid(nextUid);
		//worker.setDisplayText(null);
		// close stream
		ois.close();
		worker.setProgressBarValue(capacity);
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

		FileOutputStream istream = new FileOutputStream(headerFile.getPath());
		ObjectOutputStream p = new ObjectOutputStream(istream);

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

			if (h != null)
				saveHeader(p, h);
		}
		//p.flush();
		p.close();
	}

	/**
	 * @param p
	 * @param h
	 * @throws Exception
	 */
	protected abstract void loadHeader(ObjectInputStream p, HeaderInterface h)
		throws Exception;

	/**
	 * @param p
	 * @param h
	 * @throws Exception
	 */
	protected abstract void saveHeader(ObjectOutputStream p, HeaderInterface h)
		throws Exception;

	/**
	 * @param list
	 */
	/**
	 * @param list
	 */
	public void setHeaderList(HeaderList list) {
		headerList = list;
	}

	/**
	 * @param worker
	 * @throws Exception
	 */
	public void sync(WorkerStatusController worker) throws Exception {
		if (worker != null) {
			worker.setDisplayText("Syncing headercache...");
		}
		DataStorageInterface ds = folder.getDataStorageInstance();

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
				if( source.length() == 0) {
					ds.removeMessage(uids[i]);
					continue;
				}

				header = parser.parseHeader(source);

				
				ColumbaHeader h = ColumbaHeader.stripHeaders(header);
				
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

	

}
