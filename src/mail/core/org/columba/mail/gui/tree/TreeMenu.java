/*
 * Created on 12.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.tree;

import org.columba.core.gui.frame.FrameController;
import org.columba.core.xml.XmlElement;
import org.columba.mail.gui.menu.MailContextMenu;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class TreeMenu extends MailContextMenu {

	//protected PopupMenuGenerator menuGenerator;
	//protected FrameController frameController;

	/**
	 * 
	 */
	public TreeMenu(FrameController frameController) {
		super(frameController, "org/columba/mail/action/tree_contextmenu.xml");
		
		/*
		this.frameController = frameController;

		menuGenerator =
			new PopupMenuGenerator(
				frameController,
				"org/columba/mail/action/tree_contextmenu.xml");
		menuGenerator.createPopupMenu(this);
		*/
	}

	public void extendMenuFromFile(String path) {
		menuGenerator.extendMenuFromFile(path);
		menuGenerator.createPopupMenu(this);
	}

	public void extendMenu(XmlElement menuExtension) {
		menuGenerator.extendMenu(menuExtension);
	}

}
