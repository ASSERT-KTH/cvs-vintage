/*
 * Created on 12.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.table;

import javax.swing.JPopupMenu;

import org.columba.core.gui.FrameController;
import org.columba.core.gui.menu.PopupMenuGenerator;
import org.columba.core.xml.XmlElement;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class TableMenu extends JPopupMenu {

	protected PopupMenuGenerator menuGenerator;
	protected FrameController frameController;

	/**
	 * 
	 */
	public TableMenu(FrameController frameController) {
		this.frameController = frameController;

		menuGenerator =
			new PopupMenuGenerator(
				frameController,
				"org/columba/mail/action/table_contextmenu.xml");
		menuGenerator.createPopupMenu(this);
	}

	public void extendMenuFromFile(String path) {
		menuGenerator.extendMenuFromFile(path);
		menuGenerator.createPopupMenu(this);
	}

	public void extendMenu(XmlElement menuExtension) {
		menuGenerator.extendMenu(menuExtension);
	}

}
