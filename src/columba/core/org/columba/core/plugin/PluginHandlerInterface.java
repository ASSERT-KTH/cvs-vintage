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
package org.columba.core.plugin;

import java.util.ListIterator;

import org.columba.core.xml.XmlElement;

/**
 * @author fdietz
 *
 * The PluginHandler is responsible for managing a plugin
 * extension point.
 * 
 * Plugins register at the plugin handler.
 * 
 * Use the plugin handler to load a plugin.
 * 
 */
public interface PluginHandlerInterface {

	/**
	 * return ID identification string of handler
	 * 
	 * @return	<class>String</class> containing ID
	 */
	public String getId();
	
	/**
	 * return top level xml tree node
	 * 
	 * all plugins are child nodes of this parent node
	 * 
	 * @return	<class>XmlElement</class> parent node
	 */
	public XmlElement getParent();
	
	/**
	 * 
	 * return list of external plugins
	 * 
	 * @return	<interface>List</interface> containing all external plugins
	 */
	public ListIterator getExternalPlugins();
	
}
