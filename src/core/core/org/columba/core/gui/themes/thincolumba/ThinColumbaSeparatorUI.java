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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ThinColumbaSeparatorUI
	extends javax.swing.plaf.basic.BasicToolBarSeparatorUI {

	public ThinColumbaSeparatorUI() {
		shadow = UIManager.getColor("controlDkShadow");
		highlight = UIManager.getColor("controlLtHighlight");
	}

	public static ComponentUI createUI(JComponent c) {
		return new ThinColumbaSeparatorUI();
	}

	public void paint(Graphics g, JComponent c) {
		Dimension s = c.getSize();
		int sWidth = s.width / 2;

		g.setColor(shadow);
		g.drawLine(sWidth, 0, sWidth, s.height);

		g.setColor(highlight);
		g.drawLine(sWidth + 1, 0, sWidth + 1, s.height);
	}
}
