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
import javax.management.IntrospectionException;
import org.jboss.logging.Logger;

/**
 * This is the main Service Controller. A controller can deploy a service to a
 * JBOSS-SYSTEM It installs by delegating, it configures by delegating
 *
 * @see org.jboss.system.Service
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version $Revision: 1.12 $ <p>
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
   //extends ServiceMBeanSupport
       implements ServiceControllerMBean, MBeanRegistration
{

   private final Logger log = Logger.create(getClass());


   /**
    * A mapping from the Service interface method names to the corresponding
    * index into the ServiceProxy.hasOp array.
    */
   private static HashMap serviceOpMap = new HashMap();
   /**
    * Map between mbeans and the mbean references they have
    */
   private static HashMap mbeanToMBeanRefsMap = new HashMap();
   //inverse map
   private static HashMap mbeanRefToMBeansMap = new HashMap();

   /**
    * The set of mbeans that can't find all their mbean references, mapped to 
    * the mbeans they can't find.
    */
   private static HashMap suspendedToMissingMBeanMap = new HashMap();

   /**
    * The map between missing mbeans and what is waiting for them : 
    * inverse of suspendedToMissingMBeanMap
    */
   private static HashMap missingToSuspendedMBeanMap = new HashMap();


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
   List startedServices = new ArrayList();
   
   /** The map keeps the list of objectNames to services. */
   Map nameToServiceMap = new HashMap();

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   public Logger getLog()
   {
      return log;
   }

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
      ObjectName[] deployed = new ObjectName[startedServices.size()];
      startedServices.toArray(deployed);

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
      //at least make a new version!
      //undeploy(objectName);

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
      boolean waiting = false;
      try
      {
         ArrayList mBeanRefs = configurator.configure(mbeanElement);
         if (mBeanRefs.size() > 0) 
         {
            synchronized (this)
            {
               mbeanToMBeanRefsMap.put(objectName, mBeanRefs);
               //ArrayList waitingFor = new ArrayList();
               Iterator refs = mBeanRefs.iterator();
               while (refs.hasNext()) 
               {
                  //First, record dependencies
                  ObjectName ref = (ObjectName)refs.next();
                  ArrayList backRefs = (ArrayList)mbeanRefToMBeansMap.get(ref);
                  if (backRefs == null) 
                  {
                     backRefs = new ArrayList();
                     mbeanRefToMBeansMap.put(ref, backRefs);
                  } // end of if ()
                  if (!backRefs.contains(objectName)) 
                  {
                     backRefs.add(objectName);        
                  } // end of if ()   
                  //Now, does the needed mbean exist? if not, note suspension.
                  //We could use the mbean server or our own service map.
                  //if (!nameToServiceMap.containsKey(ref)) 
                  if (!startedServices.contains(ref)) 
                  {
                     //Not there, mark suspended.
                     markWaiting(ref, objectName);
                  } // end of if ()
               } // end of while ()
            }   
         
         } // end of if (mBeanRefs.size() > 0)
      }
      catch (ConfigurationException e)
      {
         log.error("Could not configure MBean: " + objectName, e);
         throw e;
      }
      String serviceFactory = mbeanElement.getAttribute("serviceFactory");
      registerAndStartService(objectName, serviceFactory);
      return objectName;
   }

   //needs to be an mbean method, for e.g. RARDeployer to register RAR's for dependency.
   //puts a mbean that you created some other way (such as deployed rar) 
   //into dependency system so other beans can be started/stopped on its 
   //existence (or registration)
   public void registerAndStartService(ObjectName serviceName, String serviceFactory) throws Exception
   {
      try 
      {
         Service service = getServiceInstance(serviceName, serviceFactory);
         // Keep track
         nameToServiceMap.put(serviceName, service);

         start(serviceName);
         //You are done
      }
      catch (Exception e) 
      {
         log.error("Problem in registerAndStartService", e);
         //dont let anyone think we're started if we're not!
         nameToServiceMap.remove(serviceName);
         throw e;
      } // end of try-catch

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
      stop(objectName);
      //We are not needing anyone or waiting for anyone
      ArrayList weNeededList = (ArrayList)mbeanToMBeanRefsMap.remove(objectName);
      if (weNeededList != null) 
      {
         //remove the back reference for each mbean we needed.
         Iterator needed = weNeededList.iterator();
         while (needed.hasNext()) 
         {
            ObjectName neededName = (ObjectName)needed.next();
            ArrayList needingList = (ArrayList)mbeanRefToMBeansMap.get(neededName);
            if (needingList != null) 
            {
               needingList.remove(objectName);
            } // end of if ()
            
         } // end of while ()
         
      } // end of if ()
      
      ArrayList weSuspendedForList = (ArrayList)suspendedToMissingMBeanMap.remove(objectName);
      if (weSuspendedForList != null) 
      {
         //remove the back reference for each mbean we suspended for.
         Iterator suspendedFor = weSuspendedForList.iterator();
         while (suspendedFor.hasNext()) 
         {
            ObjectName suspendedForName = (ObjectName)suspendedFor.next();
            ArrayList suspendedOnList = (ArrayList)missingToSuspendedMBeanMap.get(suspendedForName);
            if (suspendedOnList != null) 
            {
               suspendedOnList.remove(objectName);
            } // end of if ()
            
         } // end of while ()
         
      } // end of if ()
      
      
      // Do we have a deployed MBean?
      if (server.isRegistered(objectName))
      {
         if (log.isDebugEnabled()) {
            log.debug("undeploying " + objectName + " from server");
         }
         
         //Remove from local maps
         startedServices.remove(objectName);
         Service service = (Service)nameToServiceMap.remove(objectName);

         // Remove the MBean from the MBeanServer
         server.unregisterMBean(objectName);

         // Remove the MBeanClassLoader used by the MBean
         ObjectName loader =
            new ObjectName("ZClassLoaders:id=" + objectName.hashCode());
         if (server.isRegistered(loader)) 
         {
            server.unregisterMBean(loader);
         } // end of if ()
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

   public void postRegister(Boolean registrationDone)
   {
      if (!registrationDone.booleanValue())
      {
         log.info( "Registration of ServiceController failed" );
      }
   }
   
   public void preDeregister()
      throws Exception
   {
   }
   
   public void postDeregister()
   {
   }

   // methods about suspension.

   public synchronized boolean isSuspended(ObjectName objectName)
   {
      return suspendedToMissingMBeanMap.containsKey(objectName);
   }

   // Service implementation ----------------------------------------


   /**
    * This is the only one we should have of these lifecycle methods!
    */
   public synchronized void shutdown()
   {
      log.info("Stopping " + startedServices.size() + " services");

      List servicesCopy = new ArrayList(startedServices);
      //ListIterator enum = servicesCopy.listIterator();
      int serviceCounter = 0;
      ObjectName name = null;

      for (ListIterator i = servicesCopy.listIterator(servicesCopy.size() - 1);
           i.hasPrevious(); ) 
      {
         name = (ObjectName)i.previous();

         try
         {
            undeploy(name);
            //((Service)nameToServiceMap.get(name)).stop();
            //((Service)nameToServiceMap.get(name)).destroy();//should be obsolete soon
            //server.unregisterMBean(name);
            serviceCounter++;
         }
         catch (Throwable e)
         {
            log.error("Could not undeploy " + name, e);
         }
      }
      log.info("Stopped " + serviceCounter + " services");
   }




   /**
    * #Description of the Method
    *
    * @param serviceName Description of Parameter
    * @exception Exception Description of Exception
    */
   public void start(ObjectName serviceName) throws Exception
   {
      if (suspendedToMissingMBeanMap.containsKey(serviceName)) 
      {
         log.debug("waiting to start " + serviceName + " until dependencies are resolved");
         return;     
      } // end of if ()
       
      if (nameToServiceMap.containsKey(serviceName))
      {

         ((Service)nameToServiceMap.get(serviceName)).start();
         startedServices.add(serviceName);
      }
      else
      {
         throw new InstanceNotFoundException
            ("Could not find " + serviceName.toString());
      }
      ArrayList waitingList = (ArrayList)missingToSuspendedMBeanMap.remove(serviceName);
      if (waitingList != null) 
      {
         Iterator waiting = waitingList.iterator();
         while (waiting.hasNext()) 
         {
            ObjectName waitingName = (ObjectName)waiting.next();        
            //Is it waiting for anyone else?
            ArrayList waitingFor = (ArrayList)suspendedToMissingMBeanMap.get(waitingName);
            if (waitingFor == null) 
            {
               //maybe should be DeploymentException, but it might pull in too many classes.
               throw new Exception("Missing suspended to Missing map entry between suspended: " + waitingName + " and missing: " + serviceName);                    
            } // end of if ()
            waitingFor.remove(serviceName);
            if (waitingFor.size() == 0) 
            {
               //not waiting for anyone else, can finish deploying.
               log.debug("missing mbeans now present, finishing deployment of " + waitingName);
               suspendedToMissingMBeanMap.remove(waitingName);
               start(waitingName);
            } // end of if ()
            else 
            {
               log.debug("There are still missing mbeans, deployment of " + waitingName + " postponed");

            } // end of else
            
         } // end of while ()
         
              
      } // end of if ()
      
   }

   /**
    * #Description of the Method
    *
    * @param serviceName Description of Parameter
    * @exception Exception Description of Exception
    */
   public void stop(ObjectName serviceName) throws Exception
   {
      ArrayList usingList = (ArrayList)mbeanRefToMBeansMap.get(serviceName);
      if (usingList != null) 
      {
         //we have to stop these beans that depend on serviceName. 
         Iterator using = usingList.iterator();  
         while (using.hasNext()) 
         {
            ObjectName usingName = (ObjectName)using.next();
            log.debug("stopping object " + usingName + " using " + serviceName);
            if (!isWaitingFor(serviceName, usingName)) 
            {
               markWaiting(serviceName, usingName);
               stop(usingName);
            } // end of if ()
           
         } // end of while ()
         
      } // end of if ()
      
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
                                      String serviceFactory)
       throws ClassNotFoundException, InstantiationException, IllegalAccessException, JMException//, InstanceNotFoundException, IntrospectionException
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
         MBeanInfo info = server.getMBeanInfo(objectName);
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

   private void markWaiting(ObjectName missing, ObjectName waiting)
   {
      ArrayList waitingList = (ArrayList)missingToSuspendedMBeanMap.get(missing);
      if (waitingList == null) 
      {
         waitingList = new ArrayList();
         missingToSuspendedMBeanMap.put(missing, waitingList);
      } // end of if ()
      if (!waitingList.contains(waiting)) 
      {
         waitingList.add(waiting);        
      } // end of if ()          
      //Now do the other way.         
      ArrayList missingList = (ArrayList)suspendedToMissingMBeanMap.get(waiting);
      if (missingList == null) 
      {
         missingList = new ArrayList();
         suspendedToMissingMBeanMap.put(waiting, missingList);
      } // end of if ()
      if (!missingList.contains(missing)) 
      {
         missingList.add(missing);        
      } // end of if ()                   
   }

   private boolean isWaitingFor(ObjectName serviceName, ObjectName usingName)
   {
      ArrayList waitingList = (ArrayList)missingToSuspendedMBeanMap.get(serviceName);
      if (waitingList == null) 
      {
         return false;//noone is waiting for this guy.        
      } // end of if ()
      return waitingList.contains(usingName);
   }

   // Inner classes -------------------------------------------------

   /**
    * An implementation of InvocationHandler used to proxy of the Service
    * interface for mbeans. It determines which of the start/stop
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
      //serviceOpMap.put("init", new Integer(0));
      serviceOpMap.put("start", new Integer(1));
      //serviceOpMap.put("destroy", new Integer(2));
      serviceOpMap.put("stop", new Integer(3));
   }
}

