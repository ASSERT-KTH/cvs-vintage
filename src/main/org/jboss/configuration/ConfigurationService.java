/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.configuration;

import java.io.*;
import java.beans.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.JMRuntimeException;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;
         
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.apache.log4j.Category;

import org.jboss.system.Service;
import org.jboss.system.ServiceFactory;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.XmlHelper;

/**
 * The ConfigurationService MBean is loaded when JBoss starts up by the
 * JMX MLet.
 *
 * <p>The ConfigurationService in turn loads the jboss.jcml configuration
 *    when {@link #loadConfiguration} is invoked. This instantiates JBoss
 *    specific mbean services that wish to be controlled by the JBoss
 *    {@link ServiceControl}/{@link Service} lifecycle service.
 *
 * @see org.jboss.system.Service
 * @see org.jboss.system.ServiceControl
 *
 * @author  <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 * @author  <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.42 $
 * Revisions:
 *
 * 20010622 scott.stark: Clean up the unsafe downcast of Throwable to Exception
 */
public class ConfigurationService
extends ServiceMBeanSupport
implements ConfigurationServiceMBean
{
   /** The name of the file initial configuration is read from. */
   public static final String CONFIGURATION_FILE = "jboss.jcml";
   
   /** The name of the file that running state will be written into. */
   public static final String RUNNING_STATE_FILE = "jboss-auto.jcml";
   
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
   
   /**
    * A mapping from the Service interface method names to the
    * corresponding index into the ServiceProxy.hasOp array.
    */
   private static HashMap serviceOpMap = new HashMap();
   
   /**
    * Initialize the service operation map.
    */
   static
   {
      serviceOpMap.put("create", new Integer(0));
      serviceOpMap.put("start", new Integer(1));
      serviceOpMap.put("destroy", new Integer(2));
      serviceOpMap.put("stop", new Integer(3));
   }
   
   
   /** Instance logger. */
   private final Category log = Category.getInstance(this.getClass());
   
   /** The MBean server which this service is registered in. */
   private MBeanServer server;
   
   /** The name of the ServiceControl service. */
   private ObjectName serviceControl;
   
   /** Flag to indicate if attribute values should be automatically trimmed. */
   private boolean autoTrim;
   
   // Constructors --------------------------------------------------
   
   /**
    * Construct a <tt>ConfigurationService</tt>.
    *
    * @param autoTrim  True to enable auto-trimming of attribute values.
    */
   public ConfigurationService(final boolean autoTrim)
   {
      this.autoTrim = autoTrim;
   }
   
   /**
    * Construct a <tt>ConfigurationService</tt> that auto-trim
    * attribute values.
    */
   public ConfigurationService()
   {
      this(true);
   }
   
   // Public --------------------------------------------------------
   
   /**
    * Get the attribute value auto-trim flag.
    *
    * @return  True if attribute values are auto-trimmed.
    */
   public boolean getAutoTrim()
   {
      return autoTrim;
   }
   
   /**
    * Get the name of this object.  Always ignores the given
    * object name.
    *
    * @param server    The server which the object is registered in.
    * @param name      The user specified object name (ignored).
    * @return          The name of this object.
    */
   public ObjectName getObjectName(MBeanServer server, ObjectName name)
   throws MalformedObjectNameException
   {
      this.server = server;
      return new ObjectName(OBJECT_NAME);
   }
   
   /**
    * Return the name of the service.
    *
    * @return  Always "Configuration".
    */
   public String getName()
   {
      return "Configuration";
   }
   
   /**
    * Parses the given configuration document and sets MBean attributes.
    *
    * @param configuration     The parsed configuration document.
    *
    * @throws Exception        Failed to load.
    */
   public void load(Document configuration) throws Exception
   {
      // Get the ServiceControl MBean
      serviceControl = new ObjectName(server.getDefaultDomain(), "service", "ServiceControl");
      if (server.isRegistered(serviceControl) == false)
         throw new IllegalStateException
         ("Failed to find ServiceControl mbean, name=" + serviceControl);

      boolean debug = log.isDebugEnabled();

      try
      {
         // Set configuration to MBeans from XML
         NodeList nl = configuration.getElementsByTagName("mbean");
         for (int i = 0; i < nl.getLength(); i++)
         {
            Element mbeanElement = (Element)nl.item(i);

            // get the name of the mbean
            ObjectName objectName = parseObjectName(mbeanElement);
            MBeanInfo info;
            try
            {
               info = server.getMBeanInfo(objectName);
            } catch (InstanceNotFoundException e)
            {
               // The MBean is no longer available
               // It's ok, skip to next one
               continue;
            }

            // Set attributes
            NodeList attrs = mbeanElement.getElementsByTagName("attribute");
            for (int j = 0; j < attrs.getLength(); j++)
            {
               Element attributeElement = (Element)attrs.item(j);
               String attributeName = attributeElement.getAttribute("name");
               if (attributeElement.hasChildNodes())
               {
                  String attributeValue = ((Text)attributeElement.getFirstChild()).getData();

                  if (autoTrim)
                  {
                     attributeValue = attributeValue.trim();
                  }

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
                        Object value = attributeValue;
                        if( editor != null )
                        {
                           editor.setAsText(attributeValue);
                           value = editor.getValue();
                        }

                        if (debug)
                           log.debug(attributeName + " set to " + attributeValue + " in " + objectName);
                        server.setAttribute(objectName, new Attribute(attributeName, value));

                        break;
                     }
                  }
               }
            }
            
            // Register the mbean with the JBoss ServiceControl mbean
            registerService(objectName, info, mbeanElement);
         }
      }
      catch (Throwable e)
      {
         logException(e);
         throw new ConfigurationException("Unexpected Error", e);
      }
   }

   /**
    * Builds a string that consists of the configuration elements of
    * the currently running MBeans registered in the server.
    *
    * @throws Exception    Failed to construct configuration.
    */
   public String save() throws Exception
   {
      boolean debug = log.isDebugEnabled();

      Writer out = new StringWriter();

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.newDocument();

      Element serverElement = doc.createElement("server");

      // Store attributes as XML
      Iterator mbeans = server.queryMBeans(null, null).iterator();
      while (mbeans.hasNext())
      {
         ObjectInstance instance = (ObjectInstance)mbeans.next();
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
      XmlHelper.write(out, doc);
      
      out.close();
      
      // Return configuration
      return out.toString();
   }
   
   /**
    * Saves the current configuration of each registered MBean to
    * the {@link #RUNNING_STATE_FILE} file.  This will only occur if
    * a file of the that name exists in the classpath.
    *
    * @throws Exception    Failed to save configuration.
    */
   public void saveConfiguration() throws Exception
   {
      // Get XML
      String xml = save();
      
      // Get JCML file
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      URL confFile = loader.getResource(RUNNING_STATE_FILE);
      
      if (confFile != null)
      {
         // Store to auto-saved JCML file
         PrintWriter out = null;
         try
         {
            out = new PrintWriter(new FileOutputStream(confFile.getFile()));
            out.print(xml);
         }
         catch (FileNotFoundException e)
         {
            log.error("Configuration file " + confFile.getFile() +
                      " must be available and writable.", e);
         }
         finally
         {
            out.close();
         }
      }
   }
   
   /**
    * Load the configuration from the {@link #CONFIGURATION_FILE},
    * installs and initailize configured MBeans and registeres the
    * beans as services.
    *
    * <p>This is a 2-step process:
    * <ol>
    *   <li>Load user conf. and create MBeans from that.
    *   <li>Apply user conf to created MBeans.
    * </ol>
    *
    * @throws Exception    ???
    */
   public void loadConfiguration() throws Exception
   {
      // The class loader used to kocal the configuration file
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      
      // Load user config from XML, and create the MBeans
      InputStream input = loader.getResourceAsStream(CONFIGURATION_FILE);
      
      //Modified by Vinay Menon
      StringBuffer sbufData = new StringBuffer();
      BufferedReader br = new BufferedReader(new InputStreamReader(input));
      
      String sTmp;
      String eol = System.getProperty("line.separator");
      try
      {
         while((sTmp = br.readLine())!=null)
         {
            sbufData.append(sTmp);
            sbufData.append(eol);
         }
      }
      finally
      {
         input.close();
      }
      //Modification Ends
      
      // Parse XML
      Document doc;
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder parser = factory.newDocumentBuilder();
      
      try
      {
         doc = parser.parse(new InputSource(new StringReader(sbufData.toString())));
      }
      catch (SAXException e)
      {
         throw new IOException(e.getMessage());
      }
      
      // create mbeans for the parsed configuration
      create(doc);
      
      // load each created mbean, set attributes and register services
      load(doc);
   }
   
   // Protected -----------------------------------------------------
   
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
    * Provides a wrapper around the information about which constructor
    * that MBeanServer should use to construct a MBean.
    * Please note that only basic datatypes (type is then the same as
    * you use to declare it "short", "int", "float" etc.) and any class
    * having a constructor taking a single "String" as only parameter.
    *
    * <p>XML syntax for contructor:
    *   <pre>
    *      <constructor>
    *         <arg type="xxx" value="yyy"/>
    *         ...
    *         <arg type="xxx" value="yyy"/>
    *      </constructor>
    *   </pre>
    */
   private static class ConstructorInfo
   {
      /** An empty parameters list. */
      public static final Object EMPTY_PARAMS[] = {};
      
      /** An signature list. */
      public static final String EMPTY_SIGNATURE[] = {};
      
      /** The constructor signature. */
      public String[] signature = EMPTY_SIGNATURE;
      
      /** The constructor parameters. */
      public Object[] params = EMPTY_PARAMS;
      
      /**
       * Create a ConstructorInfo object for the given configuration.
       *
       * @param element   The element to build info for.
       * @return          A constructor information object.
       *
       * @throws ConfigurationException   Failed to create info object.
       */
      public static ConstructorInfo create(Element element)
         throws ConfigurationException
      {
         ConstructorInfo info = new ConstructorInfo();
         
         NodeList list = element.getElementsByTagName("constructor");
         if (list.getLength() > 1)
         {
            throw new ConfigurationException
            ("only one <constructor> element may be defined");
         }
         else if (list.getLength() == 1)
         {
            element = (Element)list.item(0);
            
            // get all of the "arg" elements
            list = element.getElementsByTagName("arg");
            int length = list.getLength();
            info.params = new Object[length];
            info.signature = new String[length];
            
            // decode the values into params & signature
            for (int j=0; j<length; j++)
            {
               Element arg = (Element)list.item(j);
               //
               // NOTE: should coerce value to the correct type??
               //
               // Add support for primitive Data Types
               String signature = arg.getAttribute("type");
               String value = arg.getAttribute("value");
               Object realValue = value;
               if( signature != null )
               {
                  if( signature.equals( "short" ) )
                  {
                     signature = Short.TYPE.getName();
                     realValue = new Short( value );
                  }
                  else if( signature.equals( "int" ) )
                  {
                     signature = Integer.TYPE.getName();
                     realValue = new Integer( value );
                  }
                  else if( signature.equals( "long" ) )
                  {
                     signature = Long.TYPE.getName();
                     realValue = new Long( value );
                  }
                  else if( signature.equals( "byte" ) )
                  {
                     signature = Byte.TYPE.getName();
                     realValue = new Byte( value );
                  }
                  else if( signature.equals( "char" ) )
                  {
                     signature = Character.TYPE.getName();
                     realValue = new Character( value.charAt( 0 ) );
                  }
                  else if( signature.equals( "float" ) )
                  {
                     signature = Float.TYPE.getName();
                     realValue = new Float( value );
                  }
                  else if( signature.equals( "double" ) )
                  {
                     signature = Double.TYPE.getName();
                     realValue = new Double( value );
                  }
                  else if( signature.equals( "boolean" ) )
                  {
                     signature = Boolean.TYPE.getName();
                     realValue = new Boolean( value );
                  }
                  else
                  {
                     try
                     {
                        // Check if there is a constructor with a single String as
                        // only parameter
                        Class signatureClass = Thread.currentThread().getContextClassLoader().loadClass( signature );
                        Constructor signatureConstructor = signatureClass.getConstructor( new Class[]{ String.class } );
                        realValue = signatureConstructor.newInstance( new Object[] { value } );
                     }
                     catch( Exception e )
                     {
                     }
                  }
               }
               info.signature[j] = signature;
               info.params[j] = realValue;
            }
         }
         
         return info;
      }
   }
   
   /**
    * Parses the given configuration document and creates MBean
    * instances in the current MBean server.
    *
    * @param configuration     The configuration document.
    *
    * @throws ConfigurationException   The configuration document contains
    *                                  invalid or missing syntax.
    * @throws Exception                Failed for some other reason.
    */
   private void create(Document configuration) throws Exception
   {
      try
      {
         ObjectName loader = new ObjectName(server.getDefaultDomain(), "service", "MLet");
         
         // Set configuration to MBeans from XML
         NodeList nl = configuration.getElementsByTagName("mbean");
         for(int i = 0; i < nl.getLength(); i++)
         {
            Element mbeanElement = (Element)nl.item(i);
            
            // get the name of the mbean
            ObjectName objectName = parseObjectName(mbeanElement);
            
            MBeanInfo info;
            try
            {
               info = server.getMBeanInfo(objectName);
            }
            catch (InstanceNotFoundException e)
            {
               // The MBean is no longer available
               // If class is given, instantiate it
               String code = mbeanElement.getAttribute("code");
               if (code == null)
               {
                  throw new ConfigurationException
                  ("missing 'code' attribute");
               }
               
               try
               {
                  // get the constructor params/sig to use
                  ConstructorInfo constructor =
                  ConstructorInfo.create(mbeanElement);
                  
                  // Create the MBean instance
                  ObjectInstance instance =
                  server.createMBean(code,
                  objectName,
                  loader,
                  constructor.params,
                  constructor.signature);
                  info = server.getMBeanInfo(instance.getObjectName());
               }
               catch (Throwable ex)
               {
                  log.error("Could not create MBean " +
                            objectName + "(" + code + ")", ex);
                  logException(ex);
                  // Ah what the heck.. skip it
                  continue;
               }
            }
            
            // info is not being used
         }
      }
      catch(Throwable e)
      {
         logException(e);
         throw new ConfigurationException("Unexpected Error", e);
      }
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
         log.error("Unable to check parameter of type '" + type + "'", e);
         return false;
      }
      
      try
      {
         cls = Class.forName(className);
      } catch (ClassNotFoundException e)
      {
         log.error("Unable to check MBean of type '" + className + "'", e);
         return false;
      }
      
      try
      {
         Method m = cls.getMethod("set" + attribute, new Class[] { arg });
         return isSetterMethod(m);
      }
      catch (NoSuchMethodException ignore)
      {
      }
      
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
      boolean isSetterMethod = false;
      if (m != null)
      {
         isSetterMethod = Modifier.isPublic(m.getModifiers());
         isSetterMethod &= !Modifier.isStatic(m.getModifiers());
         isSetterMethod &= m.getReturnType().equals(Void.TYPE);
      }
      
      return isSetterMethod;
   }
   
   /**
    * Register the mbean given by objectName with the ServiceControl service.
    *
    * @param objectName
    * @param info
    * @param mbeanElement
    *
    * @throws ClassNotFoundException
    * @throws InstantiationException
    * @throws IllegalAccessException
    */
   private void registerService(ObjectName objectName, MBeanInfo info,
      Element mbeanElement)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException
   {
      // Check for a serviceFactory attribute
      String serviceFactory = mbeanElement.getAttribute("serviceFactory");
      Service service = getServiceInstance(objectName, info, serviceFactory);
      
      if (service != null)
      {
         Object[] args = { service };
         String[] signature = { "org.jboss.system.Service" };
         try
         {
            server.invoke(serviceControl, "register", args, signature);
         }
         catch(Exception e)
         {
            logException(e);
         }
      }
   }

   /**
    * Get the Service interface through which the mbean given by
    * objectName will be managed.
    *
    * @param objectName
    * @param info
    * @param serviceFactory
    *
    * @throws ClassNotFoundException
    * @throws InstantiationException
    * @throws IllegalAccessException
    */
   private Service getServiceInstance(ObjectName objectName, MBeanInfo info,
      String serviceFactory)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException
   {
      Service service = null;
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      if (serviceFactory != null && serviceFactory.length() > 0)
      {
         Class clazz = loader.loadClass(serviceFactory);
         ServiceFactory factory = (ServiceFactory) clazz.newInstance();
         service = factory.createService(server, objectName);
      }
      else
      {
         MBeanOperationInfo[] opInfo = info.getOperations();
         Class[] interfaces = { org.jboss.system.Service.class };
         InvocationHandler handler = new ServiceProxy(objectName, opInfo);
         service = (Service) Proxy.newProxyInstance(loader, interfaces, handler);
      }
      
      return service;
   }
   
   /**
    * Go through the myriad of nested JMX exception to pull out the
    * true exception if possible and log it.
    *
    * @param e     The exception to be logged.
    */
   private void logException(Throwable e)
   {
      if (e instanceof RuntimeErrorException)
      {
         e = ((RuntimeErrorException)e).getTargetError();
      } else if (e instanceof RuntimeMBeanException)
      {
         e = ((RuntimeMBeanException)e).getTargetException();
      } else if (e instanceof RuntimeOperationsException)
      {
         e = ((RuntimeOperationsException)e).getTargetException();
      } else if (e instanceof MBeanException)
      {
         e = ((MBeanException)e).getTargetException();
      } else if (e instanceof ReflectionException)
      {
         e = ((ReflectionException)e).getTargetException();
      }
      
      log.error("error", e);
   }
   
   /**
    * An implementation of InvocationHandler used to proxy of the Service
    * interface for mbeans. It determines which of the init/start/stop/destroy
    * methods of the Service interface an mbean implements by inspecting its
    * MBeanOperationInfo values. Each Service interface method that has a
    * matching operation is forwarded to the mbean by invoking the method
    * through the MBeanServer object.
    */
   private class ServiceProxy implements InvocationHandler
   {
      private boolean[] hasOp =
      { false, false, false, false };
      private ObjectName objectName;
      
      /**
       * Go through the opInfo array and for each operation that
       * matches on of the Service interface methods set the corresponding
       * hasOp array value to true.
       *
       * @param objectName
       * @param opInfo
       */
      public ServiceProxy(ObjectName objectName,
      MBeanOperationInfo[] opInfo)
      {
         this.objectName = objectName;
         int opCount = 0;
         
         for (int op = 0; op < opInfo.length; op ++)
         {
            MBeanOperationInfo info = opInfo[op];
            String name = info.getName();
            Integer opID = (Integer) serviceOpMap.get(name);
            if (opID == null)
            {
               continue;
            }
            
            // Validate that is a no-arg void return type method
            if (info.getReturnType().equals("void") == false)
               continue;
            if (info.getSignature().length != 0)
               continue;
            
            hasOp[opID.intValue()] = true;
            opCount++;
         }
         
         // Log a warning if the mbean does not implement
         // any Service methods
         if (opCount == 0)
            log.warn(objectName +
                     " does not implement any Service methods");
      }
      
      /**
       * Map the method name to a Service interface method index and
       * if the corresponding hasOp array element is true, dispatch the
       * method to the mbean we are proxying.
       *
       * @param proxy
       * @param method
       * @param args
       * @return              Always null.
       *
       * @throws Throwable
       */
      public Object invoke(Object proxy, Method method, Object[] args)
      throws Throwable
      {
         String name = method.getName();
         Integer opID = (Integer) serviceOpMap.get(name);
         
         if (opID != null && hasOp[opID.intValue()] == true )
         {
            try
            {
               String[] sig =
               {}
               ;
               server.invoke(objectName, name, args, sig);
            } catch (JMRuntimeException e)
            {
               logException(e);
            }
            catch (JMException e)
            {
               logException(e);
            }
         }
         
         return null;
      }
   }
}

/* Log
6/13/2001 Andreas Schaefer Added type "short" for constructor argument and
                           any class having a constructor taking a "String"
                           as only parameter
 */
