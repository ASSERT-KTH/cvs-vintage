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
package org.columba.calendar.config;

import java.awt.Color;
import java.io.File;

import org.columba.calendar.base.api.ICalendarItem;
import org.columba.calendar.config.api.ICalendarList;
import org.columba.calendar.config.api.IConfig;
import org.columba.core.io.DiskIO;
import org.columba.core.shutdown.ShutdownManager;

public class Config implements IConfig {

	private ICalendarList calendarList;

	private static Config instance;

	private XMLPersistence persistence;

	private File calendarDirectory;

	private Config() throws Exception {

		// get Columba's top-level configuration directory
		File file = org.columba.core.config.Config.getDefaultConfigPath();

		// create top-level configuration directory
		calendarDirectory = new File(file, "calendar");
		DiskIO.ensureDirectory(calendarDirectory);
		persistence = new XMLPersistence(calendarDirectory);

		// load configuration from persistence
		boolean success = persistence.load();
		if ( success == false) {
			createDefaults();
		}

		// make sure configuration is saved when exiting
		ShutdownManager.getInstance().register(new Runnable() {
			public void run() {
				try {
					persistence.save();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	public static IConfig getInstance() {
		if (instance == null) {
			try {
				instance = new Config();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return instance;
	}

	private void createDefaults() {
		calendarList = new CalendarList();

		calendarList.add("private", ICalendarItem.TYPE.LOCAL, "Private", new Color(-19276));
		calendarList.add("work", ICalendarItem.TYPE.LOCAL, "Work", new Color(-4915276));
	}

	public ICalendarList getCalendarList() {
		return calendarList;
	}

	public File getConfigDirectory() {
		return calendarDirectory;
	}

}
