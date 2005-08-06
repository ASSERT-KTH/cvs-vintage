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
package org.columba.core.plugin;

import java.io.InputStream;
import java.util.Enumeration;

/**
 * Extension handler is a registry for extensions and resembles a hook
 * to extend Columba's functionality.
 * 
 * @author fdietz
 *
 */
public interface IExtensionHandler {

	/**
	 * Add new extension to handler.
	 * 
	 * @param id			extension id, unique for this extension handler
	 * 
	 * @param extension		extension
	 */
	public void addExtension(String id, IExtension extension);
	
	
	/**
	 * Add many extensions at once using a xml file.
	 * 
	 * @param is		xml file path
	 */
	public void loadExtensionsFromStream(InputStream is);
	
	/**
	 * Check if extension exists.
	 * 
	 * @param id		extension id
	 * @return			true, if extension exists. False, otherwise.
	 */
	public boolean exists(String id);
	
	
	/**
	 * Get extension.
	 * 
	 * @param id		extension id
	 * @return			extension
	 */
	public IExtension getExtension(String id);
	
	/** 
	 * Retrieve enumeration of all extensions.
	 * 
	 * @return	enumeration of IExtension
	 */
	public Enumeration getExtensionEnumeration();
	
	/**
	 * Retrieve enumeration of all external extensions.
	 * 
	 * @return	enumeration of IExtension
	 */
	public Enumeration getExternalExtensionsEnumeration();
	
	/**
	 * Retrieve array of all extension ids.
	 * 
	 * @return	String array of ids
	 */
	public String[] getPluginIdList();
	
	/**
	 * Get id of this extension handler.
	 * 
	 * @return		extension handler id
	 */
	public String getId();
}
