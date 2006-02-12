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
import java.util.StringTokenizer;
import java.util.Vector;

import org.columba.calendar.base.VCalendarUIDGenerator;

public class BasicVCalendarModel implements ICalendarModel {

	private String id;

	private String componentType;

	private Calendar dtStart;

	private Calendar dtEnt;

	private Calendar dtStamp;

	private String summary;

	private String description;

	private String location;

	private String priority;

	private String eventClass;

	private String transsp;

	private URL url;

	private Vector categoryList = new Vector();

	private byte[] attachment;

	public BasicVCalendarModel(String componentType) {
		super();

		this.componentType = componentType;
		
		// generate default unique id
		this.id = new VCalendarUIDGenerator().newUID();
	}

	/**
	 * @see org.columba.calendar.model.ICalendarModel#getAttachment()
	 */
	public byte[] getAttachment() {
		return attachment;
	}

	/**
	 * @param attachment
	 *            The attachment to set.
	 */
	public void setAttachment(byte[] attachment) {
		this.attachment = attachment;
	}

	/**
	 * @see org.columba.calendar.model.ICalendarModel#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @see org.columba.calendar.model.ICalendarModel#getDtEnt()
	 */
	public Calendar getDtEnt() {
		return dtEnt;
	}

	/**
	 * @param dtEnt
	 *            The dtEnt to set.
	 */
	public void setDtEnt(Calendar dtEnt) {
		this.dtEnt = dtEnt;
	}

	/**
	 * @see org.columba.calendar.model.ICalendarModel#getDtStamp()
	 */
	public Calendar getDtStamp() {
		return dtStamp;
	}

	/**
	 * @param dtStamp
	 *            The dtStamp to set.
	 */
	public void setDtStamp(Calendar dtStamp) {
		this.dtStamp = dtStamp;
	}

	/**
	 * @see org.columba.calendar.model.ICalendarModel#getDtStart()
	 */
	public Calendar getDtStart() {
		return dtStart;
	}

	/**
	 * @param dtStart
	 *            The dtStart to set.
	 */
	public void setDtStart(Calendar dtStart) {
		this.dtStart = dtStart;
	}

	/**
	 * @see org.columba.calendar.model.ICalendarModel#getEventClass()
	 */
	public String getEventClass() {
		return eventClass;
	}

	/**
	 * @param eventClass
	 *            The eventClass to set.
	 */
	public void setEventClass(String eventClass) {
		this.eventClass = eventClass;
	}

	/**
	 * @see org.columba.calendar.model.ICalendarModel#getId()
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @see org.columba.calendar.model.ICalendarModel#getLocation()
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @param location
	 *            The location to set.
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * @see org.columba.calendar.model.ICalendarModel#getPriority()
	 */
	public String getPriority() {
		return priority;
	}

	/**
	 * @param priority
	 *            The priority to set.
	 */
	public void setPriority(String priority) {
		this.priority = priority;
	}

	/**
	 * @see org.columba.calendar.model.ICalendarModel#getSummary()
	 */
	public String getSummary() {
		return summary;
	}

	/**
	 * @param summary
	 *            The summary to set.
	 */
	public void setSummary(String summary) {
		this.summary = summary;
	}

	/**
	 * @see org.columba.calendar.model.ICalendarModel#getTranssp()
	 */
	public String getTranssp() {
		return transsp;
	}

	/**
	 * @param transsp
	 *            The transsp to set.
	 */
	public void setTranssp(String transsp) {
		this.transsp = transsp;
	}

	/**
	 * @see org.columba.calendar.model.ICalendarModel#getUrl()
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            The url to set.
	 */
	public void setUrl(URL url) {
		this.url = url;
	}

	public void addCategory(String category) {
		categoryList.add(category);
	}

	public void removeCategory(String category) {
		categoryList.remove(category);
	}

	/**
	 * @see org.columba.calendar.model.ICalendarModel#getCategoryIterator()
	 */
	public Iterator getCategoryIterator() {
		return categoryList.iterator();
	}

	public void setCategories(String categoryAsString) {
		categoryList = new Vector();

		for (StringTokenizer t = new StringTokenizer(categoryAsString, ","); t
				.hasMoreTokens();) {
			categoryList.add(t.nextToken());
		}
	}

	/**
	 * @see org.columba.calendar.model.ICalendarModel#getType()
	 */
	public String getType() {
		return componentType;
	}

	public String getCategories() {
		StringBuffer b = new StringBuffer();

		for (Iterator i = categoryList.iterator(); i.hasNext();) {

			b.append(i.next());

			if (i.hasNext()) {
				b.append(',');
			}
		}

		return b.toString();
	}
}
