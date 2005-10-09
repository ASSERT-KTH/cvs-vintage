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
package org.columba.addressbook.model;

public class PhoneModel {

	public final static int TYPE_BUSINESS_PHONE = 0;
	public final static int TYPE_ASSISTANT_PHONE = 1;
	public final static int TYPE_BUSINESS_FAX = 2;
	public final static int TYPE_CALLBACK_PHONE = 3;
	public final static int TYPE_CAR_PHONE = 4;
	public final static int TYPE_COMPANY_PHONE = 5;
	public final static int TYPE_HOME_PHONE = 6;
	public final static int TYPE_HOME_FAX = 7;
	public final static int TYPE_ISDN = 8;
	public final static int TYPE_MOBILE_PHONE = 9;
	public final static int TYPE_OTHER_PHONE = 10;
	public final static int TYPE_OTHER_FAX = 11;
	public final static int TYPE_PAGER = 12;
	public final static int TYPE_PRIMARY_PHONE = 13;
	public final static int TYPE_RADIO = 14;
	public final static int TYPE_TELEX = 15;
	public final static int TYPE_TTY = 16;
	
	
	public final static String[] NAMES = new String[] { "BusinessPhone",
			"AssistantPhone", "BusinessFax", "CallbackPhone", "CarPhone",
			"CompanyPhone", "HomePhone", "HomeFax", "ISDN", "Mobil Phone",
			"OtherPhone", "OtherFax", "Pager", "PrimaryPhone", "Radio",
			"Telex", "TTY" };

	private String number;

	private int type;

	public PhoneModel(String number, int type) {
		this.number = number;
		this.type = type;

		if (type >= NAMES.length)
			throw new IllegalArgumentException("unsupported type");
	}

	public PhoneModel(String number, String type) {
		this.number = number;

		boolean foundMatch = false;
		for (int i = 0; i < NAMES.length; i++) {
			if (type.equals(NAMES[i])) {
				foundMatch = true;
				this.type = i;
			}
		}

		if (!foundMatch)
			throw new IllegalArgumentException("unsupported type: " + type);
	}

	/**
	 * @return Returns the number.
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}

	public String getTypeString() {
		return NAMES[type];
	}

}
