/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.MalformedURLException;
import java.security.Principal;
import java.util.Map;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Enumeration;

import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.EJBObject;
import javax.ejb.EJBMetaData;
import javax.ejb.CreateException;
import javax.ejb.RemoveException;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.Reference;
import javax.naming.NamingEnumeration;
import javax.naming.NameClassPair;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.NameNotFoundException;
import javax.naming.spi.ObjectFactory;
import javax.transaction.TransactionManager;
import javax.sql.DataSource;

import org.jboss.logging.Logger;
import org.jboss.security.EJBSecurityManager;
import org.jboss.security.RealmMapping;

import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.EnvEntryMetaData;
import org.jboss.metadata.EjbRefMetaData;
import org.jboss.metadata.ResourceRefMetaData;
import org.jboss.metadata.ApplicationMetaData;

import org.jnp.interfaces.Naming;
import org.jnp.interfaces.java.javaURLContextFactory;
import org.jnp.server.NamingServer;

import org.jboss.ejb.plugins.local.BaseLocalContainerInvoker;

/**
 *    This is the base class for all EJB-containers in JBoss. A Container
 *    functions as the central hub of all metadata and plugins. Through this
 *    the container plugins can get hold of the other plugins and any metadata they need.
 *
 *    The ContainerFactory creates instances of subclasses of this class and calls the appropriate
 *    initialization methods.
 *
 *    A Container does not perform any significant work, but instead delegates to the plugins to provide for
 *    all kinds of algorithmic functionality.
 *
 *   @see ContainerFactory
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @author <a href="marc.fleury@telkel.com">Marc Fleury</a>
 *   @version $Revision: 1.40 $
 */
