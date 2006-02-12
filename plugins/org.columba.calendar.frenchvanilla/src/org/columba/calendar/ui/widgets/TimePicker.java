// The contents of this file are subject to the Mozilla Public License Version
// 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.calendar.ui.widgets;

import javax.swing.JComboBox;

public class TimePicker extends JComboBox {

	int hour;

	int minutes;

	public TimePicker() {

		for (int i = 0; i <= 23; i++) {
			addItem(i + ":00");
			addItem(i + ":30");
		}

		hour = 15;
		minutes = 0;

		setSelectedItem("15:00");
	}

	public int getHour() {
		String value = (String) getSelectedItem();
		int h = Integer.parseInt(value.substring(0, 2));
		int m = Integer.parseInt(value.substring(0, 2));

		this.hour = h;
		this.minutes = m;

		return m;
	}

	public int getMinutes() {
		String value = (String) getSelectedItem();
		int h = Integer.parseInt(value.substring(0, 2));
		int m = Integer.parseInt(value.substring(0, 2));

		this.hour = h;
		this.minutes = m;

		return m;
	}

	public void setHour(int hour) {
		this.hour = hour;

		StringBuffer buf = new StringBuffer();
		buf.append(hour);
		buf.append(":");
		buf.append(minutes);

		setSelectedItem(buf.toString());
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;

		StringBuffer buf = new StringBuffer();
		buf.append(hour);
		buf.append(":");
		buf.append(minutes);

		setSelectedItem(buf.toString());

	}
}
