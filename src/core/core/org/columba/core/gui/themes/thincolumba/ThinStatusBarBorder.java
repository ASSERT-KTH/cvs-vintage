package org.columba.core.gui.themes.thincolumba;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.AbstractBorder;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ThinStatusBarBorder extends AbstractBorder {

	protected Insets editorBorderInsets = new Insets(3, 2, 2, 0);

	public void paintBorder(
		Component c,
		Graphics g,
		int x,
		int y,
		int w,
		int h) {
		g.translate(x, y);

		g.setColor(MetalLookAndFeel.getControlDarkShadow());
		g.drawLine(w - 1, 0, w - 1, h - 1);
		g.drawLine(1, h - 1, w - 1, h - 1);
		g.setColor(MetalLookAndFeel.getControl());
		g.drawLine(0, 0, w - 2, 0);
		g.setColor(MetalLookAndFeel.getControlHighlight());
		g.drawLine(0, 1, w - 2, 1);
		g.drawLine(0, 0, 0, h - 2);

		g.translate(-x, -y);
	}

	public Insets getBorderInsets(Component c) {
		return editorBorderInsets;
	}

}
