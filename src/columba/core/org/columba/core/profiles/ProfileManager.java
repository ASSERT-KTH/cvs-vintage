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
package org.columba.core.profiles;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.columba.core.io.DiskIO;
import org.columba.core.util.OSInfo;
import org.columba.core.xml.XmlElement;
import org.columba.core.xml.XmlIO;

/**
 * Manages profiles consisting of configuration folders.
 * <p>
 * Every profile has a name and a loation pointing to the configuration folder.
 * <p>
 * A profiles.xml configuration file is saved in the default config directory,
 * storing all profiles information.
 * 
 * @author fdietz
 */
public class ProfileManager {

	/**
	 * location of configuration directory
	 */
	private File location;

	/**
	 * profiles.xml file
	 */
	private File profilesConfig;

	/**
	 * top-level xml node
	 */
	private XmlElement profiles;

	/**
	 * using singleton pattern to instanciate class
	 */
	private static ProfileManager instance;

	/**
	 * Comment for <code>xml</code>
	 */
	private XmlIO xml;

	/**
	 * default constructor
	 */
	public ProfileManager() {
		super();

		if (OSInfo.isWindowsPlatform()) {
			location = new File("config");
		} else {
			location = new File(System.getProperty("user.home"), ".columba");
		}

		profilesConfig = new File(location, "profiles.xml");

	}
	/**
	 * Get instance of class
	 * 
	 * @return instance
	 */
	public static ProfileManager getInstance() {
		if (instance == null)
			instance = new ProfileManager();

		return instance;
	}

	/**
	 * Get profile with name
	 * 
	 * @param name
	 *            name of class
	 * 
	 * @return return profile if available. Otherwise, return null
	 */
	protected Profile getProfileForName(String name) {
		for (int i = 0; i < profiles.count(); i++) {

			XmlElement profile = profiles.getElement(i);
			String n = profile.getAttribute("name");
			if (name.equals(n)) {
				location = new File(profile.getAttribute("location"));
				return new Profile(n, location);
			}
		}
		return null;
	}

	/**
	 * Get profile with location.
	 * 
	 * @param path
	 *            location of configuration directory
	 * @return profile if available. Otherwise, return null.
	 */
	protected Profile getProfileForLocation(String path) {
		for (int i = 0; i < profiles.count(); i++) {
			XmlElement profile = profiles.getElement(i);
			String location = profile.getAttribute("location");
			if (path.equals(location)) {
				String n = profile.getAttribute("name");
				return new Profile(n, new File(location));
			}
		}
		return null;
	}

	/**
	 * Get profile.
	 * 
	 * @param location
	 *            location of config folder
	 * 
	 * @return profile if available. Otherwise, return null
	 */
	public Profile getProfile(String location) {
		// load profiles.xml
		loadProfilesConfiguration();

		Profile profile = null;
		if (location == null) {
			// prompt user for profile
			profile = promptForProfile();
		} else {
			// use commandline-specified location
			profile = getProfileForLocation(location);
			if (profile == null) {
				// create profile
				XmlElement profileElement = new XmlElement("profile");
				profileElement.addAttribute("name", location);
				profileElement.addAttribute("location", location);
				profiles.addElement(profileElement);

				// save to profiles.xml
				try {
					xml.save();
				} catch (Exception e) {
					e.printStackTrace();
				}

				profile = getProfileForLocation(location);
			}
		}

		return profile;
	}

	/**
	 * Open dialog and prompt user for profile
	 * 
	 * @return profile
	 */
	protected Profile promptForProfile() {
		return new Profile("Default", location);
	}

	/**
	 * Load profile configuration.
	 */
	protected void loadProfilesConfiguration() {
		if (!profilesConfig.exists()) {
			// create profile config file
			String hstr = "org/columba/core/config/profiles.xml";
			try {
				DiskIO.copyResource(hstr, profilesConfig);
			} catch (IOException e) {
			}
		}

		// load profile config file
		try {
			URL url = profilesConfig.toURL();
			xml = new XmlIO(url);
			xml.load();
			profiles = xml.getRoot().getElement("profiles");

		} catch (MalformedURLException mue) {
		}
	}
}