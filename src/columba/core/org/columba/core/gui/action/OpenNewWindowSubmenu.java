/*
 * Created on 28.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.gui.action;

import org.columba.core.action.IMenu;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class OpenNewWindowSubmenu extends IMenu {

	/**
	 * @param controller
	 * @param caption
	 */
	public OpenNewWindowSubmenu(AbstractFrameController controller, String caption) {
		super(controller, MailResourceLoader.getString(
		"menu",
		"mainframe",
		"menu_file_opennewwindow"));
		
	}

}
