/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.system;

import java.lang.reflect.Constructor;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.MalformedObjectNameException;

import org.jboss.logging.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** 
 * A helper class for the controller.
 *   
 * @see Service
 * 
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.11 $
 * 
 * <p><b>Revisions:</b>
 * <p><b>2001/08/03 marcf </b>
 * <ul>
 *   <li>Initial version checked in
 * </ul>
 */
public class ServiceCreator 
{
   // Attributes ----------------------------------------------------

   /** Instance logger. */
   private static final Logger log = Logger.getLogger(ServiceCreator.class);
   
   private MBeanServer server;
   
   public ServiceCreator(final MBeanServer server) {
      this.server = server;
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
   public ObjectInstance install(Element mbeanElement) throws Exception
   {
      boolean debug = log.isDebugEnabled();

      ObjectName name = parseObjectName(mbeanElement);

      // marcf fixme add and remove the classlaoder from the controller

      ObjectName loader = new ObjectName("jboss.system.classloader:id=" +
                                         name.hashCode());

      MBeanClassLoader cl = new MBeanClassLoader(name);

      if (!server.isRegistered(loader))
         server.registerMBean(cl, loader);

      // If class is given, instantiate it
      String code = mbeanElement.getAttribute("code");
      if (code == null)
      {
         throw new ConfigurationException("missing 'code' attribute");
      }

      // get the constructor params/sig to use
      ConstructorInfo constructor =

      ConstructorInfo.create(mbeanElement);
      
      if (debug) log.debug("About to create bean: "+name);

		
      // Create the MBean instance
      try 
      {
         log.info("code "+code);
         ObjectInstance instance = server.createMBean(code,
                                                      name,
                                                      loader,
                                                      constructor.params,
                                                      constructor.signature);
         if (debug)
            log.debug("Created bean: "+name);
		
         return instance;
      } 
      catch (Exception e) 
      {
         // didn't work, unregister in case the jmx agent is screwed.
         try 
         {
            server.unregisterMBean(name);
         }
         catch (Exception ignore) {}
         
         throw e;
      }
   }	
   
   public void remove(ObjectName name) throws Exception
   {
      // add defaut domain if there isn't one in this name
      String domain = name.getDomain();
      int hcode = name.hashCode();
      if (domain == null || "".equals(domain)) {
         name = new ObjectName(server.getDefaultDomain() + name);
      }

      // Remove the MBean from the MBeanServer
      server.unregisterMBean(name);

      // Remove the MBeanClassLoader used by the MBean
      ObjectName loader = new ObjectName("jboss.system.classloader:id=" + hcode);
      if (server.isRegistered(loader))
      {
         server.unregisterMBean(loader);
         if (log.isDebugEnabled())
            log.debug("unregistered caossloader for: " + name);
      }
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
         if (list.getLength() > 1) {
            throw new ConfigurationException
            ("only one <constructor> element may be defined");
         }
         else if (list.getLength() == 1) {
            element = (Element)list.item(0);
            
            // get all of the "arg" elements
            list = element.getElementsByTagName("arg");
            int length = list.getLength();
            info.params = new Object[length];
            info.signature = new String[length];
            
            // decode the values into params & signature
            for (int j=0; j<length; j++) {
               Element arg = (Element)list.item(j);
               //
               // NOTE: should coerce value to the correct type??
               //
               // Add support for primitive Data Types
               String signature = arg.getAttribute("type");
               String value = arg.getAttribute("value");
               Object realValue = value;
               if( signature != null ) {
                  if( signature.equals( "short" ) ) {
                     signature = Short.TYPE.getName();
                     realValue = new Short( value );
                  } else
                  if( signature.equals( "int" ) ) {
                     signature = Integer.TYPE.getName();
                     realValue = new Integer( value );
                  } else
                  if( signature.equals( "long" ) ) {
                     signature = Long.TYPE.getName();
                     realValue = new Long( value );
                  } else
                  if( signature.equals( "byte" ) ) {
                     signature = Byte.TYPE.getName();
                     realValue = new Byte( value );
                  } else
                  if( signature.equals( "char" ) ) {
                     signature = Character.TYPE.getName();
                     realValue = new Character( value.charAt( 0 ) );
                  } else
                  if( signature.equals( "float" ) ) {
                     signature = Float.TYPE.getName();
                     realValue = new Float( value );
                  } else
                  if( signature.equals( "double" ) ) {
                     signature = Double.TYPE.getName();
                     realValue = new Double( value );
                  } else
                  if( signature.equals( "boolean" ) ) {
                     signature = Boolean.TYPE.getName();
                     realValue = new Boolean( value );
                  }
                  else
                  {
                     try {
                        // Check if there is a constructor with a single String as
                        // only parameter
                        Class signatureClass =
                        Thread.currentThread().getContextClassLoader().loadClass( signature );
                        Constructor signatureConstructor =
                        signatureClass.getConstructor( new Class[] { String.class } );
                        realValue = signatureConstructor.newInstance(
                           new Object[] { value }
                        );
                     }
                     catch( Exception e ) {
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
}
