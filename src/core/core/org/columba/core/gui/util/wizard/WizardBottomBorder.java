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

package org.columba.core.gui.util.wizard;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;

public class WizardBottomBorder extends AbstractBorder {
	protected Insets borderInsets = new Insets(0, 0, 0, 2);
	
		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
		g.setColor(UIManager.getColor("Button.darkShadow"));
		g.drawLine(x, y + h - 2, x + w - 1, y + h - 2);
		g.setColor(Color.white);
		g.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
	}
	
	public Insets getBorderInsets(Component c) {
		return borderInsets;
	}
}

