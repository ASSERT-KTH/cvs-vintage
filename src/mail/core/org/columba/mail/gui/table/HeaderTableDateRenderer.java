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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.UIManager;

public class HeaderTableDateRenderer extends DefaultLabelRenderer {

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

			/*
		if (!(value instanceof Date)) {
			setText("");
			return this;
		}

		if (value instanceof String)
			return this;
		*/
		
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
