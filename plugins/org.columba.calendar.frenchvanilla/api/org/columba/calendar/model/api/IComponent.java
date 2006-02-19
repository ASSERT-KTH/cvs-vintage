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

import java.util.Calendar;

/**
 * Defines an iCalendar component.
 * 
 * @author fdietz
 */
public interface IComponent {

	public enum TYPE {
		EVENT, TODO, FREEBUSY, TIMEZONE, JOURNAL
	};

	/**
	 * 
	 * @return Returns type of component
	 */
	public abstract TYPE getType();

	/**
	 * @return Returns the id.
	 */
	public abstract String getId();

	/**
	 * @return Returns the dtStamp.
	 */
	public abstract Calendar getDtStamp();
	
	public abstract void setDtStamp(Calendar calendar);

}