/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.system;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.StringTokenizer;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jboss.deployment.DeploymentException;
import org.jboss.logging.Logger;
import org.jboss.util.DOMWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
* Service configuration helper.
*
* @author <a href="mailto:marc@jboss.org">Marc Fleury</a>
* @author <a href="mailto:hiram@jboss.org">Hiram Chirino</a>
* @version $Revision: 1.17 $
*
* <p><b>20010830 marc fleury:</b>
* <ul>
*   <li>Initial import
* </ul>
* <p><b>20010831 hiram chirino:</b>
* <ul>
*   <li>Added suppport for org.w3c.dom.Element type mbean attributes.
*      The first child Element of the &lt;attribute ...&gt; is used
*      to set the value of the attribute.
* </ul>
*/
public class ServiceConfigurator
{
   /** Primitive type name -> class map. */
   private static Hashtable primitives = new Hashtable();
   
   /** Setup the primitives map. */
   static
   {
      primitives.put("int", Integer.TYPE);
      primitives.put("boolean", Boolean.TYPE);
      primitives.put("double", Double.TYPE);
      primitives.put("float", Float.TYPE);
      primitives.put("long", Long.TYPE);
   }
   
   /** The MBean server which this service is registered in. */
   private MBeanServer server;
   
   /** The instance logger. */
   private static Logger log = Logger.getLogger(ServiceConfigurator.class);
   
   // Constructors --------------------------------------------------
   
   public ServiceConfigurator(final MBeanServer server)
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
      boolean debug = log.isDebugEnabled();

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
         for (int i = 0; i < attributes.length; i++)
         {
            if (attributes[i].isReadable() && isAttributeWriteable(server.getObjectInstance(name).getClassName(), attributes[i].getName(), attributes[i].getType()))
            {
               if (!attributes[i].isWritable())
               {
                  if (debug)
                     log.debug("Detected JMX Bug: Server reports attribute '"+attributes[i].getName() + "' is not writeable for MBean '" + name.getCanonicalName() + "'");
               }
               Element attributeElement = doc.createElement("attribute");
               Object value = server.getAttribute(name, attributes[i].getName());

               attributeElement.setAttribute("name", attributes[i].getName());

               if (value != null)
               {
                  attributeElement.appendChild(doc.createTextNode(value.toString()));
               }
               
               mbeanElement.appendChild(attributeElement);
               hasAttributes = true;
            }
         }
         
