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
package org.columba.calendar.config;

import java.awt.Color;
import java.util.Enumeration;
import java.util.Hashtable;

import org.columba.calendar.base.CalendarItem;
import org.columba.calendar.base.api.ICalendarItem;
import org.columba.calendar.config.api.ICalendarList;

public class CalendarList implements ICalendarList {

	private Hashtable<String, ICalendarItem> hashtable = new Hashtable<String, ICalendarItem>();

	public CalendarList() {
		super();
	}

	public Enumeration<ICalendarItem> getElements() {
		return hashtable.elements();
	}

	public ICalendarItem add(String id, ICalendarItem.TYPE type, String name, Color color) {
		ICalendarItem item = new CalendarItem(id,type,  name, color);

		hashtable.put(id, item);

		return item;
	}

	public ICalendarItem remove(String id) {
		return hashtable.remove(id);
	}

}
