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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Enumeration;

import org.columba.core.command.WorkerStatusController;
import org.columba.core.config.HeaderItem;
import org.columba.core.config.TableItem;
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.config.MailConfig;
import org.columba.mail.folder.RemoteFolder;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderList;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class RemoteHeaderCache {

	protected HeaderList headerList;

	protected File headerFile;

	protected boolean headerCacheAlreadyLoaded;

	protected RemoteFolder folder;

	/**
	 * Constructor for RemoteHeaderCache.
	 * @param folder
	 */
	public RemoteHeaderCache(RemoteFolder folder) {
		this.folder = folder;

		headerList = new HeaderList();

		headerFile = new File(folder.getDirectoryFile(), ".header");
	}

	public boolean isHeaderCacheAlreadyLoaded() {
		return headerCacheAlreadyLoaded;
	}

	public boolean exists(Object uid) throws Exception {
		if (headerList.contains(uid) == true)
			return true;

		return false;
	}


	public HeaderList getHeaderList(WorkerStatusController worker) throws Exception {
		// if there exists a ".header" cache-file
		//  try to load the cache
		if (headerCacheAlreadyLoaded==false) {
			try {

				load(worker);
				headerCacheAlreadyLoaded = true;
			} catch (Exception ex) {
				ex.printStackTrace();
				headerCacheAlreadyLoaded = true;

			}

			if ( headerList == null ) headerList = new HeaderList();
		}



		return headerList;
	}


	protected void loadHeader(ObjectInputStream p, ColumbaHeader h)
		throws Exception {
		String uid = (String) p.readObject();
		h.set("columba.uid", uid);

		h.set("columba.flags.seen", new Boolean(p.readBoolean()));

		h.set("columba.flags.answered", new Boolean(p.readBoolean()));
		h.set("columba.flags.flagged", new Boolean(p.readBoolean()));
		h.set("columba.flags.expunged", new Boolean(p.readBoolean()));
		h.set("columba.flags.draft", new Boolean(p.readBoolean()));
		h.set("columba.flags.recent", new Boolean(p.readBoolean()));

		Date date = (Date) p.readObject();
		h.set("columba.date", date);

		h.set("columba.size", p.readObject());

		String from = (String) p.readObject();
		h.set("columba.from", from);

		Boolean b = (Boolean) p.readObject();
		h.set("columba.attachment", b);

		//int priority = p.readInt();
		h.set("columba.priority", (Integer) p.readObject());

		String host = (String) p.readObject();
		h.set("columba.host", host);

		TableItem v =
			MailConfig.getMainFrameOptionsConfig().getTableItem();
		String column;
		Object o;
		for (int j = 0; j < v.count(); j++) {
			HeaderItem headerItem = v.getHeaderItem(j);
			column = (String) headerItem.get("name");
			o = p.readObject();

			if (o == null) {
			} else if (o instanceof String) {
				String value = (String) o;
				h.set(column, value);
			} else if (o instanceof Integer) {
				Integer value = (Integer) o;
				h.set(column, value);

			} else if (o instanceof Boolean) {
				Boolean value = (Boolean) o;
				h.set(column, value);
			} else if (o instanceof Date) {
				Date value = (Date) o;
				h.set(column, value);
			}

		}

	}

	protected void saveHeader(ObjectOutputStream p, ColumbaHeader h)
		throws Exception {
		p.writeObject(h.get("columba.uid"));

		p.writeBoolean(((Boolean) h.get("columba.flags.seen")).booleanValue());
		p.writeBoolean(((Boolean) h.get("columba.flags.answered")).booleanValue());
		p.writeBoolean(((Boolean) h.get("columba.flags.flagged")).booleanValue());
		p.writeBoolean(((Boolean) h.get("columba.flags.expunged")).booleanValue());
		p.writeBoolean(((Boolean) h.get("columba.flags.draft")).booleanValue());
		p.writeBoolean(((Boolean) h.get("columba.flags.recent")).booleanValue());

		p.writeObject(h.get("columba.date"));

		p.writeObject(h.get("columba.size"));

		p.writeObject(h.get("columba.from"));

		p.writeObject(h.get("columba.attachment"));

		p.writeObject(h.get("columba.priority"));

		p.writeObject(h.get("columba.host"));

		TableItem v =
			MailConfig.getMainFrameOptionsConfig().getTableItem();
		String column;

		Object o;
		for (int j = 0; j < v.count(); j++) {
			HeaderItem headerItem = v.getHeaderItem(j);
			column = (String) headerItem.get("name");
			o = h.get(column);
			p.writeObject(o);
		}
	}

  public void load(WorkerStatusController worker) throws Exception {
    ColumbaLogger.log.info("loading header-cache=" + headerFile);
    headerList = new HeaderList();

    try {
      FileInputStream istream = new FileInputStream(headerFile.getPath());
      ObjectInputStream p = new ObjectInputStream(istream);

      int capacity = p.readInt();
      ColumbaLogger.log.info("capacity=" + capacity);

      /*
		if (capacity != folder.getDataStorageInstance().getMessageCount()) {
        // messagebox headercache-file is corrupted

        headerList = folder.getDataStorageInstance().recreateHeaderList(worker);
        return;
		}
      */

      worker.setDisplayText("Loading headers from cache...");

      Integer uid;

      //System.out.println("Number of Messages : " + capacity);

      if (worker != null)
        worker.setProgressBarMaximum(capacity);

      for (int i = 1; i <= capacity; i++) {
        if (worker != null)
          worker.setProgressBarValue(i);

        //ColumbaHeader h = message.getHeader();
        ColumbaHeader h = new ColumbaHeader();

        // read current number of message
        //p.readInt();

        loadHeader(p, h);

        //System.out.println("message="+h.get("subject") );

        headerList.add( h, (String) h.get("columba.uid") );

        //folder.setNextUid(((Integer) h.get("columba.uid")).intValue());
      }

      // close stream
      p.close();
    } catch (java.io.FileNotFoundException ex){
      // For those times when existing IMAP folders have no local header
      // file.
    }
  }

	public void save(WorkerStatusController worker) throws Exception {
		// we didn't load any header to save
		if (isHeaderCacheAlreadyLoaded() == false)
			return;

		ColumbaLogger.log.info("saveing header-cache=" + headerFile);
		// this has to called only if the uid becomes higher than Integer allows
		//cleanUpIndex();

		//System.out.println("saving headerfile: "+ headerFile.toString() );

		FileOutputStream istream = new FileOutputStream(headerFile.getPath());
		ObjectOutputStream p = new ObjectOutputStream(istream);

		//int count = getMessageFileCount();
		int count = headerList.count();
		if ( count == 0 ) return;

		p.writeInt(count);

		ColumbaHeader h;
		//Message message;

		for (Enumeration e = headerList.keys(); e.hasMoreElements();) {
			String str = (String) e.nextElement();

			h = (ColumbaHeader) headerList.getHeader( str );

			saveHeader(p,h);
		}
		/*
		for (int i = 0; i < count; i++) {
			p.writeInt(i + 1);

			h = headerList.getHeader( new Integer(i).toString() );

			saveHeader(p, h);

		}
		*/

		//p.flush();
		p.close();
	}

}
