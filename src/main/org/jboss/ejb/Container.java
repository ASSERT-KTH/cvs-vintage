/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.lang.reflect.Method;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.Reference;
import javax.naming.NamingException;
import javax.naming.StringRefAddr;
import javax.naming.RefAddr;
import javax.naming.NameNotFoundException;
import javax.transaction.TransactionManager;


import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.BeanLockManager;
import org.jboss.logging.Logger;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.EnvEntryMetaData;
import org.jboss.metadata.EjbRefMetaData;
import org.jboss.metadata.EjbLocalRefMetaData;
import org.jboss.metadata.ResourceRefMetaData;
import org.jboss.metadata.ResourceEnvRefMetaData;
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.security.EJBSecurityManager;
import org.jboss.security.RealmMapping;

import org.jboss.ejb.plugins.local.BaseLocalContainerInvoker;

/**
 * This is the base class for all EJB-containers in JBoss. A Container
 * functions as the central hub of all metadata and plugins. Through this
 * the container plugins can get hold of the other plugins and any metadata
 * they need.
 *
 * <p>The ContainerFactory creates instances of subclasses of this class
 *    and calls the appropriate initialization methods.
 *    
 * <p>A Container does not perform any significant work, but instead delegates
 *    to the plugins to provide for all kinds of algorithmic functionality.
 *
 * @see ContainerFactory
 * 
 * @author <a href="mailto:rickard.oberg@jboss.org">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1.62 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>2001/07/26 bill burke:</b>
 * <ul>
 * <li> Added BeanLockManager.
 * </ul>
 * <p><b>2001/08/13 scott.stark:</b>
 * <ul>
 * <li> Added DynamicMBean support for method invocations and access to EJB interfaces.
 * </ul>
 */
