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

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Logger;

import org.columba.api.command.IStatusObservable;
import org.columba.core.logging.Logging;
import org.columba.mail.folder.AbstractLocalFolder;
import org.columba.mail.folder.AbstractMessageFolder;
import org.columba.mail.folder.IDataStorage;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.IColumbaHeader;
import org.columba.mail.message.IHeaderList;
import org.columba.mail.util.MailResourceLoader;
import org.columba.ristretto.io.Source;
import org.columba.ristretto.message.Flags;
import org.columba.ristretto.message.MailboxInfo;
import org.columba.ristretto.parser.HeaderParser;

/**
 * Implementation of a local headercache facility, which is also able to resync
 * itself with the {@IDataStorage}.
 * 
 * @author fdietz
 */
public class LocalHeaderCache extends AbstractHeaderCache {

	protected AbstractMessageFolder folder;

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.mail.folder.headercache");


	public LocalHeaderCache(AbstractMessageFolder folder) {
		super(new File(folder.getDirectoryFile(), ".header"));

		this.folder = folder;
	}

	public IStatusObservable getObservable() {
		return folder.getObservable();
	}
	

	/**
	 * @param worker
	 * @throws Exception
	 */
	public void load(IHeaderList headerList) throws Exception {
		LOG.fine("loading header-cache=" + headerFile);

		try {
			reader = new ObjectReader(headerFile);

			int capacity = ((Integer) reader.readObject()).intValue();
			LOG.fine("capacity=" + capacity);

			//System.out.println("Number of Messages : " + capacity);
			if (getObservable() != null) {
				getObservable().setMessage(
						folder.getName()
								+ ": "
								+ MailResourceLoader.getString("statusbar",
										"message", "load_headers"));
				getObservable().setMax(capacity);
				getObservable().resetCurrent(); // setCurrent(0)
			}

			int nextUid = -1;

			// exists/unread/recent should be set to 0
			folder.setMessageFolderInfo(new MailboxInfo());

			for (int i = 0; i < capacity; i++) {
				if (getObservable() != null) {
					getObservable().setCurrent(i);
				}

				ColumbaHeader h = createHeaderInstance();

				loadHeader(h);

				headerList.add(h, (Integer) h.get("columba.uid"));

				if (h.getFlags().getRecent()) {
					// no recent messages should exist on startup
					// --> remove recent flag
					h.getFlags().setRecent(false);
					//folder.getMessageFolderInfo().incRecent();
				}

				if (!h.getFlags().getSeen()) {
					folder.getMessageFolderInfo().incUnseen();
				}

				folder.getMessageFolderInfo().incExists();

				int aktUid = ((Integer) h.get("columba.uid")).intValue();

				if (nextUid < aktUid) {
					nextUid = aktUid;
				}
			}

			/*
			 * // Check if the count of the if (needToSync(capacity)) {
			 * Logging.log.fine( "need to recreateHeaderList() because
			 * capacity is not matching");
			 * 
			 * throw new FolderInconsistentException(); }
			 */
			nextUid++;
			LOG.info("next UID for new messages =" + nextUid);
			((AbstractLocalFolder) folder).setNextMessageUid(nextUid);

		} catch (Exception e) {
			LOG.severe("Error loading local header cache!");

			if (Logging.DEBUG) {
				e.printStackTrace();
			}
		} finally {
			reader.close();
		}

	}

	/**
	 * @param worker
	 * @throws Exception
	 */
	public void persistHeaderList(IHeaderList headerList) throws IOException {
		// we didn't load any header to save
		if (!isHeaderCacheLoaded()) {
			return;
		}

		LOG.fine("saving header-cache=" + headerFile);

		// this has to called only if the uid becomes higher than Integer
		// allows
		//cleanUpIndex();
		try {
			writer = new ObjectWriter(headerFile);
		} catch (Exception e) {
			if (Logging.DEBUG) {
				e.printStackTrace();
			}
		}

		// write total number of headers to file
		int count = headerList.count();
		LOG.fine("capacity=" + count);
		writer.writeObject(new Integer(count));

		ColumbaHeader h;

		//Message message;
		for (Enumeration e = headerList.keys(); e.hasMoreElements();) {
			Object uid = e.nextElement();

			h = (ColumbaHeader) headerList.get(uid);

			saveHeader(h);
		}

		writer.close();
	}

	
	protected void loadHeader(ColumbaHeader h) throws IOException {
		h.getAttributes().put("columba.uid", new Integer(reader.readInt()));

		super.loadHeader(h);
	}

	protected void saveHeader(ColumbaHeader h) throws IOException {
		writer.writeInt(((Integer) h.getAttributes().get("columba.uid"))
				.intValue());

		super.saveHeader(h);
	}

}