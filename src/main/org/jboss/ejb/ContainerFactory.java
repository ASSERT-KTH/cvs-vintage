/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.beans.Beans;
import java.beans.beancontext.BeanContextServicesSupport;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.management.MBeanServer;
import javax.management.MBeanRegistration;
import javax.management.ObjectName;
import javax.transaction.TransactionManager;

import org.jboss.ejb.deployment.jBossEjbJar;
import org.jboss.ejb.deployment.jBossFileManager;
import org.jboss.ejb.deployment.jBossFileManagerFactory;
import org.jboss.ejb.deployment.jBossEnterpriseBean;
import org.jboss.ejb.deployment.jBossEnterpriseBeans;
import org.jboss.ejb.deployment.jBossSession;
import org.jboss.ejb.deployment.jBossEntity;
import org.jboss.ejb.deployment.ContainerConfiguration;
import org.jboss.ejb.deployment.ContainerConfigurations;

import org.jboss.logging.Log;
import org.jboss.logging.ConsoleLogging;
import org.jboss.logging.ConsoleLoggingMBean;

import org.jboss.util.MBeanProxy;
import org.jboss.web.WebProviderMBean;

import org.jboss.ejb.plugins.*;

/**
 *   A CF is used to create implementations of EJB Containers. 
 *   It encapsulates the notion of a configuration since all containers
 *   that are created use the same persistence engine, the same implementation
 *   of containers, and so on. If several different such configurations are desired,
 *   several containerfactories should be used.
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *   @version $Revision: 1.8 $
 */
