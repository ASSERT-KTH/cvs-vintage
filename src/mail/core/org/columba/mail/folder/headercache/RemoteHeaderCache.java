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
import java.util.Enumeration;
import java.util.logging.Logger;

import org.columba.core.command.StatusObservable;
import org.columba.core.main.Main;
import org.columba.mail.folder.AbstractMessageFolder;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderList;
import org.columba.mail.util.MailResourceLoader;

/**
 * IMAP-specific implementation of a header cache.
 * 
 * @author fdietz
 */
public class RemoteHeaderCache extends AbstractHeaderCache {

	protected AbstractMessageFolder folder;

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.mail.folder.headercache");

	private boolean configurationChanged;

	/**
	 * Constructor for RemoteHeaderCache.
	 * 
	 * @param folder
	 */
	public RemoteHeaderCache(AbstractMessageFolder folder) {
		super(new File(folder.getDirectoryFile(), ".header"));

		this.folder = folder;

		configurationChanged = false;
	}

	public StatusObservable getObservable() {
		return folder.getObservable();
	}

	public void load() throws Exception {
		LOG.fine("loading header-cache=" + headerFile);
		headerList = new HeaderList();

		try {
			reader = new ObjectReader(headerFile);
		} catch (Exception e) {
			if (Main.DEBUG) {
				e.printStackTrace();
			}
		}

		int capacity = ((Integer) reader.readObject()).intValue();

		LOG.fine("capacity=" + capacity);

		int additionalHeaderfieldsCount = ((Integer) reader.readObject())
				.intValue();

		if (additionalHeaderfieldsCount != 0) {
			// user-defined headerfields found
			// -> read all keys from file
			for (int i = 0; i < additionalHeaderfieldsCount; i++) {
				additionalHeaderfields.add((String) reader.readObject());
			}
		}

		if (getObservable() != null) {
			getObservable().setMessage(
					MailResourceLoader.getString("statusbar", "message",
							"load_headers"));
			getObservable().setMax(capacity);
			getObservable().resetCurrent();
		}

		for (int i = 1; i <= capacity; i++) {
			if (getObservable() != null) {
				getObservable().setCurrent(i);
			}

			ColumbaHeader h = new ColumbaHeader();

			loadHeader(h);

			headerList.add(h, (Integer) h.get("columba.uid"));
		}

		// close stream
		reader.close();

		if (configurationChanged) {
			// headerfield cache configuration changed
			// -> try to properly fill the cache again
			//reorganizeCache();
		}

		// we are done
		if (getObservable() != null) {
			getObservable().clearMessageWithDelay();
			getObservable().resetCurrent();
		}
	}

	public void save() throws Exception {
		// we didn't load any header to save
		if (!isHeaderCacheLoaded()) {
			return;
		}

		LOG.fine("saving header-cache=" + headerFile);

		try {
			writer = new ObjectWriter(headerFile);
		} catch (Exception e) {
			if (Main.DEBUG) {
				e.printStackTrace();
			}
		}

		int count = headerList.count();

		if (count == 0) {
			return;
		}

		writer.writeObject(new Integer(count));

		ColumbaHeader h;

		for (Enumeration e = headerList.keys(); e.hasMoreElements();) {
			Object uid = (Integer) e.nextElement();

			h = (ColumbaHeader) headerList.get(uid);

			saveHeader(h);
		}

		writer.close();
	}

	protected void loadHeader(ColumbaHeader h) throws Exception {
		h.set("columba.uid", reader.readObject());

		super.loadHeader(h);
	}

	protected void saveHeader(ColumbaHeader h) throws Exception {
		writer.writeObject(h.get("columba.uid"));

		super.saveHeader(h);
	}

}