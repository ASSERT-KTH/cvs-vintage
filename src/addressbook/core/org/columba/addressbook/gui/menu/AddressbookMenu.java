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

package org.columba.addressbook.gui.menu;

import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.menu.Menu;
import org.columba.core.gui.menu.MenuBarGenerator;
import org.columba.core.gui.util.NotifyDialog;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.PluginHandlerNotFoundException;
import org.columba.core.pluginhandler.MenuPluginHandler;

/**
 * @author frd
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AddressbookMenu extends Menu {
    /**
 * @param xmlRoot
 * @param frameMediator
 */
    public AddressbookMenu(String xmlRoot, FrameMediator frameController) {
        super(xmlRoot, frameController);

        try {
            ((MenuPluginHandler) MainInterface.pluginManager.getHandler(
                "org.columba.addressbook.menu")).insertPlugins(this);
        } catch (PluginHandlerNotFoundException ex) {
            NotifyDialog d = new NotifyDialog();
            d.showDialog(ex);
        }
    }

    public MenuBarGenerator createMenuBarGeneratorInstance(String xmlRoot,
        FrameMediator frameController) {
        if (menuGenerator == null) {
            menuGenerator = new AddressbookMenuBarGenerator(frameController,
                    xmlRoot);
        }

        return menuGenerator;
    }
}
