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
import org.jboss.web.WebServiceMBean;

import org.jboss.ejb.plugins.*;

import org.jboss.verifier.BeanVerifier;
import org.jboss.verifier.event.VerificationEvent;
import org.jboss.verifier.event.VerificationListener;


/**
*   A ContainerFactory is used to deploy EJB applications. It can be given a URL to
*  an EJB-jar or EJB-JAR XML file, which will be used to instantiate containers and make
*  them available for invocation.
*
*   @see Container
*   @author Rickard Öberg (rickard.oberg@telkel.com)
*   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*   @author <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
*
*   @version $Revision: 1.25 $
*/
public class ContainerFactory
	extends org.jboss.util.ServiceMBeanSupport
	implements ContainerFactoryMBean
{
	// Constants -----------------------------------------------------
	public static String DEFAULT_STATELESS_CONFIGURATION = "Default Stateless SessionBean";
	public static String DEFAULT_STATEFUL_CONFIGURATION = "Default Stateful SessionBean";
	public static String DEFAULT_ENTITY_BMP_CONFIGURATION = "Default BMP EntityBean";
	public static String DEFAULT_ENTITY_CMP_CONFIGURATION = "Default CMP EntityBean";

	// Attributes ----------------------------------------------------
	// The logger of this service
	Log log = new Log(getName());

	// A map of current deployments. If a deployment is made and it is already in this map,
	// then undeploy it first (i.e. make it a re-deploy).
	HashMap deployments = new HashMap();

	// Verify EJB-jar contents on deployments
	boolean verifyDeployments = false;

	// Public --------------------------------------------------------
    
    /**
     * Implements the abstract <code>getObjectName()</code> method in superclass
     * to return this service's name.
     *
     * @param   server
     * @param   name
     *
     * @exception MalformedObjectNameException
     * @return
     */
	public ObjectName getObjectName(MBeanServer server, ObjectName name)
	   throws javax.management.MalformedObjectNameException
	{
	   return new ObjectName(OBJECT_NAME);
	}

    /**
     * Implements the abstract <code>getName()</code> method in superclass to
     * return the name of this object.
     *
     * @return <tt>'Container factory'</code>
     */
	public String getName()
	{
	   return "Container factory";
	}

    /**
     * Implements the template method in superclass. This method stops all the
     * applications in this server.
     */
	public void stopService()
	{
		Iterator apps = deployments.values().iterator();
		while (apps.hasNext())
		{
			Application app = (Application)apps.next();
			app.stop();
		}
	}

    /**
     * Implements the template method in superclass. This method destroys all
     * the applications in this server and clears the deployments list.
     */
	public void destroyService()
	{
		Iterator apps = deployments.values().iterator();
		while (apps.hasNext())
		{
			Application app = (Application)apps.next();
			app.destroy();
		}

		deployments.clear();
	}

    /**
     * Enables/disables the application bean verification upon deployment.
     *
     * @param   verify  true to enable; false to disable
     */
	public void setVerifyDeployments(boolean verify)
	{
		verifyDeployments = verify;
	}

    /**
     * Returns the state of bean verifier (on/off)
     *
     * @param   true if enabled; false otherwise
     */
	public boolean getVerifyDeployments()
	{
		return verifyDeployments;
	}

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
	*   whose structure resembles that of an EJB-JAR. <p>
	*
	*   The latter is useful for development since no packaging is required.
	*
	* @param       url  URL where EJB deployment information is contained
    *
	* @exception   DeploymentException
	*/
	public synchronized void deploy(URL url)
		throws DeploymentException
	{
		// Create application
		Application app = new Application();

		try
		{
			Log.setLog(log);

			// Check if already deployed -> undeploy first, this is re-deploy
			if (deployments.containsKey(url))
				undeploy(url);

            // Check validity
            try {
                // wrapping this into a try - catch block to prevent errors in
                // verifier from stopping the deployment
                
                if (verifyDeployments)
                {
                    BeanVerifier verifier = new BeanVerifier();
    
                    verifier.addVerificationListener(new VerificationListener()
                    {
                       public void beanChecked(VerificationEvent event)
                       {
                            System.out.println("Got event: " + event.getMessage());
                       }
                    });
    
                    verifier.verify(url);
                }
            }
            catch (Throwable t) {
                System.out.println(t);
            }
    
			app.setURL(url);

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
//			ClassLoader cl = new EJBClassLoader(new URL[] {url}, null, jar.isSecure());
			ClassLoader cl = efm.getClassLoader();

			// Get list of beans for which we will create containers
			Iterator beans = jar.getEnterpriseBeans().iterator();

			// Deploy beans
			Context ctx = new InitialContext();
			while(beans.hasNext())
			{
				jBossEnterpriseBean bean = (jBossEnterpriseBean)beans.next();

				log.log("Deploying "+bean.getEjbName());

				if (bean instanceof jBossSession) // Is session?
				{
					if (((jBossSession)bean).getSessionType().equals("Stateless")) // Is stateless?
					{
						// Create container
						StatelessSessionContainer container = new StatelessSessionContainer();

						// Create classloader for this container
						container.setClassLoader(new BeanClassLoader(cl));

						// Set metadata
						container.setMetaData(bean);

                        // use the new metadata classes in org.jboss.metadata
                        container.setBeanMetaData(efm.getMetaData().getBean(bean.getEjbName()));

						// Set transaction manager
						container.setTransactionManager((TransactionManager)new InitialContext().lookup("TransactionManager"));

						// Get container configuration
						ContainerConfiguration conf = bean.getContainerConfiguration();

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
						container.setContainerInvoker((ContainerInvoker)cl.loadClass(conf.getContainerInvoker()).newInstance());

						// Set instance pool
						container.setInstancePool((InstancePool)cl.loadClass(conf.getInstancePool()).newInstance());

						// Create interceptors

						container.addInterceptor(new LogInterceptor());
						container.addInterceptor(new SecurityInterceptor());
						container.addInterceptor(new TxInterceptor());
						container.addInterceptor(new StatelessSessionInstanceInterceptor());

						// Finally we add the last interceptor from the container
						container.addInterceptor(container.createContainerInterceptor());

						// Add container to application
						app.addContainer(container);
					} else // Stateful
					{
						boolean implemented = false;

						//if (!implemented) throw new Error("Stateful Container not implemented yet");

						// Create container
						StatefulSessionContainer container = new StatefulSessionContainer();

						// Create classloader for this container
						container.setClassLoader(new BeanClassLoader(cl));

						// Set metadata
						container.setMetaData(bean);
                        container.setBeanMetaData(efm.getMetaData().getBean(bean.getEjbName()));

						// Set transaction manager
						container.setTransactionManager((TransactionManager)new InitialContext().lookup("TransactionManager"));

						// Get container configuration
						ContainerConfiguration conf = bean.getContainerConfiguration();

						// Make sure we have a default configuration
						if (conf == null)
						{
							log.warning("No configuration chosen. Using default configuration");

							conf =  jar.getContainerConfigurations().getContainerConfiguration(DEFAULT_STATEFUL_CONFIGURATION);

							// Make sure this bean knows the configuration he is using
							bean.setConfigurationName(DEFAULT_STATEFUL_CONFIGURATION);
						}

						// Set container invoker
						container.setContainerInvoker((ContainerInvoker)cl.loadClass(conf.getContainerInvoker()).newInstance());

						// Set instance cache
						container.setInstanceCache((InstanceCache)cl.loadClass(conf.getInstanceCache()).newInstance());

						// Set instance pool
						container.setInstancePool((InstancePool)cl.loadClass(conf.getInstancePool()).newInstance());

						// Set persistence manager
						container.setPersistenceManager((StatefulSessionPersistenceManager)cl.loadClass(conf.getPersistenceManager()).newInstance());

						// Create interceptors
						container.addInterceptor(new LogInterceptor());
						container.addInterceptor(new TxInterceptor());
						container.addInterceptor(new StatefulSessionInstanceInterceptor());
						container.addInterceptor(new SecurityInterceptor());

						container.addInterceptor(container.createContainerInterceptor());

						// Add container to application
						app.addContainer(container);
					}
				} else // Entity
				{
					// Create container
					EntityContainer container = new EntityContainer();

					// Create classloader for this container
					container.setClassLoader(new BeanClassLoader(cl));

					// Set metadata
					container.setMetaData(bean);
                    container.setBeanMetaData(efm.getMetaData().getBean(bean.getEjbName()));

					// Set transaction manager
					container.setTransactionManager((TransactionManager)new InitialContext().lookup("TransactionManager"));

					// Get container configuration
					ContainerConfiguration conf = bean.getContainerConfiguration();

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
					container.setContainerInvoker((ContainerInvoker)cl.loadClass(conf.getContainerInvoker()).newInstance());

					// Set instance cache
					container.setInstanceCache((InstanceCache)cl.loadClass(conf.getInstanceCache()).newInstance());

					// Set instance pool
					container.setInstancePool((InstancePool)cl.loadClass(conf.getInstancePool()).newInstance());

					// Set persistence manager 
					if (((jBossEntity) bean).getPersistenceType().equals("Bean")) {
						
						//Should be BMPPersistenceManager
						container.setPersistenceManager((EntityPersistenceManager)cl.loadClass(conf.getPersistenceManager()).newInstance());
				 	}
					else {
						
						// CMP takes a manager and a store
						org.jboss.ejb.plugins.CMPPersistenceManager persistenceManager = new org.jboss.ejb.plugins.CMPPersistenceManager();
					    
						//Load the store from configuration
						persistenceManager.setPersistenceStore((EntityPersistenceStore)cl.loadClass(conf.getPersistenceManager()).newInstance());
				
						// Set the manager on the container
						container.setPersistenceManager(persistenceManager);
					}
					
					// Create interceptors
					container.addInterceptor(new LogInterceptor());
					container.addInterceptor(new SecurityInterceptor());
					container.addInterceptor(new TxInterceptor());
					container.addInterceptor(new EntityInstanceInterceptor());
					container.addInterceptor(new EntitySynchronizationInterceptor());

					container.addInterceptor(container.createContainerInterceptor());

					// Add container to application
					app.addContainer(container);
				}
			}

			// Init application
			app.init();

			// Start application
			app.start();

			// Add to webserver so client can access classes through dynamic class downloading
			WebServiceMBean webServer = (WebServiceMBean)MBeanProxy.create(WebServiceMBean.class, WebServiceMBean.OBJECT_NAME);
			webServer.addClassLoader(cl);

			// Done
			log.log("Deployed application: "+app.getName());

			// Register deployment
			deployments.put(url, app);
		} catch (Throwable e)
		{
			e.printStackTrace();

			app.stop();
			app.destroy();

			throw new DeploymentException("Could not deploy "+url.toString());
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
		app.stop();
		app.destroy();

		// Remove deployment
		deployments.remove(url);

		// Done
		log.log("Undeployed application: "+app.getName());

		Log.unsetLog();
	}

	// Protected -----------------------------------------------------
}
