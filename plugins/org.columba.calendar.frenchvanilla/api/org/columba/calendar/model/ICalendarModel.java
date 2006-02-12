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

public interface ICalendarModel {

	/**
	 * 
	 * @return Returns type of component
	 */
	String getType();

	/**
	 * @return Returns the attachment.
	 */
	byte[] getAttachment();

	/**
	 * @return Returns the description.
	 */
	String getDescription();

	/**
	 * @return Returns the dtEnt.
	 */
	Calendar getDtEnt();

	/**
	 * @return Returns the dtStamp.
	 */
	Calendar getDtStamp();

	/**
	 * @return Returns the dtStart.
	 */
	Calendar getDtStart();

	/**
	 * @return Returns the eventClass.
	 */
	String getEventClass();

	/**
	 * @return Returns the id.
	 */
	String getId();

	/**
	 * @return Returns the location.
	 */
	String getLocation();

	/**
	 * @return Returns the priority.
	 */
	String getPriority();

	/**
	 * @return Returns the summary.
	 */
	String getSummary();

	/**
	 * @return Returns the transsp.
	 */
	String getTranssp();

	/**
	 * @return Returns the url.
	 */
	URL getUrl();

	Iterator getCategoryIterator();

	String getCategories();

}