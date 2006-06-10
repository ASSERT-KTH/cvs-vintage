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

import org.columba.calendar.model.api.ITodo;

public class Todo extends Component implements ITodo {

	private Calendar dtStart;

	private Calendar due;

	private String summary;

	private String description;

	private String priority;

	private String eventClass;

	private URL url;

	private CategoryList categoryList = new CategoryList();

	public Todo(Calendar dtStart, Calendar due, String summary) {
		super(TYPE.TODO);

		if (dtStart == null)
			throw new IllegalArgumentException("dtStart == null");

		if (due == null)
			throw new IllegalArgumentException("due == null");

		if (summary == null)
			throw new IllegalArgumentException("summary == null");

		this.dtStart = dtStart;
		this.due = due;
		this.summary = summary;
	}

	public Calendar getDue() {
		return due;
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

}
