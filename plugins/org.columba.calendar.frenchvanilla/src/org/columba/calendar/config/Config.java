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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

import org.columba.core.io.DiskIO;
import org.columba.core.shutdown.ShutdownManager;

/**
 * Main entrypoint for all configuration dating.
 * 
 * @author fdietz
 */
public class Config {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.calendar.config");

	private static final String CALENDARS = "calendars";

	private static final String USER_INTERFACE_OPTIONS = "user_interface_options";

	private static final String GENERAL = "general";

	public static final String CALENDAR_TYPE = "type";

	public static final String CALENDAR_COLOR = "color";

	public static final String CALENDAR_NAME = "name";

	public static final String NODE_ID_LOCAL_ROOT = "root_local";

	public static final String NODE_ID_WEB_ROOT = "root_web";

	public static final String NODE_ID_PRIVATE = "private";
	public static final String NODE_ID_WORK = "work";


	private Preferences prefs;

	private static Config config = new Config();

	private File calendarDirectory;

	private File configFile;

	private Config() {
		super();

		// get Columba's top-level configuration directory
		File file = org.columba.core.config.Config.getDefaultConfigPath();

		calendarDirectory = new File(file, "calendar");
		DiskIO.ensureDirectory(calendarDirectory);

		load();

		ShutdownManager.getInstance().register(new Runnable() {

			public void run() {
				try {
					save();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 
	 */
	private void cleanupConfiguration() {
		// cleanup calendar preferences
		prefs = Preferences.userRoot().node("/calendar");
		try {
			prefs.removeNode();
		} catch (BackingStoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void load() {
		configFile = new File(calendarDirectory, "options.xml");

		if (configFile.exists()) {
			try {
				LOG
						.info("loading config file: "
								+ configFile.getAbsolutePath());

				InputStream is = new FileInputStream(configFile);

				Preferences.importPreferences(is);

				is.close();

				prefs = Preferences.userRoot().node("/calendar");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidPreferencesFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			LOG.info("creating default config file: "
					+ configFile.getAbsolutePath());

			try {
				prefs = Preferences.userRoot().node("/calendar");

				createDefaultCalendarOptions();
			} catch (BackingStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static Config getInstance() {
		return config;
	}

	public Preferences getGeneralOptions() {
		return prefs.node(Config.GENERAL);
	}

	public Preferences getUserInterfaceOptions() {
		return prefs.node(Config.USER_INTERFACE_OPTIONS);
	}

	public Preferences getCalendarOptions() {
		return prefs.node(Config.CALENDARS);

	}

	public void createDefaultCalendarOptions() throws BackingStoreException {
		// create default preferences
		Preferences prefs = getCalendarOptions();
		String[] children = prefs.childrenNames();
		if (children.length == 0) {
			// creating default calendar "Personal"
			LOG.info("creating default calendar <Personal>");

			String calendarId = Config.NODE_ID_PRIVATE;
			Preferences node = prefs.node(calendarId);
			node.put(Config.CALENDAR_NAME, "Private");
			node.putInt(Config.CALENDAR_COLOR, new Color(255, 180, 180)
					.getRGB());
			node.put(Config.CALENDAR_TYPE, "local");
			
			String calendarId2 = Config.NODE_ID_WORK;
			Preferences node2 = prefs.node(calendarId2);
			node2.put(Config.CALENDAR_NAME, "Work");
			node2.putInt(Config.CALENDAR_COLOR, new Color(180, 255, 180)
					.getRGB());
			node2.put(Config.CALENDAR_TYPE, "local");
			
		}
	}

	private void save() {

		// create temporary copy
		File file = new File(calendarDirectory, "backup.xml");
		if (file.exists())
			file.delete();

		configFile.renameTo(file);

		configFile = new File(calendarDirectory, "options.xml");

		LOG.info("saving config file: " + configFile);

		try {

			OutputStream os = new FileOutputStream(configFile);

			prefs.exportSubtree(os);

			os.close();

			cleanupConfiguration();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @return Returns the calendarDirectory.
	 */
	public File getCalendarDirectory() {
		return calendarDirectory;
	}

}
