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
package org.columba.calendar.resourceloader;

import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import org.columba.core.resourceloader.GlobalResourceLoader;

/**
 * ResourceLoader class
 * @author unknown
 *
 */
public class ResourceLoader {
	private static String ICON_PATH = "/org/columba/calendar/icons";
	private static String i18nPath = "org.columba.calendar.i18n";	
	
	/**
	 * getMiscIcon method
	 * @param resourceName
	 * @return icon
	 */
	public static ImageIcon getMiscIcon(String resourceName) {
		if (resourceName == null)
			throw new IllegalArgumentException("resourceName == null");

		URL url = ResourceLoader.class.getResource(ResourceLoader.ICON_PATH
				+ "/MISC/" + resourceName);

		if (url == null)
			url = getFallback(true);

		ImageIcon icon = new ImageIcon(url);
		return icon;
	}

	/**
	 * getIcon method
	 * @param name
	 * @return standard icon
	 */
	public static ImageIcon getIcon(String name) {
		return getIcon(ResourceLoader.ICON_PATH, name, false);
	}

	/**
	 * getSmallIcon method
	 * @param name
	 * @return small icon
	 */
	public static ImageIcon getSmallIcon(String name) {
		return getIcon(ResourceLoader.ICON_PATH, name, true);
	}

	/**
	 * getIcon method
	 * @param path
	 * @param name
	 * @param small
	 * @return icon
	 */
	public static ImageIcon getIcon(String path, String name, boolean small) {
		URL url;

		if (small)
			url = ResourceLoader.class.getResource(path + "/16x16/" + name);
		else
			url = ResourceLoader.class.getResource(path + "/22x22/" + name);

		if (url == null)
			url = getFallback(small);

		ImageIcon icon = new ImageIcon(url);

		return icon;
	}

	/**
	 * GetFallback method - returns correct size image-missing icon if other icon does not exist
	 * @param small
	 * @return icon 
	 */
	private static URL getFallback(boolean small) {
		String path;
		String name;
		URL url;
		path = "org/columba/core/icons";
		name = "image-missing.png";
		if (small)

			url = ResourceLoader.class.getResource(path + "/16x16/" + name);
		else
			url = ResourceLoader.class.getResource(path + "/22x22/" + name);
		return url;
	}

	/**
	 * getString method - gets i18n bundle name
	 * @param resourceBundleName
	 * @param resourceName
	 * @return resource bundle
	 */
	public static final String getString(String resourceBundleName,
			String resourceName) {
		ResourceBundle bundle = null;
		String bundlePath = i18nPath + "." + resourceBundleName;

		try {
			bundle = ResourceBundle.getBundle(bundlePath, Locale.getDefault());

			return bundle.getString(resourceName);
		} catch (MissingResourceException e) {

			// fall-back to global resource loader
			return GlobalResourceLoader.getString(null, resourceBundleName,
					resourceName);
		}
	}
}
