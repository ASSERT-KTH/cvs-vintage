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
package org.columba.calendar.model.api;

import java.net.URL;
import java.util.Calendar;

/**
 * Defines an iCalendar VTODO component.
 * <p>
 * <pre>
 * 4.6.2 To-do Component
 * 
 * Component Name: VTODO
 * 
 * Purpose: Provide a grouping of calendar properties that describe a to-do.
 * 
 * Formal Definition: A "VTODO" calendar component is defined by the following
 * notation:
 * 
 * todoc = "BEGIN" ":" "VTODO" CRLF todoprop *alarmc "END" ":" "VTODO" CRLF
 * 
 * todoprop = *( ; the following are optional, ; but MUST NOT occur more than
 * once
 * 
 * class / completed / created / description / dtstamp / dtstart / geo /
 * last-mod / location / organizer / percent / priority / recurid / seq / status /
 * summary / uid / url / ; either 'due' or 'duration' may appear in ; a
 * 'todoprop', but 'due' and 'duration' ; MUST NOT occur in the same 'todoprop'
 * 
 * due / duration / ; the following are optional, ; and MAY occur more than once
 * 
 * attach / attendee / categories / comment / contact / exdate / exrule /
 * rstatus / related / resources / rdate / rrule / x-prop )
 * </pre>
 * 
 * @author fdietz
 * 
 */
public interface ITodo extends IComponent, ICategoryList, IAttachmentList {

	public abstract Calendar getDue();

	/**
	 * @return Returns the dtStart.
	 */
	public abstract Calendar getDtStart();

	/**
	 * @return Returns the priority.
	 */
	public abstract String getPriority();

	/**
	 * @return Returns the summary.
	 */
	public abstract String getSummary();

	/**
	 * @return Returns the description.
	 */
	public abstract String getDescription();

	/**
	 * @return Returns the url.
	 */
	public abstract URL getUrl();

	/**
	 * @return Returns the eventClass.
	 */
	public abstract String getEventClass();

}
