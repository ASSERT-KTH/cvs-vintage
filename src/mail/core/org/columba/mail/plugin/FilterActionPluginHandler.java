package org.columba.mail.plugin;

import java.io.File;

import org.columba.core.xml.XmlElement;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class FilterActionPluginHandler extends AbstractFilterPluginHandler {

	/**
	 * Constructor for FilterActionPluginHandler.
	 * @param config
	 */
	public FilterActionPluginHandler() {
		super(
			"filter_actions",
			"org/columba/mail/filter/filter_actions.xml",
			"actionlist");

	}

	public void addPlugin(String name, File pluginFolder, XmlElement element) {
		super.addPlugin(name, pluginFolder, element);

		
		XmlElement child = element.getElement("arguments/action");

		parentNode.addElement(child);

	}

}
