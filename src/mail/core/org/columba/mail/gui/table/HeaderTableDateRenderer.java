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

import org.columba.mail.gui.util.*;
import org.columba.mail.gui.table.util.*;
import org.columba.mail.message.*;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.tree.*;

import java.text.*;
import java.util.*;

public class HeaderTableDateRenderer
	extends JLabel
	implements TableCellRenderer {
	private Border unselectedBorder = null;
	private Border selectedBorder = null;
	private boolean isBordered = true;

	static SimpleDateFormat dfWeek =
		new SimpleDateFormat("EEE HH:mm", Locale.getDefault());
	static DateFormat dfCommon =
		DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

	static final long OneDay = 24 * 60 * 60 * 1000;
	static TimeZone localTimeZone = TimeZone.getDefault();

	private JTree tree;

	private Font plainFont, boldFont;

	public HeaderTableDateRenderer(JTree tree, boolean isBordered) {
		super();
		this.tree = tree;
		this.isBordered = isBordered;

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

	public static int getLocalDaysDiff(long t) {

		return (int)
			((System.currentTimeMillis()
				+ localTimeZone.getRawOffset()
				- ((t + localTimeZone.getRawOffset()) / OneDay) * OneDay)
				/ OneDay);
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

		if (messageNode == null) {
			System.out.println("messagenode is null");
			return this;
		}

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

		if (!(value instanceof Date)) {
			setText("");
			return this;
		}

		if (value instanceof String)
			return this;

		Date date = (Date) value;

		if (date == null)
			return this;

		int diff = getLocalDaysDiff(date.getTime());

		//if ( today
		if ((diff >= 0) && (diff < 7))
			setText(dfWeek.format(date));
		else
			setText(dfCommon.format(date));

		return this;

	}
}
