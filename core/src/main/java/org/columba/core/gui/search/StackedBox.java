// The contents of this file are subject to the Mozilla Public License Version
// 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.core.gui.search;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;

import org.jdesktop.swingx.VerticalLayout;

/**
 * StackedBox class
 * Stacks components vertically in boxes. Each box is created with a title and a
 * component.
 * @author fdietz
 */
public class StackedBox extends JPanel implements Scrollable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 6499186046747795448L;

	// private Color titleBackgroundColor;
	//
	// private Color titleForegroundColor;
	//
	// private Color separatorColor;

	// private Border separatorBorder;

	/**
	 * StackedBox default constructor
	 */
	public StackedBox() {
		setLayout(new VerticalLayout());
		setOpaque(true);
		// setBackground(UIManager.getColor("TextField.background"));

		// separatorBorder = new SeparatorBorder();
		// separatorBorder = new RoundedBorder(Color.LIGHT_GRAY);
		// setTitleForegroundColor(UIManager.getColor("Label.foreground"));
		// setTitleBackgroundColor(UIManager.getColor("Label.background"));
		// setSeparatorColor(Color.BLUE);
		// setTitleBackgroundColor(new Color(248, 248, 248));
		// setSeparatorColor(new Color(230, 230, 230));
	}

	// public Color getSeparatorColor() {
	// return separatorColor;
	// }
	//
	// public void setSeparatorColor(Color separatorColor) {
	// this.separatorColor = separatorColor;
	// }
	//
	// public Color getTitleForegroundColor() {
	// return titleForegroundColor;
	// }
	//
	// public void setTitleForegroundColor(Color titleForegroundColor) {
	// this.titleForegroundColor = titleForegroundColor;
	// }
	//
	// public Color getTitleBackgroundColor() {
	// return titleBackgroundColor;
	// }
	//
	// public void setTitleBackgroundColor(Color titleBackgroundColor) {
	// this.titleBackgroundColor = titleBackgroundColor;
	// }

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
