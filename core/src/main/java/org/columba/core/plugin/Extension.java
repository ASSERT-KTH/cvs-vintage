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
package org.columba.core.plugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import org.columba.api.plugin.ExtensionMetadata;
import org.columba.api.plugin.IExtension;
import org.columba.api.plugin.IExtensionInterface;
import org.columba.api.plugin.PluginException;
import org.columba.api.plugin.PluginMetadata;
import org.columba.core.logging.Logging;
import org.columba.core.main.Main;

/**
 * An extension providing the metadata of an extension and the runtime context
 * for instanciation.
 * 
 * @author fdietz
 */
public class Extension implements IExtension {

	private static final String FILE_PLUGIN_JAR = "plugin.jar";

	private static final java.util.logging.Logger LOG = java.util.logging.Logger
			.getLogger("org.columba.core.plugin");

	private ExtensionMetadata metadata;

	private PluginMetadata pluginMetadata;

	private boolean internalPlugin;

	private IExtensionInterface cachedInstance;

	/**
	 * Enabled by default. But, in case of an error on instanciation its
	 * disabled.
	 */
	private boolean enabled = true;

	/**
	 * Constructor used by internal extensions
	 * 
	 * @param metadata
	 */
	public Extension(ExtensionMetadata metadata, boolean internal) {
		this.metadata = metadata;

		internalPlugin = internal;
	}

	/**
	 * Constructor used by external extensions.
	 * 
	 * @param pluginMetadata
	 * @param metadata
	 */
	public Extension(PluginMetadata pluginMetadata, ExtensionMetadata metadata) {
		this.metadata = metadata;
		this.pluginMetadata = pluginMetadata;

		internalPlugin = false;
	}

	/**
	 * @see org.columba.api.plugin.IExtension#getMetadata()
	 */
	public ExtensionMetadata getMetadata() {
		return metadata;
	}

	/**
	 * @see org.columba.api.plugin.IExtension#instanciateExtension(java.lang.Object[])
	 */
	public IExtensionInterface instanciateExtension(Object[] arguments)
			throws PluginException {

		if (!enabled)
			throw new PluginException("Extension <" + getMetadata().getId()
					+ "> was disabled due to a former instanciation error");

		String id = null;
		File pluginDirectory = null;

		String className = metadata.getClassname();

		if (pluginMetadata != null) {
			id = pluginMetadata.getId();
			// if external plugin, we need the directory of it
			pluginDirectory = pluginMetadata.getDirectory();
		}

		IExtensionInterface plugin = null;

		// if available, load cached instance
		if ((metadata.isSingleton()) && (cachedInstance != null)) {
			plugin = cachedInstance;

		} else {

			try {

				if (isInternal())

					// use default Java classlodaer
					plugin = instanciateJavaClass(className, arguments);

				else {
					//
					// external plugin
					//				

					// just in case that someone who developers on a plugin
					// adds the plugin files to his classpath, we try to
					// load
					// them with the default classloader

					// use default Java classlodaer

					// try {
					// plugin = instanciateJavaClass(className, arguments);
					// if (plugin != null)
					// return plugin;
					//
					// } catch (Exception e) {
					// //handleException(e);
					// } catch (Error e) {
					// //handleException(e);
					// }

					// use external Java URL classloader
					plugin = instanciateExternalJavaClass(arguments,
							pluginDirectory, className);

				}

				// remember instance
				if (metadata.isSingleton())
					cachedInstance = plugin;

			} catch (Exception e) {
				logErrorMessage(e);

				// disable extension
				enabled = false;

				throw new PluginException(this, e);
			} catch (Error e) {
				logErrorMessage(e);

				// disable extension
				enabled = false;

				throw new PluginException(this, e);
			}

		}
		return plugin;
	}

	/**
	 * @param e
	 */
	private void logErrorMessage(Throwable e) {
		if (e.getCause() != null) {
			LOG.severe(e.getCause().getMessage());
			if (Logging.DEBUG)
				e.getCause().printStackTrace();
		} else {
			LOG.severe(e.getMessage());
			if (Logging.DEBUG)
				e.printStackTrace();
		}
	}

