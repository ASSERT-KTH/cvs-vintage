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
package org.columba.core.resourceloader;

import java.net.URL;

import javax.swing.ImageIcon;

import org.columba.core.io.DiskIO;

public class ImageLoader {

	private static final String ICON_PATH = "org/columba/core/icons";
	
	public static ImageIcon getMimetypeIcon(String name) {
		URL url;

		url = DiskIO.getResourceURL(ICON_PATH + "/MIMETYPE/" + name);
		
		if (url == null) 
			url = getFallback(false);

		ImageIcon icon = new ImageIcon(url);

		return icon;
	}

	public static ImageIcon getIcon(String name) {
		return getIcon(ImageLoader.ICON_PATH, name, false);
	}

	public static ImageIcon getSmallIcon(String name) {
		return getIcon(ImageLoader.ICON_PATH, name, true);
	}

	public static ImageIcon getIcon(String path, String name, boolean small) {
		URL url;

		if (small)
			url = DiskIO.getResourceURL(path + "/16x16/" + name);
		else
			url = DiskIO.getResourceURL(path + "/22x22/" + name);

		if (url == null) {
			url = getFallback(small);
		}

		ImageIcon icon = new ImageIcon(url);

		return icon;
	}

	public static ImageIcon getMiscIcon(String name) {
		URL url;
		String path = ImageLoader.ICON_PATH;

		url = DiskIO.getResourceURL(path + "/MISC/" + name);

		if (url == null) {
			url = getFallback(true);
		}

		ImageIcon icon = new ImageIcon(url);

		return icon;
	}

	private static URL getFallback(boolean small) {
		String path;
		String name;
		URL url;
		path = ImageLoader.ICON_PATH;
		name = "image-missing.png";
		if (small)
			url = DiskIO.getResourceURL(path + "/16x16/" + name);
		else
			url = DiskIO.getResourceURL(path + "/22x22/" + name);
		return url;
	}

}