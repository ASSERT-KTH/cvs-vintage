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
package org.columba.calendar.base;

import java.awt.Color;

import javax.swing.Icon;

import org.columba.calendar.base.api.ICalendarItem;


public class CalendarItem implements ICalendarItem {
	private String name;

	private String id;

	private Color color;

	private boolean selected;

	private TYPE type;
	
	public CalendarItem(String id, ICalendarItem.TYPE type, String name, Color color) {
		this.id = id;
		this.type = type;
		this.name = name;
		this.color = color;
	}

	/**
	 * @return Returns the color.
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public Icon getIcon() {
		return null;
	}

	public boolean isSelected() {
		return selected;
	}

	public String toString() {
		return name;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public TYPE getType() {
		return type;
	}
}
