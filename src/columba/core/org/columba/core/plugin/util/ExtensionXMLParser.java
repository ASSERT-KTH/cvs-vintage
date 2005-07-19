package org.columba.core.plugin.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Vector;

import org.columba.core.io.DiskIO;
import org.columba.core.plugin.Extension;
import org.columba.core.plugin.ExtensionHandlerMetadata;
import org.columba.core.plugin.ExtensionMetadata;
import org.columba.core.plugin.PluginMetadata;
import org.columba.core.xml.XmlElement;
import org.columba.core.xml.XmlIO;


/**
 * Convenience methods for parsing the various xml-file resources.
 * 
 * @author Frederik Dietzs
 */
public class ExtensionXMLParser {

	private static final String XML_ELEMENT_EXTENSION = "extension";

	private static final String XML_ELEMENT_EXTENSIONLIST = "extensionlist";

	private static final String XML_ATTRIBUTE_TYPE = "type";

	private static final String XML_ELEMENT_JAR = "jar";

	private static final String XML_ELEMENT_RUNTIME = "runtime";

	private static final String XML_ATTRIBUTE_DESCRIPTION = "description";

	private static final String XML_ATTRIBUTE_CATEGORY = "category";

	private static final String XML_ATTRIBUTE_VERSION = "version";

	private static final String XML_ATTRIBUTE_NAME = "name";

	private static final String XML_ELEMENT_HANDLERLIST = "handlerlist";

	private static final String XML_ATTRIBUTE_SINGLETON = "singleton";

	private static final String XML_ATTRIBUTE_ENABLED = "enabled";

	private static final String XML_ATTRIBUTE_CLASS = "class";

	private static final String XML_ATTRIBUTE_ID = "id";

	private static final String XML_ELEMENT_PROPERTIES = "properties";

	private static final java.util.logging.Logger LOG = java.util.logging.Logger
			.getLogger("org.columba.core.plugin");

	/**
	 * Parse IExtension enumeration metadata from xml file.
	 * 
	 * @param xmlResource
	 *            path to xml resource file
	 * 
	 * @return IExtension enumeration
	 */
	public static Enumeration loadExtensionsFromFile(String xmlResource) {
		Vector vector = new Vector();

		URL url = DiskIO.getResourceURL(xmlResource);
		if ( url == null) 
			url = ExtensionXMLParser.class.getResource(xmlResource);
		
		XmlIO xmlFile = new XmlIO(url);
		xmlFile.load();
		XmlElement parent = xmlFile.getRoot().getElement(
				XML_ELEMENT_EXTENSIONLIST);
		if (parent == null) {
			LOG.severe("missing <extensionlist> element");
			return null;
		}

		ListIterator iterator = parent.getElements().listIterator();
		XmlElement extensionXmlElement;

		while (iterator.hasNext()) {
			extensionXmlElement = (XmlElement) iterator.next();

			ExtensionMetadata metadata = parseExtensionMetadata(extensionXmlElement);

			vector.add(new Extension(metadata));
		}

		return vector.elements();
	}

	/**
	 * Parse extension metadata.
	 * 
	 * @param extensionXmlElement
	 * @return
	 */
	public static ExtensionMetadata parseExtensionMetadata(
			XmlElement extensionXmlElement) {
		String id = extensionXmlElement.getAttribute(XML_ATTRIBUTE_ID);
		if (id == null) {
			LOG.severe("missing attribute \"id\"");
			return null;
		}

		String clazz = extensionXmlElement.getAttribute("class");
		if (clazz == null) {
			LOG.severe("missing attribute \"class\"");
			return null;
		}

		String enabledString = extensionXmlElement
				.getAttribute(XML_ATTRIBUTE_ENABLED);
		String singletonString = extensionXmlElement
				.getAttribute(XML_ATTRIBUTE_SINGLETON);

		XmlElement attributesElement = extensionXmlElement
				.getElement(XML_ELEMENT_PROPERTIES);
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
			metadata.setSingleton(new Boolean(singletonString).booleanValue());

		return metadata;
	}

