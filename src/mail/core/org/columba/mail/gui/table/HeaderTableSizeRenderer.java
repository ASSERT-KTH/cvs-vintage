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
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.columba.mail.gui.table.util.MessageNode;

public class HeaderTableSizeRenderer extends DefaultLabelRenderer {

	private JTree tree;
	private Font plainFont, boldFont;

	public HeaderTableSizeRenderer(JTree tree) {
		super(tree);
		this.tree = tree;

		setHorizontalAlignment(SwingConstants.RIGHT);

		//setOpaque(true); //MUST do this for background to show up.

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

		

		if (value == null) {
			setText("");
			return this;
		}

		setText( ((MessageNode)value).getHeader().get("columba.size")  + "KB");

		return super.getTableCellRendererComponent(
		table,
		value,
		isSelected,
		hasFocus,
		row,
		column);
	}
}
