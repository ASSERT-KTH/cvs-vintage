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

package org.columba.core.print;

import java.awt.Graphics2D;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Date;

public class cPrintVariable extends cParagraph {

	String codeString;
	CodeStringParser[] parser;
	int parserCount = -1;

	String[] keys = { "PAGE_NR", "PAGE_COUNT", "DATE_TODAY" };
	String[] methods = { "getPageNr", "getPageCount", "getDateToday" };

	// Methods called by the Variale parsers

	public String getPageNr() {
		return Integer.toString(page.getDocument().getPageNr(page));
	}

	public String getPageCount() {
		return Integer.toString(page.getDocument().getPageCount());
	}

	public String getDateToday() {
		DateFormat df = DateFormat.getDateInstance();
		return df.format(new Date());
	}

	// Class	

	public cPrintVariable() {
		Class c = this.getClass();
		parserCount = Array.getLength(keys);
		parser = new CodeStringParser[parserCount];

		for (int i = 0; i < parserCount; i++) {
			try {
				parser[i] =
					new CodeStringParser(this, keys[i], c.getMethod(methods[i], new Class[0]));
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}

	public void setCodeString(String n) {
		codeString = n;
		setText(n); // For getHeight() to return the rigth Value
	}

	public void print(Graphics2D g) {
		setText(processCodeString());

		super.print(g);
	}

	public String processCodeString() {
		int pos = 0;
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
						if (parser[j].clock(c))
							result.append(parser[j].getValue());
					}
				} else {
					result.append(c);
				}

			}

		}

		return result.toString();
	}

}

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

	public String getValue() {
		try {
			return (String) hit.invoke(parent, null);
		} catch (Exception e) {
			return new String("<unhandeled Exception occcured>");
		}
	}

	public void reset() {
		pos = 0;
	}

}
