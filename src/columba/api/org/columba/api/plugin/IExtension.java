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



/**
 * An extension providing the metadata of an extension and the runtime
 * context for instanciation.
 * 
 * @author fdietz
 */
public interface IExtension {

	/**
	 * Get extension metadata, describing the extension and the 
	 * runtime context.
	 * 
	 * @return
	 */
	public ExtensionMetadata getMetadata();
	
	/**
	 * Instanciate the extension. Method automatically uses the
	 * constructor matching the argument list
	 * 
	 * @param arguments			argument array
	 * @return					extension instance
	 * @throws PluginException	
	 */
	public IExtensionInterface instanciateExtension(Object[] arguments) throws PluginException;
	
	/**
	 * Check if this is an internal extension. If its an external 
	 * extension, then its specified using the <code>plugin.xml</code
	 * file and uses a different classloader. 
	 * 
	 * @return	true, if extension is internal. False, otherwise.
	 * 
	 */
	public boolean isInternal();
	
	/**
	 * An extension can be disabled if an error occured on instanciation. 
	 * @return	true, if enabled. False, otherwise.
	 */
	public boolean isEnabled();

}
