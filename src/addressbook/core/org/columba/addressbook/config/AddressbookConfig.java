//The contents of this file are subject to the Mozilla Public License Version 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.

package org.columba.addressbook.config;

import java.io.File;

import org.columba.core.config.DefaultConfig;
import org.columba.core.config.DefaultXmlConfig;
import org.columba.core.xml.XmlElement;

/**
 * @version 	1.0
 * @author
 */
public class AddressbookConfig extends DefaultConfig {

	public static final String MODULE_NAME = "addressbook";

	//private static File addressbookFile;
	private static File addressbookOptionsFile;
	private static File folderFile;

	/**
	 * @see java.lang.Object#Object()
	 */
	public AddressbookConfig() {

		File configDirectory = createConfigDir(MODULE_NAME);

		/*
		addressbookFile = new File(configDirectory, "addressbook.xml");
		registerPlugin(
			addressbookFile.getName(),
			new DefaultXmlConfig(addressbookFile));
		*/
		
		addressbookOptionsFile =
			new File(configDirectory, "options.xml");
		registerPlugin(
			addressbookOptionsFile.getName(),
			new DefaultXmlConfig(addressbookOptionsFile));

		folderFile = new File(configDirectory, "tree.xml");
		registerPlugin(folderFile.getName(), new DefaultXmlConfig(folderFile));

	}
	
	public static XmlElement get(String name) {
			DefaultXmlConfig xml = getPlugin(name + ".xml");
			return xml.getRoot();
		}

	/**
	 * Method registerPlugin.
	 * @param id
	 * @param plugin
	 */
	protected static void registerPlugin(String id, DefaultXmlConfig plugin) {
		DefaultConfig.registerPlugin(MODULE_NAME, id, plugin);
	}

	/**
	 * Method getPlugin.
	 * @param id
	 * @return DefaultXmlConfig
	 */
	protected static DefaultXmlConfig getPlugin(String id) {
		return DefaultConfig.getPlugin(MODULE_NAME, id);
	}

	/**
	 * Method getAddressbookConfig.
	 * @return AddressbookXmlConfig
	 */
	/*
	public static AddressbookXmlConfig getAddressbookConfig() {
		//return addressbookConfig;
		return (AddressbookXmlConfig) getPlugin(addressbookFile.getName());
	}
	*/

	/**
	 * Method getAddressbookOptionsConfig.
	 * @return AddressbookOptionsXmlConfig
	 */
	/*
	public static AddressbookOptionsXmlConfig getAddressbookOptionsConfig() {
		//return addressbookOptionsConfig;
		return (AddressbookOptionsXmlConfig) getPlugin(
			addressbookOptionsFile.getName());
	}
	*/
	/**
	 * Method getTreeConfig.
	 * @return TreeXmlConfig
	 */

	/*
	public static TreeXmlConfig getTreeConfig() {
		//return treeConfig;
		return (TreeXmlConfig) getPlugin(folderFile.getName());
	}
	*/

}