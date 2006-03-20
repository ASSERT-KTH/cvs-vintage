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

import java.net.URL;
import java.util.Calendar;
import java.util.Iterator;

import org.columba.calendar.base.UUIDGenerator;
import org.columba.calendar.model.api.IEvent;

public class Event extends Component implements IEvent {

	private Calendar dtStart;

	private Calendar dtEnt;

	private String transsp;

	private String summary;

	private String description;

	private String location;

	private String priority;

	private String eventClass;

	private URL url;

	private CategoryList categoryList = new CategoryList();

	public Event() {
		super(TYPE.EVENT);

		dtStart = Calendar.getInstance();
		dtEnt = Calendar.getInstance();
	}

	public Event(String id) {
		super(id, TYPE.EVENT);

		dtStart = Calendar.getInstance();
		dtEnt = Calendar.getInstance();
	}

	public Event(Calendar dtStart, Calendar dtEnd, String summary) {
		super(TYPE.EVENT);

		if (dtStart == null)
			throw new IllegalArgumentException("dtStart == null");

		if (dtEnd == null)
			throw new IllegalArgumentException("dtEnd == null");

		if (summary == null)
			throw new IllegalArgumentException("summary == null");

		this.dtStart = dtStart;
		this.dtEnt = dtEnd;
		this.summary = summary;

	}

	public Calendar getDtEnt() {
		return dtEnt;
	}

	public String getLocation() {
		return location;
	}

	public String getTranssp() {
		return transsp;
	}

	public Calendar getDtStart() {
		return dtStart;
	}

	public String getPriority() {
		return priority;
	}

	public String getSummary() {
		return summary;
	}

	public String getDescription() {
		return description;
	}

	public URL getUrl() {
		return url;
	}

	public String getEventClass() {
		return eventClass;
	}

	public void addCategory(String category) {
		categoryList.addCategory(category);
	}

	public void removeCategory(String category) {
		categoryList.removeCategory(category);
	}

	public Iterator<String> getCategoryIterator() {
		return categoryList.getCategoryIterator();
	}

	public String getCategories() {
		return categoryList.getCategories();
	}

	public void setCategories(String categories) {
		categoryList.setCategories(categories);
	}

	/**
	 * @param description
	 *            The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param dtEnt
	 *            The dtEnt to set.
	 */
	public void setDtEnt(Calendar dtEnt) {
		this.dtEnt = dtEnt;
	}

	/**
	 * @param dtStart
	 *            The dtStart to set.
	 */
	public void setDtStart(Calendar dtStart) {
		this.dtStart = dtStart;
	}

	/**
	 * @param eventClass
	 *            The eventClass to set.
	 */
	public void setEventClass(String eventClass) {
		this.eventClass = eventClass;
	}

	/**
	 * @param location
	 *            The location to set.
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * @param priority
	 *            The priority to set.
	 */
	public void setPriority(String priority) {
		this.priority = priority;
	}

	/**
	 * @param summary
	 *            The summary to set.
	 */
	public void setSummary(String summary) {
		this.summary = summary;
	}

	/**
	 * @param transsp
	 *            The transsp to set.
	 */
	public void setTranssp(String transsp) {
		this.transsp = transsp;
	}

	/**
	 * @param url
	 *            The url to set.
	 */
	public void setUrl(URL url) {
		this.url = url;
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		// create new event with new UUID
		Event event = new Event(new UUIDGenerator().newUUID());
		// copy all attributes

		event.setDtStart(getDtStart());
		event.setDtEnt(getDtEnt());
		event.setDtStamp(getDtStamp());
		event.setSummary(getSummary());
		event.setLocation(getLocation());
		event.setCalendar(getCalendar());

		return event;
	}

	/**
	 * @see org.columba.calendar.model.api.IEvent#createCopy()
	 */
	public IEvent createCopy() {
		try {
			return (IEvent) clone();
		} catch (CloneNotSupportedException e) {
		}
		return null;
	}
}
