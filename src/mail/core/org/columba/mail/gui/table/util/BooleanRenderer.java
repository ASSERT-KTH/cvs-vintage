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
import javax.swing.SwingConstants;

import org.columba.mail.gui.table.DefaultLabelRenderer;

public class BooleanRenderer extends DefaultLabelRenderer {

	boolean bool;
	//String str;
	ImageIcon image;
	String key;
	
	public BooleanRenderer(JTree tree, boolean bool, ImageIcon image, String key) {
		super(tree);
	
		this.bool = bool;
		//this.str = str;
		this.image = image;
		this.key = key;
		setHorizontalAlignment(SwingConstants.CENTER);
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
			setIcon(null);
			return this;
		}

		if (value instanceof String)
			return this;

		
		if (bool == true) {
			Boolean b = (Boolean) ( (MessageNode)value).getHeader().get(key);
			if (b == null)
				return this;

			if (b.equals(Boolean.TRUE)) {
				setIcon(image);
			} else {
				setIcon(null);
			}
		} else {
			Boolean b = (Boolean) value;
			if (b == null)
				return this;

			if (b.equals(Boolean.FALSE)) {
				setIcon(image);
			} else {
				setIcon(null);
			}
		}
		
		return this;
	}
}
