package org.columba.core.plugin;


/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class InterpreterHandler extends AbstractPluginHandler {

	/**
	 * Constructor for InterpreterHandler.
	 * @param id
	 * @param config
	 */
	
	public InterpreterHandler() {
			super("interpreter", null);
		}
	
	

	/**
	 * @see org.columba.core.plugin.AbstractPluginHandler#getDefaultNames()
	 */
	public String[] getDefaultNames() {
		return null;
	}
	
	/*
	public Object getPlugin(String name, String className, Object[] args)
			throws Exception {

		ColumbaLogger.log.debug("trying to load interpreter plugin");
		
			try {
				return loadPlugin(className, args);
			} catch (ClassNotFoundException ex) {
				
				XmlElement parent = (XmlElement) externalPlugins.get(name);
				XmlElement child = parent.getElement("runtime");
				//String type = child.getAttribute("type");
				
				File file = (File) pluginFolders.get(name);

				return PluginLoader.loadExternalPlugin(className, "java", file, args);
			}

		}
	*/
}
