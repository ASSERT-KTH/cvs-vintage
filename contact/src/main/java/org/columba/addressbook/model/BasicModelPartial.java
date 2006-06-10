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

/**
 * @author fdietz
 * 
 */
public class BasicModelPartial implements IBasicModelPartial {

	protected boolean contact;

	protected String id;

	protected String name;

	protected String description;

	/**
	 * Default constructor
	 * 
	 */
	public BasicModelPartial(boolean contact) {
		this.contact = contact;
	}

	public BasicModelPartial(String id, boolean contact) {
		if (id == null)
			throw new IllegalArgumentException("id == null");

		this.contact = contact;
		this.id = id;
	}

	public BasicModelPartial(String id, String name, boolean contact) {
		this(id, contact);

		if (name == null)
			throw new IllegalArgumentException("name == null");

		this.name = name;
	}

	/**
	 * @return Returns the contact.
	 */
	public boolean isContact() {
		return contact;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return name;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public IBasicModelPartial clone() {
		IBasicModelPartial p = new BasicModelPartial(id, name, contact);
		return p;
	}
}