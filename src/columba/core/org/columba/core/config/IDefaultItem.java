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
package org.columba.core.config;

import org.columba.core.xml.XmlElement;

/**
 * @author fdietz
 *
 */
public interface IDefaultItem {
	XmlElement getRoot();

	/************************ composition pattern **********************/
	XmlElement getElement(String pathToElement);

	XmlElement getChildElement(int index);

	int getChildCount();

	XmlElement getChildElement(String pathToElement, int index);

	boolean contains(String key);

	String get(String key);

	String get(String pathToElement, String key);

	/*
	 public String get(String pathToElement, String key, String defaultValue) {
	 XmlElement parent = getElement(pathToElement);

	 return parent.getAttribute(key, defaultValue);
	 }
	 */void set(String key, String newValue);

	void set(String pathToElement, String key, String newValue);

	/**************************** helper classes ***************************/
	int getInteger(String key);

	int getInteger(String key, int defaultValue);

	int getInteger(String pathToElement, String key);

	int getInteger(String pathToElement, String key, int defaultValue);

	void set(String key, int value);

	void set(String pathToElement, String key, int value);

	boolean getBoolean(String key, boolean defaultValue);

	boolean getBoolean(String key);

	boolean getBoolean(String pathToElement, String key);

	boolean getBoolean(String pathToElement, String key, boolean defaultValue);

	void set(String key, boolean value);

	void set(String pathToElement, String key, boolean value);

	/*
	 public DefaultItem(Document doc)
	 {
	 this.document = doc;
	 }
	 */boolean equals(Object obj);

	/** {@inheritDoc} */
	int hashCode();

	/** {@inheritDoc} */
	Object clone();

	/**
	 * @param string
	 * @param string2
	 * @return
	 */
	String getString(String key, String defaultValue);
}