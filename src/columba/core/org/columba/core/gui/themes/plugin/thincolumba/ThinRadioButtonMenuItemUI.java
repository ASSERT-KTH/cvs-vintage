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
package org.columba.core.gui.themes.plugin.thincolumba;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicRadioButtonMenuItemUI;

import org.columba.core.gui.util.EmptyIcon;
import org.columba.core.gui.util.ImageLoader;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ThinRadioButtonMenuItemUI extends BasicRadioButtonMenuItemUI {

	public static EmptyIcon emptyIcon = new EmptyIcon();
	
	public static ComponentUI createUI(JComponent c) {
		return new ThinRadioButtonMenuItemUI();
	}

	protected Dimension getPreferredMenuItemSize(
		JComponent c,
		Icon checkIcon,
		Icon arrowIcon,
		int defaultTextIconGap) {
		JMenuItem b = (JMenuItem) c;
		Icon icon = (Icon) b.getIcon();

		if (icon == null)
			b.setIcon(new EmptyIcon());
		Dimension d =
			super.getPreferredMenuItemSize(
				c,
				checkIcon,
				arrowIcon,
				defaultTextIconGap);

		
		
		return d;
	}
	
	public void paint(Graphics g, JComponent c) {
		JRadioButtonMenuItem item = (JRadioButtonMenuItem) c;
		
		ThinUtilities.enableAntiAliasing(g);
		
		
		if ( item.isSelected() )
		{
			item.setIcon( ImageLoader.getSmallImageIcon("radiobutton.png") );
		}
		else
		{
			item.setIcon( emptyIcon );
		}
		super.paint(g, c);
	}
}
