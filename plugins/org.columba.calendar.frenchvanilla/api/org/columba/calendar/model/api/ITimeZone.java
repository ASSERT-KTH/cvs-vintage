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

/**
 * Defines an iCalendar VTIMEZONE component.
 * <p>
 * <pre>
 * 4.6.5 Time Zone Component
 * 
 * Component Name: VTIMEZONE
 * 
 * Purpose: Provide a grouping of component properties that defines a time zone.
 * 
 * Formal Definition: A "VTIMEZONE" calendar component is defined by the
 * following notation:
 * 
 * timezonec = "BEGIN" ":" "VTIMEZONE" CRLF
 * 
 * 2*(
 *  ; 'tzid' is required, but MUST NOT occur more ; than once
 * 
 * tzid /
 *  ; 'last-mod' and 'tzurl' are optional, but MUST NOT occur more than once
 * 
 * last-mod / tzurl /
 *  ; one of 'standardc' or 'daylightc' MUST occur ..; and each MAY occur more
 * than once.
 * 
 * standardc / daylightc /
 *  ; the following is optional, ; and MAY occur more than once
 * 
 * x-prop
 *  )
 * 
 * "END" ":" "VTIMEZONE" CRLF
 * 
 * standardc = "BEGIN" ":" "STANDARD" CRLF
 * 
 * tzprop
 * 
 * "END" ":" "STANDARD" CRLF
 * 
 * daylightc = "BEGIN" ":" "DAYLIGHT" CRLF
 * 
 * tzprop
 * 
 * "END" ":" "DAYLIGHT" CRLF
 * 
 * tzprop = 3*(
 *  ; the following are each REQUIRED, ; but MUST NOT occur more than once
 * 
 * dtstart / tzoffsetto / tzoffsetfrom /
 *  ; the following are optional, ; and MAY occur more than once
 * 
 * comment / rdate / rrule / tzname / x-prop
 *  )
 * </pre>
 * 
 * @author fdietz
 * 
 */
public interface ITimeZone extends IComponent {

	/**
	 * @return Returns the url.
	 */
	public abstract URL getTZUrl();
}
