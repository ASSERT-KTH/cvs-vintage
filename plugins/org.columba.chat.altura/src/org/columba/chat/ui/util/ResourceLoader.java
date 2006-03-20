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
package org.columba.chat.ui.util;

import java.net.URL;

import javax.swing.ImageIcon;

import org.columba.core.io.DiskIO;

public class ResourceLoader {

	public static ImageIcon getImage(String resourceName) {
		if (resourceName == null)
			throw new IllegalArgumentException("resourceName == null");

		String path = "/org/columba/chat/images/" + resourceName;

		URL url = ResourceLoader.class.getResource(path);
		if (url == null)
			return null;

		ImageIcon icon = new ImageIcon(url);

		return icon;
	}

	public static ImageIcon getIcon(String name) {
		return getIcon("org/columba/chat/icons", name, false);
	}

	public static ImageIcon getSmallIcon(String name) {
		return getIcon("org/columba/chat/icons", name, true);
	}

	public static ImageIcon getIcon(String path, String name, boolean small) {
		URL url;

		if (small)
			url = DiskIO.getResourceURL(path + "/16x16/" + name);
		else
			url = DiskIO.getResourceURL(path + "/22x22/" + name);

		if (url == null) {
			path = "org/columba/core/icons";
			name = "image-missing.png";
			if (small)

				url = DiskIO.getResourceURL(path + "/16x16/" + name);
			else
				url = DiskIO.getResourceURL(path + "/22x22/" + name);
		}

		ImageIcon icon = new ImageIcon(url);

		return icon;
	}

}
