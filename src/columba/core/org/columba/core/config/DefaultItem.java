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
package org.columba.core.config;

import java.util.StringTokenizer;

import org.columba.core.xml.XmlElement;

/**
 * Composition wrapper for <code>XmlElement</code>. Provides many convinience
 * methods for easy access.
 * 
 * @author fdietz
 */
public class DefaultItem implements Cloneable, IDefaultItem {
	XmlElement root;

	public DefaultItem(XmlElement root) {
		this.root = root;
	}

	public XmlElement getRoot() {
		return root;
	}

	/** ********************** composition pattern ********************* */
	public XmlElement getElement(String pathToElement) {

		/*
		 * XmlElement child = getRoot().getElement(pathToElement);
		 * 
		 * return child;
		 */
		return getElement(pathToElement, true);
	}

	public XmlElement getElement(String pathToElement, boolean create) {
		XmlElement child = getRoot();

		StringTokenizer tok = new StringTokenizer(pathToElement, "/");
		while (tok.hasMoreTokens()) {
			String token = (String) tok.nextToken();
			XmlElement e = child.getElement(token);
			if (e == null && create) {
				e = child.addSubElement(token);

			}
			child = e;

		}

		return child;
	}

	public XmlElement getChildElement(int index) {
		return getRoot().getElement(index);
	}

	public int getChildCount() {
		return getRoot().count();
	}

	public XmlElement getChildElement(String pathToElement, int index) {
		return getRoot().getElement(pathToElement).getElement(index);
	}

	public boolean contains(String key) {
		return getRoot().getAttributes().containsKey(key);
	}

	public String get(String key) {
		return getRoot().getAttribute(key);
	}

	public String getString(String pathToElement, String key) {
		XmlElement element = getElement(pathToElement);

		if (element != null) {
      if (key == null || key.length() == 0)
        return element.getData();
      else
        return element.getAttribute(key);
		} else {
			return null;
		}
	}

	public void setString(String key, String newValue) {
		getRoot().addAttribute(key, newValue);
	}

	public void setString(String pathToElement, String key, String newValue) {
		XmlElement element = getElement(pathToElement);
		if (element == null) {
			element = root.addSubElement(pathToElement);
		}

		element.addAttribute(key, newValue);
	}

	/** ************************** helper classes ************************** */
	public int getInteger(String key) {
		String value = get(key);

		return Integer.parseInt(value);
	}

	public int getIntegerWithDefault(String key, int defaultValue) {
		String value = get(key);

		if (value == null) {
			value = new Integer(defaultValue).toString();
			setString(key, value);
		}

		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public int getInteger(String pathToElement, String key) {
		String value = getString(pathToElement, key);

		return Integer.parseInt(value);
	}

	public int getIntegerWithDefault(String pathToElement, String key, int defaultValue) {
		String value = getString(pathToElement, key);

		if (value == null) {
			value = new Integer(defaultValue).toString();
			setString(pathToElement, key, value);
		}

		int result=-1;
		try {
			result = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			// this is no integer value
			return defaultValue;
		}
		
		return result;
	}

	public void setInteger(String key, int value) {
		setString(key, Integer.toString(value));
	}

	public void setInteger(String pathToElement, String key, int value) {
		setString(pathToElement, key, Integer.toString(value));
	}

	public boolean getBooleanWithDefault(String key, boolean defaultValue) {
		String value = get(key);

		if (value == null) {
			value = Boolean.toString(defaultValue);
			setString(key, value);
		}

		return Boolean.valueOf(value).booleanValue();
	}

	public boolean getBoolean(String key) {
		String value = get(key);

		return Boolean.valueOf(value).booleanValue();
	}

	public boolean getBoolean(String pathToElement, String key) {
		String value = getString(pathToElement, key);

		return Boolean.valueOf(value).booleanValue();
	}

	public boolean getBooleanWithDefault(String pathToElement, String key,
			boolean defaultValue) {
		String value = getString(pathToElement, key);

		if (value == null) {
			value = Boolean.valueOf(defaultValue).toString();
			setString(pathToElement, key, value);
		}

		return Boolean.valueOf(value).booleanValue();
	}

	public void setBoolean(String key, boolean value) {
		setString(key, value ? Boolean.TRUE.toString() : Boolean.FALSE
				.toString());
	}

	public void setBoolean(String pathToElement, String key, boolean value) {
		setString(pathToElement, key, value ? Boolean.TRUE.toString()
				: Boolean.FALSE.toString());
	}

	/** {@inheritDoc} */
	public boolean equals(Object obj) {
		boolean equal = false;

		if ((obj != null) && (obj instanceof IDefaultItem)) {
			DefaultItem other = (DefaultItem) obj;

			if ((root == other.root)
					|| ((root != null) && root.equals(other.root))) {
				equal = true;
			}
		}

		return equal;
	}

	/** {@inheritDoc} */
	public int hashCode() {
		int hashCode = 43;

		if (root != null) {
			hashCode += (root.hashCode() * 97);
		}

		return hashCode;
	}

	/** {@inheritDoc} */
	public Object clone() {
		try {
			DefaultItem other = (DefaultItem) super.clone();
			other.root = (XmlElement) root.clone(); // make a deep copy

			return other;
		} catch (CloneNotSupportedException cnse) {
			throw new InternalError("Could not clone DefaultItem: " + cnse);
		}
	}

	/**
	 * @param string
	 * @param string2
	 * @return
	 */
	public String getStringWithDefault(String key, String defaultValue) {
		String result = (String) getRoot().getAttribute(key);
		if (result == null) {
			result = defaultValue;
		}
		return result;
	}

	/**
	 * @see org.columba.core.config.IDefaultItem#getStringWithDefault(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public String getStringWithDefault(String pathToElement, String key, String defaultValue) {
		String value = getString(pathToElement, key);

		if (value == null) {
			setString(pathToElement, key, value);
		}
		
		return defaultValue;
	}
	
	public void notifyObservers(String path) {
		XmlElement e = getElement(path);
		e.notifyObservers();
	}
}