/*
 * Created on 29.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.composer.menu;

import org.columba.core.gui.frame.FrameController;
import org.columba.core.gui.menu.Menu;
import org.columba.core.gui.menu.MenuBarGenerator;

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
	public ComposerMenu(String xmlRoot, FrameController frameController) {
		super(xmlRoot, frameController);

	}

	public MenuBarGenerator createMenuBarGeneratorInstance(
		String xmlRoot,
		FrameController frameController) {
		if (menuGenerator == null) {
			menuGenerator =
				new ComposerMenuBarGenerator(frameController, xmlRoot);
		}

		return menuGenerator;
	}

}
