/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins.jrmp.server;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.rmi.ServerException;
import java.rmi.RemoteException;
import java.rmi.MarshalledObject;
import java.rmi.server.RemoteServer;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.security.Principal;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

import javax.ejb.EJBMetaData;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.naming.Name;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.management.ObjectName;
import javax.management.MBeanException;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.Container;
import org.jboss.ejb.ContainerInvokerContainer;
import org.jboss.ejb.ContainerInvoker;
import org.jboss.ejb.MethodInvocation;
import org.jboss.ejb.plugins.jrmp.interfaces.RemoteMethodInvocation;
import org.jboss.ejb.plugins.jrmp.interfaces.HomeHandleImpl;
import org.jboss.ejb.plugins.jrmp.interfaces.GenericProxy;
import org.jboss.ejb.plugins.jrmp.interfaces.ContainerRemote;
import org.jboss.ejb.plugins.jrmp.interfaces.EJBMetaDataImpl;
import org.jboss.logging.Logger;
import org.jboss.metadata.XmlLoadable;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.EntityMetaData;
import org.jboss.metadata.SessionMetaData;
import org.jboss.security.SecurityAssociation;
import org.jboss.tm.TransactionPropagationContextFactory;
import org.jboss.tm.TransactionPropagationContextImporter;

import org.w3c.dom.Element;

