package org.columba.core.plugin;

import java.io.File;
import java.net.URL;
import java.util.Hashtable;

import org.columba.core.loader.DefaultClassLoader;
import org.columba.core.loader.ExternalClassLoader;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.scripting.Python;
import org.columba.core.xml.XmlElement;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class AbstractPluginHandler {

	protected PluginListConfig pluginListConfig;
	protected String id;

	protected Hashtable externalPlugins;

	protected Hashtable pluginFolders;

	/**
	 * Constructor for PluginHandler.
	 */
	public AbstractPluginHandler(String id, String config) {
		super();

		this.id = id;

		pluginListConfig = new PluginListConfig(config);

		externalPlugins = new Hashtable();

		pluginFolders = new Hashtable();

	}

	protected PluginListConfig getConfig() {
		return pluginListConfig;
	}

	public String getId() {
		return id;
	}

	public abstract String[] getDefaultNames();

	public Object loadPlugin(String className, Object[] args)
		throws Exception {

		return new DefaultClassLoader().instanciate(className, args);

	}

	public Object loadExternalPlugin(
		String className,
		String type,
		File file,
		Object[] args)
		throws Exception {

		if (type.equals("python")) {
			
			
			String pythonFile = file.toString()+"/"+className.toString();
			
			
			String pythonClass = className.toString().substring(0,className.toString().length()-3);
			//Class pluginClass = Class.forName(pythonClass);
			return Python.instanciate(pythonFile, pythonClass, args,  "test");
			
		} else {
			String path = file.getPath();

			URL[] url = new URL[1];
			URL newURL = new File(path).toURL();
			url[0] = newURL;
			ColumbaLogger.log.debug("url=" + newURL);

			return new ExternalClassLoader(url).instanciate(className, args);
		}
	}

	public Object getPlugin(String name, String className, Object[] args)
		throws Exception {

		try {
			return loadPlugin(className, args);
		} catch (ClassNotFoundException ex) {
			XmlElement parent = (XmlElement) externalPlugins.get(name);
			XmlElement child = parent.getElement("runtime");
			String type = child.getAttribute("type");

			File file = (File) pluginFolders.get(name);

			return loadExternalPlugin(className, type, file, args);
		}

	}

	public void addPlugin(String name, File pluginFolder, XmlElement element) {

		externalPlugins.put(name, element);

		pluginFolders.put(name, pluginFolder);
	}

}
