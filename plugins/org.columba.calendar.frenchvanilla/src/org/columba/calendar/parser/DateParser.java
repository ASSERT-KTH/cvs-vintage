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
package org.columba.calendar.parser;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class DateParser {

	public DateParser() {
		super();
	}

	public static String createStringFromCalendar(Calendar calendar) {
		return DateFormat.getInstance().format(calendar.getTime());
	}

	public static Calendar createCalendarFromString(String str) throws IllegalArgumentException{
		if ( str == null ) throw new IllegalArgumentException("str == null");
		
		if ( str.length() == 0 ) throw new IllegalArgumentException("str.length() == 0");
		
		Calendar c = Calendar.getInstance();

		try {
			Date d = DateFormat.getInstance().parse(str);
			c.setTime(d);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return c;
	}

}
