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
package org.columba.calendar.ui.util;

import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import org.columba.core.resourceloader.GlobalResourceLoader;

public class ResourceLoader {

	private static String path = "/org/columba/calendar/images/";

	private static String i18nPath = "org.columba.calendar.i18n";

	public static final ImageIcon getImageIcon(String filename) {
		if (filename == null)
			throw new IllegalArgumentException("filename == null");

		URL url = ResourceLoader.class.getResource(path + filename);

		// URL url = DiskIO.getResourceURL(path + filename);

		if (url == null) {
			return null;
		}

		ImageIcon icon = new ImageIcon(url);
		return icon;
	}

	public static final String getString(String resourceBundleName,
			String resourceName) {
		ResourceBundle bundle = null;

		String bundlePath = i18nPath + "." + resourceBundleName;

		try {
			bundle = ResourceBundle.getBundle(bundlePath, Locale.getDefault());

			return bundle.getString(resourceName);
		} catch (MissingResourceException e) {
			System.out.println(e.getMessage());
			System.out.println("path=" + bundlePath);

			// fall-back to global resource loader
			return GlobalResourceLoader.getString(null, resourceBundleName,
					resourceName);
		}
	}
}
