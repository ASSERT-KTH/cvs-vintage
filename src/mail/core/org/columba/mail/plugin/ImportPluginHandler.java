/*
 * Created on 23.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.plugin;

import org.columba.core.plugin.AbstractPluginHandler;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ImportPluginHandler extends AbstractPluginHandler {

	/**
	 * @param id
	 * @param config
	 */
	public ImportPluginHandler() {
		super("org.columba.mail.import", "org/columba/mail/plugin/import.xml");

		parentNode = getConfig().getRoot().getElement("importlist");
	}

}
