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
import org.jdom.Document;
import org.jdom.Element;

/**
 * @author fdietz
 *
 */
public interface IContact {
	/**
	 * Get top-level xml element.
	 * 
	 * @return get top-level xml element
	 */
	public abstract Element getRootElement();

	/**
	 * Set CDATA value in xml element.
	 * 
	 * @param key
	 *            selected xml element
	 * @param value
	 *            CDATA text
	 */
	public abstract void formatSet(String key, String value);

	/**
	 * Set CDATA value in xml element.
	 * 
	 * @param key
	 *            selected xml element
	 * @param value
	 *            CDATA text
	 */
	public abstract void formatSet(String key, String prefix, String value);

	/**
	 * Set textual value of xml element.
	 * 
	 * @param key
	 *            selected xml element
	 * @param value
	 *            text value
	 */
	public abstract void set(String key, String value);

	/**
	 * Set textual value of xml child element.
	 * 
	 * @param key
	 *            selectd xml element
	 * @param prefix
	 *            child with element name prefix
	 * @param value
	 *            text value
	 */
	public abstract void set(String key, String prefix, String value);

	/**
	 * Get textual value of xml element.
	 * 
	 * @param key
	 *            selected xml element
	 * @return text value
	 */
	public abstract String get(String key);

	/**
	 * Get CDATA value of xml element.
	 * 
	 * @param key
	 *            selected xml element
	 * 
	 * @return CDATA value
	 */
	public abstract String formatGet(String key);

	/**
	 * Get textual data of xml child element.
	 * 
	 * @param key
	 *            selected xml element
	 * @param prefix
	 *            name of child element
	 * @return text data
	 */
	public abstract String get(String key, String prefix);

	public abstract boolean exists(String key, String prefix);
	
	public abstract boolean exists(String key);
	
	/**
	 * @return Returns the doc.
	 */
	public abstract Document getDocument();

	/**
	 * @return Returns the uid.
	 */
	public abstract Object getUid();

	public abstract void fillFullName(String fullName);
}