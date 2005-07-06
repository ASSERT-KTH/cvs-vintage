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

import java.io.File;
import java.net.URL;
import java.util.Enumeration;

import org.columba.core.plugin.exception.PluginHandlerNotFoundException;
import org.columba.core.xml.XmlIO;

/**
 * Plugin manager is a singleton registry for all plugins and all
 * extension handlers.
 * 
 * @author fdietz
 *
 */
public interface IPluginManager {

	public void addHandler(String id, IExtensionHandler handler);
	public IExtensionHandler getHandler(String id) throws PluginHandlerNotFoundException;
	public String addPlugin(File folder);
	public XmlIO getConfiguration(String id);
	public PluginMetadata getPluginMetadata(String id);
	public URL getInfoURL(String id);
	public String[] getPluginIds();
	public Enumeration getPluginMetadataEnumeration();
	public void initPlugins();
	public void addHandlers(String xmlResource);
}
