//The contents of this file are subject to the Mozilla Public License Version 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.core.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.columba.core.loader.ExternalClassLoader;
import org.columba.core.main.Main;
import org.columba.core.pluginhandler.InterpreterHandler;
import org.columba.core.scripting.AbstractInterpreter;

/**
 * PluginLoader handles the different kinds of classloaders.
 * <p>
 * Possible candidates can be java classes in Columba or outside and classes,
 * which are handled by the interpreter plugin. (python classes at this time
 * only)
 * 
 * 
 * @author freddy
 */
public final class PluginLoader {

	private static final Logger LOG = Logger
			.getLogger("org.columba.core.plugin");

	/**
	 * Constructor for PluginLoader.
	 */
	public PluginLoader() {
	}

	/**
	 * @param className
	 *            name of class
	 * @param type
	 *            type of plugin (java|jar|python)
	 * @param file
	 *            File which specifies the class/jar/py file
	 * @param args
	 *            arguments for the plugin
	 * @return instance of plugin
	 * @throws Exception
	 */
	public Object loadExternalPlugin(String className, String type, File file,
			Object[] args) throws Exception {
		if ((className == null) || (type == null) || (file == null)) {
			return null;
		}

		if (Main.DEBUG) {
			LOG.fine("loading.. " + className);
		}

		Object object = null;

		// is this a java plugin?
		if (type.equals("java") || type.equals("jar")) {

			URL[] urls = getURLs(file);

			object = new ExternalClassLoader(urls).instanciate(className, args);

		} else {
			// this is some other plugin type
			// -> use appropriate interpreter plugin

			InterpreterHandler handler = (InterpreterHandler) PluginManager
					.getInstance().getHandler("org.columba.core.interpreter");

			Object instance = handler.getInterpreter(type);

			if (instance != null) {
				AbstractInterpreter ip = (AbstractInterpreter) instance;

				String interpreterFile = file.getAbsolutePath() + "/"
						+ className;

				//String interpreterClass = className.substring(0,
				// className.length() - 3);

				object = ip.instanciate(interpreterFile, className, args);

			}
		}

		return object;
	}

	private URL[] getURLs(File file) throws MalformedURLException {
		//		 plugin-directory
		String path = file.getPath();

		List urlList = new Vector();

		URL newURL = new File(path).toURL();
		urlList.add(newURL);

		// we add every jar-file in /lib, too
		// plugin-directory
		File directory = file.getParentFile();

		File lib = new File(file, "lib");

		if (lib.exists()) {
			File[] libList = lib.listFiles();

			for (int i = 0; i < libList.length; i++) {
				File f = libList[i];

				if (f.getName().endsWith(".jar")) {
					// jar-file found
					urlList.add(f.toURL());
				} else if (f.isDirectory()) {
					urlList.add(f.toURL());
				}
			}
		}

		URL[] url = new URL[urlList.size()];

		for (int i = 0; i < urlList.size(); i++) {
			url[i] = (URL) urlList.get(i);
		}

		if (Main.DEBUG) {
			for (int i = 0; i < url.length; i++) {
				LOG.finest("url[" + i + "]=" + url[i]);
			}
		}

		return url;
	}
}