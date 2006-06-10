package org.columba.mail.gui.message.viewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.AbstractBorder;

public class MessageBorder extends AbstractBorder {

	protected Color lineColor;

	protected int thickness;

	protected boolean roundedCorners;

	protected int arc = 6;
	
	public MessageBorder(Color lineColor, int thickness, boolean roundedCorners) {
		super();

		this.lineColor = lineColor;
		this.thickness = thickness;
		this.roundedCorners = roundedCorners;

	}

	/**
	 * Paints the border for the specified component with the specified position
	 * and size.
	 * 
	 * @param c
	 *            the component for which this border is being painted
	 * @param g
	 *            the paint graphics
	 * @param x
	 *            the x position of the painted border
	 * @param y
	 *            the y position of the painted border
	 * @param width
	 *            the width of the painted border
	 * @param height
	 *            the height of the painted border
	 */
	public void paintBorder(Component c, Graphics g, int x, int y, int width,
			int height) {
		Color oldColor = g.getColor();
		int i;

		g.setColor(lineColor);
		for (i = 0; i < thickness; i++) {
			if (!roundedCorners)
				g.drawRect(x + i, y + i, width - i - i - 1, height - i - i - 1);
			else
				g.drawRoundRect(x + i, y + i, width - i - i - 1, height - i - i
						- 1, arc, arc);
		}
		g.setColor(oldColor);
	}

	/**
	 * Returns the insets of the border.
	 * 
	 * @param c
	 *            the component for which this border insets value applies
	 */
	public Insets getBorderInsets(Component c) {
		return new Insets(thickness, thickness, thickness, thickness);
	}

	/**
	 * Reinitialize the insets parameter with this Border's current Insets.
	 * 
	 * @param c
	 *            the component for which this border insets value applies
	 * @param insets
	 *            the object to be reinitialized
	 */
	public Insets getBorderInsets(Component c, Insets insets) {
		insets.left = insets.top = insets.right = insets.bottom = thickness;
		return insets;
	}

	/**
	 * Returns the color of the border.
	 */
	public Color getLineColor() {
		return lineColor;
	}

	/**
	 * Returns the thickness of the border.
	 */
	public int getThickness() {
		return thickness;
	}

}
