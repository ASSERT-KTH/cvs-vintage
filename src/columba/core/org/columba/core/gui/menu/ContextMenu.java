/*
 * Created on 12.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.gui.menu;

import javax.swing.JPopupMenu;

import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.xml.XmlElement;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ContextMenu extends JPopupMenu {

	protected PopupMenuGenerator menuGenerator;
	/**
	 * 
	 */
	public ContextMenu(AbstractFrameController frameController, String path) {
		super();

		menuGenerator = createPopupMenuGeneratorInstance(path, frameController);
		
		menuGenerator.createPopupMenu(this);

		
	}
	
	public PopupMenuGenerator createPopupMenuGeneratorInstance(
			String xmlRoot,
			AbstractFrameController frameController) {
			if (menuGenerator == null) {
				menuGenerator = new PopupMenuGenerator(frameController, xmlRoot);
			}

			return menuGenerator;
		}

	public void extendMenuFromFile(String path) {
		menuGenerator.extendMenuFromFile(path);
		menuGenerator.createPopupMenu(this);
	}

	public void extendMenu(XmlElement menuExtension) {
		menuGenerator.extendMenu(menuExtension);
	}

}
