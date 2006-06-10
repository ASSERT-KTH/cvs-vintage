// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.folderoptions;

import org.columba.core.xml.XmlElement;
import org.columba.mail.folder.IMailbox;

/**
 * @author fdietz
 *
 */
public interface IFolderOptionsController {
	/**
	 * Get plugin with specific name.
	 *
	 * @param name      name of plugin
	 * @return          instance of plugin
	 */
	//AbstractFolderOptionsPlugin getPlugin(String name);

	/**
	 * Load all folder options for this folder.
	 *
	 * @param folder        selected folder
	 */
	void load(IMailbox folder, int state);

	/**
	 * Save all folder options for this folder.
	 *
	 * @param folder        selected folder
	 */
	void save(IMailbox folder);

	/**
	 * Load all folder options globally.
	 *
	 */
	void load(int state);

	/**
	 * Get parent configuration node of plugin.
	 * <p>
	 * Example for the sorting plugin configuration node. This is
	 * how it can be found in options.xml and tree.xml:<br>
	 * <pre>
	 *  <sorting column="Date" order="true" />
	 * </pre>
	 * <p>
	 *
	 * @param folder        selected folder
	 * @param name          name of plugin (example: ColumnOptions)
	 * @return              parent configuration node
	 */
	XmlElement getConfigNode(IMailbox folder, String name);

	/**
	 * Create default settings for this folder.
	 *
	 * @param folder                selected folder
	 */
	void createDefaultSettings(IMailbox folder);
}