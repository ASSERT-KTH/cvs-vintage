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


package org.columba.thincolumba;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicMenuItemUI;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ThinMenuItemUI extends BasicMenuItemUI {

	public static ComponentUI createUI(JComponent c) {
		return new ThinMenuItemUI();
	}

	
	
	public void paint(Graphics g, JComponent c) {
		ThinUtilities.enableAntiAliasing(g);

		super.paint(g, c);
	}

	private static final int MINIMUM_WIDTH = 80;

	private MenuRenderer renderer;

	protected void installDefaults() {
		super.installDefaults();
		renderer =
			new MenuRenderer(
				menuItem,
				iconBorderEnabled(),
				acceleratorFont,
				selectionForeground,
				disabledForeground,
				acceleratorForeground,
				acceleratorSelectionForeground);
		Integer gap =
			(Integer) UIManager.get("MenuItem.textIconGap");
		defaultTextIconGap = gap != null ? gap.intValue() : 2;
	}

	// RadioButtonMenuItems and CheckBoxMenuItems will override
	protected boolean iconBorderEnabled() {
		return false;
	}

	protected void uninstallDefaults() {
		super.uninstallDefaults();
		renderer = null;
	}

	
	protected Dimension getPreferredMenuItemSize(
		JComponent c,
		Icon aCheckIcon,
		Icon anArrowIcon,
		int textIconGap) {
		Dimension size =
			renderer.getPreferredMenuItemSize(
				c,
				aCheckIcon,
				anArrowIcon,
				textIconGap);
		int width = Math.max(MINIMUM_WIDTH, size.width);
		int height = size.height;
		return new Dimension(width, height);
	}

	protected void paintMenuItem(
		Graphics g,
		JComponent c,
		Icon aCheckIcon,
		Icon anArrowIcon,
		Color background,
		Color foreground,
		int textIconGap) {
		renderer.paintMenuItem(
			g,
			c,
			aCheckIcon,
			anArrowIcon,
			background,
			foreground,
			textIconGap);
	}
	

}