         if (hasAttributes)
         {
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

   /**
   * The <code>configure</code> method configures an mbean based on the xml element configuration
   * passed in.  Three formats are supported:
   * &lt;attribute name="(name)"&gt;(value)&lt;/attribute&gt;
   * &lt;depends optional-attribute-name="(name)"&gt;(object name of mbean referenced)&lt;/depends&gt;
   * &lt;depends-list optional-attribute-name="(name)"&gt;
   * [list of]  &lt;/depends-list-element&gt;(object name)&lt;/depends-list-element&gt;
   * &lt;/depends-list&gt;
   *
   * @param mbeanElement an <code>Element</code> value
   * @return a <code>ArrayList</code> of all the mbeans this one references.
   * @exception Exception if an error occurs
   */
   public ArrayList configure(Element mbeanElement)
   throws Exception
   {

      // Set configuration to MBeans from XML

      boolean debug = log.isDebugEnabled();

      // get the name of the mbean
      ObjectName objectName = parseObjectName(mbeanElement);

      MBeanInfo info;
      try
      {
         info = server.getMBeanInfo(objectName);
      } catch (InstanceNotFoundException e)
      {
         // The MBean is no longer available
         throw new DeploymentException("trying to configure nonexistent mbean: " + objectName);
      }
      // Set mbean references (object names)
      ArrayList mbeans = new ArrayList();

      // Set attributes
      MBeanAttributeInfo[] attributes = info.getAttributes();
      //NodeList attrs = mbeanElement.getElementsByTagName("attribute");
      NodeList attrs = mbeanElement.getChildNodes();
      for (int j = 0; j < attrs.getLength(); j++)
      {
	 if (attrs.item(j).getNodeType() == Node.ELEMENT_NODE)
	 {

	    Element element = (Element)attrs.item(j);
	    if (element.getTagName().equals("attribute"))
	    {
	       String attributeName = element.getAttribute("name");
	    attrfound:
	       if (element.hasChildNodes())
	       {
		  // Get the attribute value
		  Node n = element.getFirstChild();
		  String attributeText = null;
		  if( n instanceof Text )
		  {
		     attributeText = ((Text)n).getData().trim();
		  }

		  for (int k = 0; k < attributes.length; k++)
		  {
		     if (attributeName.equals(attributes[k].getName()))
		     {
			String typeName = attributes[k].getType();
			Class typeClass;
			if (primitives.containsKey(typeName))
			{
			   typeClass = (Class)primitives.get(typeName);
			}
			else
			{
			   typeClass = Class.forName(typeName);
			}

			Object value = null;

			// HRC: Is the attribute type a org.w3c.dom.Element??
			if (typeClass.equals(Element.class))
			{
			   // Then we can pass the first child Element of this
			   // element
			   NodeList nl = element.getChildNodes();
			   for (int i=0; i < nl.getLength(); i++)
			   {
			      n = nl.item(i);
			      if (n.getNodeType() == Node.ELEMENT_NODE)
			      {
				 value = (Element) n;
				 break;
			      }
			   }
			}
			
			if (value == null)
			{
			   PropertyEditor editor = PropertyEditorManager.findEditor(typeClass);
			   editor.setAsText(attributeText);
			   value = editor.getValue();
			}
			
			if (debug)
			   log.debug(attributeName + " set to " + value + " in " + objectName);
                  
			server.setAttribute(objectName, new Attribute(attributeName, value));
                  
			break attrfound;
		     }//if name matches
		  }//for attr names
		  throw new DeploymentException("No Attribute found with name: " +  attributeName);
	       }//if has children
	    }
	    //end of "attribute
	    else if (element.getTagName().equals("depends"))
	    {
      

	    dependAttrFound:
	       if (!element.hasChildNodes()) 
	       {
		  throw new DeploymentException("No ObjectName supplied for depends in  " + objectName);   
         
	       }		
         
	       String mbeanRefName = element.getAttribute("optional-attribute-name");
	       if ("".equals(mbeanRefName)) 
	       {
		  mbeanRefName = null;
	       } // end of if ()
         
	       // Get the mbeanRef value
	       String value = ((Text)element.getFirstChild()).getData().trim();
 
	       ObjectName dependsObjectName = new ObjectName(value);
	       if (!mbeans.contains(dependsObjectName)) 
	       {
		  mbeans.add(dependsObjectName);
	       } // end of if ()
	       if (debug)
		  log.debug("considering " + ((mbeanRefName == null)? "<anonymous>": mbeanRefName.toString()) + " with object name " + dependsObjectName);
	       if (mbeanRefName != null) 
	       {
		  //if if doesn't exist or has wrong type, we'll get an exception
		  server.setAttribute(objectName, new Attribute(mbeanRefName, dependsObjectName));
	       } // end of if ()
	    }  
	    //end of depends
	    else if (element.getTagName().equals("depends-list"))
	    {      
      
	       String dependsListName = element.getAttribute("optional-attribute-name");
	       if ("".equals(dependsListName)) 
	       {
		  dependsListName = null;
	       } // end of if ()

	       NodeList dependsList = element.getElementsByTagName("depends-list-element");
	       ArrayList dependsListNames = new ArrayList();
	       for (int l = 0; l < dependsList.getLength(); l++) 
	       {
		  Element dependsElement = (Element)dependsList.item(l);
		  if (!dependsElement.hasChildNodes()) 
		  {
		     throw new DeploymentException("Empty depends-list-element!");    
		  } // end of if ()

		  // Get the depends value
		  String dependsValue = ((Text)dependsElement.getFirstChild()).getData().trim();
		  ObjectName dependsObjectName = new ObjectName(dependsValue);
		  if (!dependsListNames.contains(dependsObjectName)) 
		  {
		     dependsListNames.add(dependsObjectName);
		  } // end of if ()
		  if (!mbeans.contains(dependsObjectName)) 
		  {
		     mbeans.add(dependsObjectName);
		  } // end of if ()
            
	       } // end of for ()
	       if (dependsListName != null) 
	       {
		  server.setAttribute(objectName, new Attribute(dependsListName, dependsListNames));
	       } // end of if ()
	    }//end of depends-list
	 }
      }
      return mbeans;
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
      if (name == null || name.trim().equals(""))
      {
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
      try
      {
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
      } catch (ClassNotFoundException e)
      {
         log.error("Unable to check parameter of type '" + type + "'");
         return false;
      }
      
      try
      {
         cls = Class.forName(className);
      } catch (ClassNotFoundException e)
      {
         log.error("Unable to check MBean of type '" + className + "'");
         return false;
      }
      
      try
      {
         Method m = cls.getMethod("set" + attribute, new Class[]
            { arg });
         return isSetterMethod(m);
      } catch (NoSuchMethodException ignore)
      {}
      
      return false;
   }
   
   /**
   * Check if the given method is a "setter" method.
   *
   * @param m     The method to check.
   * @return      True if the method is a "setter" method.
   */
   private boolean isSetterMethod(final Method m)
   {
      if (m != null)
      {
         return
         Modifier.isPublic(m.getModifiers()) &&
         !Modifier.isStatic(m.getModifiers()) &&
         m.getReturnType().equals(Void.TYPE);
      }
      
      return false;
   }
}
