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
package org.columba.calendar.ui.comp;

import java.awt.Component;
import java.util.Hashtable;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;

import org.columba.calendar.config.Config;

public class CalendarPicker extends JComboBox {

	private Hashtable<String, String> table = new Hashtable<String, String>(10);

	public CalendarPicker() {
		super();

		try {
			Preferences prefs = Config.getInstance().getCalendarOptions();
			String[] children = prefs.childrenNames();
			for (int i = 0; i < children.length; i++) {
				String calendarId = children[i];
				Preferences childNode = prefs.node(calendarId);
				String[] keys = childNode.keys();
				String name = childNode.get(Config.CALENDAR_NAME, null);
				int colorInt = childNode.getInt(Config.CALENDAR_COLOR, -1);
				String type = childNode.get(Config.CALENDAR_TYPE, "local");

				addItem(calendarId);

				table.put(calendarId, name);
			}

			setSelectedIndex(0);

		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// custom renderer to convert from calendar id to calendar name
		setRenderer(new MyListCellRenderer());

	}

	class MyListCellRenderer extends DefaultListCellRenderer {

		MyListCellRenderer() {

		}

		/**
		 * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList,
		 *      java.lang.Object, int, boolean, boolean)
		 */
		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);

			String name = table.get(value);

			setText(name);

			return this;
		}

	}
}