public class ContainerFactory
   implements ContainerFactoryMBean, MBeanRegistration
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   Log log = new Log("Container factory");
   
   MBeanServer server;
   
   HashMap deployments = new HashMap();
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void deploy(String url)
      throws MalformedURLException, DeploymentException
   {
      deploy(new URL(url));
   }
   
   public void undeploy(String url)
      throws MalformedURLException, DeploymentException
   {
      undeploy(new URL(url));
   }
   
   /**
    *   Deploy EJBs pointed to by an URL.
    *   The URL may point to an EJB-JAR, an EAR-JAR, or an codebase
    *   whose structure resembles that of an EJB-JAR.
    *
    *   The latter is useful for development since no packaging is required
    *
    * @param   url  URL where EJB deployment information is contained
    * @return     The created containers
    * @exception   DeploymentException  
    */
   public synchronized void deploy(URL url)
      throws DeploymentException
   {
      try
      {
         // Check if already deployed -> undeploy first, this is re-deploy
         if (deployments.containsKey(url))
            undeploy(url);
         
         // Create application
         Application app = new Application();
         app.setURL(url);
         
         Log.setLog(log);
         log.log("Deploying:"+url);
         
         jBossFileManagerFactory fact = new jBossFileManagerFactory();
         
         // Create classloader
         jBossFileManager efm = (jBossFileManager)fact.createFileManager();
         
         // Setup beancontext
         BeanContextServicesSupport beanCtx = new BeanContextServicesSupport();
         beanCtx.add(Beans.instantiate(getClass().getClassLoader(), "com.dreambean.ejx.xml.ProjectX"));
         beanCtx.add(efm);
         
         // Load XML
         jBossEjbJar jar = efm.load(url);
         ClassLoader cl = new EJBClassLoader(new URL[] {url}, getClass().getClassLoader(), ((jBossEnterpriseBeans)jar.getEnterpriseBeans()).isSecure());
         
         Iterator beans = jar.getEnterpriseBeans().iterator();
         
         ArrayList containers = new ArrayList();
         
         // Deploy bean
         Context ctx = new InitialContext();
         while(beans.hasNext())
         {
            Container con = null;
            jBossEnterpriseBean bean = (jBossEnterpriseBean)beans.next();
            log.log("Deploying "+bean.getEjbName());
            if (bean instanceof jBossSession) // Is session?
            {
               if (((jBossSession)bean).getSessionType().equals("Stateless")) // Is stateless?
               {
                  con = new StatelessSessionContainer();
                  
//                  con.addInterceptor(new LogInterceptor());
//                  con.addInterceptor(new TxInterceptor());
                  con.addInterceptor(new StatelessSessionInstanceInterceptor());
                  con.addInterceptor(new SecurityInterceptor());
                  
                  con.addInterceptor(con.createContainerInterceptor());
                  
                  con.setClassLoader(new BeanClassLoader(cl));
                  con.setMetaData(bean);
				   
                  ContainerConfiguration conf = jar.getContainerConfigurations().getContainerConfiguration(bean.getConfigurationName());
                 
				  // Make sure we have a default configuration
				  if (conf == null) {
					  
					 log.log("Using default configuration");
					 
					 conf =  jar.getContainerConfigurations().getContainerConfiguration("Default Stateless SessionBean");
				  }
				  
                  con.setContainerInvoker((ContainerInvoker)cl.loadClass(conf.getContainerInvoker()).newInstance());
                  con.setInstancePool((InstancePool)cl.loadClass(conf.getInstancePool()).newInstance());
                  
//                con.setTransactionManager((TransactionManager)cl.loadClass(conf.getTransactionManager()).newInstance());
                  con.setTransactionManager(new org.jboss.tm.TxManager());
                  
                  containers.add(con);
               } else // Stateful
               {
				   throw new Error("Stateful beans not yet implemented");
               }
            } else // Entity
            {
                  con = new EntityContainer();
                  
//                  con.addInterceptor(new LogInterceptor());
//                  con.addInterceptor(new TxInterceptor());
                  con.addInterceptor(new EntityInstanceInterceptor());
                  con.addInterceptor(new SecurityInterceptor());
                  con.addInterceptor(new EntitySynchronizationInterceptor());
                 
                  con.addInterceptor(con.createContainerInterceptor());
                  
				  con.setClassLoader(new BeanClassLoader(cl));
                  con.setMetaData(bean);
                  
                  ContainerConfiguration conf = jar.getContainerConfigurations().getContainerConfiguration(bean.getConfigurationName());
                  
				  // Make sure we have a default configuration
				  if (conf == null) {
					  
					 log.log("Using default configuration");
					 if (((jBossEntity) bean).getPersistenceType().equalsIgnoreCase("bean")) {
						 
						 // BMP case
						 conf =  jar.getContainerConfigurations().getContainerConfiguration("BMP EntityBean");
				     }
					 else { 
					 
					     // CMP case
						 conf =  jar.getContainerConfigurations().getContainerConfiguration("CMP EntityBean");
				     }
				  }
                  con.setContainerInvoker((ContainerInvoker)cl.loadClass(conf.getContainerInvoker()).newInstance());
                  ((EntityContainer)con).setInstanceCache((InstanceCache)cl.loadClass(conf.getInstanceCache()).newInstance());
                  con.setInstancePool((InstancePool)cl.loadClass(conf.getInstancePool()).newInstance());
                  ((EntityContainer)con).setPersistenceManager((EntityPersistenceManager)cl.loadClass(conf.getPersistenceManager()).newInstance());
                       
//                con.setTransactionManager((TransactionManager)cl.loadClass(conf.getTransactionManager()).newInstance());
				  con.setTransactionManager(new org.jboss.tm.TxManager());
                  
                  containers.add(con);
                  
            }
            
            if (con != null)
               con.setApplication(app);
         }
         
         // Init/Start container
         for (int i = 0; i < containers.size(); i++)
         {
            Container con = (Container)containers.get(i);
            
            // Register Container with JMX
//            server.registerMBean(con, new ObjectName("EJB:service=Container,application="+app.getName()+",name="+con.getMetaData().getEjbName()));
            
            // Init container
            con.init();
            
            // Start
            con.start();
            log.log("Started: "+con.getMetaData().getEjbName());
         }
         
         // Bind in JNDI
         for (int i = 0; i < containers.size(); i++)
         {
            Container con = (Container)containers.get(i);
            rebind(ctx, con.getMetaData().getJndiName(), con.getContainerInvoker().getEJBHome());
            
            // Done
            log.log("Bound "+con.getMetaData().getEjbName() + " to " + con.getMetaData().getJndiName());
         }
         
         // Add to webserver so client can access classes through dynamic class downloading
         WebProviderMBean webServer = (WebProviderMBean)MBeanProxy.create(WebProviderMBean.class, WebProviderMBean.OBJECT_NAME);
         webServer.addClassLoader(cl);
         
         // Register Application with JMX
//         server.registerMBean(app, new ObjectName("EJB:service=Application,name="+app.getName()));
         
         // Done
         log.log("Deployed application: "+app.getName());
            
         // Register deployment
         deployments.put(url, app);
      } catch (Exception e)
      {
         e.printStackTrace();
         throw new DeploymentException("Could not deploy "+url.toString(),e);
      } finally
      {
         Log.unsetLog();
      }
   }
      

   /**
    *   Remove previously deployed EJBs.
    *
    * @param   url  
    * @exception   DeploymentException  
    */
   public void undeploy(URL url)
      throws DeploymentException
   {
      Application app = (Application)deployments.get(url);
      
      // Check if deployed
      if (app == null)
      {
         throw new DeploymentException("URL not deployed");
      }
      
      // Undeploy application
      Log.setLog(log);
      log.log("Undeploying:"+url);
      try
      {
         
         // Unbind in JNDI
         Iterator enum = app.getContainers().iterator();
         Context ctx = new InitialContext();
         while (enum.hasNext())
         {
            Container con = (Container)enum.next();
            ctx.unbind(con.getMetaData().getJndiName());
            
            // Done
            log.log("Unbound: "+con.getMetaData().getJndiName());
         }
         
         // Stop/destroy container
         enum = app.getContainers().iterator();
         while (enum.hasNext())
         {
            Container con = (Container)enum.next();
            
            // Init container
            con.stop();
            
            // Start
            con.destroy();
            
            // Done
            log.log("Removed: "+con.getMetaData().getJndiName());
         }
         
         // Remove deployment
         deployments.remove(url);
         
         // Done
         log.log("Undeployed application: "+app.getName());
      } catch (Exception e)
      {
         throw new DeploymentException("Undeploy failed", e);
      } finally
      {
         Log.unsetLog();
      }
   }

   // MBeanRegistration ---------------------------------------------
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws java.lang.Exception
   {
      this.server = server;
      
      return new ObjectName(OBJECT_NAME);
//      return name;
   }
   
   public void postRegister(java.lang.Boolean registrationDone)
   {
   }
   
   public void preDeregister()
      throws java.lang.Exception
   {
   }
   
   public void postDeregister()
   {
   }
   
   // Protected -----------------------------------------------------
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
}
