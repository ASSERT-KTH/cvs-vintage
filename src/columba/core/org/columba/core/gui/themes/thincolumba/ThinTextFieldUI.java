package org.columba.core.gui.themes.thincolumba;

import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalTextFieldUI;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ThinTextFieldUI extends MetalTextFieldUI {

	public static ComponentUI createUI(JComponent c) {
		return new ThinTextFieldUI();
	}

	protected void paintSafely(Graphics g) {
		ThinUtilities.enableAntiAliasing(g);
		super.paintSafely(g);
	}
}
