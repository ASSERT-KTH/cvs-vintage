/*
 * Created on Jul 6, 2005
 */
package org.columba.core.gui.docking;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.UIManager;

/**
 */
class GradientPainter {
	private Color startColor;

	private Color midColor;

	public GradientPainter(Color start, Color mid) {
		startColor = start;
		midColor = mid;
	}

	public void paintGradient(JComponent comp, Graphics g) {
		 //draw a gradient from left to right
		 
		 int h = comp.getHeight();
		 int w = comp.getWidth();
		 int mid = w;
		
		 Color bgColor = comp.getBackground();
		 Color start = startColor==null? bgColor: startColor;
		 Color middle = midColor==null? bgColor: midColor;
		
		 GradientPaint firstHalf = new GradientPaint(0, 0, start, mid, 0,
		 middle);
		 GradientPaint secondHalf = new GradientPaint(mid, 0, middle, w, 0,
		 bgColor);
				
		 Graphics2D g2 = (Graphics2D)g;
		 g2.setPaint(firstHalf);
		 g2.fillRect(0, 0, mid, h);
		 g2.setPaint(secondHalf);
		 g2.fillRect(mid-1, 0, mid, h);

		// draw gradient from middle to top and middle to bottom
		// -> this is way to fancy for use in Columba
		// Graphics2D g2 = (Graphics2D) g;
		//
		// int height = comp.getHeight();
		// int width = comp.getWidth();
		//
		// Color gradientStart = UIManager.getColor("List.selectionBackground");
		// Color gradientEnd = UIManager.getColor("List.selectionForeground");
		//
		// GradientPaint painter = new GradientPaint(0, 0, gradientEnd, 0,
		// height / 2, gradientStart);
		// g2.setPaint(painter);
		// Rectangle2D rect = new Rectangle2D.Double(0, 0, width, (height / 2.0)
		// );
		// g2.fill(rect);
		//
		// painter = new GradientPaint(0, height / 2, gradientStart, 0, height,
		// gradientEnd);
		// g2.setPaint(painter);
		// rect = new Rectangle2D.Double(0, (height / 2.0) - (1.0), width,
		// height);
		// g2.fill(rect);
	}

	public Color getMidColor() {
		return midColor;
	}

	public void setMidColor(Color midColor) {
		this.midColor = midColor;
	}

	public Color getStartColor() {
		return startColor;
	}

	public void setStartColor(Color startColor) {
		this.startColor = startColor;
	}
}