	/**
	 * Parse plugin metadata.
	 * 
	 * @param pluginElement
	 * @return
	 */
	public static PluginMetadata parsePluginMetadata(XmlElement pluginElement) {

		String id = pluginElement.getAttribute(XML_ATTRIBUTE_ID);
		String name = pluginElement.getAttribute(XML_ATTRIBUTE_NAME);
		String version = pluginElement.getAttribute(XML_ATTRIBUTE_VERSION);
		String enabled = pluginElement.getAttribute(XML_ATTRIBUTE_ENABLED);
		String category = pluginElement.getAttribute(XML_ATTRIBUTE_CATEGORY);
		String description = pluginElement
				.getAttribute(XML_ATTRIBUTE_DESCRIPTION);

		PluginMetadata pluginMetadata = new PluginMetadata(id, name,
				description, version, category, new Boolean(enabled)
						.booleanValue());

		return pluginMetadata;
	}

	/**
	 * Parse extension handler list.
	 * 
	 * @param xmlResource
	 * @return
	 */
	public static Enumeration parseExtensionHandlerlist(String xmlResource) {
		Vector vector = new Vector();
		XmlIO xmlFile = new XmlIO(DiskIO.getResourceURL(xmlResource));
		xmlFile.load();

		XmlElement list = xmlFile.getRoot().getElement(XML_ELEMENT_HANDLERLIST);
		if (list == null) {
			LOG.severe("element <handlerlist> expected.");
			return vector.elements();
		}

		Iterator it = list.getElements().iterator();
		while (it.hasNext()) {
			XmlElement child = (XmlElement) it.next();
			// skip non-matching elements
			if (child.getName().equals("handler") == false)
				continue;
			String id = child.getAttribute(XML_ATTRIBUTE_ID);
			String clazz = child.getAttribute(XML_ATTRIBUTE_CLASS);

			ExtensionHandlerMetadata metadata = new ExtensionHandlerMetadata(
					id, clazz);

			vector.add(metadata);
		}

		return vector.elements();
	}

	/**
	 * "plugin.xml" file parse.
	 * 
	 * @param pluginXmlFile		"plugin.xml" containing the plugin metadata
	 * @param hashtable			hashtable will be filled with Vector of all extensions
	 * @return					plugin metadata
	 */
	public static PluginMetadata parsePlugin(File pluginXmlFile,
			Hashtable hashtable) {
		XmlIO config = new XmlIO();

		try {
			config.setURL(pluginXmlFile.toURL());
		} catch (MalformedURLException mue) {
		}

		config.load();

		XmlElement pluginElement = config.getRoot().getElement("/plugin");

		PluginMetadata pluginMetadata = ExtensionXMLParser
				.parsePluginMetadata(pluginElement);

		// loop through all extensions this plugin uses
		for (int j = 0; j < pluginElement.count(); j++) {
			XmlElement extensionListXmlElement = pluginElement.getElement(j);

			// skip if no <extensionlist> element found
			if (extensionListXmlElement.getName().equals(
					XML_ELEMENT_EXTENSIONLIST) == false)
				continue;

			String extensionpointId = extensionListXmlElement
					.getAttribute(XML_ATTRIBUTE_ID);

			Vector vector = new Vector();

			for (int k = 0; k < extensionListXmlElement.count(); k++) {
				XmlElement extensionXmlElement = extensionListXmlElement
						.getElement(k);

				// skip if no <extension> element found
				if (extensionXmlElement.getName().equals(XML_ELEMENT_EXTENSION) == false)
					continue;

				ExtensionMetadata extensionMetadata = ExtensionXMLParser
						.parseExtensionMetadata(extensionXmlElement);
		
				vector.add(extensionMetadata);

			}
			
			hashtable.put(extensionpointId, vector);
		}

		return pluginMetadata;
	}
}
