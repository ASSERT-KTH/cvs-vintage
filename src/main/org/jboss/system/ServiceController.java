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
import java.util.LinkedList;
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
import org.jboss.logging.Logger;

/**
* This is the main Service Controller. A controller can deploy a service to a
* JBOSS-SYSTEM It installs by delegating, it configures by delegating
*
* @see org.jboss.system.Service
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
* @version $Revision: 1.19 $ <p>
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
*       of successes on init etc.  Modified to support remove and work with
*       .sar dependency management and recursive sar deployment.
* </ol>
* <b>2001210 marcf</b>
* <ol>
*   <li>Rewrite
* </ol>
*/
public class ServiceController
//extends ServiceMBeanSupport
implements ServiceControllerMBean, MBeanRegistration
{
   // Attributes ----------------------------------------------------
   
   private final Logger log = Logger.getLogger(getClass());
   
   /** A callback to the JMX MBeanServer */
   MBeanServer server;
   
   /** Creator, helper class to instanciate MBeans **/
   protected ServiceCreator creator;
   
   /** Configurator, helper class to configure MBeans **/
   protected ServiceConfigurator configurator;
   
   /** Object Name to Service Proxy map **/
   protected Map nameToServiceMap = new HashMap();
   
   /** A linked list of services in the order they were created **/
   protected List installedServices = new LinkedList();
   
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   
   public Logger getLog() { return log;}
   
   public String getName() { return "Service Controller"; }
   
   
   /**
   * Gets the Deployed attribute of the ServiceController object
   *
   * @return The Deployed value
   */
   public ObjectName[] getDeployed()
   {
      ObjectName[] deployed = new ObjectName[installedServices.size()];
      
      ListIterator iterator = installedServices.listIterator();
      
      for (int i = 0; i<installedServices.size() ; i++)
      {
         
         deployed[i] = ((ServiceContext) iterator.next()).objectName;
      
      }
      
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
   * Deploy means "instanciate and configure" so the MBean is created in the MBeanServer
   * You must call "create" and "start" separately on the MBean to affect the service lifecycle
   * deploy doesn't bother with service lifecycle only MBean instanciation/registration/configuration
   * 
   * @param mbeanElement Description of Parameter
   * @return Description of the Returned Value
   * @throws Exception ???
   */
   public synchronized ObjectName install(Element mbeanElement)
   throws Exception
   {
      
      // Create a Service Context for the service, or get one if it exists
      ServiceContext ctx = getServiceContext(parseObjectName(mbeanElement));
      
      // MARCF FIXME THINK ABOUT REMOVe IF ACTIVE HERE
      //at least make a new version!
      //remove(objectName);
      
      // It is not there so really create the component now, this registers the component in the mbeanserver
      try
      {
         creator.install(mbeanElement);
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
         log.error("Could not create MBean: " + ctx.objectName, e);
         if (e instanceof Exception)
            throw (Exception)e;
         if (e instanceof Error)
            throw (Error)e;
         
         throw new Error("unexpected throwable: " + e);
      }
      
      // We got this far
      ctx.state = ServiceContext.INSTALLED;
      
      try { 
         
         // Configure the MBean
         synchronized (this)
         {
            
            // The return is a list of MBeans this MBean "depends" on
            List mbeans = configurator.configure(mbeanElement);
            
            // Link the dependency me.idependOn(them) and them.dependsOnMe(me)
            Iterator iterator = mbeans.iterator();
            while (iterator.hasNext()) {
               
               // We work from the service context, if it doesn't exist yet, we have a wrapper (OK)
               ServiceContext service = getServiceContext((ObjectName) iterator.next());
               
               // ctx depends on service
               ctx.iDependOn.add(service);
               
               // Service needs to know I depend on him
               service.dependsOnMe.add(ctx);
            }
         } 
      }
      catch (Exception e)
      {
         log.error("Could not configure MBean: " + ctx.objectName, e);
         
         server.unregisterMBean(ctx.objectName);
         
         ctx.state = ServiceContext.FAILED ; 
         throw e;
      
      }
      
      // We got this far
      ctx.state = ServiceContext.CONFIGURED;
      
      if ( !installedServices.contains(ctx)) installedServices.add(ctx);
         
      return ctx.objectName;
   }
   
   /**
   * #Description of the Method
   *
   * @param serviceName Description of Parameter
   * @exception Exception Description of Exception
   */
   public synchronized void create(ObjectName serviceName) throws Exception
   {
      
      ServiceContext ctx = (ServiceContext) getServiceContext(serviceName);
      
      
      // Get the fancy service proxy (for the lifecycle API)
      ctx.proxy = getServiceProxy(ctx.objectName, null);
      
      // If we are already created (can happen in dependencies) just return
      if (ctx.state == ServiceContext.CREATED || ctx.state == ServiceContext.RUNNING) return;
         
      // JSR 77, and to avoid circular dependencies
      int oldState = ctx.state; 
      ctx.state= ServiceContext.CREATED;
      
      // Are all the mbeans I depend on created?   if not just return
      Iterator iterator = ctx.iDependOn.iterator();
      while (iterator.hasNext())
      {
         ServiceContext sc = (ServiceContext) iterator.next();
         
         int state = sc.state;
         
         // A dependent is not created or running
         if (!(state == ServiceContext.CREATED || state == ServiceContext.RUNNING)) {
            
            
            log.info("&&&&&&&&&&&&&&&&&&&&&&&&  waiting in create "+serviceName.toString() +" waiting on "+sc.objectName);
            ctx.state=oldState;
            return;
         }
      }
      
      // Call create on the service Proxy  
      try { ctx.proxy.create(); }
         
      catch (Exception e){ ctx.state = ServiceContext.FAILED; throw e;}
      
      // Those that depend on me are waiting for my creation, recursively create them
      Iterator iterator2 = ctx.dependsOnMe.iterator();
      while (iterator2.hasNext()) 
      {
         // marcf fixme circular dependencies?
         create(((ServiceContext) iterator2.next()).objectName);  
      }
   }
   
   
   
   /**
   * #Description of the Method
   *
   * @param serviceName Description of Parameter
   * @exception Exception Description of Exception
   */
   public synchronized void start(ObjectName serviceName) throws Exception
   {
      
      ServiceContext ctx = (ServiceContext) getServiceContext(serviceName);
      
      // If we are already started (can happen in dependencies) just return
      if (ctx.state == ServiceContext.RUNNING) return;
         
      // JSR 77, and to avoid circular dependencies
      int oldState = ctx.state; 
      ctx.state= ServiceContext.RUNNING;
      
      // Are all the mbeans I depend on created?   if not just return
      Iterator iterator = ctx.iDependOn.iterator();
      while (iterator.hasNext())
      {
         ServiceContext sctx = (ServiceContext) iterator.next();
         
         int state  = sctx.state;
         
         // A dependent is not running
         if (!(state == ServiceContext.RUNNING))
         {
            ObjectName cac = new ObjectName("Test:name=test");
            ObjectName ca2 = new ObjectName("Test:name=test");
            
            log.info("%%%%%%%%%%%%%%%%%%   waiting in start"+serviceName.toString()+" on "+sctx.objectName);
            
            ctx.state=oldState;
            return;
         }
      }
      
      // Call create on the service Proxy  
      try { ctx.proxy.start(); }
         
      catch (Exception e){ ctx.state = ServiceContext.FAILED; throw e;}
      
      // JSR 77
      ctx.state = ServiceContext.RUNNING;
      
      if (!installedServices.contains(ctx)) installedServices.add(ctx);
      
      // Those that depend on me are waiting for my creation, recursively create them
      Iterator iterator2 = ctx.dependsOnMe.iterator();
      while (iterator2.hasNext()) 
      {
         // marcf fixme circular dependencies?
         start(((ServiceContext) iterator2.next()).objectName);  
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
      
      ServiceContext ctx = (ServiceContext) nameToServiceMap.get(serviceName);
      
      if (ctx != null) 
      {
         // If we are already stopped (can happen in dependencies) just return
         if (ctx.state == ServiceContext.STOPPED) return;
            
         // JSR 77 and to avoid circular dependencies
         ctx.state = ServiceContext.STOPPED;
         
         Iterator iterator = ctx.dependsOnMe.iterator();
         while (iterator.hasNext())
         {
            
            // stop all the mbeans that depend on me
            stop(((ServiceContext) iterator.next()).objectName);  
         }
         
         // Call create on the service Proxy  
         try { ctx.proxy.stop(); }
            
         catch (Exception e){ ctx.state = ServiceContext.FAILED; throw e;}
      
      
      
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
      
      ServiceContext ctx = (ServiceContext) nameToServiceMap.get(serviceName);
      
      if (ctx != null) 
      {
         // If we are already destroyed (can happen in dependencies) just return
         if (ctx.state == ServiceContext.DESTROYED) return;
            
         // JSR 77, and to avoid circular dependencies
         ctx.state = ServiceContext.DESTROYED;
         
         Iterator iterator = ctx.dependsOnMe.iterator();
         while (iterator.hasNext())
         {
            
            // destroy all the mbeans that depend on me
            destroy(((ServiceContext) iterator.next()).objectName);  
         }
         
         // Call create on the service Proxy  
         try { ctx.proxy.destroy(); }
            
         catch (Exception e){ ctx.state = ServiceContext.FAILED; throw e;}
      
      
      }
   }   
   
   
   
   /**
   * #Description of the Method
   *
   * @param mbeanElement Description of Parameter
   * @exception Exception Description of Exception
   */
   public void remove(Element mbeanElement) throws Exception
   {
      
      remove(parseObjectName(mbeanElement));
   }
   
   
   /**
   * This MBean is going buh bye
   *
   * @param objectName Description of Parameter
   * @exception Exception Description of Exception
   */
   public void remove(ObjectName objectName) throws Exception
   {
      
      ServiceContext ctx = getServiceContext(objectName);    
      
      // Notify those that think I depend on them
      Iterator iterator = ctx.iDependOn.iterator();
      while (iterator.hasNext()) 
      {
         ((ServiceContext) iterator.next()).dependsOnMe.remove(ctx);
      }
      
      // Do we have a deployed MBean?
      if (server.isRegistered(objectName))
      {
         if (log.isDebugEnabled()) {
            log.debug("removing " + objectName + " from server");
         }
         
         nameToServiceMap.remove(objectName);
         
         // remove the mbean from the instaled ones
         installedServices.remove(ctx);
         
         creator.remove(objectName);
      }
      else 
      {
         if (log.isDebugEnabled()) {
            log.debug("no need to remove " + objectName + " from server");
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
   
   
   // Service implementation ----------------------------------------
   
   
   /**
   * This is the only one we should have of these lifecycle methods!
   */
   public synchronized void shutdown()
   {
      log.info("Stopping " + nameToServiceMap.size() + " services");
      
      
      List servicesCopy = new ArrayList(installedServices);
      
      int serviceCounter = 0;
      ObjectName name = null;
      
      for (ListIterator i = servicesCopy.listIterator(servicesCopy.size() - 1);
         i.hasPrevious(); ) 
      {
         name = ((ServiceContext)i.previous()).objectName;
         
         try
         {
            remove(name);
            serviceCounter++;
         }
         catch (Throwable e)
         {
            log.error("Could not remove " + name, e);
         }
      }
      log.info("Stopped " + serviceCounter + " services");
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
   private Service getServiceProxy(ObjectName objectName,
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
   private void logException(String message, Throwable e)
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
      log.error(message, e);
   }
   
   // Inner classes -------------------------------------------------
   
   // Create a Service Context for the service, or get one if it exists
   public synchronized ServiceContext getServiceContext(ObjectName objectName)
   {
      // If it is already there just return it
      if (nameToServiceMap.containsKey(objectName)) return (ServiceContext) nameToServiceMap.get(objectName);
         
      // If not create it, add it and return it
      ServiceContext ctx = new ServiceContext();
      ctx.objectName = objectName;
      
      // we keep track of these here
      nameToServiceMap.put(objectName, ctx);
      
      return ctx;
   }
   
   
   
   /**
   * A mapping from the Service interface method names to the corresponding
   * index into the ServiceProxy.hasOp array.
   */
   private static HashMap serviceOpMap = new HashMap();
   
   /**
   * An implementation of InvocationHandler used to proxy of the Service
   * interface for mbeans. It determines which of the start/stop
   * methods of the Service interface an mbean implements by inspecting its
   * MBeanOperationInfo values. Each Service interface method that has a
   * matching operation is forwarded to the mbean by invoking the method
   * through the MBeanServer object.
   */
   public class ServiceProxy
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
               logException("JMRuntimeException thrown during ServiceProxy operation " +
                  name + " on mbean " + objectName, e);
               throw e;
            }
            catch (JMException e)
            {
               logException("JMException thrown during ServiceProxy operation " +
                  name + " on mbean " + objectName, e);
               throw e;
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
      serviceOpMap.put("create", new Integer(0));
      serviceOpMap.put("start", new Integer(1));
      
      serviceOpMap.put("destroy", new Integer(2));
      serviceOpMap.put("stop", new Integer(3));
   }
}

