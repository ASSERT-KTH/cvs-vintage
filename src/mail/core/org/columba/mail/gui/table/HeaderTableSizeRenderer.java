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

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreePath;

import org.columba.mail.gui.table.util.MessageNode;
import org.columba.mail.message.HeaderInterface;

public class HeaderTableSizeRenderer
	extends JLabel
	implements TableCellRenderer {
	private Border unselectedBorder = null;
	private Border selectedBorder = null;
	private boolean isBordered = true;
	private JTree tree;
	private Font plainFont, boldFont;

	public HeaderTableSizeRenderer(JTree tree) {
		super();
		this.tree = tree;
		this.isBordered = true;

		setHorizontalAlignment(SwingConstants.RIGHT);

		setOpaque(true); //MUST do this for background to show up.

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
		if (isBordered) {
			if (isSelected) {
				if (selectedBorder == null) {
					selectedBorder =
						BorderFactory.createMatteBorder(
							2,
							5,
							2,
							5,
							table.getSelectionBackground());
				}
				//setBorder(selectedBorder);
				setBackground(table.getSelectionBackground());
				setForeground(table.getSelectionForeground());
			} else {
				if (unselectedBorder == null) {
					unselectedBorder =
						BorderFactory.createMatteBorder(
							2,
							5,
							2,
							5,
							table.getBackground());
				}
				setBackground(table.getBackground());
				//setBorder(unselectedBorder);
				setForeground(table.getForeground());
			}
		}

		if (value == null) {
			setText("");
			return this;
		}

		TreePath path = tree.getPathForRow(row);
		MessageNode messageNode = (MessageNode) path.getLastPathComponent();

		HeaderInterface header = messageNode.getHeader();
		if (header == null) {
			System.out.println("header is null");
			return this;
		}

		if (header.getFlags() != null) {
			if (header.getFlags().getRecent()) {
				setFont(boldFont);
			} else {
				setFont(plainFont);
			}
		}

		setText(value.toString() + "KB");

		return this;
	}
}
