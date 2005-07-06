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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Vector;

import org.columba.core.io.DiskIO;
import org.columba.core.xml.XmlElement;
import org.columba.core.xml.XmlIO;

/**
 * Extension handler is a registry for extensions and resembles a hook to extend
 * Columba's functionality.
 * 
 * @author fdietz
 * 
 */
public class ExtensionHandler implements IExtensionHandler {

	private static final String XML_ELEMENT_PROPERTIES = "properties";

	private static final String XML_ATTRIBUTE_SINGLETON = "singleton";

	private static final String XML_ATTRIBUTE_ENABLED = "enabled";

	private static final String XML_ATTRIBUTE_ID = "id";

	private static final String XML_ELEMENT_EXTENSIONLIST = "extensionlist";

	private static final java.util.logging.Logger LOG = java.util.logging.Logger
			.getLogger("org.columba.core.plugin");

	private static final String RESOURCE_PATH = "org.columba.core.i18n.dialog";

	private String id;

	protected Hashtable map = new Hashtable();

	/**
	 * @param id
	 */
	public ExtensionHandler(String id) {
		this.id = id;
	}

	/**
	 * @see org.columba.core.plugin.IExtensionHandler#addExtension(java.lang.String,
	 *      org.columba.core.plugin.IExtension)
	 */
	public void addExtension(String id, IExtension extension) {
		if (id == null)
			throw new IllegalArgumentException("id == null");
		if (extension == null)
			throw new IllegalArgumentException("extension == null");

		if ( map.containsKey(id)) {
			LOG.severe("duplicate id="+id);
			return;
			//throw new IllegalArgumentException("duplicate id="+id);
		}
		
		LOG.finest("adding " + getId() + " extension: " + id);

		map.put(id, extension);

	}

	/**
	 * @see org.columba.core.plugin.IExtensionHandler#getExtension(java.lang.String)
	 */
	public IExtension getExtension(String id) {
		if (id == null)
			throw new IllegalArgumentException("id == null");

		if (map.containsKey(id))
			return (IExtension) map.get(id);

		return null;
	}

	/**
	 * @see org.columba.core.plugin.IExtensionHandler#getId()
	 */
	public String getId() {
		return id;
	}

	/**
	 * @see org.columba.core.plugin.IExtensionHandler#exists(java.lang.String)
	 */
	public boolean exists(String id) {
		return map.containsKey(id);
	}

	/**
	 * @see org.columba.core.plugin.IExtensionHandler#loadExtensionsFromFile(java.lang.String)
	 */
	public void loadExtensionsFromFile(String xmlResource) {
		XmlIO xmlFile = new XmlIO(DiskIO.getResourceURL(xmlResource));
		xmlFile.load();
		XmlElement parent = xmlFile.getRoot().getElement(
				ExtensionHandler.XML_ELEMENT_EXTENSIONLIST);
		if (parent == null) {
			LOG.severe("missing <extensionlist> element");
			return;
		}

		ListIterator iterator = parent.getElements().listIterator();
		XmlElement extension;

		while (iterator.hasNext()) {
			extension = (XmlElement) iterator.next();
			String id = extension
					.getAttribute(ExtensionHandler.XML_ATTRIBUTE_ID);
			if (id == null) {
				LOG.severe("missing attribute \"id\"");
				continue;
			}

			String clazz = extension.getAttribute("class");
			if (clazz == null) {
				LOG.severe("missing attribute \"class\"");
				continue;
			}

			String enabledString = extension
					.getAttribute(ExtensionHandler.XML_ATTRIBUTE_ENABLED);
			String singletonString = extension
					.getAttribute(ExtensionHandler.XML_ATTRIBUTE_SINGLETON);

			XmlElement attributesElement = extension
					.getElement(ExtensionHandler.XML_ELEMENT_PROPERTIES);
			Hashtable attributes = null;
			if (attributesElement != null)
				attributes = attributesElement.getAttributes();

			ExtensionMetadata metadata = null;
			if (attributes != null)
				metadata = new ExtensionMetadata(id, clazz, attributes);
			else
				metadata = new ExtensionMetadata(id, clazz);

			if (enabledString != null)
				metadata.setEnabled(new Boolean(enabledString).booleanValue());

			if (singletonString != null)
				metadata.setSingleton(new Boolean(singletonString)
						.booleanValue());

			addExtension(id, new Extension(metadata));
		}
	}

	/**
	 * @param id
	 */
	public void handlePluginError(String id) {

		// get plugin id
		IExtension extension = getExtension(id);
		ExtensionMetadata metadata = extension.getMetadata();

		LOG.severe("Failed to load extension= " + metadata.getId());
		LOG.severe("Classname= " + metadata.getClassname());

//		JOptionPane.showMessageDialog(null, new MultiLineLabel(MessageFormat
//				.format(GlobalResourceLoader.getString(RESOURCE_PATH,
//						"pluginmanager", "errLoad.msg"), new String[] { id })),
//				GlobalResourceLoader.getString(RESOURCE_PATH, "pluginmanager",
//						"errLoad.title"), JOptionPane.ERROR_MESSAGE);

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
	 * @see org.columba.core.plugin.IExtensionHandler#getPluginIdList()
	 */
	public String[] getPluginIdList() {
		Vector result = new Vector();
		Enumeration enum = map.elements();
		while (enum.hasMoreElements()) {
			IExtension extension = (IExtension) enum.nextElement();
			boolean enabled = extension.getMetadata().isEnabled();
			String id = extension.getMetadata().getId();

			result.add(id);
		}

		return (String[]) result.toArray(new String[0]);
	}

	/**
	 * @see org.columba.core.plugin.IExtensionHandler#getExtensionEnumeration()
	 */
	public Enumeration getExtensionEnumeration() {
		return map.elements();
	}

	/**
	 * @see org.columba.core.plugin.IExtensionHandler#getExternalExtensionsEnumeration()
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
}