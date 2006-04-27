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
package org.columba.core.main;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;

import sun.misc.URLClassPath;

/**
 * Main entry point classloader.
 * 
 * @author fdietz
 */
public class MainClassLoader extends URLClassLoader {

	private Vector<URL> vector = new Vector<URL>();

	public MainClassLoader(ClassLoader parent) {
		super(new URL[] {}, parent);

		// through reflection
		URLClassLoader sysloader = (URLClassLoader) ClassLoader
				.getSystemClassLoader();

		// kids - don't do this at home
		try {
			Field ucp = URLClassLoader.class.getDeclaredField("ucp");
			ucp.setAccessible(true);
			URLClassPath currentCP = (URLClassPath) ucp.get(sysloader);
			URL[] currentURLs = currentCP.getURLs();

			// memorize all URLs
			for (int i = 0; i < currentURLs.length; i++) {
				vector.add(currentURLs[i]);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addURL(URL url) {
		super.addURL(url);
	}

	public void addURLs(URL[] urls) {
		for ( int i=0; i<urls.length; i++) {
			super.addURL(urls[i]);
		}
	}
}
