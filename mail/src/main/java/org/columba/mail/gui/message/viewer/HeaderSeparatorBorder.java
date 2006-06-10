package org.columba.mail.gui.message.viewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.AbstractBorder;

public class HeaderSeparatorBorder extends AbstractBorder {

	protected Color color;
	
	public HeaderSeparatorBorder(Color color) {
		super();
		
		this.color = color;
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
		g.setColor(color);
		g.drawLine(x,y+height-1, x+width-1, y+height-1);
		
		g.setColor(oldColor);
	}

	/**
	 * Returns the insets of the border.
	 * 
	 * @param c
	 *            the component for which this border insets value applies
	 */
	public Insets getBorderInsets(Component c) {
		return new Insets(0, 0, 1, 0);
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
		insets.left = insets.top = insets.right = insets.bottom = 1;
		return insets;
	}
	
}
