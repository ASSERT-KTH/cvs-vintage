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
import java.util.HashMap;
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
import org.jboss.ejb.EJBProxyFactory;
import org.jboss.ejb.EJBProxyFactoryContainer;
import org.jboss.ejb.ListCacheKey;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.jrmp.server.JRMPInvoker;
import org.jboss.invocation.InvocationContext;
import org.jboss.invocation.InvocationKey;
import org.jboss.logging.Logger;
import org.jboss.metadata.InvokerProxyBindingMetaData;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.ConfigurationMetaData;
import org.jboss.metadata.EntityMetaData;
import org.jboss.metadata.SessionMetaData;
import org.jboss.util.naming.Util;
import org.jboss.proxy.Interceptor;
import org.jboss.proxy.ClientContainer;
import org.jboss.proxy.ejb.handle.HomeHandleImpl;
import org.jboss.system.Registry;
import org.jboss.util.NestedRuntimeException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * As we remove the one one association between container STACK and invoker we
 * keep this around. IN the future the creation of proxies is a task done on a
 * container basis but the container as a logical representation. In other 
 * words, the container "Entity with RMI/IIOP" is not a container stack but 
 * an association at the invocation level that points to all metadata for 
 * a given container. 
 *
 * In other words this is here for legacy reason and to not disrupt the 
 * container at once. 
 * In particular we declare that we "implement" the container invoker 
 * interface when we are just implementing the Proxy generation calls.
 * Separation of concern. 
 *
 * @todo eliminate this class, at least in its present form.
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark/a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version $Revision: 1.20 $
 */
