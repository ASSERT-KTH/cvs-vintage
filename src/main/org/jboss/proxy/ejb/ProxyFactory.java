/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.proxy.ejb;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.rmi.ServerException;
import java.io.IOException;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.EJBMetaData;

import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;
import javax.management.ObjectName;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.Container;
import org.jboss.ejb.ContainerInvoker;
import org.jboss.ejb.ContainerInvokerContainer;
import org.jboss.ejb.FinderResults;
import org.jboss.ejb.ListCacheKey;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.InvocationContext;
import org.jboss.logging.Logger;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.ConfigurationMetaData;
import org.jboss.metadata.EntityMetaData;
import org.jboss.metadata.SessionMetaData;
import org.jboss.naming.Util;
import org.jboss.proxy.Interceptor;
import org.jboss.proxy.ClientContainer;
import org.jboss.proxy.ejb.handle.HomeHandleImpl;
import org.jboss.system.Registry;
import org.jboss.util.NestedRuntimeException;
import org.w3c.dom.Element;


/**
 * As we remove the one one association between container STACK and invoker we keep this around
 * IN the future the creation of proxies is a task done on a container basis but the container
 * as a logical representation, in other words, the container "Entity with RMI/IIOP" is not a 
 * container stack but an association at the invocation level that points to all metadata for 
 * a given container. 
 *
 * In other words this is here for legacy reason and to not disrupt the container at once
 * In particular we declare that we "implement" the container invoker interface when we are
 * just implementing the Proxy generation calls. Separation of concern. 
 *
 * @todo eliminate this class, at least in its present form.
 *
 *  @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 *  @author <a href="mailto:scott.stark@jboss.org">Scott Stark/a>
 *  @version $Revision: 1.14 $
 *
 *  <p><b>Revisions:</b><br>
 *  <p><b>2001/12/30: billb</b>
 *  <ol>
 *   <li>made home and bean invokers pluggable
 *  </ol>
 *  <p><b>2002/03/08: billb</b>
 *  <ol>
 *   <li>client interceptors from config.
 *  </ol>
 */
