/*
 * Created on 09.04.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.util;

import org.columba.core.action.IMenu;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.main.MainInterface;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CharacterEncodingSubMenu extends IMenu {

	
	
	/**
	 * @param controller
	 * @param caption
	 */
	public CharacterEncodingSubMenu(AbstractFrameController controller) {
		super(
			controller,
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_view_charset"));

		setIcon(ImageLoader.getImageIcon("stock_font_16.png"));

		createMenu();
	}

	protected void createMenu() {		
		MainInterface.charsetManager.createMenu(this, controller.getMouseTooltipHandler());

	}

}
