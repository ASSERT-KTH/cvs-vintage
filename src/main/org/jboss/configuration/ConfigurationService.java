/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.configuration;

import java.io.*;
import java.beans.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import javax.management.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;


import org.jboss.logging.Log;
import org.jboss.util.Service;
import org.jboss.util.ServiceFactory;
import org.jboss.util.ServiceMBeanSupport;
import org.jboss.util.XmlHelper;

/** The ConfigurationService MBean is loaded when JBoss starts up by the
JMX MLet. The ConfigurationService in turn loads the jboss.jcml configuration
when loadConfiguration() is invoked. This instantiates JBoss specific mbean
services that wish to be controlled by the JBoss ServiceControl/Service
lifecycle service.
 *
 *   @see org.jboss.util.Service
 *   @see org.jboss.util.ServiceControl
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @author Scott_Stark@displayscape.com
 *   @version $Revision: 1.22 $
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
        primitives.put("long",Long.TYPE);
    }

   // Attributes ----------------------------------------------------
    Log log = Log.createLog(getName());

    MBeanServer server;
    ObjectName serviceControl;

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

    public void load(Document configuration)
        throws Exception
    {
        // Get the ServiceControl MBean
        serviceControl = new ObjectName(server.getDefaultDomain(), "service", "ServiceControl");
        if( server.isRegistered(serviceControl) == false )
            throw new IllegalStateException("Failed to find ServiceControl mbean, name="+serviceControl);

        try
        {          
            // Set configuration to MBeans from XML
            NodeList nl = configuration.getElementsByTagName("mbean");
            for (int i = 0; i < nl.getLength(); i++)
            {
                Element mbeanElement = (Element)nl.item(i);

                String name = mbeanElement.getAttribute("name");

                if (name == null)
                  continue; // MBean ObjectName must be given

                ObjectName objectName = new ObjectName(name);

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

                                log.debug(attributeName +" set to "+attributeValue+" in "+name);
                                server.setAttribute(objectName, new Attribute(attributeName, value));

                                break;
                            }
                        }
                    }

                }

                // Register the mbean with the JBoss ServiceControl mbean
                registerService(objectName, info, mbeanElement);
            }
        } catch (Throwable e)
        {
            if (e instanceof RuntimeMBeanException)
            {
               e = ((RuntimeMBeanException)e).getTargetException();
            }
            else if( e instanceof MBeanException)
            {
               e = ((MBeanException)e).getTargetException();
            }

            Log.getLog().exception(e);
            throw (Exception)e;
        }
    }

   public String save()
        throws Exception
   {
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
            mbeanElement.setAttribute("name",name.toString());

            MBeanInfo info = server.getMBeanInfo(name);
            mbeanElement.setAttribute("code",info.getClassName());
            MBeanAttributeInfo[] attributes = info.getAttributes();
            boolean hasAttributes = true;
            for (int i = 0; i < attributes.length; i++)
            {
                if (attributes[i].isReadable() && isAttributeWriteable(server.getObjectInstance(name).getClassName(), attributes[i].getName(), attributes[i].getType()))
                {
                    if(!attributes[i].isWritable())
                        log.debug("Detected JMX Bug: Server reports attribute '"+attributes[i].getName()+"' is not writeable for MBean '"+name.getCanonicalName()+"'");
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
        XmlHelper.write(out, doc);

        out.close();

        // Return configuration
        return out.toString();
   }

   public void saveConfiguration()
      throws Exception
   {
      // Get XML
      String xml = save();

      // Get JCML file
      URL confFile = Thread.currentThread().getContextClassLoader().getResource("jboss-auto.jcml");

      if (confFile != null)
      {
         // Store to auto-saved JCML file
         PrintWriter out = null;
         try {
         	out = new PrintWriter(new FileOutputStream(confFile.getFile()));
	     } catch (java.io.FileNotFoundException e) {
	     	log.error("Configuration file "+confFile.getFile()+" must be available and writable.");
	     	log.exception(e);
	     }
	     out.print(xml);
	     out.close();
      }
   }

    public void loadConfiguration()
       throws Exception
    {
      // This is a 2-step process
      // 1) Load user conf. and create MBeans from that
      // 2) Apply user conf to created MBeans

       // Load user config from XML, and create the MBeans
       InputStream conf = Thread.currentThread().getContextClassLoader().getResourceAsStream("jboss.jcml");
       byte[] arr = new byte[conf.available()];
       conf.read(arr);
       conf.close();
       String cfg = new String(arr);

       // Parse XML
       Document userConf;
       DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
       DocumentBuilder parser = factory.newDocumentBuilder();


       try
       {
           userConf = parser.parse(new InputSource(new StringReader(cfg)));
       }
       catch (SAXException se)
       {
            throw new IOException(se.getMessage());
       }

       create(userConf);

       // Apply user conf
       conf = Thread.currentThread().getContextClassLoader().getResourceAsStream("jboss.jcml");
       arr = new byte[conf.available()];
       conf.read(arr);
       conf.close();
       cfg = new String(arr);

       load(userConf);
    }

    // Protected -----------------------------------------------------
    protected void create(Document configuration)
        throws Exception
    {
        try
        {
            // Set configuration to MBeans from XML
            NodeList nl = configuration.getElementsByTagName("mbean");
            for (int i = 0; i < nl.getLength(); i++)
            {
                Element mbeanElement = (Element)nl.item(i);

                String name = mbeanElement.getAttribute("name");

                if (name == null)
                  continue; // MBean ObjectName must be given

                ObjectName objectName = new ObjectName(name);

                MBeanInfo info;
                try {
                    info = server.getMBeanInfo(objectName);
                } catch (InstanceNotFoundException e)
                {
                  // The MBean is no longer available
                  // If class is given, instantiate it
                  String code = mbeanElement.getAttribute("code");
                  if (code != null)
                  {
                     try
                     {
                        // Create MBean
                        ObjectInstance instance = server.createMBean(code, objectName,
                            new ObjectName(server.getDefaultDomain(), "service", "MLet"));
                        
                        info = server.getMBeanInfo(instance.getObjectName());
                     } catch (Throwable ex)
                     {
                        log.error("Could not create MBean "+name+"("+code+")");
                        if (ex instanceof RuntimeMBeanException)
                           ex = ((RuntimeMBeanException)ex).getTargetException();
                        if (ex instanceof RuntimeOperationsException)
                           ex = ((RuntimeOperationsException)ex).getTargetException();
                        else if (ex instanceof MBeanException)
                           ex = ((MBeanException)ex).getTargetException();
                        else if (ex instanceof ReflectionException)
                        	ex = ((ReflectionException)ex).getTargetException();
                        log.exception(ex);
                        // Ah what the heck.. skip it
                        continue;
                     }
                  } else
                  {
                     // No code attribute given - can't instantiate
                     // it's ok, skip to next one
                     continue;
                  }
                }
            }
        } catch (Throwable e)
        {
            if (e instanceof RuntimeMBeanException)
            {
               e = ((RuntimeMBeanException)e).getTargetException();
            }

            Log.getLog().exception(e);
            throw (Exception)e;
        }
    }

    private boolean isAttributeWriteable(String className, String attribute, String type) {
        Class arg = null;
        Class cls = null;
        try {
            if(type.equals("int"))
                arg = Integer.TYPE;
            else if(type.equals("boolean"))
                arg = Boolean.TYPE;
            else if(type.equals("float"))
                arg = Float.TYPE;
            else if(type.equals("byte"))
                arg = Byte.TYPE;
            else if(type.equals("short"))
                arg = Short.TYPE;
            else if(type.equals("char"))
                arg = Character.TYPE;
            else if(type.equals("long"))
                arg = Long.TYPE;
            else if(type.equals("double"))
                arg = Double.TYPE;
            else arg = Class.forName(type);
        } catch(ClassNotFoundException e) {
            log.error("Unable to check parameter of type '"+type+"'");
            return false;
        }
        try {
            cls = Class.forName(className);
        } catch(ClassNotFoundException e) {
            log.error("Unable to check MBean of type '"+className+"'");
            return false;
        }
        try {
            Method m = cls.getMethod("set"+attribute, new Class[]{arg});
            return m != null && Modifier.isPublic(m.getModifiers()) && !Modifier.isStatic(m.getModifiers()) && m.getReturnType().equals(Void.TYPE);
        } catch(NoSuchMethodException e) {}
        return false;
    }

    /** Register the mbean given by objectName with the ServiceControl service.
    */
    void registerService(ObjectName objectName, MBeanInfo info, Element mbeanElement)
        throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        // Check for a serviceFactory attribute
        String serviceFactory = mbeanElement.getAttribute("serviceFactory");
        Service service = getServiceInstance(objectName, info, serviceFactory);
        if( service != null )
        {
            Object[] args = {service};
            String[] signature = {"org.jboss.util.Service"};
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

    /** Get the Service interface through which the mbean given by objectName will
        be managed.
    */
    Service getServiceInstance(ObjectName objectName, MBeanInfo info, String serviceFactory)
        throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        Service service = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if( serviceFactory != null && serviceFactory.length() > 0 )
        {
            Class clazz = loader.loadClass(serviceFactory);
            ServiceFactory factory = (ServiceFactory) clazz.newInstance();
            service = factory.createService(server, objectName);
        }
        else
        {
            MBeanOperationInfo[] opInfo = info.getOperations();
            Class[] interfaces = { org.jboss.util.Service.class };
            InvocationHandler handler = new ServiceProxy(objectName, opInfo);
            service = (Service) Proxy.newProxyInstance(loader, interfaces, handler);
        }
        return service;
    }

    /** A mapping from the Service interface method names to the
    corresponding index into the ServiceProxy.hasOp array.
    */
    static HashMap serviceOpMap = new HashMap();
    static
    {
        serviceOpMap.put("init", new Integer(0));
        serviceOpMap.put("start", new Integer(1));
        serviceOpMap.put("destroy", new Integer(2));
        serviceOpMap.put("stop", new Integer(3));
    }
    /** An implementation of InvocationHandler used to proxy of the Service
    interface for mbeans. It determines which of the init/start/stop/destroy
    methods of the Service interface an mbean implements by inspecting its
    MBeanOperationInfo values. Each Service interface method that has a
    matching operation is forwarded to the mbean by invoking the method
    through the MBeanServer object.
    */
    class ServiceProxy implements InvocationHandler
    {

        private boolean[] hasOp = {false, false, false, false};
        private ObjectName objectName;

        /** Go through the opInfo array and for each operation that
        matches on of the Service interface methods set the corresponding
        hasOp array value to true.
        */
        ServiceProxy(ObjectName objectName, MBeanOperationInfo[] opInfo)
        {
            this.objectName = objectName;
            for(int op = 0; op < opInfo.length; op ++)
            {
                MBeanOperationInfo info = opInfo[op];
                String name = info.getName();
                Integer opID = (Integer) serviceOpMap.get(name);
                if( opID == null )
                   continue;

                // Validate that is a no-arg void return type method
                if( info.getReturnType().equals("void") == false )
                    continue;
                if( info.getSignature().length != 0 )
                    continue;
                hasOp[opID.intValue()] = true;
            }
        }

        /** Map the method name to a Service interface method index and
        if the corresponding hasOp array element is true, dispatch the
        method to the mbean we are proxying.
        @return null always.
        */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            String name = method.getName();
            Integer opID = (Integer) serviceOpMap.get(name);
            if( opID != null && hasOp[opID.intValue()] == true )
            {
                try
                {
                    String[] sig = {};
                    server.invoke(objectName, name, args, sig);
                }
                catch(JMRuntimeException e)
                {
                    logException(e);
                }
                catch(JMException e)
                {
                    logException(e);
                }
            }
            return null;
        }
    }

    /** Go through the myriad of nested JMX exception to pull out the
        true exception if possible and log it.
    */
    void logException(Exception e)
    {
        if( e instanceof RuntimeErrorException )
        {
            Throwable t = ((RuntimeErrorException)e).getTargetError();
            log.exception(t);
        }
        else if( e instanceof RuntimeMBeanException)
        {
            Throwable t = ((RuntimeMBeanException)e).getTargetException();
            log.exception(t);
        }
        else if( e instanceof RuntimeOperationsException)
        {
            Throwable t = ((RuntimeOperationsException)e).getTargetException();
            log.exception(t);
        }
        else if( e instanceof MBeanException)
        {
            Throwable t = ((MBeanException)e).getTargetException();
            log.exception(t);
        }
        else if( e instanceof ReflectionException)
        {
            Throwable t = ((ReflectionException)e).getTargetException();
            log.exception(t);
        }
        else
            log.exception(e);
    }
}
