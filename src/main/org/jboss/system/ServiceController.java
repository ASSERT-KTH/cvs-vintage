/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.system;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import java.util.Map;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.JMRuntimeException;
import javax.management.MBeanException;

import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;

import org.w3c.dom.Element;

/**
 * This is the main Service Controller. A controller can deploy a service to a
 * JBOSS-SYSTEM It installs by delegating, it configures by delegating
 *
 * @see org.jboss.system.Service
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version $Revision: 1.9 $ <p>
 *
 * <b>Revisions:</b> <p>
 *
 * <b>20010830 marcf</b>
 * <ol>
 *   <li>Initial version checked in
 * </ol>
 * <b>20010908 david jencks</b>
 * <ol>
 *   <li>fixed tabs to spaces and log4j logging. Made it report the number
 *       of successes on init etc.  Modified to support undeploy and work with
 *       .sar dependency management and recursive sar deployment.
 * </ol>
 */
public class ServiceController
       extends ServiceMBeanSupport
       implements ServiceControllerMBean, MBeanRegistration
{
   /**
    * A mapping from the Service interface method names to the corresponding
    * index into the ServiceProxy.hasOp array.
    */
   private static HashMap serviceOpMap = new HashMap();

   /**
    * Helper classes to create and configure the classes
    */
   protected ServiceCreator creator;
   /**
    * Description of the Field
    */
   protected ServiceConfigurator configurator;

   // Attributes ----------------------------------------------------

   /** A callback to the JMX MBeanServer */
   MBeanServer server;

   /** The array list keeps the order of deployment. */
   List services = new ArrayList();
   
   /** The map keeps the list of objectNames to services. */
   Map nameToServiceMap = new HashMap();

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   /**
    * Gets the Name attribute of the ServiceController object
    *
    * @return The Name value
    */
   public String getName()
   {
      return "Service Controller";
   }


   /**
    * Gets the Deployed attribute of the ServiceController object
    *
    * @return The Deployed value
    */
   public ObjectName[] getDeployed()
   {
      ObjectName[] deployed = new ObjectName[services.size()];
      services.toArray(deployed);

      return deployed;
   }

   /**
    * Gets the Configuration attribute of the ServiceController object
    *
    * @param objectNames Description of Parameter
    * @return The Configuration value
    * @exception Exception Description of Exception
    */
   public String getConfiguration(ObjectName[] objectNames) throws Exception
   {
      return configurator.getConfiguration(objectNames);
   }

   /**
    * Deploy the beans
    *
    * @param mbeanElement Description of Parameter
    * @return Description of the Returned Value
    * @throws Exception ???
    */
   public ObjectName deploy(Element mbeanElement)
          throws Exception
   {
      // The ObjectName of the bean
      ObjectName objectName = parseObjectName(mbeanElement);

      undeploy(objectName);

      // It is not there so really create the component now
      try
      {
         creator.create(mbeanElement);
      }

      catch (MBeanException mbe)
      {
         mbe.getTargetException().printStackTrace();
         throw mbe.getTargetException();
      }
      catch (RuntimeMBeanException mbe)
      {
         mbe.getTargetException().printStackTrace();
         throw mbe.getTargetException();
      }
      catch (RuntimeErrorException ree)
      {
         ree.getTargetError().printStackTrace();
         throw ree.getTargetError();
      }
      //
      // catch (MalformedObjectNameException mone) {}
      // catch (ReflectionException re) {}
      // catch (InstanceNotFoundException re) {}
      //
      catch (Throwable e)
      {
         log.error("Could not create MBean: " + objectName, e);
         if (e instanceof Exception)
            throw (Exception)e;
         if (e instanceof Error)
            throw (Error)e;
         throw new Error("unexpected throwable: " + e);
      }
      
      // Configure the MBean
      try
      {
         configurator.configure(mbeanElement);
      }
      catch (ConfigurationException e)
      {
         log.error("Could not configure MBean: " + objectName, e);
         throw e;
      }

      String serviceFactory = mbeanElement.getAttribute("serviceFactory");

      MBeanInfo info = server.getMBeanInfo(objectName);

      Service service = getServiceInstance(objectName,
            server.getMBeanInfo(objectName),
            mbeanElement.getAttribute("serviceFactory"));

      //Mbeans are init'ed and started by ServiceDeployer now.

      // we need to keep an order on the MBean it encapsulates dependencies
      services.add(objectName);
      // Keep track
      nameToServiceMap.put(objectName, service);

      //You are done
      return objectName;
   }

   /**
    * #Description of the Method
    *
    * @param mbeanElement Description of Parameter
    * @exception Exception Description of Exception
    */
   public void undeploy(Element mbeanElement) throws Exception
   {

      undeploy(parseObjectName(mbeanElement));
   }


   /**
    * #Description of the Method
    *
    * @param objectName Description of Parameter
    * @exception Exception Description of Exception
    */
   public void undeploy(ObjectName objectName) throws Exception
   {
      
      // Do we have a deployed MBean?
      if (server.isRegistered(objectName))
      {
         if (log.isDebugEnabled()) {
            log.debug("undeploying " + objectName + " from server");
         }
         
         //Remove from local maps
         services.remove(objectName);
         Service service = (Service)nameToServiceMap.remove(objectName);

         // Remove the MBean from the MBeanServer
         server.unregisterMBean(objectName);

         // Remove the MBeanClassLoader used by the MBean
         ObjectName loader =
            new ObjectName("ZClassLoaders:id=" + objectName.hashCode());
         server.unregisterMBean(loader);
      }
      else 
      {
         if (log.isDebugEnabled()) {
            log.debug("no need to undeploy " + objectName + " from server");
         }

      } // end of else
      
   }

   // MBeanRegistration implementation ----------------------------------------

   /**
    * #Description of the Method
    *
    * @param server Description of Parameter
    * @param name Description of Parameter
    * @return Description of the Returned Value
    * @exception Exception Description of Exception
    */
   public ObjectName preRegister(MBeanServer server, ObjectName name)
          throws Exception
   {
      this.server = server;

      creator = new ServiceCreator(server);
      configurator = new ServiceConfigurator(server);

      log.info("Controller MBean online");
      return name == null ? new ObjectName(OBJECT_NAME) : name;
   }

   // Service implementation ----------------------------------------

   /**
    * #Description of the Method
    *
    * @exception Exception Description of Exception
    */
   public void init()
          throws Exception
   {

      log.info("Initializing " + services.size() + " services");

      List servicesCopy = new ArrayList(services);
      Iterator enum = servicesCopy.iterator();
      int serviceCounter = 0;
      ObjectName name = null;

      while (enum.hasNext())
      {
         name = (ObjectName)enum.next();
         try
         {
            ((Service)nameToServiceMap.get(name)).init();
            serviceCounter++;
         }
         catch (Throwable e)
         {
            log.error("Could not initialize " + name, e);
         }
      }
      
      log.info("Initialized " + serviceCounter + " services");
   }

   /**
    * #Description of the Method
    *
    * @exception Exception Description of Exception
    */
   public void start()
          throws Exception
   {
      log.info("Starting " + services.size() + " services");

      List servicesCopy = new ArrayList(services);
      Iterator enum = servicesCopy.iterator();
      int serviceCounter = 0;
      ObjectName name = null;

      while (enum.hasNext())
      {
         name = (ObjectName)enum.next();

         try
         {
            ((Service)nameToServiceMap.get(name)).start();
            serviceCounter++;
         }
         catch (Throwable e)
         {
            log.error("Could not start " + name, e);
         }
      }
      log.info("Started " + serviceCounter + " services");
   }

   /**
    * #Description of the Method
    */
   public void stop()
   {
      log.info("Stopping " + services.size() + " services");

      List servicesCopy = new ArrayList(services);
      ListIterator enum = servicesCopy.listIterator();
      int serviceCounter = 0;
      ObjectName name = null;

      while (enum.hasNext())
      {
         enum.next();
      }
      // pass them all
      while (enum.hasPrevious())
      {
         name = (ObjectName)enum.previous();

         try
         {
            ((Service)nameToServiceMap.get(name)).stop();
            serviceCounter++;
         }
         catch (Throwable e)
         {
            log.error("Could not stop " + name, e);
         }
      }
      log.info("Stopped " + serviceCounter + " services");
   }

   /**
    * #Description of the Method
    */
   public void destroy()
   {
      log.info("Destroying " + services.size() + " services");

      List servicesCopy = new ArrayList(services);
      ListIterator enum = servicesCopy.listIterator();
      int serviceCounter = 0;
      ObjectName name = null;

      while (enum.hasNext())
      {
         enum.next();
      }
      // pass them all
      while (enum.hasPrevious())
      {
         name = (ObjectName)enum.previous();

         try
         {
            ((Service)nameToServiceMap.get(name)).destroy();
            serviceCounter++;
         }
         catch (Throwable e)
         {
            log.error("Could not destroy" + name, e);
         }
      }
      
      log.info("Destroyed " + serviceCounter + " services");
   }

   /**
    * #Description of the Method
    *
    * @param serviceName Description of Parameter
    * @exception Exception Description of Exception
    */
   public void init(ObjectName serviceName) throws Exception
   {
      if (nameToServiceMap.containsKey(serviceName))
      {

         ((Service)nameToServiceMap.get(serviceName)).init();
      }
      else
      {
         throw new InstanceNotFoundException
            ("Could not find " + serviceName.toString());
      }
   }

   /**
    * #Description of the Method
    *
    * @param serviceName Description of Parameter
    * @exception Exception Description of Exception
    */
   public void start(ObjectName serviceName) throws Exception
   {
      if (nameToServiceMap.containsKey(serviceName))
      {

         ((Service)nameToServiceMap.get(serviceName)).start();
      }
      else
      {
         throw new InstanceNotFoundException
            ("Could not find " + serviceName.toString());
      }
   }

   /**
    * #Description of the Method
    *
    * @param serviceName Description of Parameter
    * @exception Exception Description of Exception
    */
   public void stop(ObjectName serviceName) throws Exception
   {
      if (nameToServiceMap.containsKey(serviceName))
      {

         ((Service)nameToServiceMap.get(serviceName)).stop();
      }
      else
      {
         throw new InstanceNotFoundException
            ("Could not find " + serviceName.toString());
      }
   }

   /**
    * #Description of the Method
    *
    * @param serviceName Description of Parameter
    * @exception Exception Description of Exception
    */
   public void destroy(ObjectName serviceName) throws Exception
   {
      if (nameToServiceMap.containsKey(serviceName))
      {

         ((Service)nameToServiceMap.get(serviceName)).destroy();
      }
      else
      {
         throw new InstanceNotFoundException
            ("Could not find " + serviceName.toString());
      }
   }

   /**
    * Get the Service interface through which the mbean given by objectName
    * will be managed.
    *
    * @param objectName
    * @param info
    * @param serviceFactory
    * @return The ServiceInstance value
    * 
    * @throws ClassNotFoundException
    * @throws InstantiationException
    * @throws IllegalAccessException
    */
   private Service getServiceInstance(ObjectName objectName,
                                      MBeanInfo info,
                                      String serviceFactory)
          throws ClassNotFoundException, InstantiationException, IllegalAccessException
   {
      Service service = null;
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      if (serviceFactory != null && serviceFactory.length() > 0)
      {
         Class clazz = loader.loadClass(serviceFactory);
         ServiceFactory factory = (ServiceFactory)clazz.newInstance();
         service = factory.createService(server, objectName);
      }
      else
      {
         MBeanOperationInfo[] opInfo = info.getOperations();
         Class[] interfaces = {org.jboss.system.Service.class};
         InvocationHandler handler = new ServiceProxy(objectName, opInfo);
         service = (Service)Proxy.newProxyInstance(loader, interfaces, handler);
      }

      return service;
   }

   // Protected -----------------------------------------------------

   /**
    * Parse an object name from the given element attribute 'name'.
    *
    * @param element   Element to parse name from.
    * @return          Object name.
    * 
    * @throws ConfigurationException   Missing attribute 'name' (thrown if 
    *                                  'name' is null or "").
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
    * Go through the myriad of nested JMX exception to pull out the true
    * exception if possible and log it.
    *
    * @param e The exception to be logged.
    */
   private void logException(Throwable e)
   {
      if (e instanceof RuntimeErrorException)
      {
         e = ((RuntimeErrorException)e).getTargetError();
      }
      else if (e instanceof RuntimeMBeanException)
      {
         e = ((RuntimeMBeanException)e).getTargetException();
      }
      else if (e instanceof RuntimeOperationsException)
      {
         e = ((RuntimeOperationsException)e).getTargetException();
      }
      else if (e instanceof MBeanException)
      {
         e = ((MBeanException)e).getTargetException();
      }
      else if (e instanceof ReflectionException)
      {
         e = ((ReflectionException)e).getTargetException();
      }
      e.printStackTrace();
      log.error(e);
   }

   // Inner classes -------------------------------------------------

   /**
    * An implementation of InvocationHandler used to proxy of the Service
    * interface for mbeans. It determines which of the init/start/stop/destroy
    * methods of the Service interface an mbean implements by inspecting its
    * MBeanOperationInfo values. Each Service interface method that has a
    * matching operation is forwarded to the mbean by invoking the method
    * through the MBeanServer object.
    */
   private class ServiceProxy
      implements InvocationHandler
   {
      private boolean[] hasOp = { false, false, false, false };
      private ObjectName objectName;

      /**
       * Go through the opInfo array and for each operation that matches on of
       * the Service interface methods set the corresponding hasOp array value
       * to true.
       *
       * @param objectName
       * @param opInfo
       */
      public ServiceProxy(ObjectName objectName,
                          MBeanOperationInfo[] opInfo)
      {
         this.objectName = objectName;
         int opCount = 0;

         for (int op = 0; op < opInfo.length; op++)
         {
            MBeanOperationInfo info = opInfo[op];
            String name = info.getName();
            Integer opID = (Integer)serviceOpMap.get(name);
            if (opID == null)
            {
               continue;
            }

            // Validate that is a no-arg void return type method
            if (info.getReturnType().equals("void") == false)
            {
               continue;
            }
            if (info.getSignature().length != 0)
            {
               continue;
            }

            hasOp[opID.intValue()] = true;
            opCount++;
         }

         // Log a warning if the mbean does not implement
         // any Service methods
         if (opCount == 0)
         {
            log.warn(objectName +
                  " does not implement any Service methods");
         }
      }

      /**
       * Map the method name to a Service interface method index and if the
       * corresponding hasOp array element is true, dispatch the method to the
       * mbean we are proxying.
       *
       * @param proxy
       * @param method
       * @param args
       * @return             Always null.
       * @throws Throwable
       */
      public Object invoke(Object proxy, Method method, Object[] args)
             throws Throwable
      {
         String name = method.getName();
         Integer opID = (Integer)serviceOpMap.get(name);

         if (opID != null && hasOp[opID.intValue()] == true)
         {
            try
            {
               String[] sig = {};
               server.invoke(objectName, name, args, sig);
            }
            catch (JMRuntimeException e)
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

   /**
    * Initialize the service operation map.
    */
   static
   {
      serviceOpMap.put("init", new Integer(0));
      serviceOpMap.put("start", new Integer(1));
      serviceOpMap.put("destroy", new Integer(2));
      serviceOpMap.put("stop", new Integer(3));
   }
}

