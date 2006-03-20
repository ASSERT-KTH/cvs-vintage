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
package org.columba.calendar.model;

import java.util.Calendar;

import org.columba.calendar.base.VCalendarUIDGenerator;
import org.columba.calendar.model.api.IComponent;

public class Component implements IComponent {

	private String id;

	private TYPE type;

	private Calendar dtStamp;

	private String calendarId;
	
	public Component(String id, TYPE type) {
		if (id == null)
			throw new IllegalArgumentException("id == null");
		if (type == null)
			throw new IllegalArgumentException("type == null");
		
		this.id = id;
		this.type = type;
		
		dtStamp = Calendar.getInstance();
	}

	public Component(TYPE type) {
		if (type == null)
			throw new IllegalArgumentException("type == null");
		
		// generate default unique id
		this.id = new VCalendarUIDGenerator().newUID();
		
		this.type = type;
		
		dtStamp = Calendar.getInstance();

	}

	public TYPE getType() {
		return type;
	}

	public String getId() {
		return id;
	}

	public Calendar getDtStamp() {
		return dtStamp;
	}

	public void setDtStamp(Calendar calendar) {
		this.dtStamp = calendar;
	}

	public String getCalendar() {
		return calendarId;
	}

	public void setCalendar(String calendar) {
		this.calendarId = calendar;		
	}

}
