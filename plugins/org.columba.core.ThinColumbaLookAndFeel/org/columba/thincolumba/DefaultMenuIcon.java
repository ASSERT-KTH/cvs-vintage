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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Icon;


/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DefaultMenuIcon implements Icon {
	private final Icon icon;
	private final int width;
	private final int height;
	private final int xOffset;
	private final int yOffset;

	public DefaultMenuIcon() {
		this(null);
	}

	public DefaultMenuIcon(Icon icon) {
		Dimension minimumSize = new Dimension(16,16);
		this.icon = icon;
		int iconWidth = icon == null ? 0 : icon.getIconWidth();
		int iconHeight = icon == null ? 0 : icon.getIconHeight();
		width = Math.max(iconWidth, Math.max(20, minimumSize.width));
		height = Math.max(iconHeight, Math.max(20, minimumSize.height));
		xOffset = Math.max(0, (width - iconWidth) / 2);
		yOffset = Math.max(0, (height - iconHeight) / 2);
	}

	public int getIconHeight() {
		return height;
	}
	public int getIconWidth() {
		return width;
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		if (icon != null)
			icon.paintIcon(c, g, x + xOffset, y + yOffset);
	}
}
