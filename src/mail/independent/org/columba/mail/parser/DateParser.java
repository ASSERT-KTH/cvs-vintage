//The contents of this file are subject to the Mozilla Public License Version 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.

package org.columba.mail.parser;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateParser {
	static final SimpleDateFormat DateHeaderFormats[] =
		{
			new SimpleDateFormat(
				"EEE, dd MMM yy HH:mm:ss z",
				new DateFormatSymbols(Locale.US)),
			new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss z",
				new DateFormatSymbols(Locale.US)),
			new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss",
				new DateFormatSymbols(Locale.US)),
			new SimpleDateFormat(
				"dd MMM yyyy HH:mm:ss z",
				new DateFormatSymbols(Locale.US)),
			new SimpleDateFormat(
				"EEE MMM dd HH:mm:ss yyyy",
				new DateFormatSymbols(Locale.US))};

	public DateParser() {
	}

	public static Date parseString(String str) {

		//System.out.println("Date: "+str);

		Date date = null;

		for (int i = 0; i < DateHeaderFormats.length; i++) {
			try {
				date = DateHeaderFormats[i].parse(str);
			} catch (Exception e) {
				//System.out.println( e.getMessage() );
			}
			if (date != null)
				break;
		}

		//System.out.println("Parsed Date : "+formatDate(date));

		if (date == null)
			return new Date(0);
		else
			return date;

	}

	public static String formatDate(Date date) {
		//       Date date = new Date();
		//date = parseString(str);
		if (date == null) {
			return "";
		} else {
			return DateFormat
				.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM)
				.format(date);
		}

	}
}
