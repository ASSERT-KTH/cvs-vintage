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

package org.columba.mail.pop3;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;

import org.columba.core.command.WorkerStatusController;
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.folder.headercache.AbstractHeaderCache;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderList;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class POP3HeaderCache extends AbstractHeaderCache {

	protected POP3Server server;
	/**
	 * Constructor for POP3HeaderCache.
	 * @param folder
	 */
	public POP3HeaderCache(POP3Server server) {
		super(server.getConfigFile());
	}

	public void load(WorkerStatusController worker) throws Exception {
                ColumbaLogger.log.info("loading header-cache=" + headerFile);
		headerList = new HeaderList();

		ObjectInputStream p = openInputStream();

		int capacity = p.readInt();
                ColumbaLogger.log.info("capacity=" + capacity);

		if ( worker != null)
		worker.setDisplayText(MailResourceLoader.getString(
                                "statusbar",
                                "message",
                                "load_headers"));

		if (worker != null)
			worker.setProgressBarMaximum(capacity);

		for (int i = 1; i <= capacity; i++) {
			if (worker != null)
				worker.setProgressBarValue(i);

			ColumbaHeader h = new ColumbaHeader();

			loadHeader(p, h);

			headerList.add(h, h.get("columba.pop3uid"));
			//headerList.add(h, (String) h.get("columba.uid"));
		}

		// close stream
		closeInputStream();
	}

	public void save(WorkerStatusController worker) throws Exception {
		// we didn't load any header to save
		if (!isHeaderCacheLoaded())
			return;

                ColumbaLogger.log.info("saving header-cache=" + headerFile);

		ObjectOutputStream p = openOutputStream();

		int count = headerList.count();
		if (count == 0)
			return;

		p.writeInt(count);

		ColumbaHeader h;

		for (Enumeration e = headerList.keys(); e.hasMoreElements();) {
			String str = (String) e.nextElement();

			h = (ColumbaHeader) headerList.getHeader(str);

			saveHeader(p, h);
		}

		closeOutputStream();
	}

	/**
	 * @see org.columba.modules.mail.folder.AbstractHeaderCache#loadHeader(ObjectInputStream, ColumbaHeader)
	 */
	protected void loadHeader(ObjectInputStream p, ColumbaHeader h)
		throws Exception {
		super.loadHeader(p, h);

		Object uid = p.readObject();
		h.set("columba.pop3uid", uid);
	}

	/**
	 * @see org.columba.modules.mail.folder.AbstractHeaderCache#saveHeader(ObjectOutputStream, ColumbaHeader)
	 */
	protected void saveHeader(ObjectOutputStream p, ColumbaHeader h)
		throws Exception {
		super.saveHeader(p, h);
		p.writeObject(h.get("columba.pop3uid"));
	}
}
