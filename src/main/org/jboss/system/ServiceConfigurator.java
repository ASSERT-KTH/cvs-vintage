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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Service configuration helper.
 * 
 * @author <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:hiram@jboss.org">Hiram Chirino</a>
 * @version $Revision: 1.6 $
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
   static {
      primitives.put("int", Integer.TYPE);
      primitives.put("boolean", Boolean.TYPE);
      primitives.put("double", Double.TYPE);
      primitives.put("float", Float.TYPE);
      primitives.put("long", Long.TYPE);
   }
	
   /** The MBean server which this service is registered in. */
   private MBeanServer server;

   /** The instance logger. */
   private static Logger log = Logger.create("Configurator");
	
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
	
    /**
     * The <code>configure</code> method configures an mbean based on the xml element configuration
     * passed in.  Three formats are supported:
     * &lt;attribute name="(name)"&gt;(value)&lt;/attribute&gt;
     * &lt;mbean-ref name="(name)"&gt;(object name of mbean referenced)&lt;/mbean-ref&gt;
     * &lt;mbean-ref-list name="(name)"&gt;
     * [list of]  &lt;/mbean-ref-list-element&gt;(object name)&lt;/mbean-ref-list-element&gt;
     * &lt;/mbean-ref-list&gt;
     *
     * @param mbeanElement an <code>Element</code> value
     * @return a <code>ArrayList</code> of all the mbeans this one references.
     * @exception Exception if an error occurs
     */
    public ArrayList configure(Element mbeanElement) 
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
         // It's ok, just return It is ????? Why??  Oh yeah?
         throw new DeploymentException("trying to configure nonexistent mbean: " + objectName);
         //log.debug("object name " + objectName + " is no longer available");
         //return true;
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
						
                  Object value = null;
						
                  // HRC: Is the attribute type a org.w3c.dom.Element??
                  if (typeClass.equals(Element.class)) {
                     // Then we can pass the first child Element of this 
                     // attributeElement
                     NodeList nl = attributeElement.getChildNodes();
                     for (int i=0; i < nl.getLength(); i++) {
                        org.w3c.dom.Node n = nl.item(i);
                        if (n.getNodeType() == n.ELEMENT_NODE) {
                           value = (Element)n;
                           break;
                        }
                     }
                  }
						
                  if (value == null) {
                     PropertyEditor editor = PropertyEditorManager.findEditor(typeClass);
                     editor.setAsText(attributeValue);
                     value = editor.getValue();
                  }
							
                  log.debug(attributeName + " set to " + attributeValue + " in " + objectName);
                  server.setAttribute(objectName, new Attribute(attributeName, value));
						
                  break;
               }
            }
         }
      }
      // Set mbean references (object names)
      ArrayList mBeanRefs = new ArrayList();
      NodeList mbeanRefElements = mbeanElement.getElementsByTagName("mbean-ref");
      log.debug("found " + mbeanRefElements.getLength() + " mbean-ref elements");
      for (int j = 0; j < mbeanRefElements.getLength(); j++) {
         Element mbeanRefElement = (Element)mbeanRefElements.item(j);
         String mbeanRefName = mbeanRefElement.getAttribute("name");
         if (!mbeanRefElement.hasChildNodes()) 
         {
            throw new DeploymentException("No ObjectName supplied for mbean-ref " + mbeanRefName);   

         }		
         // Get the mbeanRef value
         String mbeanRefValue = ((Text)mbeanRefElement.getFirstChild()).getData().trim();
         ObjectName mbeanRefObjectName = new ObjectName(mbeanRefValue);
         log.debug("considering " + mbeanRefName + " with object name " + mbeanRefObjectName);
         MBeanAttributeInfo[] attributes = info.getAttributes();
         for (int k = 0; k < attributes.length; k++) {
            if (mbeanRefName.equals(attributes[k].getName())) {
               String typeName = attributes[k].getType();
               if (!"javax.management.ObjectName".equals(typeName)) 
               {
                  throw new DeploymentException("Trying to set " + mbeanRefName + " as an MBeanRef when it is not of type ObjectName");   
               } // end of if ()
               if (!mBeanRefs.contains(mbeanRefObjectName)) 
               {
                  mBeanRefs.add(mbeanRefObjectName);
               } // end of if ()

               log.debug(mbeanRefName + " set to " + mbeanRefValue + " in " + objectName);
               server.setAttribute(objectName, new Attribute(mbeanRefName, mbeanRefObjectName));

               break;
            }
         }
         
      }
      // Set lists of mbean references (object names)

      NodeList mBeanRefLists = mbeanElement.getElementsByTagName("mbean-ref-list");
      for (int j = 0; j < mBeanRefLists.getLength(); j++) {
         Element mBeanRefListElement = (Element)mBeanRefLists.item(j);
         String mBeanRefListName = mBeanRefListElement.getAttribute("name");

         MBeanAttributeInfo[] attributes = info.getAttributes();
         for (int k = 0; k < attributes.length; k++) {
            if (mBeanRefListName.equals(attributes[k].getName())) {

               NodeList mBeanRefList = mBeanRefListElement.getElementsByTagName("mbean-ref-list-element");
               ArrayList mBeanRefListNames = new ArrayList();
               for (int l = 0; l < mBeanRefList.getLength(); l++) 
               {
                  Element mBeanRefElement = (Element)mBeanRefList.item(l);
                  if (!mBeanRefElement.hasChildNodes()) 
                  {
                     throw new DeploymentException("Empty mbean-ref-list-element!");    
                  } // end of if ()

                  // Get the mbeanRef value
                  String mBeanRefValue = ((Text)mBeanRefElement.getFirstChild()).getData().trim();
                  ObjectName mBeanRefObjectName = new ObjectName(mBeanRefValue);
                  if (!mBeanRefListNames.contains(mBeanRefObjectName)) 
                  {
                     mBeanRefListNames.add(mBeanRefObjectName);
                  } // end of if ()
                  if (!mBeanRefs.contains(mBeanRefObjectName)) 
                  {
                     mBeanRefs.add(mBeanRefObjectName);
                  } // end of if ()
                  
               } // end of for ()

               log.debug(mBeanRefListName + " set to " + mBeanRefListNames + " in " + objectName);
               server.setAttribute(objectName, new Attribute(mBeanRefListName, mBeanRefListNames));

               break;
            }

         }
      }
      return mBeanRefs;
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
