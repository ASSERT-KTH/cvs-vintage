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
package org.columba.core.gui.themes.thincolumba;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

/**
 * @author fdietz
 *
 * Taken some ideas from www.jgoodies.com
 * 
 */
public class MenuRenderer {

	protected static final String HTML_KEY = BasicHTML.propertyKey;

	private static final String MAX_TEXT_WIDTH = "maxTextWidth";
	private static final String MAX_ACC_WIDTH = "maxAccWidth";

	static Rectangle zeroRect = new Rectangle(0, 0, 0, 0);
	static Rectangle iconRect = new Rectangle();
	static Rectangle textRect = new Rectangle();
	static Rectangle acceleratorRect = new Rectangle();
	static Rectangle checkIconRect = new Rectangle();
	static Rectangle arrowIconRect = new Rectangle();
	static Rectangle viewRect = new Rectangle(Short.MAX_VALUE, Short.MAX_VALUE);
	static Rectangle r = new Rectangle();

	private final JMenuItem menuItem;
	private final boolean iconBorderEnabled; // when selected or pressed.
	private final Font acceleratorFont;
	private final Color selectionForeground;
	private final Color disabledForeground;
	private final Color acceleratorForeground;
	private final Color acceleratorSelectionForeground;

	private final String acceleratorDelimiter;
	private final Icon fillerIcon;

	public MenuRenderer(
		JMenuItem menuItem,
		boolean iconBorderEnabled,
		Font acceleratorFont,
		Color selectionForeground,
		Color disabledForeground,
		Color acceleratorForeground,
		Color acceleratorSelectionForeground) {
		this.menuItem = menuItem;
		this.iconBorderEnabled = iconBorderEnabled;
		this.acceleratorFont = acceleratorFont;
		this.selectionForeground = selectionForeground;
		this.disabledForeground = disabledForeground;
		this.acceleratorForeground = acceleratorForeground;
		this.acceleratorSelectionForeground = acceleratorSelectionForeground;
		this.acceleratorDelimiter =
			UIManager.getString("MenuItem.acceleratorDelimiter");
		this.fillerIcon = new DefaultMenuIcon();
	}

	private Icon getIcon(JMenuItem aMenuItem, Icon defaultIcon) {
		Icon icon = aMenuItem.getIcon();
		if (icon == null)
			return defaultIcon;

		ButtonModel model = aMenuItem.getModel();
		if (!model.isEnabled()) {
			return model.isSelected()
				? aMenuItem.getDisabledSelectedIcon()
				: aMenuItem.getDisabledIcon();
		} else if (model.isPressed() && model.isArmed()) {
			Icon pressedIcon = aMenuItem.getPressedIcon();
			return pressedIcon != null ? pressedIcon : icon;
		} else if (model.isSelected()) {
			Icon selectedIcon = aMenuItem.getSelectedIcon();
			return selectedIcon != null ? selectedIcon : icon;
		} else
			return icon;
	}

	private boolean hasCustomIcon() {
		return getIcon(menuItem, null) != null;
	}

