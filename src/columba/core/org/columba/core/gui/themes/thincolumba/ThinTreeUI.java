package org.columba.core.gui.themes.thincolumba;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTreeUI;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ThinTreeUI extends MetalTreeUI {

	final static float dash1[] = { 1.1f };
	final static BasicStroke dashed =
		new BasicStroke(
			0.5f,
			BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_ROUND,
			10.0f,
			dash1,
			0.0f);

	public static ComponentUI createUI(JComponent c) {
		return new ThinTreeUI();
	}

	public void paint(Graphics g, JComponent c) {
		ThinUtilities.enableAntiAliasing(g);
		super.paint(g, c);
	}

	protected void installDefaults() {
		super.installDefaults();

		setExpandedIcon(new ExpandedIcon(true));
		setCollapsedIcon(new ExpandedIcon(false));

	}

	protected void paintVerticalLine(
		Graphics g,
		JComponent c,
		int x,
		int top,
		int bottom) {

		Graphics2D graphics = (Graphics2D) g;

		g.setColor(MetalLookAndFeel.getControlDarkShadow());

		drawDashedVerticalLine(g, x, top, bottom);
	}

	protected void paintHorizontalLine(
		Graphics g,
		JComponent c,
		int y,
		int left,
		int right) {
		Graphics2D graphics = (Graphics2D) g;

		g.setColor(MetalLookAndFeel.getControlDarkShadow());

		drawDashedHorizontalLine(g, y, left, right);

	}
}
