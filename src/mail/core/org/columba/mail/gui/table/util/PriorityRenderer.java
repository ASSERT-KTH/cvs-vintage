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

package org.columba.mail.gui.table.util;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.JTree;

import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.gui.table.DefaultLabelRenderer;

public class PriorityRenderer extends DefaultLabelRenderer {

	private ImageIcon image1 =
		ImageLoader.getSmallImageIcon("priority-high.png");
	private ImageIcon image2 = null;
	private ImageIcon image3 = null;
	private ImageIcon image4 =
		ImageLoader.getSmallImageIcon("priority-low.png");

	public PriorityRenderer(JTree tree, boolean isBordered) {
		super(tree);

		//setOpaque(true); //MUST do this for background to show up.

	}

	public Component getTableCellRendererComponent(
		JTable table,
		Object value,
		boolean isSelected,
		boolean hasFocus,
		int row,
		int column) {

		super.getTableCellRendererComponent(
			table,
			value,
			isSelected,
			hasFocus,
			row,
			column);

		if (value == null) {
			setText("");
			return this;
		}

		Integer in = (Integer) value;
		if (in == null)
			return this;

		int i = in.intValue();

		if (i == 1) {
			//setForeground( Color.red );
			//setText("!!");
			setIcon(image1);

			//setToolTipText("Highest Priority");
		} else if (i == 2) {
			//setForeground( Color.red );
			setIcon(image2);
			//setToolTipText("High Priority");
			//setText("!");
		} else if (i == 3)
			setIcon(null);
		else if (i == 4) {
			//eteTextForeground( Color.blue );
			setIcon(image3);
			//setToolTipText("Low Priority");
			//setText("!");
		} else if (i == 5) {
			//setForeground( Color.blue );
			setIcon(image4);
			// setToolTipText("Lowest Priority");
			//setText("!!");
		}

		return this;
	}
}
