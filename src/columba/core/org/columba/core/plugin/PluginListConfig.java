package org.columba.core.plugin;

import java.io.File;
import java.net.URL;

import org.columba.core.io.DiskIO;
import org.columba.core.xml.XmlIO;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class PluginListConfig extends XmlIO {

	File file;
	/**
	 * Constructor for PluginListConfig.
	 */
	public PluginListConfig(String fileName) {
		super();

		URL url = DiskIO.getResourceURL(fileName);

		file = new File(url.getFile());
		setFile(file);
		
		load();
		
	}

}
