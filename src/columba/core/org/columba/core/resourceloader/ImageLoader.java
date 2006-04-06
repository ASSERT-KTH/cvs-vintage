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
import java.util.Hashtable;

import javax.swing.ImageIcon;

import org.columba.core.io.DiskIO;

public class ImageLoader {



	static boolean ICON_SET = false;

	private static Hashtable hashtable = new Hashtable();

	// ******** FOLLOWS STANDARD RESOURCE RETRIEVAL (file or jar protocol)
	// ***************
	/**
	 * @deprecated
	 */
	public static ImageIcon getUnsafeImageIcon(String name) {
		URL url;

		if (hashtable.containsKey(name) == true) {
			return (ImageIcon) hashtable.get(name);
		}

		url = DiskIO.getResourceURL("org/columba/core/images/" + name);

		if (url == null) {
			return null;
		}

		ImageIcon icon = new ImageIcon(url);

		hashtable.put(name, icon);

		return icon;
	}

	public static ImageIcon getIcon(String name) {
		return getIcon("org/columba/core/icons", name, false);
	}

	public static ImageIcon getSmallIcon(String name) {
		return getIcon("org/columba/core/icons", name, true);
	}

	public static ImageIcon getIcon(String path, String name, boolean small) {
		URL url;

//		if (hashtable.containsKey(name) == true) {
//			return (ImageIcon) hashtable.get(name);
//		}

		if (small)
			url = DiskIO.getResourceURL(path + "/16x16/" + name);
		else
			url = DiskIO.getResourceURL(path + "/22x22/" + name);

		if (url == null) {
			path = "org/columba/core/icons";
			name = "image-missing.png";
			if ( small )
				
				url =  DiskIO.getResourceURL(path + "/16x16/" +name);
			else
				url = DiskIO.getResourceURL(path + "/22x22/" + name);
		}

		ImageIcon icon = new ImageIcon(url);

		hashtable.put(name, icon);

		return icon;
	}

	/**
	 * @deprecated
	 * @param name
	 * @return
	 */
	public static ImageIcon getSmallImageIcon(String name) {

		URL url;

		if (hashtable.containsKey(name) == true) {
			return (ImageIcon) hashtable.get(name);
		}

		url = DiskIO.getResourceURL("org/columba/core/images/" + name);

		if (url == null) {
			url = DiskIO
					.getResourceURL("org/columba/core/images/brokenimage_small.png");
		}

		ImageIcon icon = new ImageIcon(url);

		hashtable.put(name, icon);

		return icon;
	}

	/**
	 * @deprecated
	 * @param name
	 * @return
	 */
	public static ImageIcon getImageIcon(String name) {
		URL url;

		if (hashtable.containsKey(name) == true) {
			return (ImageIcon) hashtable.get(name);
		}

		url = DiskIO.getResourceURL("org/columba/core/images/" + name);

		if (url == null) {
			url = DiskIO
					.getResourceURL("org/columba/core/images/brokenimage.png");
		}

		ImageIcon icon = new ImageIcon(url);

		hashtable.put(name, icon);

		return icon;
	}

	/**
	 * @deprecated
	 * @param name
	 * @return
	 */
	public static ImageIcon getImageIconResource(String name) {
		URL url;

		url = DiskIO.getResourceURL(name);

		if (url == null) {
			url = DiskIO
					.getResourceURL("org/columba/core/images/brokenimage.png");
		}

		ImageIcon icon = new ImageIcon(url);

		hashtable.put(name, icon);

		return icon;
	}

}