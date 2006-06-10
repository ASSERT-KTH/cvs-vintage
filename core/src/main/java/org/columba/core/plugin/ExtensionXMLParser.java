package org.columba.core.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.columba.api.plugin.ExtensionHandlerMetadata;
import org.columba.api.plugin.ExtensionMetadata;
import org.columba.api.plugin.PluginMetadata;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Convenience methods for parsing the various xml-file resources.
 * 
 * @author Frederik Dietzs
 */
public class ExtensionXMLParser {

	private static final String XML_ELEMENT_EXTENSION = "extension";

	private static final String XML_ELEMENT_EXTENSIONLIST = "extensionlist";

	private static final String XML_ATTRIBUTE_DESCRIPTION = "description";

	private static final String XML_ATTRIBUTE_CATEGORY = "category";

	private static final String XML_ATTRIBUTE_VERSION = "version";

	private static final String XML_ATTRIBUTE_NAME = "name";

	private static final String XML_ELEMENT_HANDLERLIST = "handlerlist";

	private static final String XML_ATTRIBUTE_SINGLETON = "singleton";

	private static final String XML_ATTRIBUTE_ENABLED = "enabled";

	private static final String XML_ATTRIBUTE_CLASS = "class";

	private static final String XML_ATTRIBUTE_PARENT = "parent";

	private static final String XML_ATTRIBUTE_ID = "id";

	private static final String XML_ELEMENT_PROPERTIES = "properties";

	private static final java.util.logging.Logger LOG = java.util.logging.Logger
			.getLogger("org.columba.core.plugin");

	/**
	 * Parse IExtension enumeration metadata from xml file.
	 * 
	 * @param is
	 *            inputstream of xml extension file
	 * @param pluginMetadata
	 *            can be <code>null</code>, in case of internal plugin
	 * @param internal
	 *            true, if internal, False, otherwise.
	 * @return enumeration of <code>Extension</code>
	 */
	public Enumeration loadExtensionsFromStream(InputStream is,
			PluginMetadata pluginMetadata, boolean internal) {
		Vector<Extension> vector = new Vector<Extension>();

		Document doc = retrieveDocument(is);

		Element parent = doc.getRootElement();
	
		if (parent == null || !parent.getName().equals(XML_ELEMENT_EXTENSIONLIST)) {
			LOG.severe("missing <extensionlist> element");
			return null;
		}

		Iterator iterator = parent.getChildren().listIterator();
		Element extensionXmlElement;

		while (iterator.hasNext()) {
			extensionXmlElement = (Element) iterator.next();

			ExtensionMetadata metadata = parseExtensionMetadata(extensionXmlElement);

			if (internal == true)
				vector.add(new Extension(metadata, internal));
			else
				vector.add(new Extension(pluginMetadata, metadata));

		}

		return vector.elements();
	}

