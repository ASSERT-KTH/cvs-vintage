// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.mail.config;

import java.io.File;

import org.columba.core.config.DefaultConfig;
import org.columba.core.config.DefaultXmlConfig;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MailConfig extends DefaultConfig {

	public static final String MODULE_NAME = "mail";

	private static File accountFile;
	private static File folderFile;
	private static File mainFrameOptionsFile;
	private static File popManageOptionsFile;
	private static File composerOptionsFile;

	/**
	 * @see java.lang.Object#Object()
	 */
	public MailConfig() {

		
			
		File configDirectory = createConfigDir(MODULE_NAME);

		accountFile = new File(configDirectory, "account.xml");
		registerPlugin(
			accountFile.getName(),
			new AccountXmlConfig(accountFile));

		folderFile = new File(configDirectory, "tree.xml");
		registerPlugin(folderFile.getName(), new FolderXmlConfig(folderFile));

		mainFrameOptionsFile =
			new File(configDirectory, "mainframeoptions.xml");
		registerPlugin(
			mainFrameOptionsFile.getName(),
			new MainFrameOptionsXmlConfig(mainFrameOptionsFile));

		popManageOptionsFile =
			new File(configDirectory, "popmanageoptions.xml");
		registerPlugin(
			popManageOptionsFile.getName(),
			new PopManageOptionsXmlConfig(popManageOptionsFile));

		composerOptionsFile = new File(configDirectory, "composeroptions.xml");
		registerPlugin(
			composerOptionsFile.getName(),
			new ComposerOptionsXmlConfig(composerOptionsFile));

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
	 * Method getAccountList.
	 * @return AccountList
	 */
	public static AccountList getAccountList() {
		return getAccountConfig().getAccountList();
	}

	/**
	 * Method getAccountConfig.
	 * @return AccountXmlConfig
	 */
	public static AccountXmlConfig getAccountConfig() {
		//return accountConfig;

		return (AccountXmlConfig) getPlugin(accountFile.getName());
	}

	/**
	 * Method getFolderConfig.
	 * @return FolderXmlConfig
	 */
	public static FolderXmlConfig getFolderConfig() {
		//return folderConfig;

		return (FolderXmlConfig) getPlugin(folderFile.getName());
	}

	/**
	 * Method getMainFrameOptionsConfig.
	 * @return MainFrameOptionsXmlConfig
	 */
	public static MainFrameOptionsXmlConfig getMainFrameOptionsConfig() {
		//return mainFrameOptionsConfig;
		return (MainFrameOptionsXmlConfig) getPlugin(
			mainFrameOptionsFile.getName());
	}

	/**
	 * Method getPopManageOptionsConfig.
	 * @return PopManageOptionsXmlConfig
	 */
	public static PopManageOptionsXmlConfig getPopManageOptionsConfig() {
		//return popManageOptionsConfig;
		return (PopManageOptionsXmlConfig) getPlugin(
			popManageOptionsFile.getName());
	}

	
	/**
	 * Method getComposerOptionsConfig.
	 * @return ComposerOptionsXmlConfig
	 */
	public static ComposerOptionsXmlConfig getComposerOptionsConfig() {
		//return composerOptionsConfig;

		return (ComposerOptionsXmlConfig) getPlugin(
			composerOptionsFile.getName());
	}

}
