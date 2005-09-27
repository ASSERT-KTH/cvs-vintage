/*
 * Created on Jul 6, 2005
 */
package org.columba.core.gui.docking;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.UIManager;

/**
 */
class GradientTitlebar extends TitleBar {

	public static Color DEFAULT_MID_COLOR = UIManager
			.getColor("Menu.background");

	public static Color DEFAULT_START_COLOR = UIManager
			.getColor("Menu.selectionBackground");

	private GradientPainter gradient;

	public GradientTitlebar(String text) {
		super(text);

		setOpaque(false);
		gradient = new GradientPainter(DEFAULT_START_COLOR, DEFAULT_MID_COLOR);
		setStartColor(DEFAULT_START_COLOR);
		setMidColor(DEFAULT_MID_COLOR);

		label.setForeground(UIManager.getColor("Menu.selectionForeground"));
		label.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN));
	}

	public void setStartColor(Color color) {
		gradient.setStartColor(color == null ? DEFAULT_START_COLOR : color);
	}

	public void setMidColor(Color color) {
		gradient.setMidColor(color == null ? DEFAULT_MID_COLOR : color);
	}

	protected void paintComponent(Graphics g) {
		gradient.paintGradient(this, g);
		super.paintComponent(g);
	}

	public void updateUI() {
		super.updateUI();

		DEFAULT_MID_COLOR = UIManager.getColor("Menu.background");
		DEFAULT_START_COLOR = UIManager.getColor("Menu.selectionBackground");

		if (label != null) {
			label.setForeground(UIManager.getColor("Menu.selectionForeground"));
			label.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN));
		}

		gradient = new GradientPainter(DEFAULT_START_COLOR, DEFAULT_MID_COLOR);

		setStartColor(DEFAULT_START_COLOR);
		setMidColor(DEFAULT_MID_COLOR);

	}
}
