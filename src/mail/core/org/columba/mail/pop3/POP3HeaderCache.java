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

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Logger;

import org.columba.api.command.IStatusObservable;
import org.columba.mail.folder.headercache.AbstractHeaderCache;
import org.columba.mail.folder.headercache.CachedHeaderfields;
import org.columba.mail.folder.headercache.ObjectReader;
import org.columba.mail.folder.headercache.ObjectWriter;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.IHeaderList;
import org.columba.mail.util.MailResourceLoader;

/**
 * Header caching facility very similar to the ones used by folders.
 * <p>
 * We need this for the managing server/messages remotely feature, which shows a
 * messagelist of all messages on the POP3 server to the user.
 * 
 * @author freddy
 */
public class POP3HeaderCache extends AbstractHeaderCache {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger.getLogger("org.columba.mail.pop3");

	protected POP3Server server;

	/**
	 * Constructor for POP3HeaderCache.
	 * 
	 * @param folder
	 */
	public POP3HeaderCache(POP3Server server) {
		super(server.getConfigFile());

		this.server = server;
	}

	public IStatusObservable getObservable() {
		return server.getObservable();
	}

	public void load(IHeaderList headerList) throws IOException {
		LOG.severe(server.getAccountItem().getName() + " loading header-cache=" + headerFile);

		try {
			reader = new ObjectReader(headerFile);
		} catch (Exception e) {
			LOG.severe("Could not open pop3 cache: " + e.getMessage());
		}

		Integer c = new Integer(0);
		
		try {
			c = (Integer) reader.readObject();
		} catch (ClassNotFoundException e1) {
		}
		
		if (c == null) {
			LOG.warning("Headercache is empty!");
			// no data in file
			reader.close();
			return;
		}

		int capacity = c.intValue();
		LOG.info("capacity=" + capacity);

		if (getObservable() != null) {
			getObservable().setMessage(
					MailResourceLoader.getString("statusbar", "message",
							"load_headers"));
		}

		if (getObservable() != null) {
			getObservable().setMax(capacity);
		}

		for (int i = 1; i <= capacity; i++) {
			if (getObservable() != null) {
				getObservable().setCurrent(i);
			}

			ColumbaHeader h = new ColumbaHeader();

			try {
				loadHeader(h);
				
				if( h.get("columba.pop3uid") == null ) {
					LOG.severe("No POP3Uid found in header!");
					LOG.severe(h.toString());
				} else {					
					headerList.add(h, h.get("columba.pop3uid"));
				}
			} catch (Exception e) {
				e.printStackTrace();
				LOG.warning("Could not load header " + i + " / " + capacity);
			}
		}

		// close stream
		reader.close();
	}

	public void persistHeaderList(IHeaderList headerList) throws IOException {
		// we didn't load any header to save
		if (!isHeaderCacheLoaded()) {
			return;
		}

		LOG.fine("saving pop3 header-cache=" + headerFile);

		try {
			writer = new ObjectWriter(headerFile);
		} catch (Exception e) {
			LOG.severe("Could not write pop3 cache: " + e.getMessage());
		}

		int count = headerList.count();

		if (count == 0) {
			return;
		}

		writer.writeObject(new Integer(count));

		ColumbaHeader h;

		for (Enumeration e = headerList.keys(); e.hasMoreElements();) {
			String str = (String) e.nextElement();

			h = (ColumbaHeader) headerList.get(str);

			try {
				saveHeader(h);
			} catch (Exception e1) {
				LOG
						.severe("Could not save header to pop3 cache. Header source:\n"
								+ h.toString());
			}
		}

		writer.close();
	}

	protected void loadHeader(ColumbaHeader h) throws IOException {
		String[] columnNames = CachedHeaderfields.POP3_HEADERFIELDS;
		Class[] columnTypes = CachedHeaderfields.POP3_HEADERFIELDS_TYPE;

		for (int j = 0; j < columnNames.length; j++) {
			try {
				Object value = null;

				if (columnTypes[j] == Integer.class) {
					value = new Integer(reader.readInt());
				} else if (columnTypes[j] == Date.class) {
					value = new Date(reader.readLong());
				} else if (columnTypes[j] == String.class) {
					value = reader.readString();
				} else {
					value = reader.readObject();
				}

				if (value != null) {
					h.set(columnNames[j], value);
				}
			} catch (Exception e) {
				LOG.severe("Could not load headerfield " + columnNames[j]);
				e.printStackTrace();
			}
		}
	}

	protected void saveHeader(ColumbaHeader h) throws IOException {
		String[] columnNames = CachedHeaderfields.POP3_HEADERFIELDS;
		Class[] columnTypes = CachedHeaderfields.POP3_HEADERFIELDS_TYPE;
		Object o;

		for (int j = 0; j < columnNames.length; j++) {
			o = h.get(columnNames[j]);

			if (columnTypes[j] == Integer.class) {
				if (o == null) {
					writer.writeInt(0);
				} else {
					writer.writeInt(((Integer) o).intValue());
				}
			} else if (columnTypes[j] == Date.class) {
				if (o == null) {
					writer.writeLong(System.currentTimeMillis());
				} else {
					writer.writeLong(((Date) o).getTime());
				}
			} else if (columnTypes[j] == String.class) {
				if (o == null) {
					writer.writeString("");
				} else {
					writer.writeString((String) o);
				}
			} else {
				writer.writeObject(o);
			}
		}
	}

	/**
	 * @see org.columba.mail.folder.headercache.AbstractHeaderCache#add(org.columba.mail.message.ColumbaHeader)
	 */
	/*
	public void add(ColumbaHeader header) {
		ColumbaHeader strippedHeader = new ColumbaHeader();
		for (int i = 0; i < CachedHeaderfields.POP3_HEADERFIELDS.length; i++) {
			strippedHeader.set(CachedHeaderfields.POP3_HEADERFIELDS[i], header
					.get(CachedHeaderfields.POP3_HEADERFIELDS[i]));
		}

		headerList.add(strippedHeader, strippedHeader.getAttributes().get(
				"columba.pop3uid"));
	}*/
}
