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
package org.columba.calendar.ui.base;

import java.beans.PropertyVetoException;
import java.util.Calendar;

import org.columba.calendar.ui.base.api.IActivity;


import com.miginfocom.util.PropertyKey;

public class Activity implements IActivity {

	private com.miginfocom.calendar.activity.Activity wrapped;

	public Activity(com.miginfocom.calendar.activity.Activity wrapped) {
		super();

		this.wrapped = wrapped;
	}

	public String getId() {
		return (String) wrapped.getID();
	}

	public String getProperty(String propertyKey) {
		Object value = wrapped.getProperty(PropertyKey.getKey(propertyKey));

		return (String) value;
	}

	public void setProperty(String propertyKey, String propertyValue) {

		try {
			// enabled event-firing
			wrapped.setProperty(PropertyKey.getKey(propertyKey), propertyValue,
					Boolean.TRUE);
		} catch (PropertyVetoException e) {
			throw new IllegalArgumentException("illegal argument", e);
		}
	}

	public String getSummary() {
		return wrapped.getSummary();
	}

	public Calendar getDtStart() {
		return wrapped.getDateRangeForReading().getStart();
	}

	public Calendar getDtEnd() {
		return wrapped.getDateRangeForReading().getEnd(true);
	}

}
