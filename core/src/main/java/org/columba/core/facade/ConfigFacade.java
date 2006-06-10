package org.columba.core.facade;

import org.columba.core.config.Config;
import org.columba.core.xml.XmlElement;

public class ConfigFacade {

	/**
	 * @param configName
	 *            id of config-file example: options
	 * 
	 * @return XmlElement represents an xml-treenode
	 */
	public static XmlElement getConfigElement(String configName) {
		XmlElement root = Config.getInstance().get(configName);

		return root;
	}
}
