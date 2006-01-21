/*
 * Created on Jul 6, 2005
 */
package org.columba.core.gui.docking;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;

import javax.swing.UIManager;

/**
 */
class GradientTitleBar extends DefaultTitleBar {

	private Color startColor;

	private Color fillColor;

	private Color midColor;

	private Color activeTitleColor;

	private Color inactiveTitleColor;

	public GradientTitleBar(String text, Color midColor, Color startColor,
			Color fillColor) {
		super(text);

		this.midColor = midColor;
		this.startColor = startColor;
		this.fillColor = fillColor;

		setOpaque(false);

		label.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN));
	}

	public void setActiveTitleColor(Color activeTitleColor) {
		this.activeTitleColor = activeTitleColor;

		label.setForeground(activeTitleColor);
	}

	public void setInactiveTitleColor(Color inactiveTitleColor) {
		this.inactiveTitleColor = inactiveTitleColor;

		label.setForeground(inactiveTitleColor);
	}

	public void setStartColor(Color color) {
		this.startColor = color;

	}

	public void setMidColor(Color color) {
		this.midColor = color;

	}

	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;

	}

	protected void paintComponent(Graphics g) {
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		int h = getHeight() + 1;
		int w = getWidth();

		int mid = w;

		Graphics2D g2 = (Graphics2D) g;

		GeneralPath path = generatePath(h, w);
		g2.setColor(fillColor);
		g2.fill(path);

		path = generateTopPath(w);
		GradientPaint painter = new GradientPaint(0, 0, startColor, 0, 5,
				midColor);
		g2.setPaint(painter);
		g2.fill(path);

		path = generatePath(h, w);
		g2.setColor(UIManager.getColor("controlDkShadow"));
		g2.draw(path);
		
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_DEFAULT);
	}

	public void updateUI() {
		super.updateUI();

		if (label != null) {
			label.setForeground(UIManager.getColor("Menu.selectionForeground"));
			label.setFont(UIManager.getFont("Label.font")
					.deriveFont(Font.PLAIN));
		}
		//
		// gradient = new GradientPainter(ACTIVE_START_COLOR, ACTIVE_MID_COLOR);
		//
		// setStartColor(ACTIVE_START_COLOR);
		// setMidColor(ACTIVE_MID_COLOR);

	}

	private GeneralPath generateTopPath(int w) {
		GeneralPath path;
		path = new GeneralPath();
		// top right
		path.append(new Arc2D.Float(w - 10 - 1, 0, 10, 10, 0, 90, Arc2D.OPEN),
				true);
		// top
		path.append(new Line2D.Float(5, 0, w - 5 - 1, 0), true);
		// top left
		path.append(new Arc2D.Float(0, 0, 10, 10, 90, 90, Arc2D.OPEN), true);

		return path;
	}

	private GeneralPath generatePath(int h, int w) {
		GeneralPath path = new GeneralPath();
		// top right
		path.append(new Arc2D.Float(w - 10 - 1, 0, 10, 10, 0, 90, Arc2D.OPEN),
				true);
		// top
		path.append(new Line2D.Float(5, 0, w - 5 - 1, 0), true);
		// top left
		path.append(new Arc2D.Float(0, 0, 10, 10, 90, 90, Arc2D.OPEN), true);
		// left
		path.append(new Line2D.Float(0, 5, 0, h - 1), true);
		// bottom
		path.append(new Line2D.Float(0, h - 1, w - 1, h - 1), true);
		// right
		path.append(new Line2D.Float(w - 1, 5, w - 1, h - 1), true);
		return path;
	}

	// class GradientPainter {
	// private Color startColor;
	//
	// private Color fillColor;
	//
	// private Color midColor;
	//
	// public GradientPainter(Color start, Color mid, Color fill) {
	// startColor = start;
	// midColor = mid;
	// fillColor = fill;
	// }
	//
	// public void paintGradient(JComponent comp, Graphics g) {
	//
	// ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	// RenderingHints.VALUE_ANTIALIAS_ON);
	//
	// int h = comp.getHeight() + 1;
	// int w = comp.getWidth();
	//
	// int mid = w;
	//
	// Graphics2D g2 = (Graphics2D) g;
	//
	// GeneralPath path = generatePath(h, w);
	//
	// g2.setColor(fillColor);
	//
	// g2.fill(path);
	//
	// path = generateTopPath(w);
	//
	// Color gradientStart = startColor;
	// Color gradientEnd = midColor;
	// GradientPaint painter = new GradientPaint(0, 0, gradientStart, 0,
	// 5, gradientEnd);
	// g2.setPaint(painter);
	// g2.fill(path);
	//
	// path = generatePath(h, w);
	// g2.setColor(UIManager.getColor("controlDkShadow"));
	// g2.draw(path);
	// ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	// RenderingHints.VALUE_ANTIALIAS_DEFAULT);
	// }
	//
	// private GeneralPath generateTopPath(int w) {
	// GeneralPath path;
	// path = new GeneralPath();
	// // top right
	// path.append(new Arc2D.Float(w - 10 - 1, 0, 10, 10, 0, 90,
	// Arc2D.OPEN), true);
	// // top
	// path.append(new Line2D.Float(5, 0, w - 5 - 1, 0), true);
	// // top left
	// path
	// .append(new Arc2D.Float(0, 0, 10, 10, 90, 90, Arc2D.OPEN),
	// true);
	//
	// return path;
	// }
	//
	// private GeneralPath generatePath(int h, int w) {
	// GeneralPath path = new GeneralPath();
	// // top right
	// path.append(new Arc2D.Float(w - 10 - 1, 0, 10, 10, 0, 90,
	// Arc2D.OPEN), true);
	// // top
	// path.append(new Line2D.Float(5, 0, w - 5 - 1, 0), true);
	// // top left
	// path
	// .append(new Arc2D.Float(0, 0, 10, 10, 90, 90, Arc2D.OPEN),
	// true);
	// // left
	// path.append(new Line2D.Float(0, 5, 0, h - 1), true);
	// // bottom
	// path.append(new Line2D.Float(0, h - 1, w - 1, h - 1), true);
	// // right
	// path.append(new Line2D.Float(w - 1, 5, w - 1, h - 1), true);
	// return path;
	// }
	//
	// public Color getMidColor() {
	// return midColor;
	// }
	//
	// public void setMidColor(Color midColor) {
	// this.midColor = midColor;
	// }
	//
	// public Color getStartColor() {
	// return startColor;
	// }
	//
	// public void setStartColor(Color startColor) {
	// this.startColor = startColor;
	// }
	//
	// public void setFillColor(Color fillColor) {
	// this.fillColor = fillColor;
	// }
	// }

}
