// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.core.gui.themes.thincolumba;

import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.*;
import javax.swing.text.*;

import java.awt.*;
import java.awt.event.*;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ThinColumbaMenuItemUI extends BasicMenuItemUI {

	private static String acceleratorDelimiter;

	// these rects are used for painting and preferredsize calculations.
	// they used to be regenerated constantly.  Now they are reused.
	static Rectangle zeroRect = new Rectangle(0, 0, 0, 0);
	static Rectangle iconRect = new Rectangle();
	static Rectangle textRect = new Rectangle();
	static Rectangle acceleratorRect = new Rectangle();
	static Rectangle checkIconRect = new Rectangle();
	static Rectangle arrowIconRect = new Rectangle();
	static Rectangle viewRect = new Rectangle(Short.MAX_VALUE, Short.MAX_VALUE);
	static Rectangle r = new Rectangle();

	/* Client Property keys for text and accelerator text widths */
	static final String MAX_TEXT_WIDTH = "maxTextWidth";
	static final String MAX_ACC_WIDTH = "maxAccWidth";

	public static ComponentUI createUI(JComponent c) {
		acceleratorDelimiter = UIManager.getString("MenuItem.acceleratorDelimiter");
		return new ThinColumbaMenuItemUI();
	}

	public Dimension getPreferredSize(JComponent c) {
		return getPreferredMenuItemSize(
			c,
			checkIcon,
			arrowIcon,
			defaultTextIconGap);
	}

	private void resetRects() {
		iconRect.setBounds(zeroRect);
		textRect.setBounds(zeroRect);
		acceleratorRect.setBounds(zeroRect);
		checkIconRect.setBounds(zeroRect);
		arrowIconRect.setBounds(zeroRect);
		viewRect.setBounds(0, 0, Short.MAX_VALUE, Short.MAX_VALUE);
		r.setBounds(zeroRect);
	}

	protected Dimension getPreferredMenuItemSize(
		JComponent c,
		Icon checkIcon,
		Icon arrowIcon,
		int defaultTextIconGap) {
		JMenuItem b = (JMenuItem) c;
		Icon icon = (Icon) b.getIcon();
		String text = b.getText();
		KeyStroke accelerator = b.getAccelerator();
		String acceleratorText = "";

		if (accelerator != null) {
			int modifiers = accelerator.getModifiers();
			if (modifiers > 0) {
				acceleratorText = KeyEvent.getKeyModifiersText(modifiers);
				//acceleratorText += "-";
				acceleratorText += acceleratorDelimiter;
			}
			int keyCode = accelerator.getKeyCode();
			if (keyCode != 0) {
				acceleratorText += KeyEvent.getKeyText(keyCode);
			} else {
				acceleratorText += accelerator.getKeyChar();
			}
		}

		Font font = b.getFont();
		FontMetrics fm = b.getToolkit().getFontMetrics(font);
		FontMetrics fmAccel = b.getToolkit().getFontMetrics(acceleratorFont);

		resetRects();

		layoutMenuItem(
			fm,
			text,
			fmAccel,
			acceleratorText,
			icon,
			checkIcon,
			arrowIcon,
			b.getVerticalAlignment(),
			b.getHorizontalAlignment(),
			b.getVerticalTextPosition(),
			b.getHorizontalTextPosition(),
			viewRect,
			iconRect,
			textRect,
			acceleratorRect,
			checkIconRect,
			arrowIconRect,
			text == null ? 0 : defaultTextIconGap,
			defaultTextIconGap);
		// find the union of the icon and text rects
		r.setBounds(textRect);

		
		/*
		System.out.println("iconRect="+iconRect);
		
		if ( iconRect.width == 0 )
		{
			iconRect.width = 16;
			iconRect.height = 16;
			System.out.println("correcting iconRect="+iconRect);
		}
		*/
		
		r =
			SwingUtilities.computeUnion(
				iconRect.x,
				iconRect.y,
				iconRect.width,
				iconRect.height,
				r);

		//   r = iconRect.union(textRect);

		// To make the accelerator texts appear in a column, find the widest MenuItem text
		// and the widest accelerator text.

		//Get the parent, which stores the information.
		Container parent = menuItem.getParent();

		//Check the parent, and see that it is not a top-level menu.
		if (parent != null
			&& parent instanceof JComponent
			&& !(menuItem instanceof JMenu
				&& ((JMenu) menuItem).isTopLevelMenu())) {
			JComponent p = (JComponent) parent;

			//Get widest text so far from parent, if no one exists null is returned.
			Integer maxTextWidth =
				(Integer) p.getClientProperty(
					ThinColumbaMenuItemUI.MAX_TEXT_WIDTH);
			Integer maxAccWidth =
				(Integer) p.getClientProperty(
					ThinColumbaMenuItemUI.MAX_ACC_WIDTH);

			int maxTextValue =
				maxTextWidth != null ? maxTextWidth.intValue() : 0;
			int maxAccValue = maxAccWidth != null ? maxAccWidth.intValue() : 0;

			//Compare the text widths, and adjust the r.width to the widest.
			if (r.width < maxTextValue) {
				r.width = maxTextValue;
			} else {
				p.putClientProperty(
					ThinColumbaMenuItemUI.MAX_TEXT_WIDTH,
					new Integer(r.width));
			}

			//Compare the accelarator widths.
			if (acceleratorRect.width > maxAccValue) {
				maxAccValue = acceleratorRect.width;
				p.putClientProperty(
					ThinColumbaMenuItemUI.MAX_ACC_WIDTH,
					new Integer(acceleratorRect.width));
			}

			//Add on the widest accelerator 
			r.width += maxAccValue;
			r.width += defaultTextIconGap;

		}

		if (useCheckAndArrow()) {
			// Add in the checkIcon
			r.width += checkIconRect.width;
			r.width += defaultTextIconGap;

			// Add in the arrowIcon
			r.width += defaultTextIconGap;
			r.width += arrowIconRect.width;
		}

		r.width += 2 * defaultTextIconGap;

		Insets insets = b.getInsets();
		if (insets != null) {
			r.width += insets.left + insets.right;
			r.height += insets.top + insets.bottom;
		}

		// if the width is even, bump it up one. This is critical
		// for the focus dash line to draw properly
		if (r.width % 2 == 0) {
			r.width++;
		}

		// if the height is even, bump it up one. This is critical
		// for the text to center properly
		if (r.height % 2 == 0) {
			r.height++;
		}
		
		r.width += 16 + defaultTextIconGap;
		
		
		
			
		/*
			if(!(b instanceof JMenu && ((JMenu) b).isTopLevelMenu()) ) {
			    
			    // Container parent = menuItem.getParent();
			    JComponent p = (JComponent) parent;
			    
			    System.out.println("MaxText: "+p.getClientProperty(BasicMenuItemUI.MAX_TEXT_WIDTH));
			    System.out.println("MaxACC"+p.getClientProperty(BasicMenuItemUI.MAX_ACC_WIDTH));
			    
			    System.out.println("returning pref.width: " + r.width);
			    System.out.println("Current getSize: " + b.getSize() + "\n");
		        }*/
		return r.getSize();
	}

	/** 
	* Compute and return the location of the icons origin, the 
	* location of origin of the text baseline, and a possibly clipped
	* version of the compound labels string.  Locations are computed
	* relative to the viewRect rectangle. 
	*/

	private String layoutMenuItem(
		FontMetrics fm,
		String text,
		FontMetrics fmAccel,
		String acceleratorText,
		Icon icon,
		Icon checkIcon,
		Icon arrowIcon,
		int verticalAlignment,
		int horizontalAlignment,
		int verticalTextPosition,
		int horizontalTextPosition,
		Rectangle viewRect,
		Rectangle iconRect,
		Rectangle textRect,
		Rectangle acceleratorRect,
		Rectangle checkIconRect,
		Rectangle arrowIconRect,
		int textIconGap,
		int menuItemGap) {

		layoutCompoundLabel(
			menuItem,
			fm,
			text,
			icon,
			verticalAlignment,
			horizontalAlignment,
			verticalTextPosition,
			horizontalTextPosition,
			viewRect,
			iconRect,
			textRect,
			textIconGap);

		/* Initialize the acceelratorText bounds rectangle textRect.  If a null 
		 * or and empty String was specified we substitute "" here 
		 * and use 0,0,0,0 for acceleratorTextRect.
		 */
		if ((acceleratorText == null) || acceleratorText.equals("")) {
			acceleratorRect.width = acceleratorRect.height = 0;
			acceleratorText = "";
		} else {
			acceleratorRect.width =
				SwingUtilities.computeStringWidth(fmAccel, acceleratorText);
			acceleratorRect.height = fmAccel.getHeight();
		}

		/* Initialize the checkIcon bounds rectangle's width & height.
		 */

		if (useCheckAndArrow()) {
			if (checkIcon != null) {
				checkIconRect.width = checkIcon.getIconWidth();
				checkIconRect.height = checkIcon.getIconHeight();
			} else {
				checkIconRect.width = checkIconRect.height = 0;
			}

			/* Initialize the arrowIcon bounds rectangle width & height.
			 */

			if (arrowIcon != null) {
				arrowIconRect.width = arrowIcon.getIconWidth();
				arrowIconRect.height = arrowIcon.getIconHeight();
			} else {
				arrowIconRect.width = arrowIconRect.height = 0;
			}
		}

		Rectangle labelRect = iconRect.union(textRect);
		if (isLeftToRight(menuItem)) {
			textRect.x += menuItemGap;
			iconRect.x += menuItemGap;

			// Position the Accelerator text rect
			acceleratorRect.x =
				viewRect.x
					+ viewRect.width
					- arrowIconRect.width
					- menuItemGap
					- acceleratorRect.width
					- 16;

			// Position the Check and Arrow Icons 
			if (useCheckAndArrow()) {
				checkIconRect.x = viewRect.x + menuItemGap;
				textRect.x += menuItemGap + checkIconRect.width;
				iconRect.x += menuItemGap + checkIconRect.width;
				arrowIconRect.x =
					viewRect.x
						+ viewRect.width
						- menuItemGap
						- arrowIconRect.width;
			}
			/*
			else
			{
				checkIconRect.x = viewRect.x + menuItemGap;
				textRect.x += menuItemGap + 16;
				iconRect.x += menuItemGap + 16;
				arrowIconRect.x =
					viewRect.x
						+ viewRect.width
						- menuItemGap
						- arrowIconRect.width;
			}
			*/
			
		} else {
			textRect.x -= menuItemGap;
			iconRect.x -= menuItemGap;

			// Position the Accelerator text rect
			acceleratorRect.x = viewRect.x + arrowIconRect.width + menuItemGap;

			// Position the Check and Arrow Icons 
			if (useCheckAndArrow()) {
				checkIconRect.x =
					viewRect.x
						+ viewRect.width
						- menuItemGap
						- checkIconRect.width;
				textRect.x -= menuItemGap + checkIconRect.width;
				iconRect.x -= menuItemGap + checkIconRect.width;
				arrowIconRect.x = viewRect.x + menuItemGap;
			}
		}

		// Align the accelertor text and the check and arrow icons vertically
		// with the center of the label rect.  
		acceleratorRect.y =
			labelRect.y + (labelRect.height / 2) - (acceleratorRect.height / 2);
		if (useCheckAndArrow()) {
			arrowIconRect.y =
				labelRect.y
					+ (labelRect.height / 2)
					- (arrowIconRect.height / 2);
			checkIconRect.y =
				labelRect.y
					+ (labelRect.height / 2)
					- (checkIconRect.height / 2);
		}

		/*
		System.out.println("Layout: text="+menuItem.getText()+"\n\tv="
		                   +viewRect+"\n\tc="+checkIconRect+"\n\ti="
		                   +iconRect+"\n\tt="+textRect+"\n\tacc="
		                   +acceleratorRect+"\n\ta="+arrowIconRect+"\n");
		*/

		return text;
	}

	/*
	 * Returns false if the component is a JMenu and it is a top
	 * level menu (on the menubar).
	 */
	private boolean useCheckAndArrow() {
		boolean b = true;
		if ((menuItem instanceof JMenu)
			&& (((JMenu) menuItem).isTopLevelMenu())) {
			b = false;
		}
		return b;
	}

	/*
	 * Convenience function for determining ComponentOrientation.  Helps us
	 * avoid having Munge directives throughout the code.
	 */
	static boolean isLeftToRight(Component c) {
		return c.getComponentOrientation().isLeftToRight();
	}

	public static String layoutCompoundLabel(
		FontMetrics fm,
		String text,
		Icon icon,
		int verticalAlignment,
		int horizontalAlignment,
		int verticalTextPosition,
		int horizontalTextPosition,
		Rectangle viewR,
		Rectangle iconR,
		Rectangle textR,
		int textIconGap) {
		return layoutCompoundLabelImpl(
			null,
			fm,
			text,
			icon,
			verticalAlignment,
			horizontalAlignment,
			verticalTextPosition,
			horizontalTextPosition,
			viewR,
			iconR,
			textR,
			textIconGap);
	}

	/**
	 * Compute and return the location of the icons origin, the
	 * location of origin of the text baseline, and a possibly clipped
	 * version of the compound labels string.  Locations are computed
	 * relative to the viewR rectangle.
	 * This layoutCompoundLabel() does not know how to handle LEADING/TRAILING
	 * values in horizontalTextPosition (they will default to RIGHT) and in
	 * horizontalAlignment (they will default to CENTER).
	 * Use the other version of layoutCompoundLabel() instead.
	 */
	private static String layoutCompoundLabelImpl(
		JComponent c,
		FontMetrics fm,
		String text,
		Icon icon,
		int verticalAlignment,
		int horizontalAlignment,
		int verticalTextPosition,
		int horizontalTextPosition,
		Rectangle viewR,
		Rectangle iconR,
		Rectangle textR,
		int textIconGap) {
		/* Initialize the icon bounds rectangle iconR.
		 */

		if (icon != null) {
			iconR.width = icon.getIconWidth();
			iconR.height = icon.getIconHeight();
		} else {
			iconR.width = iconR.height = 0;
		}

		/* Initialize the text bounds rectangle textR.  If a null
		 * or and empty String was specified we substitute "" here
		 * and use 0,0,0,0 for textR.
		 */

		boolean textIsEmpty = (text == null) || text.equals("");

		View v = null;
		if (textIsEmpty) {
			textR.width = textR.height = 0;
			text = "";
		} else {
			v = (c != null) ? (View) c.getClientProperty("html") : null;
			if (v != null) {
				textR.width = (int) v.getPreferredSpan(View.X_AXIS);
				textR.height = (int) v.getPreferredSpan(View.Y_AXIS);
			} else {
				textR.width = SwingUtilities.computeStringWidth(fm, text);
				textR.height = fm.getHeight();
			}
		}

		/* Unless both text and icon are non-null, we effectively ignore
		 * the value of textIconGap.  The code that follows uses the
		 * value of gap instead of textIconGap.
		 */

		int gap = (textIsEmpty || (icon == null)) ? 0 : textIconGap;
		

		if (!textIsEmpty) {

			/* If the label text string is too wide to fit within the available
			 * space "..." and as many characters as will fit will be
			 * displayed instead.
			 */

			int availTextWidth;

			if (horizontalTextPosition == SwingUtilities.CENTER) {
				availTextWidth = viewR.width;
			} else {
				availTextWidth = viewR.width - (iconR.width + gap);
			}

			if (textR.width > availTextWidth) {
				if (v != null) {
					textR.width = availTextWidth;
				} else {
					String clipString = "...";
					int totalWidth =
						SwingUtilities.computeStringWidth(fm, clipString);
					int nChars;
					for (nChars = 0; nChars < text.length(); nChars++) {
						totalWidth += fm.charWidth(text.charAt(nChars));
						if (totalWidth > availTextWidth) {
							break;
						}
					}
					text = text.substring(0, nChars) + clipString;
					textR.width = SwingUtilities.computeStringWidth(fm, text);
				}
			}
		}

		/* Compute textR.x,y given the verticalTextPosition and
		 * horizontalTextPosition properties
		 */

		if (verticalTextPosition == SwingUtilities.TOP) {
			if (horizontalTextPosition != SwingUtilities.CENTER) {
				textR.y = 0;
			} else {
				textR.y = - (textR.height + gap);
			}
		} else if (verticalTextPosition == SwingUtilities.CENTER) {
			textR.y = (iconR.height / 2) - (textR.height / 2);
		} else { // (verticalTextPosition == BOTTOM)
			if (horizontalTextPosition != SwingUtilities.CENTER) {
				textR.y = iconR.height - textR.height;
			} else {
				textR.y = (iconR.height + gap);
			}
		}

		if (horizontalTextPosition == SwingUtilities.LEFT) {
			textR.x = - (textR.width + gap);
		} else if (horizontalTextPosition == SwingUtilities.CENTER) {
			textR.x = (iconR.width / 2) - (textR.width / 2);
		} else { // (horizontalTextPosition == RIGHT)
			textR.x = (iconR.width + gap);
		}

		/* labelR is the rectangle that contains iconR and textR.
		 * Move it to its proper position given the labelAlignment
		 * properties.
		 *
		 * To avoid actually allocating a Rectangle, Rectangle.union
		 * has been inlined below.
		 */
		int labelR_x = Math.min(iconR.x, textR.x);
		int labelR_width =
			Math.max(iconR.x + iconR.width, textR.x + textR.width) - labelR_x;
		int labelR_y = Math.min(iconR.y, textR.y);
		int labelR_height =
			Math.max(iconR.y + iconR.height, textR.y + textR.height) - labelR_y;

		int dx, dy;

		if (verticalAlignment == SwingUtilities.TOP) {
			dy = viewR.y - labelR_y;
		} else if (verticalAlignment == SwingUtilities.CENTER) {
			dy =
				(viewR.y + (viewR.height / 2))
					- (labelR_y + (labelR_height / 2));
		} else { // (verticalAlignment == BOTTOM)
			dy = (viewR.y + viewR.height) - (labelR_y + labelR_height);
		}

		if (horizontalAlignment == SwingUtilities.LEFT) {
			dx = viewR.x - labelR_x;
		} else if (horizontalAlignment == SwingUtilities.RIGHT) {
			dx = (viewR.x + viewR.width) - (labelR_x + labelR_width);
		} else { // (horizontalAlignment == CENTER)
			dx =
				(viewR.x + (viewR.width / 2)) - (labelR_x + (labelR_width / 2));
		}

		/* Translate textR and glypyR by dx,dy.
		 */

		textR.x += dx;
		textR.y += dy;

		iconR.x += dx;
		iconR.y += dy;

		return text;
	}

	/**
	 * Compute and return the location of the icons origin, the
	 * location of origin of the text baseline, and a possibly clipped
	 * version of the compound labels string.  Locations are computed
	 * relative to the viewR rectangle.
	 * The JComponents orientation (LEADING/TRAILING) will also be taken
	 * into account and translated into LEFT/RIGHT values accordingly.
	 */
	public static String layoutCompoundLabel(
		JComponent c,
		FontMetrics fm,
		String text,
		Icon icon,
		int verticalAlignment,
		int horizontalAlignment,
		int verticalTextPosition,
		int horizontalTextPosition,
		Rectangle viewR,
		Rectangle iconR,
		Rectangle textR,
		int textIconGap) {
		boolean orientationIsLeftToRight = true;
		int hAlign = horizontalAlignment;
		int hTextPos = horizontalTextPosition;

		if (c != null) {
			if (!(c.getComponentOrientation().isLeftToRight())) {
				orientationIsLeftToRight = false;
			}
		}

		// Translate LEADING/TRAILING values in horizontalAlignment
		// to LEFT/RIGHT values depending on the components orientation
		switch (horizontalAlignment) {
			case SwingUtilities.LEADING :
				hAlign =
					(orientationIsLeftToRight)
						? SwingUtilities.LEFT
						: SwingUtilities.RIGHT;
				break;
			case SwingUtilities.TRAILING :
				hAlign =
					(orientationIsLeftToRight)
						? SwingUtilities.RIGHT
						: SwingUtilities.LEFT;
				break;
		}

		// Translate LEADING/TRAILING values in horizontalTextPosition
		// to LEFT/RIGHT values depending on the components orientation
		switch (horizontalTextPosition) {
			case SwingUtilities.LEADING :
				hTextPos =
					(orientationIsLeftToRight)
						? SwingUtilities.LEFT
						: SwingUtilities.RIGHT;
				break;
			case SwingUtilities.TRAILING :
				hTextPos =
					(orientationIsLeftToRight)
						? SwingUtilities.RIGHT
						: SwingUtilities.LEFT;
				break;
		}

		return layoutCompoundLabelImpl(
			c,
			fm,
			text,
			icon,
			verticalAlignment,
			hAlign,
			verticalTextPosition,
			hTextPos,
			viewR,
			iconR,
			textR,
			textIconGap);
	}

	protected void paintMenuItem(
		Graphics g,
		JComponent c,
		Icon checkIcon,
		Icon arrowIcon,
		Color background,
		Color foreground,
		int defaultTextIconGap) {
		JMenuItem b = (JMenuItem) c;
		ButtonModel model = b.getModel();

		//   Dimension size = b.getSize();
		int menuWidth = b.getWidth();
		int menuHeight = b.getHeight();
		Insets i = c.getInsets();

		resetRects();

		viewRect.setBounds(0, 0, menuWidth, menuHeight);

		viewRect.x += i.left;
		viewRect.y += i.top;
		viewRect.width -= (i.right + viewRect.x);
		viewRect.height -= (i.bottom + viewRect.y);
		
		// little hack
		viewRect.width += 16 + defaultTextIconGap;
		
		Font holdf = g.getFont();
		Font f = c.getFont();
		g.setFont(f);
		FontMetrics fm = g.getFontMetrics(f);
		FontMetrics fmAccel = g.getFontMetrics(acceleratorFont);

		// get Accelerator text
		KeyStroke accelerator = b.getAccelerator();
		String acceleratorText = "";
		if (accelerator != null) {
			int modifiers = accelerator.getModifiers();
			if (modifiers > 0) {
				acceleratorText = KeyEvent.getKeyModifiersText(modifiers);
				//acceleratorText += "-";
				acceleratorText += acceleratorDelimiter;
			}

			int keyCode = accelerator.getKeyCode();
			if (keyCode != 0) {
				acceleratorText += KeyEvent.getKeyText(keyCode);
			} else {
				acceleratorText += accelerator.getKeyChar();
			}
		}

		// layout the text and icon
		String text =
			layoutMenuItem(
				fm,
				b.getText(),
				fmAccel,
				acceleratorText,
				b.getIcon(),
				checkIcon,
				arrowIcon,
				b.getVerticalAlignment(),
				b.getHorizontalAlignment(),
				b.getVerticalTextPosition(),
				b.getHorizontalTextPosition(),
				viewRect,
				iconRect,
				textRect,
				acceleratorRect,
				checkIconRect,
				arrowIconRect,
				b.getText() == null ? 0 : defaultTextIconGap,
				defaultTextIconGap);

		// Paint background
		paintBackground(g, b, background);

		Color holdc = g.getColor();

		boolean buggy = false;
		
		// Paint the Check
		if (checkIcon != null) {
			if (model.isArmed()
				|| (c instanceof JMenu && model.isSelected())) {
				g.setColor(foreground);
			} else {
				g.setColor(holdc);
			}
			if (useCheckAndArrow())
			{
				checkIcon.paintIcon(c, g, checkIconRect.x, checkIconRect.y);
				buggy = true;
			}
			g.setColor(holdc);
		}
		

		// Paint the Icon
		if (b.getIcon() != null) {
			
			Icon icon;
			if (!model.isEnabled()) {
				icon = (Icon) b.getDisabledIcon();
			} else if (model.isPressed() && model.isArmed()) {
				icon = (Icon) b.getPressedIcon();
				if (icon == null) {
					// Use default icon
					icon = (Icon) b.getIcon();
				}
			} else {
				icon = (Icon) b.getIcon();
			}

			if (icon != null)
			{
				icon.paintIcon(c, g, iconRect.x, iconRect.y);
				buggy = true;
			}
		} 
		
		if ( buggy == false ) {
			textRect.x += 16 + defaultTextIconGap;
			
		}

		
		
		
		// Draw the Text
		if (text != null) {
			View v = (View) c.getClientProperty(BasicHTML.propertyKey);
			if (v != null) {
				v.paint(g, textRect);
			} else {
				paintText(g, b, textRect, text);
			}
		}

		// Draw the Accelerator Text
		if (acceleratorText != null && !acceleratorText.equals("")) {

			//Get the maxAccWidth from the parent to calculate the offset.
			int accOffset = 0;
			Container parent = menuItem.getParent();
			if (parent != null && parent instanceof JComponent) {
				JComponent p = (JComponent) parent;
				Integer maxValueInt =
					(Integer) p.getClientProperty(
						ThinColumbaMenuItemUI.MAX_ACC_WIDTH);
				int maxValue =
					maxValueInt != null
						? maxValueInt.intValue()
						: acceleratorRect.width;

				//Calculate the offset, with which the accelerator texts will be drawn with.
				accOffset = maxValue - acceleratorRect.width;
			}

			g.setFont(acceleratorFont);
			if (!model.isEnabled()) {
				// *** paint the acceleratorText disabled
				if (disabledForeground != null) {
					g.setColor(disabledForeground);
					BasicGraphicsUtils.drawString(
						g,
						acceleratorText,
						0,
						acceleratorRect.x - accOffset,
						acceleratorRect.y + fmAccel.getAscent());
				} else {
					g.setColor(b.getBackground().brighter());
					BasicGraphicsUtils.drawString(
						g,
						acceleratorText,
						0,
						acceleratorRect.x - accOffset,
						acceleratorRect.y + fmAccel.getAscent());
					g.setColor(b.getBackground().darker());
					BasicGraphicsUtils.drawString(
						g,
						acceleratorText,
						0,
						acceleratorRect.x - accOffset - 1,
						acceleratorRect.y + fmAccel.getAscent() - 1);
				}
			} else {
				// *** paint the acceleratorText normally
				if (model.isArmed()
					|| (c instanceof JMenu && model.isSelected())) {
					g.setColor(acceleratorSelectionForeground);
				} else {
					g.setColor(acceleratorForeground);
				}
				BasicGraphicsUtils.drawString(
					g,
					acceleratorText,
					0,
					acceleratorRect.x - accOffset,
					acceleratorRect.y + fmAccel.getAscent());
			}
		}

		// Paint the Arrow
		if (arrowIcon != null) {
			if (model.isArmed() || (c instanceof JMenu && model.isSelected()))
				g.setColor(foreground);
			if (useCheckAndArrow())
				arrowIcon.paintIcon(c, g, arrowIconRect.x, arrowIconRect.y);
		}
		g.setColor(holdc);
		g.setFont(holdf);
	}
	
	protected void paintBackground(java.awt.Graphics g, javax.swing.JMenuItem mi, java.awt.Color c) {
	Rectangle bnd = mi.getBounds();
	Color sc = g.getColor();
	g.setColor(c);
	g.fillRect(bnd.x, bnd.y, bnd.width, bnd.height);
	g.setColor(c);
    }

    protected void  paintText  (java.awt.Graphics g,javax.swing.JMenuItem mi ,java.awt.Rectangle tr,java.lang.String text) {
	g.setClip(tr);
	g.drawString(text, tr.x, tr.y+tr.height);
    }
}