public class ProxyFactory
   implements EJBProxyFactory
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
   protected String jndiBinding;
   protected int objectName;
   
   // The name of the delegate invoker
   // We have a beanInvoker and homeInvoker
   // because clustering has a different invoker for each
   // and we want to reuse code here.
   protected Invoker beanInvoker;
   protected Invoker homeInvoker;
   protected InvokerProxyBindingMetaData invokerMetaData;

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
   
   public void setInvokerMetaData(InvokerProxyBindingMetaData metadata)
   {
      this.invokerMetaData = metadata;
   }
   
   public void setInvokerBinding(String binding)
   {
      this.jndiBinding = binding;
   }
   
   public void create() throws Exception
   {
      Context ctx = new InitialContext();
      
      objectName = container.getJmxName().hashCode();
      // Create metadata

      boolean isSession = 
            !(container.getBeanMetaData() instanceof EntityMetaData);
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
         ((EJBProxyFactoryContainer)container).getRemoteClass(),
         ((EJBProxyFactoryContainer)container).getHomeClass(),
         pkClass, //null if not entity
         isSession, //Session
         isSession && ((SessionMetaData)container.getBeanMetaData()).isStateless(),//Stateless
         new HomeHandleImpl(jndiBinding));
      
      if (log.isDebugEnabled())
         log.debug("Proxy Factory for "+jndiBinding+" initialized");

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
      ObjectName oname = new ObjectName(invokerMetaData.getInvokerMBean());
      Invoker invoker = (Invoker)Registry.lookup(oname);
      if (invoker == null)
         throw new RuntimeException("invoker is null: " + oname);
      
      homeInvoker = beanInvoker = invoker;
   }


   /** Load the client interceptor classes
    */
   protected void initInterceptorClasses() throws Exception
   {
      HashMap interceptors = new HashMap();
      Element clientInterceptors = MetaData.getOptionalChild(invokerMetaData.getProxyFactoryConfig(), "client-interceptors", null);
      if (clientInterceptors != null)
      {
         NodeList children = clientInterceptors.getChildNodes();
         for (int i = 0; i < children.getLength(); i++)
         {
            Node currentChild = children.item(i);
            if (currentChild.getNodeType() == Node.ELEMENT_NODE)
            {
               Element interceptor = (Element)children.item(i);
               interceptors.put(interceptor.getTagName(), interceptor);
            }
         }
      }
      else
      {
         log.debug("client interceptors element is null");
      }
      Element homeInterceptorConf = (Element)interceptors.get(HOME_INTERCEPTOR);
      loadInterceptorClasses(homeInterceptorClasses, homeInterceptorConf);
      if( homeInterceptorClasses.size() == 0 )
         throw new DeploymentException("There are no home interface interceptors configured");

      Element beanInterceptorConf = (Element)interceptors.get(BEAN_INTERCEPTOR);
      loadInterceptorClasses(beanInterceptorClasses, beanInterceptorConf);
      if( beanInterceptorClasses.size() == 0 )
         throw new DeploymentException("There are no bean interface interceptors configured");

      Element listEntityInterceptorConf = (Element)interceptors.get(LIST_ENTITY_INTERCEPTOR);
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
	 InvocationContext context = setupInvocationContext(homeInvoker);

	 context.setValue(InvocationKey.EJB_METADATA, ejbMetaData);
         
         ClientContainer client = new ClientContainer(context);
         loadInterceptorChain(homeInterceptorClasses, client);
         
         EJBProxyFactoryContainer pfc = (EJBProxyFactoryContainer) container;
         // Create the EJBHome
         this.home = (EJBHome) Proxy.newProxyInstance(
               // Class loader pointing to the right classes from deployment
               pfc.getHomeClass().getClassLoader(),
               // The classes we want to implement home and handle
               new Class[] { pfc.getHomeClass(), Class.forName("javax.ejb.Handle")},
               // The home proxy as invocation handler
               client);

         // Create stateless session object
         // Same instance is used for all objects
         if (!(container.getBeanMetaData() instanceof EntityMetaData) &&
             ((SessionMetaData)container.getBeanMetaData()).isStateless())
         {
            // Create a stack from the description (in the future) for now we hardcode it
            context = setupInvocationContext(beanInvoker);
            
            client = new ClientContainer(context);
            
            loadInterceptorChain(beanInterceptorClasses, client);
    
            this.statelessObject = 
               (EJBObject)Proxy.newProxyInstance(
                  // Correct CL         
                  pfc.getRemoteClass().getClassLoader(),
                  // Interfaces    
                  new Class[] { pfc.getRemoteClass() } ,
                  // SLSB proxy as invocation handler
                  client
                  );
         }

         // Bind the home in the JNDI naming space
         log.debug("Binding Home " + jndiBinding);
         Util.rebind(
            // The context
            new InitialContext(),
            // Jndi name
            jndiBinding,
            // The Home
            getEJBHome());
  
        log.debug("Bound "+container.getBeanMetaData().getEjbName() + " to " + jndiBinding);      
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
         ctx.unbind(jndiBinding);
      } 
      catch (Exception e)
      {
         // ignore.
      }
   }

   // EJBProxyFactory implementation -------------------------------------
   
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
      InvocationContext context = setupInvocationContext(beanInvoker);
      
      context.setCacheId(id);
      
      ClientContainer client = new ClientContainer(context);
      try
      {
         loadInterceptorChain(beanInterceptorClasses, client);
      }
      catch(Exception e)
      {
         throw new NestedRuntimeException("Failed to load interceptor chain", e);
      }

      EJBProxyFactoryContainer pfc = (EJBProxyFactoryContainer) container;
      return (EJBObject)Proxy.newProxyInstance(
         // Classloaders
         pfc.getRemoteClass().getClassLoader(),
         // Interfaces
         new Class[] { pfc.getRemoteClass() },
         // Proxy as invocation handler
         client);
   }

   /** Create an EJBObject proxy for an entity given its primary key.
    */
   public Object getEntityEJBObject(Object id)
   {
      InvocationContext context =  setupInvocationContext(beanInvoker);
      
      context.setCacheId(id);
      
      ClientContainer client = new ClientContainer(context);
      
      try
      {
         loadInterceptorChain(beanInterceptorClasses, client);
      }
      catch(Exception e)
      {
         throw new NestedRuntimeException("Failed to load interceptor chain", e);
      }

      EJBProxyFactoryContainer pfc = (EJBProxyFactoryContainer) container;
      return (EJBObject)Proxy.newProxyInstance(
         // Classloaders
         pfc.getRemoteClass().getClassLoader(),
         // Interfaces
         new Class[] { pfc.getRemoteClass() },
         // Proxy as invocation handler
         client);
   }

   /** Create a Collection EJBObject proxies for an entity given its primary keys.
    */
   public Collection getEntityCollection(Collection ids)
   {
      ArrayList list = new ArrayList(ids.size());
      Iterator idEnum = ids.iterator();
      EJBProxyFactoryContainer pfc = (EJBProxyFactoryContainer) container;

      while(idEnum.hasNext())
      {
         InvocationContext context = setupInvocationContext(beanInvoker);
      
         context.setCacheId(idEnum.next());
      
         ClientContainer client = new ClientContainer(context);
      
         try
         {
            loadInterceptorChain(beanInterceptorClasses, client);
         }
         catch(Exception e)
         {
            throw new NestedRuntimeException(
                  "Failed to load interceptor chain", e);
         }
         
         list.add(Proxy.newProxyInstance(
                  pfc.getRemoteClass().getClassLoader(),
                  new Class[] { pfc.getRemoteClass() },
                  client));
      }
      return list;
   }

   /**
    * The <code>setupInvocationContext</code> method puts standard
    * data in the InvocationContext.  Some of this, in particular the
    * methodHashToTxSupportMap, should be inserted by the client side
    * interceptor in a "start" lifecycle method.  Since this does not
    * yet exist, it is hardcoded here.
    *
    * @param invoker an <code>Invoker</code> value
    * @return an <code>InvocationContext</code> value
    */
   private InvocationContext setupInvocationContext(Invoker invoker)
   {
      InvocationContext context = new InvocationContext();

      context.setObjectName(new Integer(objectName));
      context.setValue(InvocationKey.JNDI_NAME, jndiBinding);
      // The behavior for home proxying should be isolated in an interceptor FIXME
      context.setInvoker(invoker);
      context.setInvokerProxyBinding(invokerMetaData.getName());

      context.setMethodHashToTxSupportMap(container.getMethodHashToTxSupportMap());
      return context;
   }
}