public class ProxyFactory
   implements ContainerInvoker
{
   protected static final String HOME_INTERCEPTOR = "home";
   protected static final String BEAN_INTERCEPTOR = "bean";
   protected static final String LIST_ENTITY_INTERCEPTOR = "list-entity";

   // Metadata for the proxies
   public EJBMetaData ejbMetaData;
   
   protected static Logger log = Logger.getLogger(ProxyFactory.class);
   protected EJBHome home;
   protected EJBObject statelessObject;
   
   // The name of the bean being deployed
   protected String jndiName;
   
   // The objectName hash for the container
   protected int objectName;
   protected String jmxName;
   
   // The name of the delegate invoker
   protected Invoker homeInvoker;
   protected Invoker beanInvoker;

   protected ArrayList homeInterceptorClasses = new ArrayList();
   protected ArrayList beanInterceptorClasses = new ArrayList();
   protected ArrayList listEntityInterceptorClasses = new ArrayList();
   
   // A pointer to the container this proxy factory is dedicated to
   protected org.jboss.ejb.Container container;
   
   // Container plugin implementation -----------------------------------------
   
   public void setContainer(Container con)
   {
      this.container = con;
   }
   
   public void create() throws Exception
   {
      Context ctx = new InitialContext();
      
      jndiName = container.getBeanMetaData().getJndiName();
      jmxName = "jboss.j2ee:service=EJB,jndiName="+jndiName;
      // The objectName hashCode
      ObjectName jmx = new ObjectName(jmxName);
       
      // We keep the hashCode around for fast creation of proxies
      objectName = jmx.hashCode();
      Registry.bind(new Integer(objectName), jmx);
      log.debug("Bound jmxName="+jmx+", hash="+objectName+"into Registry");

      // Create metadata
      
      /**
         Constructor signature is
      
         public EJBMetaDataImpl(Class remote,
         Class home,
         Class pkClass,
         boolean session,
         boolean statelessSession,
         HomeHandle homeHandle)
      */
      boolean isSession = !(container.getBeanMetaData() instanceof EntityMetaData);
      Class pkClass = null;
      if (!isSession)
      {
         EntityMetaData metaData = (EntityMetaData)container.getBeanMetaData();
         String pkClassName = metaData.getPrimaryKeyClass();
         try
         {
            if (pkClassName != null)
               pkClass = container.getClassLoader().loadClass(pkClassName);
            else
               pkClass = container.getClassLoader().loadClass(metaData.getEjbClass()).getField(metaData.getPrimKeyField()).getClass();
         } catch (NoSuchFieldException e)
         {
            log.error("Unable to identify Bean's Primary Key class!  Did you specify a primary key class and/or field?  Does that field exist?");
            throw new RuntimeException("Primary Key Problem");
         } catch (NullPointerException e)
         {
            log.error("Unable to identify Bean's Primary Key class!  Did you specify a primary key class and/or field?  Does that field exist?");
            throw new RuntimeException("Primary Key Problem");
         }
      }
      ejbMetaData = new EJBMetaDataImpl(
         ((ContainerInvokerContainer)container).getRemoteClass(),
         ((ContainerInvokerContainer)container).getHomeClass(),
         pkClass, //null if not entity
         isSession, //Session
         isSession && ((SessionMetaData)container.getBeanMetaData()).isStateless(),//Stateless
         new HomeHandleImpl(jndiName));
      
      if (log.isDebugEnabled())
         log.debug("Proxy Factory for "+jndiName+" initialized");

      initInterceptorClasses();
   }

   /** Become fully available. At this point our invokers should be started
    and we can bind the homes into JNDI.
    */
   public void start() throws Exception
   {
      setupInvokers();
      bindProxy();
   }

   /** Lookup the invokers in the object registry. This typically cannot
    be done until our start method as the invokers may need to be started
    themselves.
    */
   protected void setupInvokers() throws Exception
   {
      ObjectName oname;
      
      // Get the local invoker
      oname = new ObjectName(container.getBeanMetaData().getHomeInvoker());
      homeInvoker = (Invoker)Registry.lookup(oname);
      if (homeInvoker == null)
         throw new RuntimeException("homeInvoker is null: " + oname);
      oname = new ObjectName(container.getBeanMetaData().getBeanInvoker());
      beanInvoker = (Invoker)Registry.lookup(oname);
      if (beanInvoker == null)
         throw new RuntimeException("beanInvoker is null: " + oname);
   }

   /** Load the client interceptor classes
    */
   protected void initInterceptorClasses() throws Exception
   {
      ConfigurationMetaData configMetaData = container.getBeanMetaData().getContainerConfiguration();

      Element homeInterceptorConf = configMetaData.getClientInterceptorConf(HOME_INTERCEPTOR);
      loadInterceptorClasses(homeInterceptorClasses, homeInterceptorConf);
      if( homeInterceptorClasses.size() == 0 )
         throw new DeploymentException("There are no home interface interceptors configured");

      Element beanInterceptorConf = configMetaData.getClientInterceptorConf(BEAN_INTERCEPTOR);
      loadInterceptorClasses(beanInterceptorClasses, beanInterceptorConf);
      if( beanInterceptorClasses.size() == 0 )
         throw new DeploymentException("There are no bean interface interceptors configured");

      Element listEntityInterceptorConf = configMetaData.getClientInterceptorConf(LIST_ENTITY_INTERCEPTOR);
      loadInterceptorClasses(listEntityInterceptorClasses, listEntityInterceptorConf);
   }

   /**
    * The <code>loadInterceptorClasses</code> load an interceptor classes from
    * configuration
    * @exception Exception if an error occurs
    */
   protected void loadInterceptorClasses(ArrayList classes, Element interceptors)
      throws Exception
   {
      Iterator interceptorElements = MetaData.getChildrenByTagName(interceptors, "interceptor");
      ClassLoader loader = container.getClassLoader();
      Interceptor last = null;
      while( interceptorElements != null && interceptorElements.hasNext() )
      {
         Element ielement = (Element) interceptorElements.next();
         String className = null;
         className = MetaData.getElementContent(ielement);
         Class clazz = loader.loadClass(className);
         classes.add(clazz);
      }
   }

   /** The <code>loadInterceptorChain</code> create instances of interceptor
    * classes previously loaded in loadInterceptorClasses
    *
    * @exception Exception if an error occurs
    */
   protected void loadInterceptorChain(ArrayList chain, ClientContainer client)
      throws Exception
   {
      Interceptor last = null;
      for (int i = 0; i < chain.size(); i++)
      {
         Class clazz = (Class)chain.get(i);
         Interceptor interceptor = (Interceptor) clazz.newInstance(); 
         if (last == null)
         {
            last = interceptor;
            client.setNext(interceptor);
         }
         else
         {
            last.setNext(interceptor);
            last = interceptor;
         }
      }
   }

   /** The <code>bindProxy</code> method creates the home proxy and binds
    * the home into jndi. It also creates the InvocationContext and client
    * container and interceptor chain.
    *
    * @exception Exception if an error occurs
    */
   protected void bindProxy() throws Exception
   {
      try
      {   
         // Create a stack from the description (in the future) for now we hardcode it
         InvocationContext context = new InvocationContext();

         context.setObjectName(new Integer(objectName));
         context.setValue(org.jboss.proxy.ejb.GenericEJBInterceptor.JNDI_NAME, jndiName);
         // The behavior for home proxying should be isolated in an interceptor FIXME
         context.setInvoker(homeInvoker);
         context.setValue(org.jboss.proxy.ejb.HomeInterceptor.EJB_METADATA, ejbMetaData);
         
         ClientContainer client = new ClientContainer(context);
         loadInterceptorChain(homeInterceptorClasses, client);
         
         ContainerInvokerContainer invoker = (ContainerInvokerContainer) container;
         // Create the EJBHome
         this.home = (EJBHome) Proxy.newProxyInstance(
               // Class loader pointing to the right classes from deployment
               invoker.getHomeClass().getClassLoader(),
               // The classes we want to implement home and handle
               new Class[] { invoker.getHomeClass(), Class.forName("javax.ejb.Handle")},
               // The home proxy as invocation handler
               client);

         // Create stateless session object
         // Same instance is used for all objects
         if (!(container.getBeanMetaData() instanceof EntityMetaData) &&
             ((SessionMetaData)container.getBeanMetaData()).isStateless())
         {
            // Create a stack from the description (in the future) for now we hardcode it
            context = new InvocationContext();
            
            context.setObjectName(new Integer(objectName));
            context.setValue(org.jboss.proxy.ejb.GenericEJBInterceptor.JNDI_NAME, jndiName);
            // The behavior for home proxying should be isolated in an interceptor FIXME
            context.setInvoker(beanInvoker);
            
            client = new ClientContainer(context);
            
            loadInterceptorChain(beanInterceptorClasses, client);
    
            this.statelessObject = 
               (EJBObject)Proxy.newProxyInstance(
                  // Correct CL         
                  invoker.getRemoteClass().getClassLoader(),
                  // Interfaces    
                  new Class[] { invoker.getRemoteClass() } ,
                  // SLSB proxy as invocation handler
                  client
                  );
         }

         // Bind the home in the JNDI naming space
         Util.rebind(
            // The context
            new InitialContext(),
            // Jndi name
            container.getBeanMetaData().getJndiName(),
            // The Home
            getEJBHome());
  
        log.debug("Bound "+container.getBeanMetaData().getEjbName() + " to " + container.getBeanMetaData().getJndiName());      
      }
      catch (Exception e)
      {
         throw new ServerException("Could not bind home", e);
      }
   }

   public void stop()
   {
   }

   public void destroy()
   {
      try
      {
         InitialContext ctx = new InitialContext();
         ctx.unbind(container.getBeanMetaData().getJndiName());
      } 
      catch (Exception e)
      {
         // ignore.
      }
   }

   // Container invoker implementation -------------------------------------
   
   public EJBMetaData getEJBMetaData()
   {
      return ejbMetaData;
   }
   
   public Object getEJBHome()
   {
      return home;
   }
   
   /** Return the EJBObject proxy for stateless sessions.
    */
   public Object getStatelessSessionEJBObject()
   {
      
      return statelessObject;
   }
   
   /** Create an EJBObject proxy for a stateful session given its session id.
    */
   public Object getStatefulSessionEJBObject(Object id)
   {
      // Create a stack from the description (in the future) for now we hardcode it
      InvocationContext context = new InvocationContext();
      
      context.setObjectName(new Integer(objectName));
      context.setCacheId(id);
      context.setValue(org.jboss.proxy.ejb.GenericEJBInterceptor.JNDI_NAME, jndiName);
      context.setInvoker(beanInvoker);
      
      ClientContainer client = new ClientContainer(context);
      try
      {
         loadInterceptorChain(beanInterceptorClasses, client);
      }
      catch(Exception e)
      {
         throw new NestedRuntimeException("Failed to load interceptor chain", e);
      }

      ContainerInvokerContainer invoker = (ContainerInvokerContainer) container;
      return (EJBObject)Proxy.newProxyInstance(
         // Classloaders
         invoker.getRemoteClass().getClassLoader(),
         // Interfaces
         new Class[] { invoker.getRemoteClass() },
         // Proxy as invocation handler
         client);
   }

   /** Create an EJBObject proxy for an entity given its primary key.
    */
   public Object getEntityEJBObject(Object id)
   {
      // Create a stack from the description (in the future) for now we hardcode it
      InvocationContext context = new InvocationContext();
      
      context.setObjectName(new Integer(objectName));
      context.setCacheId(id);
      context.setValue(org.jboss.proxy.ejb.GenericEJBInterceptor.JNDI_NAME, jndiName);
      context.setInvoker(beanInvoker);
      
      ClientContainer client = new ClientContainer(context);
      
      try
      {
         loadInterceptorChain(beanInterceptorClasses, client);
      }
      catch(Exception e)
      {
         throw new NestedRuntimeException("Failed to load interceptor chain", e);
      }

      ContainerInvokerContainer invoker = (ContainerInvokerContainer) container;
      return (EJBObject)Proxy.newProxyInstance(
         // Classloaders
         invoker.getRemoteClass().getClassLoader(),
         // Interfaces
         new Class[] { invoker.getRemoteClass() },
         // Proxy as invocation handler
         client);
   }

   /** Create a Collection EJBObject proxies for an entity given its primary keys.
    */
   public Collection getEntityCollection(Collection ids)
   {
      ArrayList list = new ArrayList(ids.size());
      Iterator idEnum = ids.iterator();
      ContainerInvokerContainer invoker = (ContainerInvokerContainer) container;
      
      if ((ids instanceof FinderResults) && ((FinderResults) ids).isReadAheadOnLoadUsed())
      {
         long listId = ((FinderResults) ids).getListId();
         
         for (int i = 0; idEnum.hasNext(); i++)
         {      
            // Create a stack from the description (in the future) for now we hardcode it
            InvocationContext context = new InvocationContext();
      
            Object id = idEnum.next();

            context.setObjectName(new Integer(objectName));
            context.setCacheId(new ListCacheKey(id, listId, i));
            context.setValue(org.jboss.proxy.ejb.GenericEJBInterceptor.JNDI_NAME, jndiName);
            context.setInvoker(beanInvoker);
      
            ClientContainer client = new ClientContainer(context);
      
            try
            {
               loadInterceptorChain(listEntityInterceptorClasses, client);
            }
            catch(Exception e)
            {
               throw new NestedRuntimeException("Failed to load interceptor chain", e);
            }
            
            list.add(Proxy.newProxyInstance(invoker.getRemoteClass().getClassLoader(),
                                            new Class[] { invoker.getRemoteClass(), ReadAheadBuffer.class },
                                            client));
         }
      }
      else
      {
         while(idEnum.hasNext())
         {
            // Create a stack from the description (in the future) for now we hardcode it
            InvocationContext context = new InvocationContext();
      
            context.setObjectName(new Integer(objectName));
            context.setCacheId(idEnum.next());
            context.setValue(org.jboss.proxy.ejb.GenericEJBInterceptor.JNDI_NAME, jndiName);
            context.setInvoker(beanInvoker);
      
            ClientContainer client = new ClientContainer(context);
      
            try
            {
               loadInterceptorChain(beanInterceptorClasses, client);
            }
            catch(Exception e)
            {
               throw new NestedRuntimeException("Failed to load interceptor chain", e);
            }
            
            list.add(Proxy.newProxyInstance(invoker.getRemoteClass().getClassLoader(),
                                            new Class[] { invoker.getRemoteClass() },
                                            client));
         }
      }
      return list;
   }

}
