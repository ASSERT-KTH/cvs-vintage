package org.columba.core.config;

import java.io.File;

import org.columba.core.xml.XmlElement;
import org.columba.core.xml.XmlIO;

/**
 * Main entrypoint for configuration management.
 * <p>
 * Stores a list of all xml files in a hashtable. Hashtable key is the name of
 * the xml file. Value is {@link XmlIO} object.
 * <p>
 * Mail and Addressbook components are just wrappers, encapsulating this class.
 * Using these wrapper classes, you don't need to specify the module name (for
 * example: mail, or addressbook) manually.
 * <p>
 * Note that all configuration file have default templates in the /res directory
 * in package org.columba.core.config. These default configuration files are
 * copied into the users's configuration directory the first time Columba is
 * started.
 * <p>
 * Config creates the top-level directory for Columba's configuration in
 * ".columba", which usually resides in the user's home directory or on older
 * Windows versions in Columba's program folder.
 * <p>
 * Saving and loading of all configuration files is handled here, too.
 * 
 * @author Frederik Dietz
 * 
 */
public interface IConfig {

	/**
	 * Returns the directory the configuration is located in.
	 */
	public abstract File getConfigDirectory();

	/**
	 * Get root xml element of configuration file.
	 * 
	 * @param name
	 *            name of configuration file
	 * 
	 * @return root xml element
	 */
	public abstract XmlElement get(String name);

}