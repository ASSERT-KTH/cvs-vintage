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

package org.columba.mail.gui.table;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.UIManager;

public class HeaderTableCommonRenderer extends DefaultLabelRenderer {

	
	private JTree tree;

	private Font plainFont, boldFont;

	

	public HeaderTableCommonRenderer(JTree tree) {
		super();
		this.tree = tree;
		
		boldFont = UIManager.getFont("Tree.font");
		boldFont = boldFont.deriveFont(Font.BOLD);

		plainFont = UIManager.getFont("Tree.font");

	}

	public void updateUI() {
		super.updateUI();

		boldFont = UIManager.getFont("Tree.font");
		boldFont = boldFont.deriveFont(Font.BOLD);

		plainFont = UIManager.getFont("Tree.font");
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

		String str = null;
		try {
			str = (String) value;
		} catch (ClassCastException ex) {
			System.out.println("headertablecommonrenderer: " + ex.getMessage());
			str = new String();
		}

		setText(str);
		return this;
	}

}