	/**
	 * Parse extension metadata.
	 * 
	 * @param extensionXmlElement
	 * @return
	 */
	public ExtensionMetadata parseExtensionMetadata(
			Element extensionXmlElement) {
		String id = extensionXmlElement.getAttributeValue(XML_ATTRIBUTE_ID);
		if (id == null) {
			LOG.severe("missing attribute \"id\"");
			return null;
		}

		String clazz = extensionXmlElement.getAttributeValue("class");
		if (clazz == null) {
			LOG.severe("missing attribute \"class\"");
			return null;
		}

		String enabledString = extensionXmlElement
				.getAttributeValue(XML_ATTRIBUTE_ENABLED);
		String singletonString = extensionXmlElement
				.getAttributeValue(XML_ATTRIBUTE_SINGLETON);

		Element attributesElement = extensionXmlElement
				.getChild(XML_ELEMENT_PROPERTIES);
		Map<String, String> attributes = new Hashtable<String, String>();
		if (attributesElement != null) {
			List list = attributesElement.getAttributes();
			for (int i=0; i<list.size(); i++) {
				Attribute a = (Attribute) list.get(i);
				attributes.put(a.getName(), a.getValue());
			}
		}

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
	public PluginMetadata parsePluginMetadata(Element pluginElement) {

		String id = pluginElement.getAttributeValue(XML_ATTRIBUTE_ID);
		String name = pluginElement.getAttributeValue(XML_ATTRIBUTE_NAME);
		String version = pluginElement.getAttributeValue(XML_ATTRIBUTE_VERSION);
		String enabled = pluginElement.getAttributeValue(XML_ATTRIBUTE_ENABLED);
		String category = pluginElement.getAttributeValue(XML_ATTRIBUTE_CATEGORY);
		String description = pluginElement
				.getAttributeValue(XML_ATTRIBUTE_DESCRIPTION);

		PluginMetadata pluginMetadata = new PluginMetadata(id, name,
				description, version, category, new Boolean(enabled)
						.booleanValue());

		return pluginMetadata;
	}

	/**
	 * @param vector
	 * @param xmlFile
	 * @return
	 */
	public  Enumeration<ExtensionHandlerMetadata> parseExtensionHandlerList(
			InputStream is) {
		Vector<ExtensionHandlerMetadata> vector = new Vector<ExtensionHandlerMetadata>();

		Document doc = retrieveDocument(is);
		
		Element list = doc.getRootElement();
		if (list == null || !list.getName().equals(XML_ELEMENT_HANDLERLIST)) {
			LOG.severe("element <handlerlist> expected.");
			return vector.elements();
		}

		Iterator it = list.getChildren().listIterator();
		while (it.hasNext()) {
			Element child = (Element) it.next();
			// skip non-matching elements
			if (child.getName().equals("handler") == false)
				continue;
			String id = child.getAttributeValue(XML_ATTRIBUTE_ID);
			String parent = child.getAttributeValue(XML_ATTRIBUTE_PARENT);

			ExtensionHandlerMetadata metadata = new ExtensionHandlerMetadata(
					id, parent);

			vector.add(metadata);
		}

		return vector.elements();
	}

	/**
	 * "plugin.xml" file parse.
	 * 
	 * @param is	inputstream of "plugin.xml" file
	 * @param hashtable
	 *            hashtable will be filled with Vector of all extensions
	 * @return plugin metadata
	 */
	public PluginMetadata parsePlugin(InputStream is, Hashtable hashtable) {
		Document doc = retrieveDocument(is);

		Element pluginElement = doc.getRootElement();

		PluginMetadata pluginMetadata = new ExtensionXMLParser()
				.parsePluginMetadata(pluginElement);

		// loop through all extensions this plugin uses
		Iterator it = pluginElement.getChildren().listIterator();
		while (it.hasNext() ) {
			Element extensionListXmlElement = (Element) it.next();
			
			// skip if no <extensionlist> element found
			if (extensionListXmlElement.getName().equals(
					XML_ELEMENT_EXTENSIONLIST) == false)
				continue;

			String extensionpointId = extensionListXmlElement
					.getAttributeValue(XML_ATTRIBUTE_ID);
			if (extensionpointId == null) {
				LOG.severe("missing extension point id attribute");
				continue;
			}

			Vector<ExtensionMetadata> vector = new Vector<ExtensionMetadata>();

			Iterator it2 = extensionListXmlElement.getChildren().listIterator();
			while (it2.hasNext() ) {
				Element extensionXmlElement = (Element) it2.next();

				// skip if no <extension> element found
				if (extensionXmlElement.getName().equals(XML_ELEMENT_EXTENSION) == false)
					continue;

				ExtensionMetadata extensionMetadata = new ExtensionXMLParser()
						.parseExtensionMetadata(extensionXmlElement);

				vector.add(extensionMetadata);

			}

			hashtable.put(extensionpointId, vector);
		}

		return pluginMetadata;
	}
	
	
	// retrieve JDom Document from inputstream
	private static Document retrieveDocument(InputStream is) {
		SAXBuilder builder = new SAXBuilder();
		builder.setIgnoringElementContentWhitespace(true);
		Document doc = null;
		try {
			doc = builder.build(is);
		} catch (JDOMException e) {
			LOG.severe(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			LOG.severe(e.getMessage());
			e.printStackTrace();
		}
		return doc;
	}
}
