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
package org.columba.mail.imap.parser;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ListTokenizer {

	private String s;
	private Interval i;

	public ListTokenizer( String str ) {
		this.s = str;
		i = new Interval();
	}

	public Item getNextItem() {
		Item result = new Item();

		// Search for next Item
		i.a = i.b + 1;
		// ..but Check Bounds!!
		if (i.a >= s.length())
			return null;
		while (s.charAt(i.a) == ' ') {
			i.a++;
			if (i.a >= s.length())
				return null;
		}

		// Quoted

		if (s.charAt(i.a) == '\"') {
			i.b = s.indexOf("\"", i.a + 1);

			result.setType(Item.STRING);
			result.setValue(s.substring(i.a + 1, i.b));
		}

		// Parenthesized

		else if (s.charAt(i.a) == '(') {
			i.b = getClosingParenthesis(s, i.a);

			result.setType(Item.PARENTHESIS);
			result.setValue(s.substring(i.a + 1, i.b));
		}

		// NIL or Number

		else {
			i.b = s.indexOf(" ", i.a + 1);
			if (i.b == -1)
				i.b = s.length();

			String item = s.substring(i.a, i.b);
			i.b--;

			/*
			if (item.equals("NIL")) {
				result.setType(Item.NIL);
			} else {
				result.setValue(new Integer(item));
				result.setType(Item.NUMBER);
			}
			*/
			result.setValue(item);
		}

		return result;
	}

	static public int getClosingParenthesis(String s, int openPos) {
		int nextOpenPos = s.indexOf("(", openPos + 1);
		int nextClosePos = s.indexOf(")", openPos + 1);

		while ((nextOpenPos < nextClosePos) & (nextOpenPos != -1)) {
			nextClosePos = s.indexOf(")", nextClosePos + 1);
			nextOpenPos = s.indexOf("(", nextOpenPos + 1);
		}
		return nextClosePos;
	}

	class Item {
		public static final int STRING = 0;
		public static final int PARENTHESIS = 1;
		public static final int NIL = 2;
		public static final int NUMBER = 3;

		private Object value;
		private int type;
		/**
		 * Returns the type.
		 * @return int
		 */
		public int getType() {
			return type;
		}

		/**
		 * Returns the value.
		 * @return Object
		 */
		public Object getValue() {
			return value;
		}

		/**
		 * Sets the type.
		 * @param type The type to set
		 */
		public void setType(int type) {
			this.type = type;
		}

		/**
		 * Sets the value.
		 * @param value The value to set
		 */
		public void setValue(Object value) {
			this.value = value;
		}

	}

	class Interval {
		public int a, b;
		public int type;

		public Interval(int a, int b) {
			this.a = a;
			this.b = b;
		}

		public Interval() {
			a = -1;
			b = -1;
		}

		public void reset() {
			a = -1;
			b = -2;
		}
	}

}
