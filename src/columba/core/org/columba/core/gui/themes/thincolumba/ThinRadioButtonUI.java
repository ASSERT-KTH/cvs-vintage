package org.columba.core.gui.themes.thincolumba;

import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalRadioButtonUI;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ThinRadioButtonUI extends MetalRadioButtonUI {
	private static final ComponentUI radioButton = new ThinRadioButtonUI();

	public static ComponentUI createUI(JComponent c) {
		return radioButton;
	}

	public void paint(Graphics g, JComponent c) {
		ThinUtilities.enableAntiAliasing(g);
		super.paint(g, c);
	}
}
