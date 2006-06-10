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
package org.columba.calendar.ui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JMenuItem;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.calendar.base.api.IActivity;
import org.columba.calendar.base.api.ICalendarItem;
import org.columba.calendar.command.CalendarCommandReference;
import org.columba.calendar.command.MoveEventCommand;
import org.columba.calendar.config.Config;
import org.columba.calendar.config.api.ICalendarList;
import org.columba.calendar.store.CalendarStoreFactory;
import org.columba.calendar.store.api.ICalendarStore;
import org.columba.calendar.ui.calendar.api.ActivitySelectionChangedEvent;
import org.columba.calendar.ui.calendar.api.IActivitySelectionChangedListener;
import org.columba.calendar.ui.frame.api.ICalendarMediator;
import org.columba.core.command.Command;
import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.menu.IMenu;

public class MoveActivityMenu extends IMenu implements
		IActivitySelectionChangedListener {

	private Hashtable<String, JMenuItem> table = new Hashtable<String, JMenuItem>();

	public MoveActivityMenu(IFrameMediator controller) {
		super(controller, "Move", "MoveActivity");

		ICalendarList list = Config.getInstance().getCalendarList();
		Enumeration<ICalendarItem> e = list.getElements();
		while (e.hasMoreElements()) {
			final ICalendarItem calendarItem = e.nextElement();
			JMenuItem item = new JMenuItem(calendarItem.getId());
			table.put(calendarItem.getId(), item);

			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					/**
					 * move activity
					 */

					ICalendarMediator m = (ICalendarMediator) getFrameMediator();

					IActivity activity = m.getCalendarView()
							.getSelectedActivity();

					ICalendarStore store = CalendarStoreFactory.getInstance()
							.getLocaleStore();

					Command command = new MoveEventCommand(
							new CalendarCommandReference(store, calendarItem,
									activity));

					CommandProcessor.getInstance().addOp(command);

				}
			});

			add(item);
		}

		ICalendarMediator m = (ICalendarMediator) getFrameMediator();
		m.getCalendarView().addSelectionChangedListener(this);
	}

	public void selectionChanged(ActivitySelectionChangedEvent event) {
		if (event.getSelection().length == 0)
			setEnabled(false);
		else {

			setEnabled(true);

			// enable all menuitems
			Enumeration<JMenuItem> e = table.elements();
			while (e.hasMoreElements()) {
				JMenuItem m = e.nextElement();
				m.setEnabled(true);
			}

			// retrieve selected activity
			IActivity activity = event.getSelection()[0];
			// activity belongs to calendar id ?
			String calendarId = activity.getCalendarId();

			// disable this calendar's id
			JMenuItem menuItem = table.get(calendarId);
			menuItem.setEnabled(false);
		}

	}
}