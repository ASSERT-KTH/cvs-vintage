package org.columba.core.gui.themes.thincolumba;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.plaf.metal.MetalBorders;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ThinBorders extends MetalBorders {

	private static Border border;

	public static class ButtonBorder extends MetalBorders.ButtonBorder {
		public void paintBorder(
			Component c,
			Graphics g,
			int x,
			int y,
			int w,
			int h) {
			ThinUtilities.enableAntiAliasing(g);
			super.paintBorder(c, g, x, y, w, h);
		}
	}

	public static Border getButtonBorder() {
		if (border == null)
			border =
				new BorderUIResource.CompoundBorderUIResource(
					new ThinBorders.ButtonBorder(),
					new BasicBorders.MarginBorder());

		return border;
	}
}