	private Icon getWrappedIcon(Icon icon) {
		if (icon == null)
			return fillerIcon;
		return iconBorderEnabled
			&& hasCustomIcon()
				? new DefaultCheckIcon(icon, menuItem)
				: new DefaultMenuIcon(icon);
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

	public Dimension getPreferredMenuItemSize(
		JComponent c,
		Icon checkIcon,
		Icon arrowIcon,
		int defaultTextIconGap) {

		JMenuItem b = (JMenuItem) c;
		String text = b.getText();
		KeyStroke accelerator = b.getAccelerator();
		String acceleratorText = "";

		if (accelerator != null) {
			int modifiers = accelerator.getModifiers();
			if (modifiers > 0) {
				acceleratorText = KeyEvent.getKeyModifiersText(modifiers);
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
		FontMetrics fm = b.getFontMetrics(font);
		FontMetrics fmAccel = b.getFontMetrics(acceleratorFont);

		resetRects();

		Icon wrappedIcon = getWrappedIcon(getIcon(menuItem, checkIcon));

		layoutMenuItem(
			fm,
			text,
			fmAccel,
			acceleratorText,
			null,
			wrappedIcon,
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

		r.setBounds(textRect);
		r =
			SwingUtilities.computeUnion(
				iconRect.x,
				iconRect.y,
				iconRect.width,
				iconRect.height,
				r);

		Container parent = menuItem.getParent();

		if (parent != null
			&& parent instanceof JComponent
			&& !(menuItem instanceof JMenu
				&& ((JMenu) menuItem).isTopLevelMenu())) {
			JComponent p = (JComponent) parent;

			Integer maxTextWidth =
				(Integer) p.getClientProperty(MAX_TEXT_WIDTH);
			Integer maxAccWidth = (Integer) p.getClientProperty(MAX_ACC_WIDTH);

			int maxTextValue =
				maxTextWidth != null ? maxTextWidth.intValue() : 0;
			int maxAccValue = maxAccWidth != null ? maxAccWidth.intValue() : 0;

			if (r.width < maxTextValue) {
				r.width = maxTextValue;
			} else {
				p.putClientProperty(MAX_TEXT_WIDTH, new Integer(r.width));
			}

			if (acceleratorRect.width > maxAccValue) {
				maxAccValue = acceleratorRect.width;
				p.putClientProperty(
					MAX_ACC_WIDTH,
					new Integer(acceleratorRect.width));
			}

			r.width += maxAccValue;
			r.width += defaultTextIconGap;
		}

		if (useCheckAndArrow()) {

			r.width += checkIconRect.width;
			r.width += defaultTextIconGap;

			r.width += defaultTextIconGap;
			r.width += arrowIconRect.width;
		}

		r.width += 2 * defaultTextIconGap;

		Insets insets = b.getInsets();
		if (insets != null) {
			r.width += insets.left + insets.right;
			r.height += insets.top + insets.bottom;
		}

		if (r.height % 2 == 1) {
			r.height++;
		}
		return r.getSize();
	}

	public void paintMenuItem(
		Graphics g,
		JComponent c,
		Icon checkIcon,
		Icon arrowIcon,
		Color background,
		Color foreground,
		int defaultTextIconGap) {
		JMenuItem b = (JMenuItem) c;
		ButtonModel model = b.getModel();

		int menuWidth = b.getWidth();
		int menuHeight = b.getHeight();
		Insets i = c.getInsets();

		resetRects();

		viewRect.setBounds(0, 0, menuWidth, menuHeight);

		viewRect.x += i.left;
		viewRect.y += i.top;
		viewRect.width -= (i.right + viewRect.x);
		viewRect.height -= (i.bottom + viewRect.y);

		Font holdf = g.getFont();
		Font f = c.getFont();
		g.setFont(f);
		FontMetrics fm = g.getFontMetrics(f);
		FontMetrics fmAccel = g.getFontMetrics(acceleratorFont);

		KeyStroke accelerator = b.getAccelerator();
		String acceleratorText = "";
		if (accelerator != null) {
			int modifiers = accelerator.getModifiers();
			if (modifiers > 0) {
				acceleratorText = KeyEvent.getKeyModifiersText(modifiers);
				acceleratorText += acceleratorDelimiter;
			}

			int keyCode = accelerator.getKeyCode();
			if (keyCode != 0) {
				acceleratorText += KeyEvent.getKeyText(keyCode);
			} else {
				acceleratorText += accelerator.getKeyChar();
			}
		}

		Icon wrappedIcon = getWrappedIcon(getIcon(menuItem, checkIcon));

		String text =
			layoutMenuItem(
				fm,
				b.getText(),
				fmAccel,
				acceleratorText,
				null,
				wrappedIcon,
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

		paintBackground(g, b, background);

		Color holdc = g.getColor();
		if (model.isArmed() || (c instanceof JMenu && model.isSelected())) {
			g.setColor(foreground);
		}
		wrappedIcon.paintIcon(c, g, checkIconRect.x, checkIconRect.y);
		g.setColor(holdc);

		if (text != null) {
			View v = (View) c.getClientProperty(HTML_KEY);
			if (v != null) {
				v.paint(g, textRect);
			} else {
				paintText(g, b, textRect, text);
			}
		}

		if (acceleratorText != null && !acceleratorText.equals("")) {

			int accOffset = 0;
			Container parent = menuItem.getParent();
			if (parent != null && parent instanceof JComponent) {
				JComponent p = (JComponent) parent;
				Integer maxValueInt =
					(Integer) p.getClientProperty(MAX_ACC_WIDTH);
				int maxValue =
					maxValueInt != null
						? maxValueInt.intValue()
						: acceleratorRect.width;

				accOffset = maxValue - acceleratorRect.width;
			}

			g.setFont(acceleratorFont);
			if (!model.isEnabled()) {

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

		if (arrowIcon != null) {
			if (model.isArmed() || (c instanceof JMenu && model.isSelected()))
				g.setColor(foreground);
			if (useCheckAndArrow())
				arrowIcon.paintIcon(c, g, arrowIconRect.x, arrowIconRect.y);
		}
		g.setColor(holdc);
		g.setFont(holdf);
	}

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
		Rectangle viewRectangle,
		Rectangle iconRectangle,
		Rectangle textRectangle,
		Rectangle acceleratorRectangle,
		Rectangle checkIconRectangle,
		Rectangle arrowIconRectangle,
		int textIconGap,
		int menuItemGap) {

		SwingUtilities.layoutCompoundLabel(
			menuItem,
			fm,
			text,
			icon,
			verticalAlignment,
			horizontalAlignment,
			verticalTextPosition,
			horizontalTextPosition,
			viewRectangle,
			iconRectangle,
			textRectangle,
			textIconGap);

		if ((acceleratorText == null) || acceleratorText.equals("")) {
			acceleratorRectangle.width = acceleratorRectangle.height = 0;
			acceleratorText = "";
		} else {
			acceleratorRectangle.width =
				SwingUtilities.computeStringWidth(fmAccel, acceleratorText);
			acceleratorRectangle.height = fmAccel.getHeight();
		}

		boolean useCheckAndArrow = useCheckAndArrow();

		if (useCheckAndArrow) {
			if (checkIcon != null) {
				checkIconRectangle.width = checkIcon.getIconWidth();
				checkIconRectangle.height = checkIcon.getIconHeight();
			} else {
				checkIconRectangle.width = checkIconRectangle.height = 0;
			}

			if (arrowIcon != null) {
				arrowIconRectangle.width = arrowIcon.getIconWidth();
				arrowIconRectangle.height = arrowIcon.getIconHeight();
			} else {
				arrowIconRectangle.width = arrowIconRectangle.height = 0;
			}
		}

		Rectangle labelRect = iconRectangle.union(textRectangle);
		if (isLeftToRight(menuItem)) {
			textRectangle.x += menuItemGap;
			iconRectangle.x += menuItemGap;

			acceleratorRectangle.x =
				viewRectangle.x
					+ viewRectangle.width
					- arrowIconRectangle.width
					- menuItemGap
					- acceleratorRectangle.width;

			if (useCheckAndArrow) {
				checkIconRectangle.x = viewRectangle.x;

				textRectangle.x += menuItemGap + checkIconRectangle.width;
				iconRectangle.x += menuItemGap + checkIconRectangle.width;
				arrowIconRectangle.x =
					viewRectangle.x
						+ viewRectangle.width
						- menuItemGap
						- arrowIconRectangle.width;
			}
		} else {
			textRectangle.x -= menuItemGap;
			iconRectangle.x -= menuItemGap;

			acceleratorRectangle.x =
				viewRectangle.x + arrowIconRectangle.width + menuItemGap;

			if (useCheckAndArrow) {

				checkIconRectangle.x =
					viewRectangle.x
						+ viewRectangle.width
						- checkIconRectangle.width;
				textRectangle.x -= menuItemGap + checkIconRectangle.width;
				iconRectangle.x -= menuItemGap + checkIconRectangle.width;
				arrowIconRectangle.x = viewRectangle.x + menuItemGap;
			}
		}

		acceleratorRectangle.y =
			labelRect.y
				+ (labelRect.height / 2)
				- (acceleratorRectangle.height / 2);
		if (useCheckAndArrow) {
			arrowIconRectangle.y =
				labelRect.y
					+ (labelRect.height / 2)
					- (arrowIconRectangle.height / 2);
			checkIconRectangle.y =
				labelRect.y
					+ (labelRect.height / 2)
					- (checkIconRectangle.height / 2);
		}

		return text;
	}

	private boolean useCheckAndArrow() {
		boolean isTopLevelMenu =
			menuItem instanceof JMenu && ((JMenu) menuItem).isTopLevelMenu();
		return !isTopLevelMenu;
	}

	private boolean isLeftToRight(Component c) {
		return c.getComponentOrientation().isLeftToRight();
	}

	// Copies from 1.4.1 ****************************************************    

	/**
	 * Draws the background of the menu item.
	 * Copied from 1.4.1 BasicMenuItem to make it visible to the
	 * MenuItemLayouter
	 * 
	 * @param g the paint graphics
	 * @param aMenuItem menu item to be painted
	 * @param bgColor selection background color
	 * @since 1.4
	 */
	public void paintBackground(
		Graphics g,
		JMenuItem aMenuItem,
		Color bgColor) {
		ButtonModel model = aMenuItem.getModel();

		if (aMenuItem.isOpaque()) {
			int menuWidth = aMenuItem.getWidth();
			int menuHeight = aMenuItem.getHeight();
			Color c =
				model.isArmed()
					|| (aMenuItem instanceof JMenu && model.isSelected())
						? bgColor
						: aMenuItem.getBackground();
			Color oldColor = g.getColor();
			g.setColor(c);
			g.fillRect(0, 0, menuWidth, menuHeight);
			g.setColor(oldColor);
		}
	}

	/**
	 * Renders the text of the current menu item.
	 * <p>
	 * @param g graphics context
	 * @param aMenuItem menu item to render
	 * @param textRectangle bounding rectangle for rendering the text
	 * @param text string to render
	 * @since 1.4
	 */
	public void paintText(
		Graphics g,
		JMenuItem aMenuItem,
		Rectangle textRectangle,
		String text) {
		ButtonModel model = aMenuItem.getModel();
		FontMetrics fm = g.getFontMetrics();
		int mnemIndex = getDisplayedMnemonicIndex(aMenuItem);

		if (!model.isEnabled()) {
			// *** paint the text disabled
			if (UIManager.get("MenuItem.disabledForeground")
				instanceof Color) {
				g.setColor(UIManager.getColor("MenuItem.disabledForeground"));
				drawStringUnderlineCharAt(
					g,
					text,
					mnemIndex,
					textRectangle.x,
					textRectangle.y + fm.getAscent());
			} else {
				g.setColor(aMenuItem.getBackground().brighter());
				drawStringUnderlineCharAt(
					g,
					text,
					mnemIndex,
					textRectangle.x,
					textRectangle.y + fm.getAscent());
				g.setColor(aMenuItem.getBackground().darker());
				drawStringUnderlineCharAt(
					g,
					text,
					mnemIndex,
					textRectangle.x - 1,
					textRectangle.y + fm.getAscent() - 1);
			}
		} else {
			// *** paint the text normally
			if (model.isArmed()
				|| (aMenuItem instanceof JMenu && model.isSelected())) {
				g.setColor(selectionForeground); // Uses protected field.
			}
			drawStringUnderlineCharAt(
				g,
				text,
				mnemIndex,
				textRectangle.x,
				textRectangle.y + fm.getAscent());
		}
	}

	/**
	 * Draws a string with the graphics <code>g</code> at location
	 * (<code>x</code>, <code>y</code>)
	 * just like <code>g.drawString</code> would.
	 * The character at index <code>underlinedIndex</code>
	 * in text will be underlined. If <code>index</code> is beyond the
	 * bounds of <code>text</code> (including < 0), nothing will be
	 * underlined.
	 * 
	 * @param g Graphics to draw with
	 * @param text String to draw
	 * @param underlinedIndex Index of character in text to underline
	 * @param x x coordinate to draw at
	 * @param y y coordinate to draw at
	 * @since 1.4
	 */

	public static void drawStringUnderlineCharAt(
		Graphics g,
		String text,
		int underlinedIndex,
		int x,
		int y) {
		g.drawString(text, x, y);
		if (underlinedIndex >= 0 && underlinedIndex < text.length()) {
			FontMetrics fm = g.getFontMetrics();
			int underlineRectX =
				x + fm.stringWidth(text.substring(0, underlinedIndex));
			int underlineRectY = y;
			int underlineRectWidth = fm.charWidth(text.charAt(underlinedIndex));
			int underlineRectHeight = 1;
			g.fillRect(
				underlineRectX,
				underlineRectY + fm.getDescent() - 1,
				underlineRectWidth,
				underlineRectHeight);
		}
	}

	private static int getDisplayedMnemonicIndex(JMenuItem menuItem) {
		try {
			Method method =
				AbstractButton
					.class
					.getMethod("getDisplayedMnemonicIndex", new Class[] {
			});
			Integer result = (Integer) method.invoke(menuItem, new Object[] {
			});
			return result.intValue();
		} catch (NoSuchMethodException e) {
		} catch (InvocationTargetException e) {
		} catch (IllegalAccessException e) {
		}
		Object value = menuItem.getClientProperty("displayedMnemonicIndex");
		return (value != null && value instanceof Integer)
			? ((Integer) value).intValue()
			: findDisplayedMnemonicIndex(
				menuItem.getText(),
				menuItem.getMnemonic());
	}

	/**
	* Returns index of the first occurrence of <code>mnemonic</code>
	* within string <code>text</code>. Matching algorithm is not
	* case-sensitive.
	* <p>
	*
	* @param text The text to search through, may be null
	* @param mnemonic The mnemonic to find the character for.
	* @return index into the string if exists, otherwise -1
	*/

	private static int findDisplayedMnemonicIndex(String text, int mnemonic) {
		if (text == null || mnemonic == '\0') {
			return -1;
		}

		char uc = Character.toUpperCase((char) mnemonic);
		char lc = Character.toLowerCase((char) mnemonic);

		int uci = text.indexOf(uc);
		int lci = text.indexOf(lc);

		if (uci == -1) {
			return lci;
		} else if (lci == -1) {
			return uci;
		} else {
			return (lci < uci) ? lci : uci;
		}
	}

}
