// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.calendar.store;

import java.io.File;

import org.columba.calendar.config.Config;
import org.columba.calendar.store.api.ICalendarStore;
import org.columba.calendar.store.api.ICalendarStoreFactory;
import org.columba.calendar.store.api.StoreException;
import org.columba.core.io.DiskIO;
import org.columba.core.util.InternalException;

public class CalendarStoreFactory implements ICalendarStoreFactory {

	private File parentDirectory;

	private File storeDirectory;

	private ICalendarStore store;

	private static CalendarStoreFactory instance = new CalendarStoreFactory();

	private CalendarStoreFactory() {
		super();

		parentDirectory = Config.getInstance().getCalendarDirectory();

		storeDirectory = new File(parentDirectory, "store");
		DiskIO.ensureDirectory(storeDirectory);
	}

	public static CalendarStoreFactory getInstance() {
		return instance;
	}

	public ICalendarStore getLocaleStore() {

		if (store == null)
			try {
				store = new LocalCalendarStore(storeDirectory);
			} catch (StoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		return store;
	}

}
