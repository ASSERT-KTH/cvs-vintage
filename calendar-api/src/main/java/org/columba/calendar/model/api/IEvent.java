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
 * Defines an iCalendar VEVENT component.
 * <p>
 * 
 * <pre>
 *   4.6.1 Event Component
 *   
 *   Component Name: &quot;VEVENT&quot;
 *   
 *   Purpose: Provide a grouping of component properties that describe an event.
 *   
 *   Format Definition: A &quot;VEVENT&quot; calendar component is defined by the following
 *   notation:
 *   
 *   eventc = &quot;BEGIN&quot; &quot;:&quot; &quot;VEVENT&quot; CRLF eventprop *alarmc &quot;END&quot; &quot;:&quot; &quot;VEVENT&quot; CRLF
 *   
 *   eventprop = *( ; the following are optional, ; but MUST NOT occur more than
 *   once
 *   
 *   class / created / description / dtstart / geo / last-mod / location /
 *   organizer / priority / dtstamp / seq / status / summary / transp / uid / url /
 *   recurid / ; either 'dtend' or 'duration' may appear in ; a 'eventprop', but
 *   'dtend' and 'duration' ; MUST NOT occur in the same 'eventprop'
 *   
 *   dtend / duration / ; the following are optional, ; and MAY occur more than
 *   once
 *   
 *   attach / attendee / categories / comment / contact / exdate / exrule /
 *   rstatus / related / resources / rdate / rrule / x-prop )
 * </pre>
 * 
 * @author fdietz
 * 
 */
public interface IEvent extends IComponent, ICategoryList, IAttachmentList {

	/**
	 * @return Returns the dtEnt.
	 */
	public abstract Calendar getDtEnt();

	public abstract void setDtEnt(Calendar dtEnt);

	/**
	 * @return Returns the location.
	 */
	public abstract String getLocation();

	public abstract void setLocation(String location);

	/**
	 * @return Returns the transsp.
	 */
	public abstract String getTranssp();

	public abstract void setTranssp(String transsp);

	/**
	 * @return Returns the dtStart.
	 */
	public abstract Calendar getDtStart();

	public abstract void setDtStart(Calendar dtStart);

	/**
	 * @return Returns the priority.
	 */
	public abstract String getPriority();

	public abstract void setPriority(String priority);

	/**
	 * @return Returns the summary.
	 */
	public abstract String getSummary();

	public abstract void setSummary(String summary);

	/**
	 * @return Returns the description.
	 */
	public abstract String getDescription();

	public abstract void setDescription(String description);

	/**
	 * @return Returns the url.
	 */
	public abstract URL getUrl();

	public abstract void setUrl(URL url);

	/**
	 * @return Returns the eventClass.
	 */
	public abstract String getEventClass();

	public abstract void setEventClass(String eventClass);
	

	public abstract IEvent createCopy();
	
}
