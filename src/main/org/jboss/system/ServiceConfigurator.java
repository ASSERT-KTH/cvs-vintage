/*
* JBoss, the OpenSource J2EE server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/

package org.jboss.system;

import java.io.Writer;   
import java.util.Hashtable;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;

import javax.management.MBeanInfo;
import javax.management.Attribute;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.MBeanAttributeInfo;
import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Text;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

import org.jboss.logging.Log;
import org.jboss.util.DOMWriter;



/**
*
*
* @author  <a href="mailto:marc@jboss.org">Marc Fleury</a>`
* @version $Revision: 1.1 $
*
*   <p><b>20010830 marc fleury:</b>
*   <ul>
*      initial import
*   <li> 
*   </ul>
*/

public class ServiceConfigurator
{
	
	/** Primitive type name -> class map. */
	private static Hashtable primitives = new Hashtable();
	
	/** Setup the primitives map. */
	static {
		primitives.put("int", Integer.TYPE);
		primitives.put("boolean", Boolean.TYPE);
		primitives.put("double", Double.TYPE);
		primitives.put("float", Float.TYPE);
		primitives.put("long", Long.TYPE);
	}
	
	/** The MBean server which this service is registered in. */
	private MBeanServer server;
	private Log log = Log.createLog("Configurator");
;
	
	// Constructors --------------------------------------------------
	
	public ServiceConfigurator(MBeanServer server) 
	{
		this.server = server;
	}
	// Public  -------------------------------------------------------
	
	/**
	* Builds a string that consists of the configuration elements of
	* the currently running MBeans registered in the server.
	*
	* @throws Exception    Failed to construct configuration.
	*/
	public String getConfiguration(ObjectName[] objectNames) 
	throws Exception 
	{
		Writer out = new StringWriter();
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();
		
		Element serverElement = doc.createElement("server");
		
		// Store attributes as XML
		for (int j = 0 ; j<objectNames.length ; j++) 
		{
			ObjectInstance instance = server.getObjectInstance(objectNames[j]);
			ObjectName name = (ObjectName)instance.getObjectName();
			Element mbeanElement = doc.createElement("mbean");
			mbeanElement.setAttribute("name", name.toString());
			
			MBeanInfo info = server.getMBeanInfo(name);
			mbeanElement.setAttribute("code", info.getClassName());
			MBeanAttributeInfo[] attributes = info.getAttributes();
			boolean hasAttributes = true;
			for (int i = 0; i < attributes.length; i++) {
				if (attributes[i].isReadable() && isAttributeWriteable(server.getObjectInstance(name).getClassName(), attributes[i].getName(), attributes[i].getType())) {
					if (!attributes[i].isWritable()) {
						log.debug("Detected JMX Bug: Server reports attribute '"+attributes[i].getName() + "' is not writeable for MBean '" + name.getCanonicalName() + "'");
					}
					Element attributeElement = doc.createElement("attribute");
					Object value = server.getAttribute(name, attributes[i].getName());
					
					attributeElement.setAttribute("name", attributes[i].getName());
					
					if (value != null) {
						attributeElement.appendChild(doc.createTextNode(value.toString()));
					}
					
					mbeanElement.appendChild(attributeElement);
					hasAttributes = true;
				}
			}
			
			if (hasAttributes) {
				serverElement.appendChild(mbeanElement);
			}
		}
		
		doc.appendChild(serverElement);
		
		// Write configuration
		(new DOMWriter(out, false)).print(doc, true);
		
		out.close();
		
		// Return configuration
		return out.toString();
	}
	
	
	// Public -----------------------------------------------------
	
