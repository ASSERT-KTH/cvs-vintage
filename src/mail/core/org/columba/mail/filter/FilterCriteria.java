//The contents of this file are subject to the Mozilla Public License Version 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.mail.filter;

import org.columba.core.config.DefaultItem;
import org.columba.core.xml.XmlElement;

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
	/*
	private AdapterNode criteriaNode;
	private AdapterNode headerItemNode;
	private AdapterNode patternNode;
	private AdapterNode typeNode;

	private AdapterNode node;
	*/
	
	public FilterCriteria(XmlElement root) {
		super(root);

		
	}

	/*
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
*/

	public String getCriteriaString() {
		return getRoot().getAttribute("criteria");
		
		//return getTextValue(criteriaNode);
		//return "";
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
	
	
	public static int getCriteria( String condition) {
			

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
		return getRoot().getAttribute("headerfield");
		
		//return getTextValue(headerItemNode);
		//return "";
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
		return getRoot().getAttribute("type");
		//return getTextValue(typeNode);
		//return "";
		
		
	}

	public String getPattern() {
		return getRoot().getAttribute("pattern");
		//return getTextValue(patternNode);
		//return "";
	}

	public void setCriteria(String s) {
		getRoot().addAttribute("criteria", s );
		//setTextValue(criteriaNode, s);
		
	}

	public void setHeaderItem(String s) {
		getRoot().addAttribute("headerfield", s );
		//setTextValue(headerItemNode, s);
	}

	public void setType(String s) {
		getRoot().addAttribute("type", s );
		//setTextValue(typeNode, s);
	}

	public void setPattern(String s) {
		getRoot().addAttribute("pattern", s );
		//setTextValue(patternNode, s.trim());
	}

}
