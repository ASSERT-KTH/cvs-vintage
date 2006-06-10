/*
 * Created on Jul 6, 2005
 */
package org.columba.core.gui.docking;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.columba.core.gui.base.RoundedBorder;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.Sizes;

/**
 */
class TitleBar extends JPanel {

	protected JLabel label;

	private Vector rightButtonVector = new Vector();

	private Vector leftButtonVector = new Vector();

	private Color fillColor;

	private Color midColor;

	private Color buttonBackground;

	public TitleBar(String text, Color midColor, Color fillColor) {
		super();

		this.midColor = midColor;

		this.fillColor = fillColor;

		setOpaque(false);

		label = new JLabel(text);

		// label.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN));

		setLayout(new BorderLayout());

		layoutComponents();

		setBorder(BorderFactory.createEmptyBorder(2, 5, 3, 0));
	}

	private void layoutComponents() {
		removeAll();

		add(label, BorderLayout.CENTER);

		JPanel buttonEastPanel = new JPanel();
		buttonEastPanel.setOpaque(false);
		ButtonBarBuilder builder = new ButtonBarBuilder(buttonEastPanel);

		builder.addGlue();

		for (int i = 0; i < rightButtonVector.size(); i++) {
			builder.addFixedNarrow((JButton) rightButtonVector.get(i));
			builder.addStrut(Sizes.pixel(2));
		}

		add(buttonEastPanel, BorderLayout.EAST);

		JPanel buttonWestPanel = new JPanel();
		buttonWestPanel.setOpaque(false);
		ButtonBarBuilder builder2 = new ButtonBarBuilder(buttonWestPanel);

		builder2.addGlue();

		for (int i = 0; i < leftButtonVector.size(); i++) {
			builder2.addFixedNarrow((JButton) leftButtonVector.get(i));
			builder2.addStrut(Sizes.pixel(2));
		}

		builder2.addStrut(Sizes.pixel(4));

		add(buttonWestPanel, BorderLayout.WEST);

	}

	public JButton addButton(ImageIcon icon, Action action, String border) {
		if (icon == null)
			throw new IllegalArgumentException("icon == null");
		if (action == null)
			throw new IllegalArgumentException("action == null");
		if (border == null)
			throw new IllegalArgumentException("border == null");

		JButton b = new TitleBarButton(icon);
		b.setAction(action);

		if (border.equals(BorderLayout.EAST))
			rightButtonVector.add(b);
		else if (border.equals(BorderLayout.WEST))
			leftButtonVector.add(b);

		layoutComponents();

		return b;
	}

	public void setTitle(String title) {
		if (title == null)
			title = "";

		label.setText(title);
	}

	public String getTitle() {
		return label.getText();
	}

	public void setActiveTitleColor(Color activeTitleColor,
			Color activeButtonBackground) {

		label.setForeground(activeTitleColor);

		buttonBackground = activeButtonBackground;

	}

	public void setInactiveTitleColor(Color inactiveTitleColor,
			Color inactiveButtonBackground) {

		label.setForeground(inactiveTitleColor);

		buttonBackground = inactiveButtonBackground;

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

		int h = getHeight();
		int w = getWidth();

		

		Graphics2D g2 = (Graphics2D) g;

		// g2.setColor(fillColor);
		// g2.fillRect(0,0,w,h);

		GradientPaint firstHalf = new GradientPaint(0, 0, fillColor, 0, h,
				midColor);

		g2.setPaint(firstHalf);
		g2.fillRect(0, 0, w, h);

		// GradientPaint painter = new GradientPaint(0, 0, midColor.brighter(),
		// 0, 5, midColor);
		// g2.setPaint(painter);
		//		
		// g2.fillRect(0,0,w,5);
		//		
		// new GradientPaint(0, h-1, midColor, 0, h-1-5, Color.red);
		// g2.setPaint(painter);
		//		
		// g2.fillRect(0,h-5,w,5);

		// GeneralPath path = generatePath(h, w);
		// g2.setColor(fillColor);
		// g2.fill(path);
		//
		// path = generateTopPath(w);
		// GradientPaint painter = new GradientPaint(0, 0, startColor, 0, 5,
		// midColor);
		// g2.setPaint(painter);
		// g2.fill(path);
		//
		// path = generatePath(h, w);
		// g2.setColor(UIManager.getColor("controlDkShadow"));
		// g2.draw(path);

		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_DEFAULT);
	}

	public void updateUI() {
		super.updateUI();

		// if (label != null) {
		// label.setForeground(UIManager.getColor("Menu.selectionForeground"));
		// label.setFont(UIManager.getFont("Label.font")
		// .deriveFont(Font.PLAIN));
		// }
		//
		// gradient = new GradientPainter(ACTIVE_START_COLOR, ACTIVE_MID_COLOR);
		//
		// setStartColor(ACTIVE_START_COLOR);
		// setMidColor(ACTIVE_MID_COLOR);

	}

//	private GeneralPath generateTopPath(int w) {
//		GeneralPath path;
//		path = new GeneralPath();
//		// top right
//		path.append(new Arc2D.Float(w - 10 - 1, 0, 10, 10, 0, 90, Arc2D.OPEN),
//				true);
//		// top
//		path.append(new Line2D.Float(5, 0, w - 5 - 1, 0), true);
//		// top left
//		path.append(new Arc2D.Float(0, 0, 10, 10, 90, 90, Arc2D.OPEN), true);
//
//		return path;
//	}

//	private GeneralPath generatePath(int h, int w) {
//		GeneralPath path = new GeneralPath();
//		// top right
//		path.append(new Arc2D.Float(w - 10 - 1, 0, 10, 10, 0, 90, Arc2D.OPEN),
//				true);
//		// top
//		path.append(new Line2D.Float(5, 0, w - 5 - 1, 0), true);
//		// top left
//		path.append(new Arc2D.Float(0, 0, 10, 10, 90, 90, Arc2D.OPEN), true);
//		// left
//		path.append(new Line2D.Float(0, 5, 0, h - 1), true);
//		// bottom
//		path.append(new Line2D.Float(0, h - 1, w - 1, h - 1), true);
//		// right
//		path.append(new Line2D.Float(w - 1, 5, w - 1, h - 1), true);
//		return path;
//	}

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

	class TitleBarButton extends JButton implements MouseListener {

		private final Border LINK_BORDER = BorderFactory.createEmptyBorder(1,
				1, 1, 1);

		boolean entered = false;

		private ImageIcon icon;

		TitleBarButton(ImageIcon icon) {
			super();

			this.icon = icon;

			setOpaque(false);
			setBorder(LINK_BORDER);

			setPreferredSize(new Dimension(12, 12));

			// setMargin(new Insets(5,0,0,0));

			addMouseListener(this);
		}

		public void paintComponent(Graphics g) {
			if (entered) {
				g.setColor(buttonBackground);
				g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 1, 1);
			}

			icon.paintIcon(this, g, 0, 0);
		}

		public void mouseClicked(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
			entered = true;

			((JComponent) e.getComponent()).setBorder(new RoundedBorder(
					buttonBackground));

			repaint();
		}

		public void mouseExited(MouseEvent e) {
			entered = false;

			((JComponent) e.getComponent()).setBorder(LINK_BORDER);

			repaint();
		}
	}
}
