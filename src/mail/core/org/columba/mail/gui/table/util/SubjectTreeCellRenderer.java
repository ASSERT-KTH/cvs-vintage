//The contents of this file are subject to the Mozilla Public License Version 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.

package org.columba.mail.gui.table.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.tree.TreeCellRenderer;

import org.columba.mail.message.HeaderInterface;

public class SubjectTreeCellRenderer
	extends JLabel
	implements TreeCellRenderer {
	/** Last tree the renderer was painted in. */
	private JTree tree;

	/** Is the value currently selected. */
	protected boolean selected;
	/** True if has focus. */
	protected boolean hasFocus;
	/** True if draws focus border around icon as well. */
	private boolean drawsFocusBorderAroundIcon;

	// Icons
	/** Icon used to show non-leaf nodes that aren't expanded. */
	transient protected Icon closedIcon;

	/** Icon used to show leaf nodes. */
	transient protected Icon leafIcon;

	/** Icon used to show non-leaf nodes that are expanded. */
	transient protected Icon openIcon;

	// Colors
	/** Color to use for the foreground for selected nodes. */
	protected Color textSelectionColor;

	/** Color to use for the foreground for non-selected nodes. */
	protected Color textNonSelectionColor;

	/** Color to use for the background when a node is selected. */
	protected Color backgroundSelectionColor;

	/** Color to use for the background when the node isn't selected. */
	protected Color backgroundNonSelectionColor;

	/** Color to use for the focus indicator when the node has focus. */
	protected Color borderSelectionColor;

	private Font plainFont, boldFont, underlinedFont;

	/**
		 * Returns a new instance of DefaultTreeCellRenderer.  Alignment is
		 * set to left aligned. Icons and text color are determined from the
		 * UIManager.
		 */
	public SubjectTreeCellRenderer() {
		setHorizontalAlignment(JLabel.LEFT);

		setLeafIcon(UIManager.getIcon("Tree.leafIcon"));
		setClosedIcon(UIManager.getIcon("Tree.closedIcon"));
		setOpenIcon(UIManager.getIcon("Tree.openIcon"));

		setTextSelectionColor(UIManager.getColor("Tree.selectionForeground"));
		setTextNonSelectionColor(UIManager.getColor("Tree.textForeground"));
		setBackgroundSelectionColor(
			UIManager.getColor("Tree.selectionBackground"));
		setBackgroundNonSelectionColor(
			UIManager.getColor("Tree.textBackground"));
		setBorderSelectionColor(
			UIManager.getColor("Tree.selectionBorderColor"));
		Object value = UIManager.get("Tree.drawsFocusBorderAroundIcon");
		drawsFocusBorderAroundIcon =
			(value != null && ((Boolean) value).booleanValue());

		boldFont = UIManager.getFont("Label.font");
		boldFont = boldFont.deriveFont(Font.BOLD);

		plainFont = UIManager.getFont("Label.font");

		underlinedFont = UIManager.getFont("Tree.font");
		underlinedFont = underlinedFont.deriveFont(Font.ITALIC);
	}

	/**
		 * Returns the default icon, for the current laf, that is used to
		 * represent non-leaf nodes that are expanded.
		 */
	public Icon getDefaultOpenIcon() {
		return UIManager.getIcon("Tree.openIcon");
	}

	/**
		 * Returns the default icon, for the current laf, that is used to
		 * represent non-leaf nodes that are not expanded.
		 */
	public Icon getDefaultClosedIcon() {
		return UIManager.getIcon("Tree.closedIcon");
	}

	/**
		 * Returns the default icon, for the current laf, that is used to
		 * represent leaf nodes.
		 */
	public Icon getDefaultLeafIcon() {
		return UIManager.getIcon("Tree.leafIcon");
	}

	/**
		 * Sets the icon used to represent non-leaf nodes that are expanded.
		 */
	public void setOpenIcon(Icon newIcon) {
		openIcon = newIcon;
	}

	/**
		 * Returns the icon used to represent non-leaf nodes that are expanded.
		 */
	public Icon getOpenIcon() {
		return openIcon;
	}

	/**
		 * Sets the icon used to represent non-leaf nodes that are not expanded.
		 */
	public void setClosedIcon(Icon newIcon) {
		closedIcon = newIcon;
	}

	/**
		 * Returns the icon used to represent non-leaf nodes that are not
		 * expanded.
		 */
	public Icon getClosedIcon() {
		return closedIcon;
	}

	/**
		 * Sets the icon used to represent leaf nodes.
		 */
	public void setLeafIcon(Icon newIcon) {
		leafIcon = newIcon;
	}

	/**
		 * Returns the icon used to represent leaf nodes.
		 */
	public Icon getLeafIcon() {
		return leafIcon;
	}

	/**
		 * Sets the color the text is drawn with when the node is selected.
		 */
	public void setTextSelectionColor(Color newColor) {
		textSelectionColor = newColor;
	}

	/**
		 * Returns the color the text is drawn with when the node is selected.
		 */
	public Color getTextSelectionColor() {
		return textSelectionColor;
	}

	/**
		 * Sets the color the text is drawn with when the node isn't selected.
		 */
	public void setTextNonSelectionColor(Color newColor) {
		textNonSelectionColor = newColor;
	}

	/**
		 * Returns the color the text is drawn with when the node isn't selected.
		 */
	public Color getTextNonSelectionColor() {
		return textNonSelectionColor;
	}

	/**
		 * Sets the color to use for the background if node is selected.
		 */
	public void setBackgroundSelectionColor(Color newColor) {
		backgroundSelectionColor = newColor;
	}

	/**
		 * Returns the color to use for the background if node is selected.
		 */
	public Color getBackgroundSelectionColor() {
		return backgroundSelectionColor;
	}

	/**
		 * Sets the background color to be used for non selected nodes.
		 */
	public void setBackgroundNonSelectionColor(Color newColor) {
		backgroundNonSelectionColor = newColor;
	}

	/**
		 * Returns the background color to be used for non selected nodes.
		 */
	public Color getBackgroundNonSelectionColor() {
		return backgroundNonSelectionColor;
	}

	/**
		 * Sets the color to use for the border.
		 */
	public void setBorderSelectionColor(Color newColor) {
		borderSelectionColor = newColor;
	}

	/**
		 * Returns the color the border is drawn.
		 */
	public Color getBorderSelectionColor() {
		return borderSelectionColor;
	}

	/**
		* Subclassed to map <code>FontUIResource</code>s to null. If 
		* <code>font</code> is null, or a <code>FontUIResource</code>, this
		* has the effect of letting the font of the JTree show
		* through. On the other hand, if <code>font</code> is non-null, and not
		* a <code>FontUIResource</code>, the font becomes <code>font</code>.
		*/
	public void setFont(Font font) {
		if (font instanceof FontUIResource)
			font = null;
		super.setFont(font);
	}

	/**
		* Gets the font of this component.
		* @return this component's font; if a font has not been set
		* for this component, the font of its parent is returned
		*/
	public Font getFont() {
		Font font = super.getFont();

		if (font == null && tree != null) {
			// Strive to return a non-null value, otherwise the html support
			// will typically pick up the wrong font in certain situations.
			font = tree.getFont();
		}
		return font;
	}

	/**
		* Subclassed to map <code>ColorUIResource</code>s to null. If 
		* <code>color</code> is null, or a <code>ColorUIResource</code>, this
		* has the effect of letting the background color of the JTree show
		* through. On the other hand, if <code>color</code> is non-null, and not
		* a <code>ColorUIResource</code>, the background becomes
		* <code>color</code>.
		*/
	public void setBackground(Color color) {
		if (color instanceof ColorUIResource)
			color = null;
		super.setBackground(color);
	}

	/**
		 * Configures the renderer based on the passed in components.
		 * The value is set from messaging the tree with
		 * <code>convertValueToText</code>, which ultimately invokes
		 * <code>toString</code> on <code>value</code>.
		 * The foreground color is set based on the selection and the icon
		 * is set based on on leaf and expanded.
		 */
	public Component getTreeCellRendererComponent(
		JTree tree,
		Object value,
		boolean sel,
		boolean expanded,
		boolean leaf,
		int row,
		boolean hasFocus) {
			
		/*
		String stringValue =
			tree.convertValueToText(value, sel, expanded, leaf, row, hasFocus);
		*/
		
		this.tree = tree;
		/*
		this.hasFocus = hasFocus;
		setText(stringValue);
		*/
		
		if (sel)
			setForeground(getTextSelectionColor());
		else
			setForeground(getTextNonSelectionColor());
		
		
		/*
			if (leaf) {
				setIcon(getLeafIcon());
			} else if (expanded) {
				setIcon(getOpenIcon());
			} else {
				setIcon(getClosedIcon());
			}
		
		setComponentOrientation(tree.getComponentOrientation());

		selected = sel;
		*/
		MessageNode messageNode = (MessageNode) value;

		if (messageNode.getUserObject().equals("root")) {
			setText("...");
			setIcon(null);
			return this;
		}

		HeaderInterface header = messageNode.getHeader();
		if (header == null) {
			return this;
		}
		
		if (header.getFlags() != null) {
			if (header.getFlags().getRecent()) {
				if (!getFont().equals(boldFont))
					setFont(boldFont);
			} else if (messageNode.isHasRecentChildren()) {
				if (!getFont().equals(underlinedFont))
					setFont(underlinedFont);
			} else if (!getFont().equals(plainFont)) {
				setFont(plainFont);
			}
		}
		
		String subject = (String) header.get("Subject");
		if (subject != null)
			setText(subject);
		else
			setText("");

		setIcon(null);

		return this;
	}

	/**
		 * Paints the value.  The background is filled based on selected.
		 */
	public void paint(Graphics g) {
		/*
		Color bColor;

		if (selected) {
			bColor = getBackgroundSelectionColor();
		} else {
			bColor = getBackgroundNonSelectionColor();
			if (bColor == null)
				bColor = getBackground();
		}
		int imageOffset = -1;
		if (bColor != null) {
			Icon currentI = getIcon();

			imageOffset = getLabelStart();
			g.setColor(bColor);
			if (getComponentOrientation().isLeftToRight()) {
				g.fillRect(
					imageOffset,
					0,
					getWidth() - 1 - imageOffset,
					getHeight());
			} else {
				g.fillRect(0, 0, getWidth() - 1 - imageOffset, getHeight());
			}
		}

		if (hasFocus) {
			if (drawsFocusBorderAroundIcon) {
				imageOffset = 0;
			} else if (imageOffset == -1) {
				imageOffset = getLabelStart();
			}
			Color bsColor = getBorderSelectionColor();

			if (bsColor != null) {
				g.setColor(bsColor);
				if (getComponentOrientation().isLeftToRight()) {
					g.drawRect(
						imageOffset,
						0,
						getWidth() - 1 - imageOffset,
						getHeight() - 1);
				} else {
					g.drawRect(
						0,
						0,
						getWidth() - 1 - imageOffset,
						getHeight() - 1);
				}
			}
		}
		*/
		
		super.paint(g);
	}

	private int getLabelStart() {
		Icon currentI = getIcon();
		if (currentI != null && getText() != null) {
			return currentI.getIconWidth() + Math.max(0, getIconTextGap() - 1);
		}
		return 0;
	}

	/**
		* Overrides <code>JComponent.getPreferredSize</code> to
		* return slightly wider preferred size value.
		*/
	public Dimension getPreferredSize() {
		Dimension retDimension = super.getPreferredSize();

		if (retDimension != null)
			retDimension =
				new Dimension(retDimension.width + 3, retDimension.height);
		return retDimension;
	}

	
	public void validate() {
	}
	
	public void revalidate() {
	}
	
	public void repaint(long tm, int x, int y, int width, int height) {
	}
	
	public void repaint(Rectangle r) {
	
	}
	
	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	protected void firePropertyChange(
		String propertyName,
		Object oldValue,
		Object newValue) {
		// Strings get interned...
		if (propertyName == "text")
			super.firePropertyChange(propertyName, oldValue, newValue);
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void firePropertyChange(
		String propertyName,
		byte oldValue,
		byte newValue) {
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void firePropertyChange(
		String propertyName,
		char oldValue,
		char newValue) {
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void firePropertyChange(
		String propertyName,
		short oldValue,
		short newValue) {
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void firePropertyChange(
		String propertyName,
		int oldValue,
		int newValue) {
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void firePropertyChange(
		String propertyName,
		long oldValue,
		long newValue) {
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void firePropertyChange(
		String propertyName,
		float oldValue,
		float newValue) {
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void firePropertyChange(
		String propertyName,
		double oldValue,
		double newValue) {
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void firePropertyChange(
		String propertyName,
		boolean oldValue,
		boolean newValue) {
	}
}
