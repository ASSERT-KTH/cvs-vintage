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

package org.columba.addressbook.main;

import org.columba.addressbook.config.AddressbookConfig;
import org.columba.addressbook.gui.tree.AddressbookTreeModel;
import org.columba.addressbook.plugin.FolderPluginHandler;
import org.columba.addressbook.shutdown.SaveAllAddressbooksPlugin;
import org.columba.addressbook.util.AddressbookResourceLoader;
import org.columba.core.action.ActionPluginHandler;
import org.columba.core.gui.menu.MenuPluginHandler;
import org.columba.core.main.DefaultMain;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.PluginHandlerNotFoundException;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class AddressbookMain extends DefaultMain {

	/* (non-Javadoc)
	 * @see org.columba.core.main.DefaultMain#handleCommandLineParameters(java.lang.String[])
	 */
	public void handleCommandLineParameters(String[] args) {
		

	}

	/* (non-Javadoc)
	 * @see org.columba.core.main.DefaultMain#initConfiguration()
	 */
	public void initConfiguration() {
		new AddressbookConfig();

	}

	/* (non-Javadoc)
	 * @see org.columba.core.main.DefaultMain#initGui()
	 */
	public void initGui() {
		new AddressbookResourceLoader();

		MainInterface.addressbookTreeModel =
			new AddressbookTreeModel(
				AddressbookConfig.get("tree").getElement("/tree"));

		/*
		MainInterface.addressbookModel =
			new AddressbookFrameModel(
				AddressbookConfig.get("options").getElement(
					"/options/gui/viewlist"));
		*/
	}

	/* (non-Javadoc)
	 * @see org.columba.core.main.DefaultMain#initPlugins()
	 */
	public void initPlugins() {
		MainInterface.pluginManager.registerHandler(new FolderPluginHandler());

		MainInterface.pluginManager.registerHandler(
			new MenuPluginHandler("org.columba.addressbook.menu"));

		try {

			(
				(ActionPluginHandler) MainInterface.pluginManager.getHandler(
					"org.columba.core.action")).addActionList(
				"org/columba/addressbook/action/action.xml");
		} catch (PluginHandlerNotFoundException ex) {

		}

		MainInterface.shutdownManager.register(new SaveAllAddressbooksPlugin());

	}

}
