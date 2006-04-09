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
import java.util.Hashtable;
import java.util.Vector;

import org.columba.api.plugin.ExtensionMetadata;
import org.columba.api.plugin.IExtension;
import org.columba.api.plugin.IExtensionHandler;
import org.columba.api.plugin.PluginMetadata;

/**
 * Extension handler is a registry for extensions and resembles a hook to extend
 * Columba's functionality.
 * 
 * @author fdietz
 * 
 */
public class ExtensionHandler implements IExtensionHandler {

	private static final java.util.logging.Logger LOG = java.util.logging.Logger
			.getLogger("org.columba.core.plugin");

	private String id;
	private String parent;
	
	protected Hashtable map = new Hashtable();

	/**
	 * @param id		unique extension handler id
	 * @param parent	unique id of this extension handlers parent, can be <code>null</code>
	 */
	public ExtensionHandler(String id, String parent) {
		if ( id == null ) throw new IllegalArgumentException("id == null");
		
		this.id = id;
	}

	/**
	 * @see org.columba.api.plugin.IExtensionHandler#addExtension(java.lang.String,
	 *      org.columba.api.plugin.IExtension)
	 */
	public void addExtension(String id, IExtension extension) {
		if (id == null)
			throw new IllegalArgumentException("id == null");
		if (extension == null)
			throw new IllegalArgumentException("extension == null");

		if (map.containsKey(id)) {
			LOG.severe("duplicate id=" + id);
			return;
		}

		map.put(id, extension);

	}

	/**
	 * @see org.columba.api.plugin.IExtensionHandler#getExtension(java.lang.String)
	 */
	public IExtension getExtension(String id) {
		if (id == null)
			throw new IllegalArgumentException("id == null");

		if (map.containsKey(id))
			return (IExtension) map.get(id);

		return null;
	}

	/**
	 * @see org.columba.api.plugin.IExtensionHandler#getId()
	 */
	public String getId() {
		return id;
	}

	/**
	 * @see org.columba.api.plugin.IExtensionHandler#exists(java.lang.String)
	 */
	public boolean exists(String id) {
		return map.containsKey(id);
	}

	/**
	 * @see org.columba.core.plugin.IExtensionHandler#loadExtensionsFromFile(java.lang.String)
	 */

	/**
	 * @param id
	 */
	public void handlePluginError(String id) {

		// get plugin id
		IExtension extension = getExtension(id);
		ExtensionMetadata metadata = extension.getMetadata();

		LOG.severe("Failed to load extension= " + metadata.getId());
		LOG.severe("Classname= " + metadata.getClassname());

		// JOptionPane.showMessageDialog(null, new MultiLineLabel(MessageFormat
		// .format(GlobalResourceLoader.getString(RESOURCE_PATH,
		// "pluginmanager", "errLoad.msg"), new String[] { id })),
		// GlobalResourceLoader.getString(RESOURCE_PATH, "pluginmanager",
		// "errLoad.title"), JOptionPane.ERROR_MESSAGE);

		// disable plugin

	}

	/**
	 * @return Returns the map.
	 */
	/**
	 * @return
	 */
	public Hashtable getMap() {
		return map;
	}

	/**
	 * @see org.columba.api.plugin.IExtensionHandler#getPluginIdList()
	 */
	public String[] getPluginIdList() {
		Vector result = new Vector();
		Enumeration _enum = map.elements();
		while (_enum.hasMoreElements()) {
			IExtension extension = (IExtension) _enum.nextElement();
			String id = extension.getMetadata().getId();

			result.add(id);
		}

		return (String[]) result.toArray(new String[0]);
	}

	/**
	 * @see org.columba.api.plugin.IExtensionHandler#getExtensionEnumeration()
	 */
	public Enumeration getExtensionEnumeration() {
		return map.elements();
	}

	/**
	 * @see org.columba.api.plugin.IExtensionHandler#getExternalExtensionsEnumeration()
	 */
	public Enumeration getExternalExtensionsEnumeration() {
		Enumeration e = getExtensionEnumeration();

		Vector v = new Vector();
		while (e.hasMoreElements()) {
			IExtension extension = (IExtension) e.nextElement();
			if (extension.isInternal() == false)
				v.add(extension);
		}

		return v.elements();
	}

	/**
	 * @see org.columba.api.plugin.IExtensionHandler#loadExtensionsFromStream(InputStream)
	 */
	public void loadExtensionsFromStream(InputStream is) {
		Enumeration e = new ExtensionXMLParser().loadExtensionsFromStream(is,
				null, true);
		while (e.hasMoreElements()) {
			IExtension extension = (IExtension) e.nextElement();
			addExtension(extension.getMetadata().getId(), extension);
		}
	}

	public void loadExternalExtensionsFromStream(PluginMetadata pluginMetadata,
			InputStream is) {
		Enumeration e = new ExtensionXMLParser().loadExtensionsFromStream(is,
				pluginMetadata, false);
		while (e.hasMoreElements()) {
			IExtension extension = (IExtension) e.nextElement();
			addExtension(extension.getMetadata().getId(), extension);
		}
	}
}