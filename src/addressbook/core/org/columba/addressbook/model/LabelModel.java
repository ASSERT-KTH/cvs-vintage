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

public class LabelModel {

	public static final String[] NAMES = new String[] { "work", "home", "other" };

	public static final int TYPE_WORK = 0;

	public static final int TYPE_HOME = 1;

	public static final int TYPE_OTHER = 2;

	private String label;

	int type;

	public LabelModel(String label, int type) {
		if (label == null)
			throw new IllegalArgumentException("label == null");

		this.label = label;

		if (type < 0 || type >= NAMES.length)
			throw new IllegalArgumentException("unsupported type =" + type);

		this.type = type;
	}

	public LabelModel(String label, String type) {
		if (label == null)
			throw new IllegalArgumentException("label == null");
		if (type == null)
			throw new IllegalArgumentException("type == null");

		boolean foundMatch = false;
		for (int i = 0; i < NAMES.length; i++) {
			if (type.equals(NAMES[i])) {
				foundMatch = true;
				this.type = i;
			}
		}

		if (!foundMatch)
			throw new IllegalArgumentException("unsupported type =" + type);
	}

	/**
	 * @return Returns the label.
	 */
	public String getLabel() {
		return label;
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
