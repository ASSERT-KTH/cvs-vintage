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
package org.columba.chat.resourceloader;

import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import org.columba.core.io.DiskIO;
import org.columba.core.resourceloader.GlobalResourceLoader;

public class ResourceLoader {

	private static final String ICON_PATH = "/org/columba/chat/icons";

	private static String i18nPath = "org.columba.chat.i18n";

	public static ImageIcon getMiscIcon(String resourceName) {
		if (resourceName == null)
			throw new IllegalArgumentException("resourceName == null");

		URL url = ResourceLoader.class.getResource(ResourceLoader.ICON_PATH + "/MISC/"
				+ resourceName);

		if (url == null)
			url = getFallback(true);

		ImageIcon icon = new ImageIcon(url);

		return icon;
	}

	public static ImageIcon getIcon(String name) {
		return getIcon(ResourceLoader.ICON_PATH, name, false);
	}

	public static ImageIcon getSmallIcon(String name) {
		return getIcon(ResourceLoader.ICON_PATH, name, true);
	}

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
	 * @param small
	 * @return
	 */
	private static URL getFallback(boolean small) {
		String path;
		String name;
		URL url;
		path = "/org/columba/core/icons";
		name = "image-missing.png";
		if (small)
			url = ResourceLoader.class.getResource(path + "/16x16/" + name);
		else
			url = ResourceLoader.class.getResource(path + "/22x22/" + name);
		return url;
	}

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
