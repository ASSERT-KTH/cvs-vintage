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

import javax.swing.plaf.metal.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;

/**
 * Metal's split pane divider
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases.  The current serialization support is appropriate
 * for short term storage or RMI between applications running the same
 * version of Swing.  A future release of Swing will provide support for
 * long term persistence.
 *
 * @version 1.14 02/02/00
 * @author Steve Wilson
 * @author Ralph kar
 */
class ThinColumbaSplitPaneDivider extends BasicSplitPaneDivider
{
	
	
	

	private int inset = 2;

	private Color controlColor = MetalLookAndFeel.getControl();
	private Color primaryControlColor = MetalLookAndFeel.getPrimaryControl();

	public ThinColumbaSplitPaneDivider(BasicSplitPaneUI ui)
	{
		super(ui);
		//setLayout(new ColumbaDividerLayout());
	}

	public void paint(Graphics g)
	{
		
		if (splitPane.hasFocus())
		{
			//usedBumps = focusBumps;
			g.setColor(primaryControlColor);
		}
		else
		{
			//usedBumps = bumps;
			g.setColor(controlColor);
		}

		Rectangle clip = g.getClipBounds();
		Insets insets = getInsets();
		g.fillRect(clip.x, clip.y, clip.width, clip.height);
		Dimension size = getSize();
		size.width -= inset * 2;
		size.height -= inset * 2;
		int drawX = inset;
		int drawY = inset;
		if (insets != null)
		{
			size.width -= (insets.left + insets.right);
			size.height -= (insets.top + insets.bottom);
			drawX += insets.left;
			drawY += insets.top;
		}
		//usedBumps.setBumpArea(size);
		//usedBumps.paintIcon(this, g, drawX, drawY);
		//super.paint(g);

		/*
		    // Paint the border.
		    Border  border = new ColumbaSplitPaneBorder();
		    
		    if (border != null)
		    {
		    border.paintBorder(this, g, 0, 0, size.width, size.height);
		    }
		*/
	}
}