	/**
	 * Instanciate external Java class which is specified using the
	 * <code>plugin.xml</code> descriptor file.
	 * <p>
	 * Currently, this is not used!
	 * 
	 * @param arguments
	 *            class constructor arguments
	 * @param pluginDirectory
	 *            plugin directory
	 * @param className
	 *            extension classname
	 * @return extension instance
	 * @throws Exception
	 */
	private IExtensionInterface instanciateExternalJavaClass(
			Object[] arguments, File pluginDirectory, String className)
			throws Exception {
		IExtensionInterface plugin;
		URL[] urls = null;

		// all Java plugins package their class-files in "plugin.jar"
		String jarFilename = Extension.FILE_PLUGIN_JAR;

		try {
			urls = getURLs(pluginDirectory, jarFilename);
		} catch (MalformedURLException e) {
			// should never happen
			e.printStackTrace();
		}

		//
		// @author: fdietz
		// WORKAROUND:
		// we simply append URLs to the existing global class loader
		// and use the same as parent
		// 
		// Note, that we create a new URL classloader for every class
		// we instanciate. We might want to support hot-swapping
		// of changed classes later.

		// append URLs to global classloader
		Main.mainClassLoader.addURLs(urls);

		//plugin = instanciateJavaClass(className, arguments);

		// create new class loader using the global class loader as parent
		 ExternalClassLoader loader = new ExternalClassLoader(urls,
				 Main.mainClassLoader);
		 plugin = (IExtensionInterface) loader.instanciate(className,
		 arguments);

		return plugin;
	}

	private IExtensionInterface instanciateJavaClass(String className,
			Object[] arguments) throws Exception {

		if (className == null)
			throw new IllegalArgumentException("className == null");

		IExtensionInterface plugin = null;

		// use our global class loader
		ClassLoader loader = Main.mainClassLoader;
		
		Class actClass;

		actClass = loader.loadClass(className);

		//
		// we can't just load the first constructor
		// -> go find the correct constructor based
		// -> based on the arguments
		//
		if ((arguments == null) || (arguments.length == 0)) {

			plugin = (IExtensionInterface) actClass.newInstance();

		} else {
			Constructor constructor;

			constructor = ClassLoaderHelper
					.findConstructor(arguments, actClass);

			// couldn't find correct constructor
			if (constructor == null) {
				LOG.severe("Couldn't find constructor for " + className
						+ " with matching argument-list: ");
				for (int i = 0; i < arguments.length; i++) {
					LOG.severe("argument[" + i + "]=" + arguments[i]);
				}

				return null;
			} else {

				plugin = (IExtensionInterface) constructor
						.newInstance(arguments);

			}

		}

		return plugin;
	}

	/**
	 * @see org.columba.api.plugin.IExtension#isInternal()
	 */
	public boolean isInternal() {
		return internalPlugin;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("id=" + metadata.getId());
		buf.append("class=" + metadata.getClassname());

		return buf.toString();
	}

	/**
	 * Generate array of all URLs which are used to prefill the URLClassloader.
	 * <p>
	 * All jar-files in /lib directory are automatically added.
	 * 
	 * @param file
	 *            plugin directory
	 * @param jarFile
	 *            jar-file containing the extension code
	 * @return URL array
	 * @throws MalformedURLException
	 */
	private URL[] getURLs(File file, String jarFile)
			throws MalformedURLException {
		if (file == null)
			throw new IllegalArgumentException("file == null");
		if (jarFile == null)
			throw new IllegalArgumentException("jarFile == null");

		List urlList = new Vector();

		// plugin-directory
		String path = file.getPath();

		if (jarFile != null) {
			URL jarURL = new File(file, jarFile).toURL();
			urlList.add(jarURL);
		}

		URL newURL = new File(path).toURL();
		urlList.add(newURL);

		// we add every jar-file in /lib, too
		// plugin-directory

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

		if (Logging.DEBUG) {
			for (int i = 0; i < url.length; i++) {
				LOG.finest("url[" + i + "]=" + url[i]);
			}
		}

		return url;
	}

	public void setInternal(boolean internal) {
		this.internalPlugin = internal;
	}

	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

}
