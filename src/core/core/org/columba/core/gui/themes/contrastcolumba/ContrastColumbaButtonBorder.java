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

package org.columba.core.gui.themes.contrastcolumba;

import javax.swing.*;
import java.awt.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;
import org.columba.mail.gui.util.*;

import javax.swing.border.*;

public class ContrastColumbaButtonBorder
	extends AbstractBorder
	implements UIResource
{
	protected Color focus = UIManager.getColor("Button.focus");
	protected Color darkShadow = Color.black ;
	protected Color lightShadow = MetalLookAndFeel.getControlHighlight();
	protected Color mediumShadow = MetalLookAndFeel.getControlDarkShadow();

	public ContrastColumbaButtonBorder()
	{
		
	}

	public void paintBorder(Component c, Graphics g, int x, int y, int w, int h)
	{
		boolean isPressed = false;
		boolean hasFocus = false;
		boolean canBeDefault = false;
		boolean isDefault = false;

		if (c instanceof AbstractButton)
		{
			AbstractButton b = (AbstractButton) c;
			ButtonModel model = b.getModel();

			isPressed = (model.isArmed() && model.isPressed());
			hasFocus =
				(model.isArmed() && isPressed) || (b.isFocusPainted() && b.hasFocus());
			if (b instanceof JButton)
			{
				canBeDefault = ((JButton) b).isDefaultCapable();
				isDefault = ((JButton) b).isDefaultButton();
			}
		}
		int bx1 = x + 1;
		int by1 = y + 1;
		int bx2 = x + w - 2;
		int by2 = y + h - 2;

		if (canBeDefault)
		{
			if (isDefault)
			{
				ContrastColumbaGraphicUtils.drawBorder(
					g,
					x + 3,
					y + 3,
					w - 6,
					h - 6,
					lightShadow,
					mediumShadow,
					darkShadow,
					false);
			}
			bx1 += 8;
			by1 += 8;
			bx2 -= 8;
			by2 -= 8;
		}

		/*
		if (hasFocus)
		{
			g.setColor(focus);
			g.drawRect(bx1, by1, bx2 - bx1, by2 - by1);
			bx1++;
			by1++;
			bx2--;
			by2--;
		}
		*/

		if (isPressed)
		{
			g.setColor(mediumShadow);
			g.drawLine(bx1, by1, bx2, by1);
			g.drawLine(bx1, by1, bx1, by2);
			g.setColor(lightShadow);
			g.drawLine(bx2, by1 + 1, bx2, by2);
			g.drawLine(bx1 + 1, by2, bx2, by2);
			g.setColor(darkShadow);
			g.drawLine(bx1 + 1, by1 + 1, bx2 - 2, by1 + 1);
			g.drawLine(bx1 + 1, by1 + 1, bx1 + 1, by2 - 1);
		}
		else
		{
			g.setColor(lightShadow);
			g.drawLine(bx1, by1, bx2 - 1, by1);
			g.drawLine(bx1, by1, bx1, by2 - 1);
			g.setColor(darkShadow);
			g.drawLine(bx2, by1, bx2, by2);
			g.drawLine(bx1, by2, bx2, by2);
			g.setColor(mediumShadow);
			g.drawLine(bx1 + 2, by2 - 1, bx2 - 1, by2 - 1);
			g.drawLine(bx2 - 1, by1 + 2, bx2 - 1, by2 - 1);
		}

	}

	public Insets getBorderInsets(Component c)
	{
		if (c instanceof JButton)
		{
			JButton b = (JButton) c;
			return (
				b.isDefaultCapable() ? new Insets(10, 10, 10, 10) : new Insets(2, 2, 2, 2));
		}
		return new Insets(2, 2, 2, 2);
	}

	
}










