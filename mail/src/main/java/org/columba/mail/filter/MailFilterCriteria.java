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
package org.columba.mail.filter;

import org.columba.core.filter.FilterCriteria;
import org.columba.core.xml.XmlElement;

/**
 * @author fdietz
 *  
 */
public class MailFilterCriteria extends FilterCriteria {

	//	 header-item
	private static final String HEADERFIELD = "headerfield";

	public final static int SUBJECT = 0;

	public final static int FROM = 1;

	public final static int TO = 2;

	public final static int CC = 3;

	public final static int BCC = 4;

	public final static int DATE = 5;

	public final static int SIZE = 6;

	public final static int BODY = 7;

	public final static int FLAGS = 8;

	public final static int PRIORITY = 9;

	public final static int CUSTOM_HEADERFIELD = 10;
	
	public final static int COLOR = 11;
	
	public final static int ACCOUNT = 12;

	private final String[] type = { "Subject", "From", "To", "Cc", "Bcc",
			"Date", "Size", "Body", "Flags", "Priority", "Custom Headerfield", "Color", "Account" };

	public MailFilterCriteria() {
		super();
	}

	public MailFilterCriteria(FilterCriteria c) {
		super(c.getRoot());
	}

	/**
	 * @param root
	 */
	public MailFilterCriteria(XmlElement root) {
		super(root);
	}

	/**
	 * @see org.columba.core.filter.FilterCriteria#getType()
	 */
	public int getType() {

		int result = -1;

		String h = getTypeString();

		for (int i = 0; i < type.length; i++) {
			if (h.equals(type[i]))
				result = i;
		}

		return result;
	}

	public void setType(int typeIndex) {
		super.setTypeString(type[typeIndex]);
	}

	public String getHeaderfieldString() {
		return getRoot().getAttribute(MailFilterCriteria.HEADERFIELD);
	}

	public void setHeaderfieldString(String s) {
		getRoot().addAttribute(MailFilterCriteria.HEADERFIELD, s);
	}

}