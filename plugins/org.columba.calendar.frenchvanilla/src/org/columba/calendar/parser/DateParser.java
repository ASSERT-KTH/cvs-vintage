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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import net.fortuna.ical4j.util.TimeZones;

public class DateParser {

	private static final String DEFAULT_PATTERN = "yyyyMMdd'T'HHmmss";

	private static final String UTC_PATTERN = "yyyyMMdd'T'HHmmss'Z'";

	/**
	 * local date-time representation.
	 */
	private static DateFormat defaultFormat = new SimpleDateFormat(
			DEFAULT_PATTERN);

	/**
	 * UTC date-time representation.
	 */
	private static DateFormat utcFormat = new SimpleDateFormat(UTC_PATTERN);
	{
		utcFormat.setTimeZone(TimeZone.getTimeZone(TimeZones.UTC_ID));
	}

	public DateParser() {
		super();
	}

	public static String createStringFromCalendar(Calendar calendar) {
		return DateFormat.getInstance().format(calendar.getTime());
	}

	public static String createDateStringFromCalendar(Calendar calendar) {
		long millis = calendar.getTimeInMillis();
		Date date = new Date(millis);
		StringBuffer b = new StringBuffer(utcFormat.format(date));

		// TODO fix timezone
		// b.append('T');
		// Time time = new Time(millis, TimeZone.getDefault());
		// b.append(time.toString());
		return b.toString();

	}

	public static Calendar createCalendarFromDateString(String dateString) {

		if (dateString == null)
			throw new IllegalArgumentException("dateString == null");

		if (dateString.length() == 0)
			throw new IllegalArgumentException("dateString.length() == 0");

		long time = -1;
		try {
			time = utcFormat.parse(dateString).getTime();
		} catch (ParseException pe) {
			defaultFormat.setTimeZone(TimeZone.getDefault());
			try {
				time = defaultFormat.parse(dateString).getTime();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		if (time == -1)
			return null;
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		return cal;

	}

	public static Calendar createCalendarFromString(String str) {
		if (str == null)
			throw new IllegalArgumentException("str == null");

		if (str.length() == 0)
			throw new IllegalArgumentException("str.length() == 0");

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
