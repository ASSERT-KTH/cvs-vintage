package org.columba.core.gui.themes.thincolumba;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTextPaneUI;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ThinTextPaneUI extends BasicTextPaneUI {

	public static ComponentUI createUI(JComponent c) {
		return new ThinTextPaneUI();
	}

	protected void paintSafely(Graphics g) {
		if (UIManager.get("antialiasing").equals("2")) {
			Graphics2D g2 = (Graphics2D) g;

			g2.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			g2.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		} else if (UIManager.get("antialiasing").equals("1")) {

			ThinUtilities.enableAntiAliasing(g);
		}
		super.paintSafely(g);
	}
}
