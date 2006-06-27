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
package org.columba.core.print;

import java.awt.Graphics2D;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Date;

/**
 * cPrintVariable class
 * 
 * @author unknown
 * 
 */
public class cPrintVariable extends cParagraph {
	String codeString;
	CodeStringParser[] parser;
	int parserCount = -1;
	String[] keys = { "PAGE_NR", "PAGE_COUNT", "DATE_TODAY" };
	String[] methods = { "getPageNr", "getPageCount", "getDateToday" };

	/**
	 * Default Constructor for cPrintVariable
	 */
	public cPrintVariable() {
		Class c = this.getClass();
		parserCount = Array.getLength(keys);
		parser = new CodeStringParser[parserCount];

		for (int i = 0; i < parserCount; i++) {
			try {
				parser[i] = new CodeStringParser(this, keys[i], c.getMethod(
						methods[i], new Class[0]));
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Methods called by the Variable parsers
	 */
		
	/**
	 * getPageNr method
	 * 
	 * @return PageNr
	 */
	public String getPageNr() {
		return Integer.toString(page.getDocument().getPageNr(page));
	}

	/**
	 * getPageCount method
	 * 
	 * @return PageCount
	 */
	public String getPageCount() {
		return Integer.toString(page.getDocument().getPageCount());
	}

	/**
	 * getDateToday method
	 * 
	 * @return df Today's Date in Java Format
	 */
	public String getDateToday() {
		DateFormat df = DateFormat.getDateInstance();

		return df.format(new Date());
	}

	/**
	 * setCodeString method
	 * 
	 * @param n
	 */
	public void setCodeString(String n) {
		codeString = n;
		setText(n); // For getHeight() to return the right Value
	}

	/* (non-Javadoc)
	 * @see org.columba.core.print.cParagraph#print(java.awt.Graphics2D)
	 */
	public void print(Graphics2D g) {
		setText(processCodeString());
		super.print(g);
	}

	/**
	 * processCodeString method
	 * @return result
	 */
	public String processCodeString() {
		StringBuffer result = new StringBuffer();
		boolean isDecoding = false;
		char c;

		for (int i = 0; i < codeString.length(); i++) {
			c = codeString.charAt(i);

			if (c == '%') {
				isDecoding = !isDecoding;

				if (!isDecoding) {
					for (int j = 0; j < parserCount; j++) {
						parser[j].reset();
					}
				}
			} else {
				if (isDecoding) {
					for (int j = 0; j < parserCount; j++) {
						if (parser[j].clock(c)) {
							result.append(parser[j].getValue());
						}
					}
				} else {
					result.append(c);
				}
			}
		}
		return result.toString();
	}
}

/** 
 * CodeStringParser method
 * 
 * @author unknown
 *
 */
class CodeStringParser {
	Method hit;
	char[] match;
	int length;
	Object parent;
	int pos;

	public CodeStringParser(Object p, String m, Method h) {
		parent = p;
		match = m.toCharArray();
		length = m.length();
		hit = h;
		pos = 0;
	}

	//TODO: Should this be renamed to cLoc or something more appropriate than clock?
	/**
	 * clock method
	 * 
	 * @param in
	 * @return boolean
	 */
	public boolean clock(char in) {
		if (in == match[pos]) {
			pos++;

			if (pos == length) {
				pos = 0;
				return true;
			}
		} else {
			pos = 0;
		}
		return false;
	}

	/**
	 * getValue method
	 * 
	 * @return value
	 */
	public String getValue() {
		try {
			return (String) hit.invoke((Object) parent, (Object) null);
		} catch (Exception e) {
			return new String("<Unhandled Exception occcured>");
		}
	}

	/**
	 * reset method - reset (int) pos to 0
	 */
	public void reset() {
		pos = 0;
	}
}
