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
package org.columba.core.filter;

import org.columba.core.config.DefaultItem;
import org.columba.core.xml.XmlElement;

public class FilterCriteria extends DefaultItem {

	// Condition
	private static final String ELEMENT = "criteria";

	private static final String CRITERIA = "criteria";

	private static final String TYPE_DATE = "Date";

	private static final String TYPE = "type";

	private static final String PATTERN = "pattern";

	public final static int CONTAINS = 0;

	public final static int CONTAINS_NOT = 1;

	public final static int IS = 2;

	public final static int IS_NOT = 3;

	public final static int BEGINS_WITH = 4;

	public final static int ENDS_WITH = 5;

	public final static int DATE_BEFORE = 6;

	public final static int DATE_AFTER = 7;

	public final static int SIZE_SMALLER = 8;

	public final static int SIZE_BIGGER = 9;

	private final String[] criteria = { "contains", "contains not", "is",
			"is not", "begins with", "ends with", "before", "after", "smaller",
			"bigger" };

	public FilterCriteria() {
		super(new XmlElement(FilterCriteria.ELEMENT));
	}

	public FilterCriteria(XmlElement root) {
		super(root);
	}

	public String getCriteriaString() {
		return getRoot().getAttribute(FilterCriteria.CRITERIA);
	}

	public void setCriteria(int c) {
		setCriteriaString(criteria[c]);
	}

	public int getCriteria() {
		String condition = getCriteriaString();

		int c = -1;

		for (int i = 0; i < criteria.length; i++) {
			if (criteria[i].equals(condition))
				c = i;
		}

		return c;
	}

	public void setCriteriaString(String s) {
		getRoot().addAttribute(FilterCriteria.CRITERIA, s);
	}

	public String getTypeString() {
		return getRoot().getAttribute(FilterCriteria.TYPE);
	}

	public void setTypeString(String s) {
		getRoot().addAttribute(FilterCriteria.TYPE, s);
	}

	public String getPatternString() {
		return getRoot().getAttribute(FilterCriteria.PATTERN);
	}

	public void setPatternString(String pattern) {
		getRoot().addAttribute(FilterCriteria.PATTERN, pattern);
	}
}