/**
*  The <code>ContainerInvoker</code> for invoking enterprise beans
*  over the JRMP invocation transport.
*
*  @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
*  @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
*  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*  @author <a href="mailto:jplindfo@cc.helsinki.fi">Juha Lindfors</a>
*  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
*  @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>
*  @version $Revision: 1.46 $
*/
public class JRMPContainerInvoker
   extends RemoteServer
   implements ContainerRemote, ContainerInvoker, XmlLoadable
{
   // Constants -----------------------------------------------------
   protected final static int ANONYMOUS_PORT = 0;
   protected static Logger log = Logger.getLogger(JRMPContainerInvoker.class);
   
   // Attributes ----------------------------------------------------
   protected boolean optimize = false;
   /** The port the container will be exported on */
   protected int rmiPort = ANONYMOUS_PORT;
   /** An optional custom client socket factory */
   protected RMIClientSocketFactory clientSocketFactory;
   /** An optional custom server socket factory */
   protected RMIServerSocketFactory serverSocketFactory;
   /** The class name of the optional custom client socket factory */
   protected String clientSocketFactoryName;
   /** The class name of the optional custom server socket factory */
   protected String serverSocketFactoryName;
   /** The address to bind the rmi port on */
   protected String serverAddress;
   protected boolean jdk122 = false;
   protected Container container;
   protected String jndiName;
   protected EJBMetaDataImpl ejbMetaData;
   // The home can be one.
   protected EJBHome home;
   // The Stateless Object can be one.
   protected EJBObject statelessObject;
   
   protected Map beanMethodInvokerMap;
   protected Map homeMethodInvokerMap;
   
   protected ContainerInvoker ciDelegate; // Delegate depending on JDK version
   
   // Static --------------------------------------------------------
   
   private static TransactionPropagationContextFactory tpcFactory;
   private static TransactionPropagationContextImporter tpcImporter;
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void setOptimized(boolean optimize)
   {
      this.optimize = optimize;
      //DEBUG       log.debug("Container Invoker optimize set to '"+optimize+"'");
   }
   
   public boolean isOptimized()
   {
      //DEBUG  log.debug("Optimize in action: '"+optimize+"'");
      return optimize;
   }
   
   public String getJndiName()
   {
      return jndiName;
   }
   
   // ContainerService implementation -------------------------------
   public void setContainer(Container con)
   {
      this.container = con;
      ciDelegate.setContainer(con);
   }
   
   public void init()
   throws Exception
   {
      Context ctx = new InitialContext();
      
      jndiName = container.getBeanMetaData().getJndiName();
      
      // Get the transaction propagation context factory
      // and the transaction propagation context importer
      tpcFactory = (TransactionPropagationContextFactory)ctx.lookup("java:/TransactionPropagationContextExporter");
      tpcImporter = (TransactionPropagationContextImporter)ctx.lookup("java:/TransactionPropagationContextImporter");
      
      // Set the transaction manager and transaction propagation
      // context factory of the GenericProxy class
      GenericProxy.setTransactionManager((TransactionManager)ctx.lookup("java:/TransactionManager"));
      GenericProxy.setTPCFactory(tpcFactory);
      
      // Create method mappings for container invoker
      Method[] methods = ((ContainerInvokerContainer)container).getRemoteClass().getMethods();
      beanMethodInvokerMap = new HashMap();
      for (int i = 0; i < methods.length; i++)
         beanMethodInvokerMap.put(new Long(RemoteMethodInvocation.calculateHash(methods[i])), methods[i]);
      
      methods = ((ContainerInvokerContainer)container).getHomeClass().getMethods();
      homeMethodInvokerMap = new HashMap();
      for (int i = 0; i < methods.length; i++)
         homeMethodInvokerMap.put(new Long(RemoteMethodInvocation.calculateHash(methods[i])), methods[i]);
      
      try
      {
         // Get the getEJBObjectMethod
         Method getEJBObjectMethod = Class.forName("javax.ejb.Handle").getMethod("getEJBObject", new Class[0]);
         
         // Hash it
         homeMethodInvokerMap.put(new Long(RemoteMethodInvocation.calculateHash(getEJBObjectMethod)),getEJBObjectMethod);
      }
      catch (Exception e)
      {
         log.error("getEJBObject failed", e);
      }
      
      
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
      if (container.getBeanMetaData() instanceof EntityMetaData)
      {
         Class pkClass;
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
         ejbMetaData = new EJBMetaDataImpl(
            ((ContainerInvokerContainer)container).getRemoteClass(),
            ((ContainerInvokerContainer)container).getHomeClass(),
            pkClass,
            false, //Session
            false, //Stateless
            new HomeHandleImpl(jndiName));
      } else
      {
         if (((SessionMetaData)container.getBeanMetaData()).isStateless())
         {
            ejbMetaData = new EJBMetaDataImpl(
               ((ContainerInvokerContainer)container).getRemoteClass(),
               ((ContainerInvokerContainer)container).getHomeClass(),
               null, //No PK
               true, //Session
               true, //Stateless
               new HomeHandleImpl(jndiName));
         } else
         { // we are stateful
            ejbMetaData = new EJBMetaDataImpl(
               ((ContainerInvokerContainer)container).getRemoteClass(),
               ((ContainerInvokerContainer)container).getHomeClass(),
               null, //No PK
               true, //Session
               false,//Stateless
               new HomeHandleImpl(jndiName));
         }
      }
      
      ciDelegate.init();
   }
   
   public void start()
   throws Exception
   {
      try
      {
         // Export CI
         exportCI();
         GenericProxy.addLocal(container.getBeanMetaData().getJndiName(), this);
         
         InitialContext context = new InitialContext();
         
         // Bind the home in the JNDI naming space
         rebind(
            // The context
            context,
            // Jndi name
            container.getBeanMetaData().getJndiName(),
            // The Home
            ((ContainerInvokerContainer)container).getContainerInvoker().getEJBHome());
         
         // Bind a bare bones invoker in the JNDI invoker naming space
         rebind(
            // The context
            context,
            // JNDI name under the invokers moniker
            "invokers/"+container.getBeanMetaData().getJndiName(),
            // The invoker
            ((ContainerInvokerContainer)container).getContainerInvoker());
         
         
         log.debug("Bound "+container.getBeanMetaData().getEjbName() + " to " + container.getBeanMetaData().getJndiName());
      } catch (IOException e)
      {
         throw new ServerException("Could not bind either home or invoker", e);
      }
   }
   
   public void stop()
   {
      try
      {
         InitialContext ctx = new InitialContext();
         ctx.unbind(container.getBeanMetaData().getJndiName());
         ctx.unbind("invokers/"+container.getBeanMetaData().getJndiName());
         
         unexportCI();
      } 
      catch (Exception e)
      {
         // ignore.
      }
      
      GenericProxy.removeLocal(container.getBeanMetaData().getJndiName());
   }
   
   public void destroy()
   {
   }
   
   // ContainerInvoker implementation -------------------------------
   public EJBMetaData getEJBMetaData()
   {
      return ejbMetaData;
   }
   
   public EJBHome getEJBHome()
   {
      return ciDelegate.getEJBHome();
   }
   
   public EJBObject getStatelessSessionEJBObject()
   throws RemoteException
   {
      return ciDelegate.getStatelessSessionEJBObject();
   }
   
   public EJBObject getStatefulSessionEJBObject(Object id)
   throws RemoteException
   {
      return ciDelegate.getStatefulSessionEJBObject(id);
   }
   
   public EJBObject getEntityEJBObject(Object id)
   throws RemoteException
   {
      return ciDelegate.getEntityEJBObject(id);
   }
   
   public Collection getEntityCollection(Collection ids)
   throws RemoteException
   {
      return ciDelegate.getEntityCollection(ids);
   }
   
   // ContainerRemote implementation --------------------------------
   
   /**
   *  Invoke a Home interface method.
   */
   public MarshalledObject invokeHome(MarshalledObject mimo)
   throws Exception
   {
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(container.getClassLoader());
      
      try
      {
         RemoteMethodInvocation rmi = (RemoteMethodInvocation)mimo.get();
         rmi.setMethodMap(homeMethodInvokerMap);
         
         MethodInvocation message = new MethodInvocation(
            null, 
            rmi.getMethod(), 
            rmi.getArguments(),
            importTPC(rmi.getTransactionPropagationContext()),
            rmi.getPrincipal(), 
            rmi.getCredential() ); 
         
         // FIXME marcf: When we move to a global container invoker we will need to extract the name 
         // from the invocation layer. 
         ObjectName containerName = 
         new ObjectName("J2EE:service=EJB,jndiName="+container.getBeanMetaData().getJndiName());
         
         return new MarshalledObject(
            container.mbeanServer.invoke(containerName, "home", new Object[] {message}, new String[] {"java.lang.Object"})); 
      
      
      }catch (MBeanException mbe) {
         throw mbe.getTargetException();
      
      } finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }
   
   /**
   *  Invoke a Remote interface method.
   */
   public MarshalledObject invoke(MarshalledObject mimo)
   throws Exception
   {
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(container.getClassLoader());
      
      try
      {
         RemoteMethodInvocation rmi = (RemoteMethodInvocation)mimo.get();
         rmi.setMethodMap(beanMethodInvokerMap);
         
         
         // Begin the detaching of the invoker from the JMX stuff
         
         MethodInvocation message = new MethodInvocation(
            rmi.getId(), 
            rmi.getMethod(), 
            rmi.getArguments(),
            importTPC(rmi.getTransactionPropagationContext()),
            rmi.getPrincipal(), 
            rmi.getCredential() ); 
         
         // FIXME marcf: When we move to a global container invoker we will need to extract the name 
         // from the invocation layer. 
         ObjectName containerName = 
         new ObjectName("J2EE:service=EJB,jndiName="+container.getBeanMetaData().getJndiName());
        
         return new MarshalledObject(container.mbeanServer.invoke(containerName, "remote", new Object[] {message}, new String[] {"java.lang.Object"})); 
         //MBean Name for the container invoker on this bean
      
      }catch (MBeanException mbe) {
         throw mbe.getTargetException();
      }
      
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }
   
   /**
   *  Invoke a Home interface method.
   *  This is for optimized local calls.
   */
   public Object invokeHome(Method m, Object[] args, Transaction tx,
      Principal identity, Object credential)
   throws Exception
   {
      // Check if this call really can be optimized
      if (!m.getDeclaringClass().isAssignableFrom(((ContainerInvokerContainer)container).getHomeClass()))
      {
         RemoteMethodInvocation rmi = new RemoteMethodInvocation(null, m, args);
         
         // Set the transaction propagation context
         rmi.setTransactionPropagationContext(tpcFactory.getTransactionPropagationContext(tx));
         
         // Set the security stuff
         rmi.setPrincipal( SecurityAssociation.getPrincipal() );
         rmi.setCredential( SecurityAssociation.getCredential() );
         
         // Invoke on the container, enforce marshalling
         try
         {
            return invokeHome(new MarshalledObject(rmi)).get();
         } catch (Exception e)
         {
            throw (Exception)new MarshalledObject(e).get();
         }
      }
      
      // Set the right context classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(container.getClassLoader());
      
      try
      {
         return container.invokeHome(new MethodInvocation(null, m, args, tx,
               identity, credential));
      } finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }
   
   /**
   *  Invoke a Remote interface method.
   *  This is for optimized local calls.
   */
   public Object invoke(Object id, Method m, Object[] args, Transaction tx,
      Principal identity, Object credential)
   throws Exception
   {
      // Check if this call really can be optimized
      // If parent of callers classloader is != parent of our classloader -> not optimizable!
      //       if (Thread.currentThread().getContextClassLoader().getParent() != container.getClassLoader().getParent())
      if (!m.getDeclaringClass().isAssignableFrom(((ContainerInvokerContainer)container).getRemoteClass()))
      {
         RemoteMethodInvocation rmi = new RemoteMethodInvocation(id, m, args);
         
         // Set the transaction propagation context
         rmi.setTransactionPropagationContext(tpcFactory.getTransactionPropagationContext(tx));
         
         // Set the security stuff
         rmi.setPrincipal( SecurityAssociation.getPrincipal() );
         rmi.setCredential( SecurityAssociation.getCredential() );
         
         // Invoke on the container, enforce marshalling
         try
         {
            return invoke(new MarshalledObject(rmi)).get();
         } catch (Exception e)
         {
            throw (Exception)new MarshalledObject(e).get();
         }
      }
      
      // Set the right context classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(container.getClassLoader());
      
      try
      {
         return container.invoke(new MethodInvocation(id, m, args, tx, identity, credential));
      } finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }
   
   
   // XmlLoadable implementation
   public void importXml(Element element) throws DeploymentException
   {
      Element optElement = MetaData.getUniqueChild(element, "Optimized");
      if (optElement != null)
      {
         String opt = MetaData.getElementContent(optElement);
         optimize = Boolean.valueOf(opt).booleanValue();
      }
      
      if ((System.getProperty("java.vm.version").compareTo("1.3") >= 0))
         jdk122 = false;
      else
         jdk122 = true;
      
      createCIDelegate();
      
      try
      {
         Element portElement = MetaData.getUniqueChild(element, "RMIObjectPort");
         if (portElement != null)
         {
            String port = MetaData.getElementContent(portElement);
            rmiPort = Integer.parseInt(port);
         }
      } catch(NumberFormatException e)
      {
         rmiPort = ANONYMOUS_PORT;
      } catch(DeploymentException e)
      {
         rmiPort = ANONYMOUS_PORT;
      }
      
      // Load any custom socket factories
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      try
      {
         Element csfElement = MetaData.getOptionalChild(element, "RMIClientSocketFactory");
         if (csfElement != null)
         {
            clientSocketFactoryName = MetaData.getElementContent(csfElement);
         }
      } catch(Exception e)
      {
         log.error(e);
         clientSocketFactoryName = null;
      }
      try
      {
         Element ssfElement = MetaData.getOptionalChild(element, "RMIServerSocketFactory");
         if (ssfElement != null)
         {
            serverSocketFactoryName = MetaData.getElementContent(ssfElement);
         }
      } catch(Exception e)
      {
         log.error(e);
         serverSocketFactoryName = null;
      }
      Element addrElement = MetaData.getOptionalChild(element, "RMIServerSocketAddr");
      if( addrElement != null )
         this.serverAddress = MetaData.getElementContent(addrElement);
      loadCustomSocketFactories(loader);
      
      log.debug("Container Invoker RMI Port='"+(rmiPort == ANONYMOUS_PORT ? "Anonymous" : Integer.toString(rmiPort))+"'");
      log.debug("Container Invoker Client SocketFactory='"+(clientSocketFactory == null ? "Default" : clientSocketFactory.toString())+"'");
      log.debug("Container Invoker Server SocketFactory='"+(serverSocketFactory == null ? "Default" : serverSocketFactory.toString())+"'");
      log.debug("Container Invoker Server SocketAddr='"+(serverAddress == null ? "Default" : serverAddress)+"'");
      log.debug("Container Invoker Optimize='"+optimize+"'");
   }
   
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   protected void exportCI() throws Exception
   {
      UnicastRemoteObject.exportObject(this, rmiPort,
         clientSocketFactory, serverSocketFactory);
   }
   
   protected void unexportCI() throws Exception
   {
      UnicastRemoteObject.unexportObject(this, true);
   }
   
   protected void createCIDelegate() throws DeploymentException
   {
      // Create delegate depending on JDK version
      if (jdk122)
      {
         ciDelegate = new org.jboss.ejb.plugins.jrmp12.server.JRMPContainerInvoker(this);
      } else
      {
         ciDelegate = new org.jboss.ejb.plugins.jrmp13.server.JRMPContainerInvoker(this);
      }
   }
   
   protected void rebind(Context ctx, String name, Object val)
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
      
      ctx.rebind(n.get(0), val);
   }
   
   // Private -------------------------------------------------------
   private void loadCustomSocketFactories(ClassLoader loader)
   {
      try
      {
         if( clientSocketFactoryName != null )
         {
            Class csfClass = loader.loadClass(clientSocketFactoryName);
            clientSocketFactory = (RMIClientSocketFactory) csfClass.newInstance();
         }
      }
      catch(Exception e)
      {
         log.error(e);
         clientSocketFactory = null;
      }
      try
      {
         if( serverSocketFactoryName != null )
         {
            Class ssfClass = loader.loadClass(serverSocketFactoryName);
            serverSocketFactory = (RMIServerSocketFactory) ssfClass.newInstance();
            if( serverAddress != null )
            {
               // See if the server socket supports setBindAddress(String)
               try
               {
                  Class[] parameterTypes =
                  {String.class};
                  Method m = ssfClass.getMethod("setBindAddress", parameterTypes);
                  Object[] args =
                  {serverAddress};
                  m.invoke(serverSocketFactory, args);
               }
               catch(NoSuchMethodException e)
               {
                  log.error("Socket factory does not support setBindAddress(String)");
                  // Go with default address
               }
               catch(Exception e)
               {
                  log.error("Failed to setBindAddress="+serverAddress+" on socket factory");
                  // Go with default address
               }
            }
         }
         // If a bind address was specified create a DefaultSocketFactory
         else if( serverAddress != null )
         {
            DefaultSocketFactory defaultFactory = new DefaultSocketFactory();
            serverSocketFactory = defaultFactory;
            try
            {
               defaultFactory.setBindAddress(serverAddress);
            }
            catch(UnknownHostException e)
            {
               log.error("Failed to setBindAddress="+serverAddress+" on socket factory, "+e.getMessage());
            }
         }
      }
      catch(Exception e)
      {
         log.error(e);
         serverSocketFactory = null;
      }
   }
   
   /**
   *  Import a transaction propagation context into the local VM, and
   *  return the corresponding <code>Transaction</code>.
   */
   private Transaction importTPC(Object tpc)
   {
      if (tpc != null)
         return tpcImporter.importTransactionPropagationContext(tpc);
      return null;
   }
   
   // Inner classes -------------------------------------------------
}