public abstract class Container
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // This is the application that this container is a part of
   protected Application application;

   // This is the local classloader of this container. Used for loading resources that
   // must come from the local jar file for the container.  NOT for loading classes!
   protected ClassLoader localClassLoader;

   // This is the classloader of this container. All classes and resources that
   // the bean uses will be loaded from here. By doing this we make the bean re-deployable
   protected ClassLoader classLoader;

   // This is the new metadata. it includes information from both ejb-jar and jboss.xml
   // the metadata for the application can be accessed trough metaData.getApplicationMetaData()
   protected BeanMetaData metaData;

   // This is the EnterpriseBean class
   protected Class beanClass;

   // This is the TransactionManager
   protected TransactionManager tm;

   // This is the SecurityManager
   protected EJBSecurityManager sm;

   // This is the realm mapping
   protected RealmMapping rm;

   /** The custom security proxy used by the SecurityInterceptor */
   protected Object securityProxy;
   
   protected LocalContainerInvoker localContainerInvoker = 
      new BaseLocalContainerInvoker();

   // This is a cache for method permissions
   private HashMap methodPermissionsCache = new HashMap();

   protected Class localHomeInterface;
   
   protected Class localInterface;

   
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
   * @param    tm
   */
   public void setTransactionManager(TransactionManager tm)
   {
      this.tm = tm;
   }

   /**
   * Returns this container's transaction manager.
   *
   * @return  a concrete instance of javax.transaction.TransactionManager
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
   * @param   metaData
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
         permissions = getBeanMetaData().getMethodPermissions(m.getName(), m.getParameterTypes(), !home);
         methodPermissionsCache.put(m, permissions);
      }
      return permissions;

   }

   /**
   * Returns the bean class instance of this container.
   *
   * @return  instance of the Enterprise bean class
   */
   public Class getBeanClass()
   {
      return beanClass;
   }

   /**
   * The ContainerFactory calls this method.  The ContainerFactory has set all the
   * plugins and interceptors that this bean requires and now proceeds to initialize
   * the chain.  The method looks for the standard classes in the URL, sets up
   * the naming environment of the bean. The concrete container classes should
   * override this method to introduce implementation specific initialization behaviour.
   *
   * @exception   Exception   if loading the bean class failed (ClassNotFoundException)
   *                          or setting up "java:" naming environment failed (DeploymentException)
   */
   public void init()
   throws Exception
   {
      // Acquire classes from CL
      beanClass = classLoader.loadClass(metaData.getEjbClass());

      if (metaData.getHome() != null)
         localHomeInterface = classLoader.loadClass(metaData.getLocalHome());
      if (metaData.getRemote() != null)
         localInterface = classLoader.loadClass(metaData.getLocal());
      
      localContainerInvoker.setContainer( this );
      localContainerInvoker.init();
      application.addLocalHome(this, localContainerInvoker.getEJBLocalHome() );
      // Setup "java:" namespace
      setupEnvironment();
   }

   /**
   * A default implementation of starting the container service (no-op). The concrete
   * container classes should override this method to introduce implementation specific
   * start behaviour.
   *
   * @exception    Exception   an exception that occured during start
   */
   public void start()
   throws Exception
   {
      localContainerInvoker.start();
   }

   /**
   * A default implementation of stopping the container service (no-op). The concrete
   * container classes should override this method to introduce implementation specific
   * stop behaviour.
   */
   public void stop()
   {
      localContainerInvoker.stop();
   }

   /**
   * A default implementation of destroying the container service (no-op). The concrete
   * container classes should override this method to introduce implementation specific
   * destroy behaviour.
   */
   public void destroy()
   {
      localContainerInvoker.destroy();
      application.removeLocalHome( this );
   }

   /**
   *  This method is called by the ContainerInvoker when a method call comes in on the Home object.
   *
   *  The Container forwards this call to the interceptor chain for further processing.
   *
   * @param       mi  the object holding all info about this invocation
   * @return      the result of the home invocation
   * @exception   Exception
   */
   public abstract Object invokeHome(MethodInvocation mi)
   throws Exception;

   /**
   *  This method is called by the ContainerInvoker when a method call comes in on an EJBObject.
   *
   *  The Container forwards this call to the interceptor chain for further processing.
   *
   * @param       id      the id of the object being invoked. May be null if stateless
   * @param       method  the method being invoked
   * @param       args    the parameters
   * @return      the     result of the invocation
   * @exception   Exception
   */
   public abstract Object invoke(MethodInvocation mi)
   throws Exception;

   // Protected -----------------------------------------------------

   abstract Interceptor createContainerInterceptor();
   public abstract void addInterceptor(Interceptor in);

   // Private -------------------------------------------------------

   /*
   * setupEnvironment
   *
   * This method sets up the naming environment of the bean.
   * We create the java: namespace with properties, EJB-References, and
   * DataSource ressources.
   *
   */
   private void setupEnvironment()
   throws DeploymentException
   {
      try
      {
         // Since the BCL is already associated with this thread we can start using the java: namespace directly
         Context ctx = (Context) new InitialContext().lookup("java:comp");
         ctx = ctx.createSubcontext("env");

         // Bind environment properties
         {
            Iterator enum = getBeanMetaData().getEnvironmentEntries();
            while(enum.hasNext())
            {
               EnvEntryMetaData entry = (EnvEntryMetaData)enum.next();
               if (entry.getType().equals("java.lang.Integer"))
               {
                  bind(ctx, entry.getName(), new Integer(entry.getValue()));
               }
               else if (entry.getType().equals("java.lang.Long"))
               {
                  bind(ctx, entry.getName(), new Long(entry.getValue()));
               }
               else if (entry.getType().equals("java.lang.Double"))
               {
                  bind(ctx, entry.getName(), new Double(entry.getValue()));
               }
               else if (entry.getType().equals("java.lang.Float"))
               {
                  bind(ctx, entry.getName(), new Float(entry.getValue()));
               }
               else if (entry.getType().equals("java.lang.Byte"))
               {
                  bind(ctx, entry.getName(), new Byte(entry.getValue()));
               }
               else if (entry.getType().equals("java.lang.Short"))
               {
                  bind(ctx, entry.getName(), new Short(entry.getValue()));
               }
               else if (entry.getType().equals("java.lang.Boolean"))
               {
                  bind(ctx, entry.getName(), new Boolean(entry.getValue()));
               }
               else
               {
                  // Unknown type
                  // Default is string
                  bind(ctx, entry.getName(), entry.getValue());
               }
            }
         }

         // Bind EJB references
         {
            Iterator enum = getBeanMetaData().getEjbReferences();
            while(enum.hasNext())
            {

               EjbRefMetaData ref = (EjbRefMetaData)enum.next();
               Logger.debug("Binding an EJBReference "+ref.getName());

               if (ref.getLink() != null)
               {
                  // Internal link
                  Logger.debug("Binding "+ref.getName()+" to internal JNDI source: "+ref.getLink());
                  if (getApplication().getContainer(ref.getLink()) == null)
                     throw new DeploymentException ("Bean "+ref.getLink()+" not found within this application.");
                  bind(ctx, ref.getName(), new LinkRef(getApplication().getContainer(ref.getLink()).getBeanMetaData().getJndiName()));

                  //                   bind(ctx, ref.getName(), new Reference(ref.getHome(), new StringRefAddr("Container",ref.getLink()), getClass().getName()+".EjbReferenceFactory", null));
                  //                bind(ctx, ref.getName(), new LinkRef(ref.getLink()));
               }
               else
               {
                  // External link
                  if (ref.getJndiName() == null)
                  {
                     throw new DeploymentException("ejb-ref "+ref.getName()+", expected either ejb-link in ejb-jar.xml or jndi-name in jboss.xml");
                  }
                  Logger.debug("Binding "+ref.getName()+" to external JNDI source: "+ref.getJndiName());
                  bind(ctx, ref.getName(), new LinkRef(ref.getJndiName()));
               }
            }
         }

         // Bind resource references
         {
            Iterator enum = getBeanMetaData().getResourceReferences();

            // let's play guess the cast game ;)  New metadata should fix this.
            ApplicationMetaData application = getBeanMetaData().getApplicationMetaData();

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
                        Logger.debug(e);
                     }
                  }

                  // Default failed? Warn user and move on
                  // POTENTIALLY DANGEROUS: should this be a critical error?
                  if (finalName == null)
                  {
                     Logger.warning("No resource manager found for "+ref.getResourceName());
                     continue;
                  }
               }

               if (ref.getType().equals("java.net.URL"))
               {
                  // URL bindings
                  try
                  {
                     Logger.debug("Binding URL "+finalName+ " to JDNI ENC " +ref.getRefName());
                     bind(ctx, ref.getRefName(), new URL(finalName));
                  } catch (MalformedURLException e)
                  {
                     throw new NamingException("Malformed URL:"+e.getMessage());
                  }
               }
               else
               {
                  // Resource Manager bindings
                  Logger.debug("Binding resource manager "+finalName+ " with JDNI ENC " +ref.getRefName());
                  bind(ctx, ref.getRefName(), new LinkRef(finalName));
               }
            }
         }
      } catch (NamingException e)
      {
         Logger.exception(e);
         e.getRootCause().printStackTrace();
         throw new DeploymentException("Could not set up environment", e);
      }
   }



   /**
   *  Bind a value to a name in a JNDI-context, and create any missing subcontexts
   *
   * @param   ctx
   * @param   name
   * @param   val
   * @exception   NamingException
   */
   private void bind(Context ctx, String name, Object val)
   throws NamingException
   {
      // Bind val to name in ctx, and make sure that all intermediate contexts exist
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

