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
*   A ContainerFactory is used to deploy EJB applications. It can be given a URL to 
*	  an EJB-jar or EJB-JAR XML file, which will be used to instantiate containers and make
*	  them available for invocation.
*      
*   @see Container
*   @author Rickard Öberg (rickard.oberg@telkel.com)
*   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*   @version $Revision: 1.19 $
*/
public class ContainerFactory
implements ContainerFactoryMBean, MBeanRegistration
{
	// Constants -----------------------------------------------------
	public static String DEFAULT_STATELESS_CONFIGURATION = "Default Stateless SessionBean";
	public static String DEFAULT_STATEFUL_CONFIGURATION = "Default Stateful SessionBean";
	public static String DEFAULT_ENTITY_BMP_CONFIGURATION = "Default BMP EntityBean";
	public static String DEFAULT_ENTITY_CMP_CONFIGURATION = "Default CMP EntityBean";
	
	// Attributes ----------------------------------------------------
	// The logger of this service
	Log log = new Log("Container factory");
	
	// The JMX agent
	MBeanServer server;
	
	// A map of current deployments. If a deployment is made and it is already in this map,
	// then undeploy it first (i.e. make it a re-deploy).
	HashMap deployments = new HashMap();
	
	// Public --------------------------------------------------------
	
	/**
	*	Deploy the file at this URL. This method is typically called from remote administration
	*	tools that cannot handle java.net.URL's as parameters to methods
	*
	* @param   url  
	* @exception   MalformedURLException  
	* @exception   DeploymentException  
	*/
	public void deploy(String url)
	throws MalformedURLException, DeploymentException
	{
		// Delegate to "real" deployment
		deploy(new URL(url));
	}
	
	
	/**
	*	Undeploy the file at this URL. This method is typically called from remote administration
	*	tools that cannot handle java.net.URL's as parameters to methods
	*
	* @param   url  
	* @exception   MalformedURLException  
	* @exception   DeploymentException  
	*/
	public void undeploy(String url)
	throws MalformedURLException, DeploymentException
	{
		// Delegate to "real" undeployment
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
			
			// Create a file manager with which to load the files
			jBossFileManagerFactory fact = new jBossFileManagerFactory();
			jBossFileManager efm = (jBossFileManager)fact.createFileManager();
			
			// Setup beancontext
			BeanContextServicesSupport beanCtx = new BeanContextServicesSupport();
			beanCtx.add(Beans.instantiate(getClass().getClassLoader(), "com.dreambean.ejx.xml.ProjectX"));
			beanCtx.add(efm);
			
			// Load XML
			jBossEjbJar jar;
			if (url.getProtocol().startsWith("file"))
			{
				// This will copy the jar first so it isn't locked by the CL
				efm.load(new File(url.getFile()));
				jar = efm.getEjbJar();
			}
			else
			{
				jar = efm.load(url);
			}
			
			// Create classloader for this application
			//         ClassLoader cl = new EJBClassLoader(new URL[] {url}, getClass().getClassLoader(), jar.isSecure());
			ClassLoader cl = efm.getClassLoader();
			
			// Get list of beans for which we will create containers
			Iterator beans = jar.getEnterpriseBeans().iterator();
			
			// Create list of containers
			ArrayList containers = new ArrayList();
			
			// Deploy beans
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
						// Create container
						con = new StatelessSessionContainer();
						
						// Create classloader for this container
						con.setClassLoader(new BeanClassLoader(cl));
						
						// Set metadata
						con.setMetaData(bean);
						
						// Get container configuration
						ContainerConfiguration conf = jar.getContainerConfigurations().getContainerConfiguration(bean.getConfigurationName());
						
						// Make sure we have a default configuration
						if (conf == null) 
						{
							log.warning("No configuration chosen. Using default configuration");
							
							// Get the container default configuration
							conf = jar.getContainerConfigurations().getContainerConfiguration(DEFAULT_STATELESS_CONFIGURATION);
							
							// Make sure this bean knows the configuration he is using
							bean.setConfigurationName(DEFAULT_STATELESS_CONFIGURATION);
						}
						
						// Set container invoker
						((StatelessSessionContainer)con).setContainerInvoker((ContainerInvoker)cl.loadClass(conf.getContainerInvoker()).newInstance());
						
						// Set instance pool
						con.setInstancePool((InstancePool)cl.loadClass(conf.getInstancePool()).newInstance());
						
						// Create interceptors
						
						//                  con.addInterceptor(new LogInterceptor());
						//                  con.addInterceptor(new SecurityInterceptor());
						//                  con.addInterceptor(new TxInterceptor());
						
						con.addInterceptor(new StatelessSessionInstanceInterceptor());
						
						// Finally we add the last interceptor from the container
						con.addInterceptor(con.createContainerInterceptor());
						
						// Add container to application
						containers.add(con);
					
					} else // Stateful
					{
						boolean implemented = false;
						
						//if (!implemented) throw new Error("Stateful Container not implemented yet");
						
						// Create container
						con = new StatefulSessionContainer();
						
						// Create classloader for this container
						con.setClassLoader(new BeanClassLoader(cl));
						
						// Set metadata
						con.setMetaData(bean);
						
						// Get container configuration
						ContainerConfiguration conf = jar.getContainerConfigurations().getContainerConfiguration(bean.getConfigurationName());
						
						// Make sure we have a default configuration
						if (conf == null) 
						{
							log.warning("No configuration chosen. Using default configuration");
							
							conf =  jar.getContainerConfigurations().getContainerConfiguration(DEFAULT_STATEFUL_CONFIGURATION);
							
							// Make sure this bean knows the configuration he is using
							bean.setConfigurationName(DEFAULT_STATEFUL_CONFIGURATION);	
						}
						
						// Set container invoker
						((StatefulSessionContainer)con).setContainerInvoker((ContainerInvoker)cl.loadClass(conf.getContainerInvoker()).newInstance());
						
						// Set instance cache
						((StatefulSessionContainer)con).setInstanceCache((InstanceCache)cl.loadClass(conf.getInstanceCache()).newInstance());
						
						// Set persistence manager
						((StatefulSessionContainer)con).setPersistenceManager((StatefulSessionPersistenceManager)cl.loadClass(conf.getPersistenceManager()).newInstance());
						
						// Set instance pools (this is bogus anyway) should be set through default stuff 
						con.setInstancePool((InstancePool)cl.loadClass("org.jboss.ejb.plugins.StatefulSessionInstancePool").newInstance());
						
						
						// Create interceptors
						//con.addInterceptor(new LogInterceptor());
						//con.addInterceptor(new TxInterceptor());
						con.addInterceptor(new StatefulSessionInstanceInterceptor());
						//con.addInterceptor(new SecurityInterceptor());
						
						con.addInterceptor(con.createContainerInterceptor());
						
						// Add container to application
						containers.add(con);
					
					}
				} else // Entity
				{
					// Create container
					con = new EntityContainer();
					
					// Create classloader for this container
					con.setClassLoader(new BeanClassLoader(cl));
					
					// Set metadata
					con.setMetaData(bean);
					
					// Get container configuration
					ContainerConfiguration conf = jar.getContainerConfigurations().getContainerConfiguration(bean.getConfigurationName());
					
					// Make sure we have a default configuration
					if (conf == null) 
					{
						log.warning("No configuration chosen. Using default configuration");
						if (((jBossEntity) bean).getPersistenceType().equals("Bean")) 
						{
							// BMP case
							conf =  jar.getContainerConfigurations().getContainerConfiguration(DEFAULT_ENTITY_BMP_CONFIGURATION);
							
							// Make sure this bean knows the configuration he is using
							bean.setConfigurationName(DEFAULT_ENTITY_BMP_CONFIGURATION);
						}
						else 
						{ 
							// CMP case
							conf =  jar.getContainerConfigurations().getContainerConfiguration(DEFAULT_ENTITY_CMP_CONFIGURATION);
							
							// Make sure this bean knows the configuration he is using
							bean.setConfigurationName(DEFAULT_ENTITY_CMP_CONFIGURATION);
						}
					}
					
					// Set container invoker
					((EntityContainer)con).setContainerInvoker((ContainerInvoker)cl.loadClass(conf.getContainerInvoker()).newInstance());
					
					// Set instance cache
					((EntityContainer)con).setInstanceCache((InstanceCache)cl.loadClass(conf.getInstanceCache()).newInstance());
					
					// Set instance pool
					con.setInstancePool((InstancePool)cl.loadClass(conf.getInstancePool()).newInstance());
					
					// Set persistence manager
					((EntityContainer)con).setPersistenceManager((EntityPersistenceManager)cl.loadClass(conf.getPersistenceManager()).newInstance());
					
					
					// Create interceptors
					//					con.addInterceptor(new LogInterceptor());
					//					con.addInterceptor(new SecurityInterceptor());
					//					con.addInterceptor(new TxInterceptor());
					con.addInterceptor(new EntityInstanceInterceptor());
					con.addInterceptor(new EntitySynchronizationInterceptor());
					
					con.addInterceptor(con.createContainerInterceptor());
					
					// Add container to application
					containers.add(con);
				}
				
				// Set callback to application
				if (con != null)
					con.setApplication(app);
			}
			
			// Init/Start container
			for (int i = 0; i < containers.size(); i++)
			{
				Container con = (Container)containers.get(i);
				
				// Init container
				con.init();
				
				// Start
				con.start();
				log.log("Started: "+con.getMetaData().getEjbName());
			}
			
			// Bind container in global JNDI namespace
			for (int i = 0; i < containers.size(); i++)
			{
				Container con = (Container)containers.get(i);
   
                // Use rebind to make sure you overwrite the name
				rebind(ctx, con.getMetaData().getJndiName(), con.getContainerInvoker().getEJBHome());
					
				// Done
				log.log("Bound "+con.getMetaData().getEjbName() + " to " + con.getMetaData().getJndiName());
				
				/*if (con instanceof EntityContainer)
				{
					rebind(ctx, con.getMetaData().getJndiName(), ((EntityContainer)con).getContainerInvoker().getEJBHome());
					
					// Done
					log.log("Bound "+con.getMetaData().getEjbName() + " to " + con.getMetaData().getJndiName());
				} else if (con instanceof StatelessSessionContainer)
				{
					rebind(ctx, con.getMetaData().getJndiName(), ((StatelessSessionContainer)con).getContainerInvoker().getEJBHome());
					
					// Done
					log.log("Bound "+con.getMetaData().getEjbName() + " to " + con.getMetaData().getJndiName());
				} else if (con instanceof StatefulSessionContainer) 
			    {
				    rebind(ctx, con.getMetaData().getJndiName(), ((StatefulSessionContainer) con).getContainerInvoker().getEJBHome());
		            log.log("Bound "+con.getMetaData().getEjbName() + " to " + con.getMetaData().getJndiName());
				}
				*/
				
			}
			
			// Add to webserver so client can access classes through dynamic class downloading
			WebProviderMBean webServer = (WebProviderMBean)MBeanProxy.create(WebProviderMBean.class, WebProviderMBean.OBJECT_NAME);
			webServer.addClassLoader(cl);
			
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
		// Get application from table
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
				
				// Stop container
				con.stop();
				
				// Destroy container
				con.destroy();
				
				// Done
				log.log("Removed: "+con.getMetaData().getEjbName());
			}
			
			// Remove deployment
			deployments.remove(url);
			
			// Done
			log.log("Undeployed application: "+app.getName());
		} catch (Exception e)
		{
			log.error("Undeploy failed");
			log.exception(e);
			
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