	public void configure(Element mbeanElement) 
	throws Exception 
	{
		
		// Set configuration to MBeans from XML
		
		// get the name of the mbean
		ObjectName objectName = parseObjectName(mbeanElement);
		
		MBeanInfo info;
		try {
			info = server.getMBeanInfo(objectName);
		} catch (InstanceNotFoundException e) {
			// The MBean is no longer available
			// It's ok, just return
			return;
		}
		
		// Set attributes
		NodeList attrs = mbeanElement.getElementsByTagName("attribute");
		for (int j = 0; j < attrs.getLength(); j++) {
			Element attributeElement = (Element)attrs.item(j);
			String attributeName = attributeElement.getAttribute("name");
			if (attributeElement.hasChildNodes()) {
				
				// Get the attribute value
				String attributeValue = ((Text)attributeElement.getFirstChild()).getData().trim();
				
				MBeanAttributeInfo[] attributes = info.getAttributes();
				for (int k = 0; k < attributes.length; k++) {
					if (attributeName.equals(attributes[k].getName())) {
						String typeName = attributes[k].getType();
						Class typeClass;
						if (primitives.containsKey(typeName)) {
							typeClass = (Class)primitives.get(typeName);
						} else {
							typeClass = Class.forName(typeName);
						}
						PropertyEditor editor = PropertyEditorManager.findEditor(typeClass);
						editor.setAsText(attributeValue);
						Object value = editor.getValue();
						
						log.debug(attributeName + " set to " + attributeValue + " in " + objectName);
						server.setAttribute(objectName, new Attribute(attributeName, value));
						
						break;
					}
				}
			}
		}
	}
	
	/**
	* Parse an object name from the given element attribute 'name'.
	*
	* @param element    Element to parse name from.
	* @return           Object name.
	*
	* @throws ConfigurationException   Missing attribute 'name'
	*                                  (thrown if 'name' is null or "").
	* @throws MalformedObjectNameException
	*/
	private ObjectName parseObjectName(final Element element)
	throws ConfigurationException, MalformedObjectNameException
	{
		String name = element.getAttribute("name");
		if (name == null || name.trim().equals("")) {
			throw new ConfigurationException
			("MBean attribute 'name' must be given.");
		}
		
		return new ObjectName(name);
	}
	
	/**
	* Checks if an attribute of a given class is writtable.
	*
	* @param className     The name of the class to check.
	* @param attribute     The name of the attribute to check.
	* @param type          The attribute type that the setter takes.
	*
	* @throws Exception    Unable to determin if attribute is writable.
	*/
	private boolean isAttributeWriteable(final String className,
		final String attribute,
		final String type)
	{
		Class arg = null;
		Class cls = null;
		try {
			if (type.equals("int"))
				arg = Integer.TYPE;
			else if (type.equals("boolean"))
				arg = Boolean.TYPE;
			else if (type.equals("float"))
				arg = Float.TYPE;
			else if (type.equals("byte"))
				arg = Byte.TYPE;
			else if (type.equals("short"))
				arg = Short.TYPE;
			else if (type.equals("char"))
				arg = Character.TYPE;
			else if (type.equals("long"))
				arg = Long.TYPE;
			else if (type.equals("double"))
				arg = Double.TYPE;
			else
				arg = Class.forName(type);
		} catch (ClassNotFoundException e) {
			log.error("Unable to check parameter of type '" + type + "'");
			return false;
		}
		
		try {
			cls = Class.forName(className);
		} catch (ClassNotFoundException e) {
			log.error("Unable to check MBean of type '" + className + "'");
			return false;
		}
		
		try {
			Method m = cls.getMethod("set" + attribute, new Class[] { arg });
			return isSetterMethod(m);
		} catch (NoSuchMethodException ignore) {}
		
		return false;
	}

	/**
	* Check if the given method is a "setter" method.
	*
	* @param m     The method to check.
	* @return      True if the method is a "setter" method.
	*/
	private boolean isSetterMethod(final Method m) {
		if (m != null) {
			return
			Modifier.isPublic(m.getModifiers()) &&
			!Modifier.isStatic(m.getModifiers()) &&
			m.getReturnType().equals(Void.TYPE);
		}
		
		return false;
	}
}



