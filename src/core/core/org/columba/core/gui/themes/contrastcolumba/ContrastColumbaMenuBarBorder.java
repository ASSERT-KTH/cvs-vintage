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

public class ContrastColumbaMenuBarBorder
	extends AbstractBorder
	implements UIResource
{
	protected static Insets borderInsets = new Insets(2, 2, 2, 2);

	protected static Color darkShadow = Color.black;
	protected static Color lightShadow = MetalLookAndFeel.getControlHighlight();
	protected static Color mediumShadow = MetalLookAndFeel.getControlDarkShadow();

	public ContrastColumbaMenuBarBorder()
	{

	}

	public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
	    ContrastColumbaGraphicUtils.drawBorder(g, x, y, w, h,
					lightShadow, mediumShadow, darkShadow,
					true);
        }
        
	public Insets getBorderInsets(Component c)
	{
		return borderInsets;
	}

}










