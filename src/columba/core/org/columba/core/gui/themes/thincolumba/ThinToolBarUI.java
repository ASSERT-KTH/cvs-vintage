package org.columba.core.gui.themes.thincolumba;

import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalToolBarUI;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ThinToolBarUI extends MetalToolBarUI {

	public static ComponentUI createUI(JComponent c) {
		return new ThinToolBarUI();
	}

	public void paint(Graphics g, JComponent c) {
		ThinUtilities.enableAntiAliasing(g);
		super.paint(g, c);
	}
}
