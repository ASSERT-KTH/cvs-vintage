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
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.UIManager;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DefaultCheckIcon extends DefaultMenuIcon {
	private final JMenuItem menuItem;

	public DefaultCheckIcon(Icon icon, JMenuItem menuItem) {
		super(icon);
		this.menuItem = menuItem;
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		paintState(g, x, y);
		super.paintIcon(c, g, x, y);
	}

	private void paintState(Graphics g, int x, int y) {
		ButtonModel model = menuItem.getModel();
		

		int w = getIconWidth();
		int h = getIconHeight();

		g.translate(x, y);
		if (model.isSelected() || model.isArmed() 
			) {
			Color background =
				model.isArmed()
					? UIManager.getColor("MenuItem.background")
					: UIManager.getColor("ScrollBar.track");
			Color upColor = UIManager.getColor("controlLtHighlight");
			Color downColor = UIManager.getColor("controlDkShadow");

			// Background
			g.setColor(background);
			g.fillRect(0, 0, w, h);
			// Top and left border
			g.setColor(model.isSelected() ? downColor : upColor);
			g.drawLine(0, 0, w - 2, 0);
			g.drawLine(0, 0, 0, h - 2);
			// Bottom and right border
			g.setColor(model.isSelected() ? upColor : downColor);
			g.drawLine(0, h - 1, w - 1, h - 1);
			g.drawLine(w - 1, 0, w - 1, h - 1);
		}
		g.translate(-x, -y);
		g.setColor(UIManager.getColor("textText"));
	}

}
