package org.columba.core.gui.search;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.VerticalLayout;

/**
 * Stacks components vertically in boxes. Each box is created with a title and a
 * component.
 */
public class StackedBox extends JPanel implements Scrollable {

//	private Color titleBackgroundColor;
//
//	private Color titleForegroundColor;
//
//	private Color separatorColor;

	//private Border separatorBorder;

	public StackedBox() {
		setLayout(new VerticalLayout());
		setOpaque(true);
	//	setBackground(UIManager.getColor("TextField.background"));

		//separatorBorder = new SeparatorBorder();
		// separatorBorder = new RoundedBorder(Color.LIGHT_GRAY);
//		setTitleForegroundColor(UIManager.getColor("Label.foreground"));
		// setTitleBackgroundColor(UIManager.getColor("Label.background"));
		// setSeparatorColor(Color.BLUE);
//		setTitleBackgroundColor(new Color(248, 248, 248));
//		setSeparatorColor(new Color(230, 230, 230));
	}

//	public Color getSeparatorColor() {
//		return separatorColor;
//	}
//
//	public void setSeparatorColor(Color separatorColor) {
//		this.separatorColor = separatorColor;
//	}
//
//	public Color getTitleForegroundColor() {
//		return titleForegroundColor;
//	}
//
//	public void setTitleForegroundColor(Color titleForegroundColor) {
//		this.titleForegroundColor = titleForegroundColor;
//	}
//
//	public Color getTitleBackgroundColor() {
//		return titleBackgroundColor;
//	}
//
//	public void setTitleBackgroundColor(Color titleBackgroundColor) {
//		this.titleBackgroundColor = titleBackgroundColor;
//	}

	/**
	 * Adds a new component to this <code>StackedBox</code>
	 * 
	 * @param box
	 */
	public void addBox(ResultBox box) {
		add(box);
	}

	/**
	 * @see Scrollable#getPreferredScrollableViewportSize()
	 */
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	/**
	 * @see Scrollable#getScrollableBlockIncrement(java.awt.Rectangle, int, int)
	 */
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return 10;
	}

	/**
	 * @see Scrollable#getScrollableTracksViewportHeight()
	 */
	public boolean getScrollableTracksViewportHeight() {
		if (getParent() instanceof JViewport) {
			return (((JViewport) getParent()).getHeight() > getPreferredSize().height);
		} else {
			return false;
		}
	}

	/**
	 * @see Scrollable#getScrollableTracksViewportWidth()
	 */
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	/**
	 * @see Scrollable#getScrollableUnitIncrement(java.awt.Rectangle, int, int)
	 */
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return 10;
	}

	

}
