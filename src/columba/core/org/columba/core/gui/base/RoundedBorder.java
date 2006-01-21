package org.columba.core.gui.base;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.border.AbstractBorder;

public class RoundedBorder extends AbstractBorder {
	private Color color;

	public RoundedBorder(Color color) {
		this.color = color;
	}

	public void paintBorder(Component c, Graphics g, int x, int y, int width,
			int height) {
		g.setColor(color);
		int y2 = y + height - 1;

		// draw horizontal lines
		g.drawLine(1, y, width - 2, y);
		g.drawLine(1, y2, width - 2, y2);

		// draw vertical lines
		g.drawLine(0, y + 1, 0, y2 - 1);
		g.drawLine(width - 1, y + 1, width - 1, y2 - 1);
	}
}
