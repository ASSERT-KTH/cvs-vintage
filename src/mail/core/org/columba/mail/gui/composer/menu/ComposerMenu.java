/*
 * Created on 29.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.composer.menu;

import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.menu.Menu;
import org.columba.core.gui.menu.MenuBarGenerator;
import org.columba.core.gui.menu.MenuPluginHandler;
import org.columba.core.gui.util.NotifyDialog;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.PluginHandlerNotFoundException;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ComposerMenu extends Menu {

	/**
	 * @param xmlRoot
	 * @param frameController
	 */
	public ComposerMenu(String xmlRoot, AbstractFrameController frameController) {
		super(xmlRoot, frameController);

		try {

			(
				(MenuPluginHandler) MainInterface.pluginManager.getHandler(
					"org.columba.mail.composer.menu")).insertPlugins(
				this);
		} catch (PluginHandlerNotFoundException ex) {
			NotifyDialog d = new NotifyDialog();
			d.showDialog(ex);
		}

	}

	public MenuBarGenerator createMenuBarGeneratorInstance(
		String xmlRoot,
		AbstractFrameController frameController) {
		if (menuGenerator == null) {
			menuGenerator =
				new ComposerMenuBarGenerator(frameController, xmlRoot);
		}

		return menuGenerator;
	}

}
