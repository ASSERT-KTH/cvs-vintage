/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
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

import org.jboss.ejb.deployment.jBossEnterpriseBean;
import com.dreambean.ejx.ejb.EnvironmentEntry;
import org.jboss.ejb.deployment.jBossEjbJar;
import org.jboss.ejb.deployment.jBossEjbReference;
import org.jboss.ejb.deployment.jBossResourceReference;
import org.jboss.ejb.deployment.ResourceManagers;
import org.jboss.ejb.deployment.ResourceManager;
import org.jboss.ejb.deployment.JDBCResource;
import org.jboss.ejb.deployment.URLResource;
import org.jboss.logging.Logger;
import org.jboss.metadata.BeanMetaData;

import org.jnp.interfaces.Naming;
import org.jnp.interfaces.java.javaURLContextFactory;
import org.jnp.server.NamingServer;

/**
 *    This is the base class for all EJB-containers in jBoss. A Container
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
 *   @version $Revision: 1.19 $
 */
public abstract class Container
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

    // This is the application that this container is a part of
   protected Application application;

    // This is the classloader of this container. All classes and resources that
    // the bean uses will be loaded from here. By doing this we make the bean re-deployable
   protected ClassLoader classLoader;

    // This is the jBoss-specific metadata. Note that it extends the generic EJB 1.1 class from EJX
   protected jBossEnterpriseBean metaData;

    // This is the Home interface class
   protected Class homeInterface;

   // This is the Remote interface class
   protected Class remoteInterface;

   // This is the EnterpriseBean class
   protected Class beanClass;

   // This is the TransactionManager
   protected TransactionManager tm;

   // This is the new MetaData construct
   protected BeanMetaData newMetaData;

   // Public --------------------------------------------------------

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
    public void setMetaData(jBossEnterpriseBean metaData)
    {
        this.metaData = metaData;
    }

    /**
     * Returns the metadata of this container.
     *
     * @return metaData;
     */
    public jBossEnterpriseBean getMetaData()
    {
        return metaData;
    }

    
    // the following two methods use the new metadata structures from
    // package org.jboss.metadata
    public void setBeanMetaData(BeanMetaData metaData) {
        newMetaData = metaData;
    }
    public BeanMetaData getBeanMetaData() {
        return newMetaData;
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
   }

   /**
    * A default implementation of stopping the container service (no-op). The concrete
    * container classes should override this method to introduce implementation specific
    * stop behaviour.
    */
   public void stop()
   {
   }

   /**
    * A default implementation of destroying the container service (no-op). The concrete
    * container classes should override this method to introduce implementation specific
    * destroy behaviour.
    */
   public void destroy()
   {
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

   // Private -------------------------------------------------------

   /*
   * setupEnvironment
   *
   * This method sets up the naming environment of the bean.
   * it sets the root it creates for the naming in the "BeanClassLoader"
   * that loader shares the root for all instances of the bean and
   * is part of the "static" metaData of the bean.
   * We create the java: namespace with properties, EJB-References, and
   * DataSource ressources.
   *
   */
   private void setupEnvironment()
      throws DeploymentException
   {
        try
        {
            // Create a new java: namespace root
          NamingServer root = new NamingServer();

            // Associate this root with the classloader of the bean
          ((BeanClassLoader)getClassLoader()).setJNDIRoot(root);

          // Since the BCL is already associated with this thread we can start using the java: namespace directly
          Context ctx = (Context) new InitialContext().lookup("java:/");
          ctx.createSubcontext("comp");
          ctx = ctx.createSubcontext("comp/env");

          // Bind environment properties
          {
             Iterator enum = getMetaData().getEnvironmentEntries();
             while(enum.hasNext())
             {
                EnvironmentEntry entry = (EnvironmentEntry)enum.next();
                if (entry.getType().equals("java.lang.Integer"))
                {
                   bind(ctx, entry.getName(), new Integer(entry.getValue()));
                } else if (entry.getType().equals("java.lang.Long"))
                {
                   bind(ctx, entry.getName(), new Long(entry.getValue()));
                } else if (entry.getType().equals("java.lang.Double"))
                {
                   bind(ctx, entry.getName(), new Double(entry.getValue()));
                } else if (entry.getType().equals("java.lang.Float"))
                {
                   bind(ctx, entry.getName(), new Float(entry.getValue()));
                } else if (entry.getType().equals("java.lang.Byte"))
                {
                   bind(ctx, entry.getName(), new Byte(entry.getValue()));
                } else if (entry.getType().equals("java.lang.Short"))
                {
                   bind(ctx, entry.getName(), new Short(entry.getValue()));
                } else if (entry.getType().equals("java.lang.Boolean"))
                {
                   bind(ctx, entry.getName(), new Boolean(entry.getValue()));
                } else
                {
                   // Unknown type
                   // Default is string
                   bind(ctx, entry.getName(), entry.getValue());
                }
             }
          }

          // Bind EJB references
          {
             Iterator enum = getMetaData().getEjbReferences();
             while(enum.hasNext())
             {

                jBossEjbReference ref = (jBossEjbReference)enum.next();
                System.out.println("Binding an EJBReference "+ref);

                Name n = ctx.getNameParser("").parse(ref.getLink());

                if (!ref.getJndiName().equals(""))
                {
                   // External link
                   Logger.debug("Binding "+ref.getName()+" to external JNDI source: "+ref.getJndiName());
                   bind(ctx, ref.getName(), new LinkRef(ref.getJndiName()));
                }
                else
                {
                   // Internal link
                   Logger.debug("Bind "+ref.getName() +" to "+ref.getLink());

                        final Container con = getApplication().getContainer(ref.getLink());

                        // Use Reference to link to ensure lazyloading.
                        // Otherwise we might try to get EJBHome from not yet initialized container
                        // will would result in nullpointer exception
                        RefAddr refAddr = new RefAddr("EJB")
                        {
                            public Object getContent()
                            {
                                 return con;
                            }
                        };
                        Reference reference = new Reference("javax.ejb.EJBObject",refAddr, new EjbReferenceFactory().getClass().getName(), null);

                        bind(ctx, ref.getName(), reference);
                }
             }
          }

          // Bind resource references
          {
             Iterator enum = getMetaData().getResourceReferences();
             
             // let's play guess the cast game ;)  New metadata should fix this.
             ResourceManagers rms = ((jBossEjbJar)getMetaData().getBeanContext().getBeanContext()).getResourceManagers();
             while(enum.hasNext())
             {
                jBossResourceReference ref = (jBossResourceReference)enum.next();

                ResourceManager rm = rms.getResourceManager(ref.getResourceName());

                    if (rm == null)
                    {
                        // Try to locate defaults
                        if (ref.getType().equals("javax.sql.DataSource"))
                        {
                            // Go through JNDI and look for DataSource - use the first one
                            Context dsCtx = new InitialContext();
                            NamingEnumeration list = dsCtx.list("");
                            while (list.hasMore())
                            {
                                NameClassPair pair = (NameClassPair)list.next();
                                try
                                {
                                    Class cl = getClass().getClassLoader().loadClass(pair.getClassName());
                                    if (DataSource.class.isAssignableFrom(cl))
                                    {
                                        // Found it!!
                                        Logger.log("Using default DataSource:"+pair.getName());
                                        rm = new JDBCResource();
                                        ((JDBCResource)rm).setJndiName(pair.getName());
                                        list.close();
                                        break;
                                    }
                                } catch (Exception e)
                                {
                                    Logger.debug(e);
                                }
                            }

                        }

                        // Default failed? Warn user and move on
                        // POTENTIALLY DANGEROUS: should this be a critical error?
                        if (rm == null)
                        {
                            Logger.warning("No resource manager found for "+ref.getResourceName());
                            continue;
                        }
                    }

                if (rm.getType().equals("javax.sql.DataSource"))
                {
                   // Datasource bindings
                   JDBCResource res = (JDBCResource)rm;
                   bind(ctx, ref.getName(), new LinkRef(res.getJndiName()));
                } else if (rm.getType().equals("java.net.URL"))
                {
                   // URL bindings
                   try
                   {
                      URLResource res = (URLResource)rm;
                      bind(ctx, ref.getName(), new URL(res.getUrl()));
                   } catch (MalformedURLException e)
                   {
                      throw new NamingException("Malformed URL:"+e.getMessage());
                   }
                }
             }
          }
        } catch (NamingException e)
        {
            e.printStackTrace();
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

    public static class EjbReferenceFactory
    implements ObjectFactory
    {
        public Object getObjectInstance(Object ref,
                                        Name name,
                                        Context nameCtx,
                                        Hashtable environment)
                                        throws Exception
        {
            Object con = ((Reference)ref).get(0).getContent();
            if (con instanceof EntityContainer)
            {
                return ((EntityContainer)con).getContainerInvoker().getEJBHome();
            } if (con instanceof StatelessSessionContainer)
            {
                return ((StatelessSessionContainer)con).getContainerInvoker().getEJBHome();
            } else
            {
                return null;
            }
        }
    }
}
