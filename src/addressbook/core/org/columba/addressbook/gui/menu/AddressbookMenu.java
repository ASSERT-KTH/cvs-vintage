/*
 * Created on 26.03.2003
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
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
