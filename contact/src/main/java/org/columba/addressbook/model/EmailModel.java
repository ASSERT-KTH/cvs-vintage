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

public class EmailModel implements IEmailModel {

	public static final String[] NAMES = new String[] { "work", "home", "other" };

	public static final int TYPE_WORK = 0;

	public static final int TYPE_HOME = 1;

	public static final int TYPE_OTHER = 2;

	private String address;

	private int type;

	public EmailModel(String address, int type) {
		if (address == null)
			throw new IllegalArgumentException("address == null");
		if (type < 0 || type >=NAMES.length)
			throw new IllegalArgumentException("type is not supported= " + type);

		this.address = address;
		this.type = type;
	}

	public EmailModel(String address, String type) {
		if (address == null)
			throw new IllegalArgumentException("address == null");
		this.address = address;

		boolean foundMatch = false;
		for (int i = 0; i < NAMES.length; i++) {
			if (type.equals(NAMES[i])) {
				foundMatch = true;
				this.type = i;
			}
		}

		// backwards compatibility
		if (type.equals("internet")) {
			foundMatch = true;
			this.type = 0;
		}

		
		//tstich: CA-41 bugfix
		if( type.equals("x400")) {
			foundMatch = true;
			this.type = 0;
		}
		
		if (!foundMatch)
			throw new IllegalArgumentException("unsupported type: " + type);

	}

	/* (non-Javadoc)
	 * @see org.columba.addressbook.model.IEmailModel#getAddress()
	 */
	public String getAddress() {
		return address;
	}

	/* (non-Javadoc)
	 * @see org.columba.addressbook.model.IEmailModel#getType()
	 */
	public int getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see org.columba.addressbook.model.IEmailModel#getTypeString()
	 */
	public String getTypeString() {
		return NAMES[type];
	}
}
