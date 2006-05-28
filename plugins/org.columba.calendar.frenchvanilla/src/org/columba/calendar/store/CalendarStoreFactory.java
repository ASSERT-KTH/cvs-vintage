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
import java.util.Iterator;

import org.columba.calendar.config.Config;
import org.columba.calendar.model.api.IComponent;
import org.columba.calendar.model.api.IComponentInfo;
import org.columba.calendar.model.api.IComponentInfoList;
import org.columba.calendar.model.api.IEventInfo;
import org.columba.calendar.store.api.ICalendarStore;
import org.columba.calendar.store.api.ICalendarStoreFactory;
import org.columba.calendar.store.api.StoreException;
import org.columba.calendar.ui.base.CalendarHelper;
import org.columba.core.io.DiskIO;

import com.miginfocom.calendar.activity.Activity;
import com.miginfocom.calendar.activity.ActivityDepository;

public class CalendarStoreFactory implements ICalendarStoreFactory {

	private File parentDirectory;

	private File storeDirectory;

	private ICalendarStore store;

	private static CalendarStoreFactory instance = new CalendarStoreFactory();

	private CalendarStoreFactory() {
		super();

		parentDirectory = Config.getInstance().getConfigDirectory();

		storeDirectory = new File(parentDirectory, "store");
		DiskIO.ensureDirectory(storeDirectory);

		initLocalStore();
	}

	private void initLocalStore() {
		store = new LocalCalendarStore(storeDirectory);

		IComponentInfoList list = store.getComponentInfoList();
		Iterator<IComponentInfo> it = list.iterator();
		while (it.hasNext()) {
			IComponentInfo item = (IComponentInfo) it.next();

			if (item.getType() == IComponent.TYPE.EVENT) {
				IEventInfo event = (IEventInfo) item;

				Activity act = CalendarHelper.createActivity(event);

				ActivityDepository.getInstance().addBrokedActivity(act, this);

			}
		}
	}

	public static CalendarStoreFactory getInstance() {
		return instance;
	}

	public ICalendarStore getLocaleStore() {
		return store;
	}

}
