package org.columba.mail.gui.message.viewer;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JDialog;

public class TransparentBackground extends JComponent {

	public TransparentBackground(JDialog dialog) {
		setOpaque(true);

		// updateBackground();
	}

	

	public void paintComponent(Graphics g) {
		g.setColor(Color.LIGHT_GRAY);

		int x = 0;
		int y = 0;
		int width = getWidth();
		int height = getHeight();

		g.drawRoundRect(x, y, width - 1, height - 1, 5, 5);
	}
}
