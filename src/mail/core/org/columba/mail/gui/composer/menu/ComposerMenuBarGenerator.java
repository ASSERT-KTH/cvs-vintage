/*
 * Created on 29.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.composer.menu;

import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.menu.MenuBarGenerator;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ComposerMenuBarGenerator extends MenuBarGenerator {

	/**
	 * @param frameController
	 * @param path
	 */
	public ComposerMenuBarGenerator(
		AbstractFrameController frameController,
		String path) {
		super(frameController, path);

	}

	/* (non-Javadoc)
		 * @see org.columba.core.gui.menu.AbstractMenuGenerator#getString(java.lang.String, java.lang.String, java.lang.String)
		 */
	public String getString(String sPath, String sName, String sID) {

		return MailResourceLoader.getString(sPath, sName, sID);
	}

}
