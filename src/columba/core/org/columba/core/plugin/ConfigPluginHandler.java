/*
 * Created on 08.08.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.plugin;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ConfigPluginHandler extends AbstractPluginHandler {

	/**
	 * @param id
	 * @param config
	 */
	public ConfigPluginHandler() {
		super("org.columba.core.config", "org/columba/core/plugin/config.xml");

		parentNode = getConfig().getRoot().getElement("configlist");
		
	}

}
