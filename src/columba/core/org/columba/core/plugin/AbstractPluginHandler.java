package org.columba.core.plugin;

import java.io.File;
import java.util.Hashtable;

import org.columba.core.loader.DefaultClassLoader;
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

		if( config != null )
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



	public Object getPlugin(String name, String className, Object[] args)
		throws Exception {

		try {
			return loadPlugin(className, args);
		} catch (ClassNotFoundException ex) {
			XmlElement parent = (XmlElement) externalPlugins.get(name);
			XmlElement child = parent.getElement("runtime");
			String type = child.getAttribute("type");
			String jar = child.getAttribute("jar");
			//String importPackages = child.getAttribute("import");
			
			File file = (File) pluginFolders.get(name);
			if ( jar!=null ) file = new File(file, jar);
			
			
			return PluginLoader.loadExternalPlugin(className, type, file, args);
		}

	}

	public void addPlugin(String name, File pluginFolder, XmlElement element) {

		externalPlugins.put(name, element);

		pluginFolders.put(name, pluginFolder);
	}

}
