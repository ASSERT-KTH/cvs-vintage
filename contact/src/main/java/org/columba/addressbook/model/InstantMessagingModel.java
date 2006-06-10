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

public class InstantMessagingModel {

	public final static int TYPE_JABBER = 0;
	public final static int TYPE_AIM = 1;
	public final static int TYPE_YAHOO = 2;
	public final static int TYPE_MSN = 3;
	public final static int TYPE_ICQ = 4;
	
	public static final String[] NAMES = new String[] { "Jabber", "AIM",
			"Yahoo", "MSN", "ICQ" };

	private String userId;

	private int type;

	public InstantMessagingModel(String userId, String type) {
		if (userId == null)
			throw new IllegalArgumentException("userId == null");
		if (type == null)
			throw new IllegalArgumentException("type == null");

		this.userId = userId;

		boolean foundMatch = false;
		for (int i = 0; i < NAMES.length; i++) {
			if (type.equals(NAMES[i])) {
				foundMatch = true;
				this.type = i;
			}
		}

		if (!foundMatch)
			throw new IllegalArgumentException("unsupported type= " + type);
	}

	public InstantMessagingModel(String userId, int type) {
		if (userId == null)
			throw new IllegalArgumentException("userId == null");
		if (type < 0 || type >= NAMES.length)
			throw new IllegalArgumentException("unsupported type= " + type);

		this.userId = userId;
		this.type = type;
	}

	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return Returns the userId.
	 */
	public String getUserId() {
		return userId;
	}

	public String getTypeString() {
		return NAMES[type];
	}

}
