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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Iterator;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyFactory;
import net.fortuna.ical4j.model.PropertyFactoryImpl;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;

import org.columba.calendar.model.api.IComponent;
import org.columba.calendar.model.api.IEvent;
import org.columba.calendar.store.api.ICalendarStore;

public class CalendarExporter {

	public CalendarExporter() {
		super();
	}

	public void exportCalendar(File file, String calendarId,
			ICalendarStore store) throws Exception {
		FileOutputStream fout = new FileOutputStream(file);

		CalendarOutputter outputter = new CalendarOutputter();
		outputter.setValidating(false);

		Calendar calendar = new Calendar();
		calendar.getProperties().add(
				new ProdId("-//Columba Project//iCal4j 1.0//EN"));
		calendar.getProperties().add(Version.VERSION_2_0);
		calendar.getProperties().add(CalScale.GREGORIAN);

		Iterator<String> it = store.getIdIterator(calendarId);
		while (it.hasNext()) {
			String id = it.next();
			IComponent c = store.get(id);
			if (c.getType() == IComponent.TYPE.EVENT) {
				IEvent event = (IEvent) c;

				VEvent v = createVEvent(event);

				calendar.getComponents().add(v);
			}

		}
		outputter.output(calendar, fout);
	}

	public void exportSingleEvent(File file, IComponent component,
			ICalendarStore store) throws Exception {
		FileOutputStream fout = new FileOutputStream(file);

		CalendarOutputter outputter = new CalendarOutputter();
		outputter.setValidating(true);

		Calendar calendar = new Calendar();
		calendar.getProperties().add(
				new ProdId("-//Columba Project//iCal4j 1.0//EN"));
		calendar.getProperties().add(Version.VERSION_2_0);
		calendar.getProperties().add(CalScale.GREGORIAN);

		
		if (component.getType() == IComponent.TYPE.EVENT) {
			IEvent event = (IEvent) component;

			VEvent v = createVEvent(event);

			calendar.getComponents().add(v);
		}

		outputter.output(calendar, fout);
	}

	/**
	 * @param event
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws ParseException
	 */
	private VEvent createVEvent(IEvent event) throws IOException,
			URISyntaxException, ParseException {
		PropertyFactory factory = PropertyFactoryImpl.getInstance();
		java.util.Calendar start = event.getDtStart();
		java.util.Calendar end = event.getDtEnt();
		// java.util.Calendar stamp = event.getDtStamp();
		String summary = event.getSummary();
		String location = event.getLocation();
		String uid = event.getId();

		VEvent v = new VEvent(new DateTime(start.getTime()),
				new DateTime(end.getTime()), summary);

		//v.getEndDate().getParameters().add(Value.DATE);
		
		// if (stamp != null) {
		// DateTime dateTime = new DateTime(stamp.getTime());
		//			
		// ParameterList list = new ParameterList();
		// list.add(new TzId( TimeZone.getDefault().toString()));
		//				
		// v.getProperties().add(
		// factory.createProperty(Property.DTSTAMP, list,
		// dateTime.toString()));
		//			
		// }

		if (location != null)
			v.getProperties().add(
					factory.createProperty(Property.LOCATION,
							new ParameterList(), location));

		if (uid != null)
			v.getProperties().add(
					factory.createProperty(Property.UID, new ParameterList(),
							uid));
		return v;
	}
}
