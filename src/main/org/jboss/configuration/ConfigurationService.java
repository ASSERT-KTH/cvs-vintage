/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.configuration;

import java.io.*;
import java.beans.*;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;

import javax.management.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import com.sun.xml.tree.*;

import org.jboss.logging.Log;
import org.jboss.util.ServiceMBeanSupport;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public class ConfigurationService
   extends ServiceMBeanSupport
   implements ConfigurationServiceMBean
{
   // Constants -----------------------------------------------------
	static Hashtable primitives = new Hashtable();
	
	static
	{
		primitives.put("int",Integer.TYPE);
		primitives.put("boolean",Boolean.TYPE);
		primitives.put("double",Double.TYPE);
		primitives.put("float",Float.TYPE);
	}
	
   // Attributes ----------------------------------------------------
	Log log = new Log(getName());
	
	String configurationUrl = "jboss.jcml";
	
	MBeanServer server;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
	public ObjectName getObjectName(MBeanServer server, ObjectName name)
	   throws javax.management.MalformedObjectNameException
	{
		this.server = server;
	   return new ObjectName(OBJECT_NAME);
	}
	
	public String getName()
	{
	   return "Configuration";
	}
	
	public void load(String configuration)
		throws Exception
	{
		try
		{
			// Parse XML
			Document doc;
			XmlDocumentBuilder xdb = new XmlDocumentBuilder();
			Parser parser = new com.sun.xml.parser.Parser();
			xdb.setParser(parser);
			
			try 
			{ 
				parser.parse(new InputSource(new StringReader(configuration)));
				doc = xdb.getDocument();
			} 
			catch (SAXException se) 
			{ 
			     throw new IOException(se.getMessage()); 
			} 
			
			// Set configuration to MBeans from XML
			NodeList nl = doc.getElementsByTagName("mbean");
			for (int i = 0; i < nl.getLength(); i++)
			{
				Element mbeanElement = (Element)nl.item(i);
				
				String name = mbeanElement.getAttribute("name");
				ObjectName objectName = new ObjectName(name);
				MBeanInfo info = server.getMBeanInfo(objectName);
				
				NodeList attrs = mbeanElement.getElementsByTagName("attribute");
				for (int j = 0; j < attrs.getLength(); j++)
				{
					Element attributeElement = (Element)attrs.item(j);
					String attributeName = attributeElement.getAttribute("name");
					if (attributeElement.hasChildNodes())
					{
						String attributeValue = ((Text)attributeElement.getFirstChild()).getData();
					
						MBeanAttributeInfo[] attributes = info.getAttributes();
						for (int k = 0; k < attributes.length; k++)
						{
							if (attributeName.equals(attributes[k].getName()))
							{
								String typeName = attributes[k].getType();
								Class typeClass;
								if (primitives.containsKey(typeName))
								{
									typeClass = (Class)primitives.get(typeName);
								} else
								{
									typeClass = Class.forName(typeName);
								}
								PropertyEditor editor = PropertyEditorManager.findEditor(typeClass);
								editor.setAsText(attributeValue);
								Object value = editor.getValue();
								
								log.log(attributeName +" set to "+attributeValue+" in "+name);
								server.setAttribute(objectName, new Attribute(attributeName, value));
								
								break;
							}
						}
					}
					
				}
			}
		} catch (Throwable e)
		{
			e.printStackTrace();
			throw (Exception)e;
		}
	}
	
   public String save()
		throws Exception
   {
		Writer out = new StringWriter();
		
		// Create new ProjectX XML doc
		XmlDocument doc = new XmlDocument();
		
		Element serverElement = doc.createElement("server");
		
		Iterator mbeans = server.queryNames(null, null).iterator();
		while (mbeans.hasNext())
		{
			ObjectName name = (ObjectName)mbeans.next();
			Element mbeanElement = doc.createElement("mbean");
			mbeanElement.setAttribute("name",name.toString());
			
			MBeanInfo info = server.getMBeanInfo(name);
			MBeanAttributeInfo[] attributes = info.getAttributes();
			boolean hasAttributes = false;
			for (int i = 0; i < attributes.length; i++)
			{
				if (attributes[i].isReadable() && attributes[i].isWritable())
				{
					Element attributeElement = doc.createElement("attribute");
					Object value = server.getAttribute(name, attributes[i].getName());
					
					attributeElement.setAttribute("name", attributes[i].getName());
					
					if (value != null)
						attributeElement.appendChild(doc.createTextNode(value.toString()));
					
					mbeanElement.appendChild(attributeElement);
					
					hasAttributes = true;
				}
			}
			
			if (hasAttributes)
				serverElement.appendChild(mbeanElement);
		}
		
		doc.appendChild(serverElement);
		
		// Write configuration
		doc.writeXml(new XmlWriteContext(out,3));
		out.close();
		
		// Return configuration
		return out.toString();
   }
	
   // Protected -----------------------------------------------------
}

