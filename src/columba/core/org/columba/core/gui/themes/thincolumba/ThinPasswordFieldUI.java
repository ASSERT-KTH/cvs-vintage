package org.columba.core.gui.themes.thincolumba;

import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPasswordFieldUI;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ThinPasswordFieldUI extends BasicPasswordFieldUI {

	public static ComponentUI createUI(JComponent c) {
		return new ThinPasswordFieldUI();
	}

	protected void paintSafely(Graphics g) {
		ThinUtilities.enableAntiAliasing(g);
		super.paintSafely(g);
	}
}
