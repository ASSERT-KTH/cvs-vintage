// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.mail.filter;

import org.columba.core.config.AdapterNode;
import org.columba.core.config.DefaultItem;
import org.w3c.dom.Document;

public class FilterCriteria extends DefaultItem {
	
	// Condition
	public final static int CONTAINS = 0;
	public final static int CONTAINS_NOT = 1;
	public final static int IS = 2;
	public final static int IS_NOT = 3;
	public final static int BEGINS_WITH = 4;
	public final static int ENDS_WITH = 5;

	public final static int DATE_BEFORE = 0;
	public final static int DATE_AFTER = 1;

	public final static int SIZE_SMALLER = 0;
	public final static int SIZE_BIGGER = 1;
	
	// header-item
	public final static int SUBJECT = 0;
	public final static int FROM = 1;
	public final static int TO = 2;
	public final static int CC = 3;
	public final static int BCC = 4;
	public final static int TO_CC = 5;
	public final static int DATE = 6;
	public final static int SIZE = 7;
	public final static int BODY = 8;
	public final static int FLAGS = 9;
	public final static int PRIORITY = 10;

	// criteria: contains = 0, contains not=1, is=2, is not=3, begins with=4 , ends with=5
	private AdapterNode criteriaNode;
	private AdapterNode headerItemNode;
	private AdapterNode patternNode;
	private AdapterNode typeNode;

	private AdapterNode node;

	public FilterCriteria(AdapterNode node, Document doc) {
		super(doc);

		this.node = node;
		if (node != null)
			parseNode();
	}

	protected void parseNode() {
		AdapterNode child;

		for (int i = 0; i < node.getChildCount(); i++) {
			child = node.getChild(i);

			if (child.getName().equals("headeritem"))
				headerItemNode = child;
			else if (child.getName().equals("criteria"))
				criteriaNode = child;
			else if (child.getName().equals("pattern"))
				patternNode = child;
			else if (child.getName().equals("type"))
				typeNode = child;
		}
	}

	public String getCriteriaString() {
		return getTextValue(criteriaNode);
	}

	public int getCriteria() {
		String condition = getCriteriaString();

		int c = -1;
		if (condition.equalsIgnoreCase("contains"))
			c = CONTAINS;

		else if (condition.equalsIgnoreCase("contains not"))
			c = CONTAINS_NOT;

		else if (condition.equalsIgnoreCase("is"))
			c = IS;

		else if (condition.equalsIgnoreCase("is not"))
			c = IS_NOT;

		else if (condition.equalsIgnoreCase("begins with"))
			c = BEGINS_WITH;

		else if (condition.equalsIgnoreCase("ends with"))
			c = ENDS_WITH;
		else if (condition.equalsIgnoreCase("before"))
			c = DATE_BEFORE;

		else if (condition.equalsIgnoreCase("after"))
			c = DATE_AFTER;
		else if (condition.equalsIgnoreCase("smaller"))
			c = SIZE_SMALLER;

		else if (condition.equalsIgnoreCase("bigger"))
			c = SIZE_BIGGER;
		return c;
	}

	public String getHeaderItemString() {
		return getTextValue(headerItemNode);
	}
	
	public int getHeaderItem()
	{
		String h = getHeaderItemString();
		int result = -1;
		if ( h.equalsIgnoreCase("Subject") ) result = SUBJECT;
		if ( h.equalsIgnoreCase("To") ) result = TO;
		if ( h.equalsIgnoreCase("From") ) result = FROM;
		if ( h.equalsIgnoreCase("Cc") ) result = CC;
		if ( h.equalsIgnoreCase("Bcc") ) result = BCC;
		if ( h.equalsIgnoreCase("To or Cc") ) result = TO_CC;
		if ( h.equalsIgnoreCase("Body") ) result = BODY;
		if ( h.equalsIgnoreCase("Date") ) result = DATE;
		if ( h.equalsIgnoreCase("Size") ) result = SIZE;
		if ( h.equalsIgnoreCase("Flags") ) result = FLAGS;
		if ( h.equalsIgnoreCase("Priority") ) result = PRIORITY;
		
		return result;
	}

	public String getType() {
		return getTextValue(typeNode);
	}

	public String getPattern() {

		return getTextValue(patternNode);
	}

	public void setCriteria(String s) {
		setTextValue(criteriaNode, s);
	}

	public void setHeaderItem(String s) {
		setTextValue(headerItemNode, s);
	}

	public void setType(String s) {
		setTextValue(typeNode, s);
	}

	public void setPattern(String s) {
		setTextValue(patternNode, s.trim());
	}

}
