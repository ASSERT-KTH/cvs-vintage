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
 * Defines an iCalendar VFREEBUSY component.
 * <p>
 * 
 * </pre>
 * 
 * 4.6.4 Free/Busy Component
 * 
 * Component Name: VFREEBUSY
 * 
 * Purpose: Provide a grouping of component properties that describe either a
 * request for free/busy time, describe a response to a request for free/busy
 * time or describe a published set of busy time.
 * 
 * Formal Definition: A "VFREEBUSY" calendar component is defined by the
 * following notation:
 * 
 * freebusyc = "BEGIN" ":" "VFREEBUSY" CRLF fbprop "END" ":" "VFREEBUSY" CRLF
 * 
 * fbprop = *(
 *  ; the following are optional, ; but MUST NOT occur more than once
 * 
 * contact / dtstart / dtend / duration / dtstamp / organizer / uid / url /
 *  ; the following are optional, ; and MAY occur more than once
 * 
 * attendee / comment / freebusy / rstatus / x-prop )
 * 
 * </pre>
 * 
 * @author fdietz
 * 
 */
public interface IFreeBusy extends IComponent {

	/**
	 * @return Returns the url.
	 */
	public abstract URL getUrl();

	/**
	 * @return Returns the dtStart.
	 */
	public abstract Calendar getDtStart();

	/**
	 * @return Returns the dtEnt.
	 */
	public abstract Calendar getDtEnt();
}
