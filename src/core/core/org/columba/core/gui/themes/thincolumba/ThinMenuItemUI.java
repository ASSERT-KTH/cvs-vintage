package org.columba.core.gui.themes.thincolumba;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicMenuItemUI;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ThinMenuItemUI extends BasicMenuItemUI {

	public static ComponentUI createUI(JComponent c) {
		
		return new ThinMenuItemUI();
	}

	public void paint(Graphics g, JComponent c) {
		//JMenuItem item = (JMenuItem) c;

		ThinUtilities.enableAntiAliasing(g);

		super.paint(g, c);
	}

	

	
}