public abstract class Container implements DynamicMBean
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /** Instance logger. */
   protected Logger log = Logger.getLogger(this.getClass());

   /** This is the application that this container is a part of */
   protected Application application;

   /**
    * This is the local classloader of this container. Used for loading
    * resources that must come from the local jar file for the container.
    * NOT for loading classes!
    */
   protected ClassLoader localClassLoader;

   /**
    * This is the classloader of this container. All classes and resources that
    * the bean uses will be loaded from here. By doing this we make the bean
    * re-deployable
    */
   protected ClassLoader classLoader;

   /**
    * This is the new metadata. it includes information from both ejb-jar and
    * jboss.xml the metadata for the application can be accessed trough
    * metaData.getApplicationMetaData()
    */
   protected BeanMetaData metaData;

   /** This is the EnterpriseBean class */
   protected Class beanClass;

   /** This is the TransactionManager */
   protected TransactionManager tm;

   /** This is the SecurityManager */
   protected EJBSecurityManager sm;

   /** This is the realm mapping */
   protected RealmMapping rm;

   /** The custom security proxy used by the SecurityInterceptor */
   protected Object securityProxy;

   /** This is the bean lock manager that is to be used */
   protected BeanLockManager lockManager;

   /** ??? */
   protected LocalContainerInvoker localContainerInvoker = 
      new BaseLocalContainerInvoker();

   /** This is a cache for method permissions */
   private HashMap methodPermissionsCache = new HashMap();

   /** ??? */
   protected Class localHomeInterface;
   
   /** ??? */   
   protected Class localInterface;
   
   /** ObjectName of the JSR-77 EJB representation **/
   protected String mEJBObjectName;
   
   // We need the visibility on the MBeanServer for prototyping, it will be removed in the future FIXME marcf
   //protected MBeanServer mbeanServer;
   public MBeanServer mbeanServer;
   

   // Public --------------------------------------------------------

   public Class getLocalClass() 
   {
      return localInterface;
   }
   
   public Class getLocalHomeClass() 
   {
      return localHomeInterface;
   }
   
   /**
    * Sets a transaction manager for this container.
    *
    * @see javax.transaction.TransactionManager
    *
    * @param tm
    */
   public void setTransactionManager(TransactionManager tm)
   {
      this.tm = tm;
   }

   /**
    * Returns this container's transaction manager.
    *
    * @return    A concrete instance of javax.transaction.TransactionManager
    */
   public TransactionManager getTransactionManager()
   {
      return tm;
   }

   public void setSecurityManager(EJBSecurityManager sm)
   {
      this.sm = sm;
   }

   public EJBSecurityManager getSecurityManager()
   {
      return sm;
   }

   public BeanLockManager getLockManager()
   {
      return lockManager;
   }
	
	public void setLockManager(BeanLockManager lockManager) 
	{
		this.lockManager = lockManager;
	}

   public void setRealmMapping(RealmMapping rm)
   {
      this.rm = rm;
   }

   public RealmMapping getRealmMapping()
   {
      return rm;
   }

   public void setSecurityProxy(Object proxy)
   {
      this.securityProxy = proxy;
   }
   
   public Object getSecurityProxy()
   {
      return securityProxy;
   }
   void setMBeanServer(MBeanServer mbeanServer)
   {
      this.mbeanServer = mbeanServer;
   }

   /**
    * Sets the application deployment unit for this container. All the bean
    * containers within the same application unit share the same instance.
    *
    * @param   app     application for this container
    */
   public void setApplication(Application app)
   {
      if (app == null)
         throw new IllegalArgumentException("Null application");

      application = app;
   }

   /**
    * Returns the application for this container.
    *
    * @return
    */
   public Application getApplication()
   {
      return application;
   }

   /**
    * Sets the local class loader for this container. 
    * Used for loading resources from the local jar file for this container. 
    * NOT for loading classes!
    *
    * @param   cl
    */
   public void setLocalClassLoader(ClassLoader cl)
   {
      this.localClassLoader = cl;
   }

   /**
    * Returns the local classloader for this container.
    *
    * @return
    */
   public ClassLoader getLocalClassLoader()
   {
      return localClassLoader;
   }

   /**
    * Sets the class loader for this container. All the classes and resources
    * used by the bean in this container will use this classloader.
    *
    * @param   cl
    */
   public void setClassLoader(ClassLoader cl)
   {
      this.classLoader = cl;
   }

   /**
    * Returns the classloader for this container.
    *
    * @return
    */
   public ClassLoader getClassLoader()
   {
      return classLoader;
   }

   /**
    * Sets the meta data for this container. The meta data consists of the
    * properties found in the XML descriptors.
    *
    * @param metaData
    */
   public void setBeanMetaData(BeanMetaData metaData)
   {
      this.metaData = metaData;
   }

   /**
    * Returns the metadata of this container.
    *
    * @return metaData;
    */
   public BeanMetaData getBeanMetaData()
   {
      return metaData;
   }

   /**
    * Returns the permissions for a method. (a set of roles)
    *
    * @return assemblyDescriptor;
    */
   public Set getMethodPermissions( Method m, boolean home )
   {
      Set permissions;

      if (methodPermissionsCache.containsKey(m)) {
         permissions = (Set) methodPermissionsCache.get( m );
      } else {
         permissions = getBeanMetaData().
            getMethodPermissions(m.getName(), m.getParameterTypes(), !home);
         methodPermissionsCache.put(m, permissions);
      }
      return permissions;
   }

   /**
    * Returns the bean class instance of this container.
    *
    * @return    instance of the Enterprise bean class.
    */
   public Class getBeanClass()
   {
      return beanClass;
   }

   /**
    * Returns a new instance of the bean class or a subclass of the bean class.
    * This factory style method is speciffically used by a container to supply
    * an implementation of the abstract accessors in EJB2.0, but could be 
    * usefull in other situations. This method should ALWAYS be used instead 
    * of getBeanClass().newInstance();
    * 
    * @return    the new instance
    * 
    * @see java.lang.Class#newInstance 
    */
   public Object createBeanClassInstance() throws Exception {
      return getBeanClass().newInstance();
   }

   /**
    * The ContainerFactory calls this method.  The ContainerFactory has set
    * all the plugins and interceptors that this bean requires and now proceeds
    * to initialize the chain.  The method looks for the standard classes in 
    * the URL, sets up the naming environment of the bean. The concrete 
    * container classes should override this method to introduce
    * implementation specific initialization behaviour.
    *
    * @throws Exception    if loading the bean class failed
    *                      (ClassNotFoundException) or setting up "java:"
    *                      naming environment failed (DeploymentException)
    */
   public void init() throws Exception
   {
      // Acquire classes from CL
      beanClass = classLoader.loadClass(metaData.getEjbClass());

      if (metaData.getLocalHome() != null)
         localHomeInterface = classLoader.loadClass(metaData.getLocalHome());
      if (metaData.getLocal() != null)
         localInterface = classLoader.loadClass(metaData.getLocal());
      
      localContainerInvoker.setContainer( this );
      localContainerInvoker.init();
      if (localHomeInterface != null)
         application.addLocalHome(this, localContainerInvoker.getEJBLocalHome() );
      // Setup "java:comp/env" namespace
      setupEnvironment();
   }

   /**
    * A default implementation of starting the container service.
    * The container registers it's dynamic MBean interface in the JMX base.
    * FIXME marcf: give some more thought as to where to start and stop MBean registration.
    * stop could be a flag in the JMX server that essentially doesn't proxy invocations but the 
    * MBean would still be registered in the MBeanServer under the right name until undeploy
    
    * The concrete container classes should override this method to introduce
    * implementation specific start behaviour.
    *
    * @throws Exception    An exception that occured during start
    */
   public void start()
      throws Exception
   {
      localContainerInvoker.start();
      String jndiName = this.getBeanMetaData().getJndiName();
      ObjectName jmxName = new ObjectName("J2EE:service=EJB,jndiName="+jndiName);
      mbeanServer.registerMBean(this, jmxName);
   }

   /**
    * A default implementation of stopping the container service (no-op). The
    * concrete container classes should override this method to introduce
    * implementation specific stop behaviour.
    */
   public void stop()
   {
      localContainerInvoker.stop();
      try
      {
         String jndiName = this.getBeanMetaData().getJndiName();
         ObjectName jmxName = new ObjectName("J2EE:service=EJB,jndiName="+jndiName);
         mbeanServer.unregisterMBean(jmxName);
      }
      catch(Exception e)
      {
      }
   }

   /**
    * A default implementation of destroying the container service (no-op).
    * The concrete container classes should override this method to introduce
    * implementation specific destroy behaviour.
    */
   public void destroy()
   {
      localContainerInvoker.destroy();
      application.removeLocalHome( this );
   }

   /**
    * This method is called by the ContainerInvoker when a method call comes
    * in on the Home object.  The Container forwards this call to the
    * interceptor chain for further processing.
    *
    * @param mi   the object holding all info about this invocation
    * @return     the result of the home invocation
    * 
    * @throws Exception
    */
   public abstract Object invokeHome(MethodInvocation mi)
      throws Exception;

   /**
    * This method is called by the ContainerInvoker when a method call comes
    * in on an EJBObject.  The Container forwards this call to the interceptor
    * chain for further processing.
    *
    * @param id        the id of the object being invoked. May be null
    *                  if stateless
    * @param method    the method being invoked
    * @param args      the parameters
    * @return          the result of the invocation
    * 
    * @throws Exception
    */
   public abstract Object invoke(MethodInvocation mi)
      throws Exception;
      
      
   // DynamicMBean interface implementation ----------------------------------------------

   public Object getAttribute(String attribute)
      throws AttributeNotFoundException,
             MBeanException,
             ReflectionException
   {
      return null;
   }
   
   public void setAttribute(Attribute attribute)
      throws AttributeNotFoundException,
             InvalidAttributeValueException,
             MBeanException,
             ReflectionException
   {
   }

   public AttributeList getAttributes(String[] attributes)
   {
      return null;
   }

   public AttributeList setAttributes(AttributeList attributes)
   {
      return null;
   }

   /**
    * Handle a operation invocation.
    */
   public Object invoke(String actionName, Object[] params, String[] signature)
      throws MBeanException, ReflectionException
   {

      
      if( params != null && params.length == 1 && (params[0] instanceof MethodInvocation) == false )
         throw new MBeanException(new IllegalArgumentException("Expected zero or single MethodInvocation argument"));

      Object value = null;
      MethodInvocation mi = null;
      if( params != null && params.length == 1 )
         mi = (MethodInvocation) params[0];

      ClassLoader callerClassLoader = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(this.classLoader);
         // Check against home, remote, localHome, local, getHome, getRemote, getLocalHome, getLocal
         if( actionName.equals("remote") )
         {
            value = invoke(mi);
         }
         else if( actionName.equals("local") )
         {
            throw new MBeanException(new UnsupportedOperationException("local is not supported yet"));
         }
         else if( actionName.equals("home") )
         {
            value = invokeHome(mi);
         }
         else if( actionName.equals("localHome") )
         {
            throw new MBeanException(new UnsupportedOperationException("localHome is not supported yet"));
         }
         else if( actionName.equals("getHome") )
         {
            String className = this.getBeanMetaData().getHome();
            if( className != null )
            {
               Class clazz = this.classLoader.loadClass(className);
               value = clazz;
            }
         }
         else if( actionName.equals("getRemote") )
         {
            String className = this.getBeanMetaData().getRemote();
            if( className != null )
            {
               Class clazz = this.classLoader.loadClass(className);
               value = clazz;
            }
         }
         else if( actionName.equals("getLocalHome") )
         {
            value = this.localHomeInterface;
         }
         else if( actionName.equals("getLocal") )
         {
            value = this.localInterface;
         }
         else
         {
            throw new MBeanException(new IllegalArgumentException("Unknown action: "+actionName));
         }
      }
      catch (Exception e)
      {
         log.error("invoke returned an exception", e);
         throw new MBeanException(e, "invoke returned an exception");
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(callerClassLoader);
      }

      return value;
   }

   /**
    * Build the container MBean information on attributes, contstructors,
    * operations, and notifications. Currently there are no attributes, no
    * constructors, no notifications, and the following ops:
    * <ul>
    * <li>'home' -> invokeHome(MethodInvocation);</li>
    * <li>'remote' -> invoke(MethodInvocation);</li>
    * <li>'localHome' -> not implemented;</li>
    * <li>'local' -> not implemented;</li>
    * <li>'getHome' -> return EBJHome interface;</li>
    * <li>'getRemote' -> return EJBObject interface</li>
    * </ul>
    */
   public MBeanInfo getMBeanInfo()
   {
      MBeanParameterInfo miInfo = new MBeanParameterInfo("method", MethodInvocation.class.getName(), "MethodInvocation data");
      MBeanConstructorInfo[] ctorInfo = null;
      MBeanOperationInfo[] opInfo = {
         new MBeanOperationInfo("home", "Invoke an EJBHome interface method",
            new MBeanParameterInfo[] {miInfo},
            "java.lang.Object", MBeanOperationInfo.ACTION_INFO),
         new MBeanOperationInfo("remote", "Invoke an EJBObject interface method",
            new MBeanParameterInfo[] {miInfo},
            "java.lang.Object", MBeanOperationInfo.ACTION_INFO),
         new MBeanOperationInfo("getHome", "Get the EJBHome interface class",
            null,
            "java.lang.Class", MBeanOperationInfo.INFO),
         new MBeanOperationInfo("getRemote", "Get the EJBObject interface class",
            null,
            "java.lang.Class", MBeanOperationInfo.INFO)
      };
      MBeanNotificationInfo[] notifyInfo = null;
      return new MBeanInfo(getClass().getName(), "EJB Container MBean",
         null, ctorInfo, opInfo, notifyInfo);
   }

   // End DynamicMBean interface

   // Protected -----------------------------------------------------

   abstract Interceptor createContainerInterceptor();
   
   public abstract void addInterceptor(Interceptor in);

   // Private -------------------------------------------------------

   /**
    * This method sets up the naming environment of the bean.
    * We create the java:comp/env namespace with properties, EJB-References,
    * and DataSource ressources.
    */
   private void setupEnvironment()
      throws DeploymentException
   {
      try
      {
         BeanMetaData beanMetaData = getBeanMetaData();
         log.debug("Begin java:comp/env for EJB: "+beanMetaData.getEjbName());
         log.debug("TCL: "+Thread.currentThread().getContextClassLoader());
         // Since the BCL is already associated with this thread we can start using the java: namespace directly
         Context ctx = (Context) new InitialContext().lookup("java:comp");
         Context envCtx = ctx.createSubcontext("env");

         // Bind environment properties
         {
            Iterator enum = beanMetaData.getEnvironmentEntries();
            while(enum.hasNext())
            {
               EnvEntryMetaData entry = (EnvEntryMetaData)enum.next();
               try
               {
                  log.debug("Binding env-entry: "+entry.getName()+" of type: "+entry.getType()+" to value:"+entry.getValue());
                  EnvEntryMetaData.bindEnvEntry(envCtx, entry);
               }
               catch(ClassNotFoundException e)
               {
                  log.error("Could not set up environment", e);
                  throw new DeploymentException("Could not set up environment", e);
               }
            }
         }

         // Bind EJB references
         {
            Iterator enum = beanMetaData.getEjbReferences();
            while(enum.hasNext())
            {

               EjbRefMetaData ref = (EjbRefMetaData)enum.next();
               log.debug("Binding an EJBReference "+ref.getName());

               if (ref.getLink() != null)
               {
                  // Internal link
                  log.debug("Binding "+ref.getName()+" to internal JNDI source: "+ref.getLink());
                  Container refContainer = getApplication().getContainer(ref.getLink());
                  if (refContainer == null)
                     throw new DeploymentException ("Bean "+ref.getLink()+" not found within this application.");
                  bind(envCtx, ref.getName(), new LinkRef(refContainer.getBeanMetaData().getJndiName()));

                  //                   bind(envCtx, ref.getName(), new Reference(ref.getHome(), new StringRefAddr("Container",ref.getLink()), getClass().getName()+".EjbReferenceFactory", null));
                  //                bind(envCtx, ref.getName(), new LinkRef(ref.getLink()));
               }
               else
               {
                  // External link
                  if (ref.getJndiName() == null)
                  {
                     throw new DeploymentException("ejb-ref "+ref.getName()+", expected either ejb-link in ejb-jar.xml or jndi-name in jboss.xml");
                  }
                  log.debug("Binding "+ref.getName()+" to external JNDI source: "+ref.getJndiName());
                  bind(envCtx, ref.getName(), new LinkRef(ref.getJndiName()));
               }
            }
         }
        
         // Bind Local EJB references
         {
            Iterator enum = beanMetaData.getEjbLocalReferences();
            // unique key name
            String uniqueKey = Long.toString( (new java.util.Date()).getTime() );
            while(enum.hasNext())
            {

               EjbLocalRefMetaData ref = (EjbLocalRefMetaData)enum.next();
               log.debug("Binding an EJBLocalReference "+ref.getName());

               if (ref.getLink() != null)
               {
                  // Internal link
                  log.debug("Binding "+ref.getName()+" to bean source: "+ref.getLink());
                  if (getApplication().getContainer(ref.getLink()) == null)
                     throw new DeploymentException ("Bean "+ref.getLink()+" not found within this application.");
                  // get local home
                  // bind it into the local namespace
                  LocalHomeObjectFactory.rebind( uniqueKey + ref.getName(), 
                                                 getApplication(), getApplication().getContainer(ref.getLink()) );
                  StringRefAddr refAddr = new StringRefAddr("nns", uniqueKey+ref.getName() );
                  Reference jndiRef = new Reference(ref.getLocalHome(),
                                                    refAddr, LocalHomeObjectFactory.class.getName(), null );
                  bind(envCtx, ref.getName(), jndiRef );

               }
               else
               {
                  throw new DeploymentException( "Local references currently require ejb-link" );
               }
            }
         }

         // Bind resource references
         {
            Iterator enum = beanMetaData.getResourceReferences();

            // let's play guess the cast game ;)  New metadata should fix this.
            ApplicationMetaData application = beanMetaData.getApplicationMetaData();

            while(enum.hasNext())
            {
               ResourceRefMetaData ref = (ResourceRefMetaData)enum.next();

               String resourceName = ref.getResourceName();
               String finalName = application.getResourceByName(resourceName);
               /* If there was no resource-manager specified then an immeadiate
                  jndi-name or res-url name should have been given */
               if (finalName == null)
                  finalName = ref.getJndiName();

               if (finalName == null)
               {
                  // the application assembler did not provide a resource manager
                  // if the type is javax.sql.Datasoure use the default one

                  if (ref.getType().equals("javax.sql.DataSource"))
                  {
                     // Go through JNDI and look for DataSource - use the first one
                     Context dsCtx = new InitialContext();
                     try
                     {
                        // Check if it is available in JNDI
                        dsCtx.lookup("java:/DefaultDS");
                        finalName = "java:/DefaultDS";
                     } catch (Exception e)
                     {
                        log.debug("failed to lookup DefaultDS; ignoring", e);
                     }
                  }

                  // Default failed? Warn user and move on
                  // POTENTIALLY DANGEROUS: should this be a critical error?
                  if (finalName == null)
                  {
                     log.warn("No resource manager found for "+ref.getResourceName());
                     continue;
                  }
               }

               if (ref.getType().equals("java.net.URL"))
               {
                  // URL bindings
                  try
                  {
                     log.debug("Binding URL: "+finalName+ " to JDNI ENC as: " +ref.getRefName());
                     bind(envCtx, ref.getRefName(), new URL(finalName));
                  } catch (MalformedURLException e)
                  {
                     throw new NamingException("Malformed URL:"+e.getMessage());
                  }
               }
               else
               {
                  // Resource Manager bindings, should validate the type...
                  log.debug("Binding resource manager: "+finalName+ " to JDNI ENC as: " +ref.getRefName());
                  bind(envCtx, ref.getRefName(), new LinkRef(finalName));
               }
            }
         }

         // Bind resource env references
         {
            Iterator enum = beanMetaData.getResourceEnvReferences();
            while( enum.hasNext() )
            {
               ResourceEnvRefMetaData resRef = (ResourceEnvRefMetaData) enum.next();
               String encName = resRef.getRefName();
               String jndiName = resRef.getJndiName();
               // Should validate the type...
               log.debug("Binding env resource: "+jndiName+ " to JDNI ENC as: " +encName);
               bind(envCtx, encName, new LinkRef(jndiName));
            }
         }

         /* Create a java:comp/env/security/security-domain link to the container
            or application security-domain if one exists so that access to the
            security manager can be made without knowing the global jndi name.
         */
         String securityDomain = metaData.getContainerConfiguration().getSecurityDomain();
         if( securityDomain == null )
            securityDomain = metaData.getApplicationMetaData().getSecurityDomain();
         if( securityDomain != null )
         {
            log.debug("Binding securityDomain: "+securityDomain+ " to JDNI ENC as: security/security-domain");
            bind(envCtx, "security/security-domain", new LinkRef(securityDomain));
            bind(envCtx, "security/subject", new LinkRef(securityDomain+"/subject"));
         }

         log.debug("End java:comp/env for EJB: "+beanMetaData.getEjbName());
      } catch (NamingException e)
      {
         log.error("Could not set up environment", e);
         log.error("root cause", e.getRootCause());
         throw new DeploymentException("Could not set up environment", e);
      }
   }


   /**
    * Bind a value to a name in a JNDI-context, and create any missing
    * subcontexts.
    *
    * @param ctx
    * @param name
    * @param val
    * 
    * @throws NamingException
    */
   private void bind(Context ctx, String name, Object val)
      throws NamingException
   {
      // Bind val to name in ctx, and make sure that all
      // intermediate contexts exist
      Name n = ctx.getNameParser("").parse(name);
      while (n.size() > 1)
      {
         String ctxName = n.get(0);
         try
         {
            ctx = (Context)ctx.lookup(ctxName);
         } catch (NameNotFoundException e)
         {
            ctx = ctx.createSubcontext(ctxName);
         }
         n = n.getSuffix(1);
      }

      ctx.bind(n.get(0), val);
   }
}
