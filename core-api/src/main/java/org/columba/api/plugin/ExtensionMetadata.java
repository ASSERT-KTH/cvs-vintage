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
package org.columba.api.plugin;

import java.util.Hashtable;
import java.util.Map;

public class ExtensionMetadata {

	private String id;
	private String classname;
	private boolean enabled=true;
	private boolean singleton=false;
	
	private Map<String, String> attributes;
	
	public ExtensionMetadata(String id, String classname, Map<String, String> attributes) {
		this(id, classname);
		
		if ( attributes == null ) throw new IllegalArgumentException("attributes == null");
		
		this.attributes = attributes; 
		
	}
	
	public ExtensionMetadata(String id, String classname) {
		if ( id == null ) throw new IllegalArgumentException("id == null");
		if ( classname == null) throw new IllegalArgumentException("classname == null");
		
		this.id = id;
		this.classname = classname;

		
 
		attributes = new Hashtable<String, String>();
	}

	/**
	 * @return Returns the classname.
	 */
	public String getClassname() {
		return classname;
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return Returns the enabled.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param enabled The enabled to set.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @return Returns the singleton.
	 */
	public boolean isSingleton() {
		return singleton;
	}

	/**
	 * @param global The singleton to set.
	 */
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}
	
	public String getAttribute(String key) {
		if ( key == null ) throw new IllegalArgumentException("key == null");
		
		return (String) attributes.get(key);
	}
	
}
