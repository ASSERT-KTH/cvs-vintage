/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.management.MBeanException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.RuntimeMBeanException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.apache.log4j.NDC;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.SubDeployerSupport;
import org.jboss.deployment.DeploymentException;

import org.jboss.management.j2ee.J2EEDeployedObject;
import org.jboss.logging.Logger;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.jmx.MBeanProxy;
import org.jboss.util.jmx.ObjectNameConverter;
import org.jboss.verifier.BeanVerifier;
import org.jboss.verifier.event.VerificationEvent;
import org.jboss.verifier.event.VerificationListener;
import org.jboss.web.WebClassLoader;
import org.jboss.web.WebServiceMBean;
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.XmlFileLoader;
import org.jboss.metadata.XmlLoadable;

import org.w3c.dom.Element;

//import org.jboss.management.j2ee.EjbModule;

/**
 * A EJBDeployer is used to deploy EJB applications. It can be given a
 * URL to an EJB-jar or EJB-JAR XML file, which will be used to instantiate
 * containers and make them available for invocation.
 *
 * @see Container
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version <tt>$Revision: 1.22 $</tt>
 */
public class EJBDeployer
   extends SubDeployerSupport
   implements EJBDeployerMBean
{
   private ServiceControllerMBean serviceController;

   /** A map of current deployments. */
   private HashMap deployments = new HashMap();
   
   /** Verify EJB-jar contents on deployments */
   private boolean verifyDeployments;
   
   /** Enable verbose verification. */
   private boolean verifierVerbose;
   
   /** Enable metrics interceptor */
   private boolean metricsEnabled;
   
   /** A flag indicating if deployment descriptors should be validated */
   private boolean validateDTDs;
   
   /**
    * Returns the deployed applications.
    */
   public Iterator getDeployedApplications()
   {
      return deployments.values().iterator();
   }
   
   public ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws MalformedObjectNameException
   {
      return name == null ? OBJECT_NAME : name;
   }
   
   /**
    * Get a reference to the ServiceController
    */
   public void startService() throws Exception
   {
      serviceController = (ServiceControllerMBean)
	 MBeanProxy.create(ServiceControllerMBean.class,
			   ServiceControllerMBean.OBJECT_NAME,
			   server);

      // register with MainDeployer
      super.startService();
   }

   /**
    * Implements the template method in superclass. This method stops all the
    * applications in this server.
    */
   public void stopService() throws Exception
   {
      for (Iterator modules = deployments.values().iterator(); modules.hasNext(); )
      {
         DeploymentInfo di = (DeploymentInfo) modules.next();
         stop(di);
      }

      // avoid concurrent modification exception
      for (Iterator modules = new ArrayList(deployments.values()).iterator(); modules.hasNext(); )
      {
         DeploymentInfo di = (DeploymentInfo) modules.next();
         destroy(di);
      }
      deployments.clear();

      // deregister with MainDeployer
      super.stopService();
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
    * @return   true if enabled; false otherwise
    */
   public boolean getVerifyDeployments()
   {
      return verifyDeployments;
   }
   
   /**
    * Enables/disables the verbose mode on the verifier.
    *
    * @param   verbose  true to enable; false to disable
    */
   public void setVerifierVerbose(boolean verbose)
   {
      verifierVerbose = verbose;
   }
   
   /**
    * Returns the state of the bean verifier (verbose/non-verbose mode)
    *
    * @return true if enabled; false otherwise
    */
   public boolean getVerifierVerbose()
   {
      return verifierVerbose;
   }
   
   /**
    * Enables/disables the metrics interceptor for containers.
    *
    * @param enable  true to enable; false to disable
    */
   public void setMetricsEnabled(boolean enable)
   {
      metricsEnabled = enable;
   }
   
   /**
    * Checks if this container factory initializes the metrics interceptor.
    *
    * @return   true if metrics are enabled; false otherwise
    */
   public boolean isMetricsEnabled()
   {
      return metricsEnabled;
   }
   
   /**
    * Get the flag indicating that ejb-jar.dtd, jboss.dtd &
    * jboss-web.dtd conforming documents should be validated
    * against the DTD.
    */
   public boolean getValidateDTDs()
   {
      return validateDTDs;
   }
   
   /**
    * Set the flag indicating that ejb-jar.dtd, jboss.dtd &
    * jboss-web.dtd conforming documents should be validated
    * against the DTD.
    */
   public void setValidateDTDs(boolean validate)
   {
      this.validateDTDs = validate;
   }
   
   public boolean accepts(DeploymentInfo di) 
   {
      // To be accepted the deployment's root name must end in jar
      String urlStr = di.url.getFile();
      if (!urlStr.endsWith("jar") && !urlStr.endsWith("jar/")) return false;
         
      // However the jar must also contain at least one ejb-jar.xml
      
      try 
      {
         URL dd = di.localCl.getResource("META-INF/ejb-jar.xml");
         
         if (dd != null) {
            return true;
         }
      }
      catch (Exception ignore) {}
      
      return false;
   }
   
   public void init(DeploymentInfo di) 
      throws DeploymentException
   {
      try {
         if (di.url.getProtocol().equalsIgnoreCase("file"))
         {
            File file = new File (di.url.getFile());
            
            // If not directory we watch the package
            if (!file.isDirectory()) {
               di.watch = di.url;
            }
            // If directory we watch the xml files
            else {
               di.watch = new URL(di.url, "META-INF/ejb-jar.xml");
            }
         }
         else {
            // We watch the top only, no directory support
            di.watch = di.url;
         }
      }
      catch (Exception e) {
         if (e instanceof DeploymentException)
            throw (DeploymentException)e;
         throw new DeploymentException("failed to initialize", e);
      }
      
      // invoke super-class initialization
      processNestedDeployments(di);
   }
   
   public synchronized void create(DeploymentInfo di)
      throws DeploymentException
   {
      boolean debug = log.isDebugEnabled();
      
      try 
      {
         // Create a file loader with which to load the files
         XmlFileLoader efm = new XmlFileLoader(validateDTDs);
         
         efm.setClassLoader(di.localCl);
         
         // Load XML
         di.metaData = efm.load();
      }
      catch (Exception e) {
         if (e instanceof DeploymentException)
            throw (DeploymentException)e;
         throw new DeploymentException("Failed to load metadata", e);
      }
      
      // wrapping this into a try - catch block to prevent errors in
      // verifier from stopping the deployment
      
      if (verifyDeployments)
      {
         // Check validity
         NDC.push("Verifier");
               
         try
         {
            BeanVerifier verifier = new BeanVerifier();

            // add a listener so we can log the results
            verifier.addVerificationListener(new VerificationListener()
               {
                  Logger log = Logger.getLogger(EJBDeployer.class, "verifier");
      
                  public void beanChecked(VerificationEvent event)
                  {
                     log.debug("Bean checked: " + event.getMessage());
                  }
      
                  public void specViolation(VerificationEvent event)
                  {
                     log.warn("EJB spec violation: " +
                              (verifierVerbose ? event.getVerbose() : event.getMessage()));
                  }
               });
            
            if (debug) {
               log.debug("Verifying " + di.url);
            }
            
            verifier.verify(di.url, (ApplicationMetaData) di.metaData, di.ucl);
         }
         catch (Throwable t)
         {
            log.warn("Verify failed; continuing", t );
         }
         finally
         {
            // unset verifier context
            NDC.pop();
         }
      }
      
      // Create application

      try
      {
         // remove reserved object name letters.  Let's hope no one takes advantage of the ambiguity.
         ObjectName ejbModule = ObjectNameConverter.convert(EjbModule.BASE_EJB_MODULE_NAME +
                                                            ",url=" + di.url);
         server.createMBean(EjbModule.class.getName(),
                            ejbModule,
                            new Object[] {di},
                            new String[] {di.getClass().getName()});
         di.deployedObject = ejbModule;
      
         if (debug) {
            log.debug( "Deploying: " + di.url );
         }
         
         // Init application
         serviceController.create(ejbModule);
      }
      catch (Exception e)
      {
         throw new DeploymentException("error in create of EjbModule: " + di.url, e);
      }
   }

   public synchronized void start(DeploymentInfo di) throws DeploymentException
   {
      try 
      {
         // Start application
         log.debug( "start application, deploymentInfo: " + di +
                    ", short name: " + di.shortName +
                    ", parent short name: " + ( di.parent == null ? "null" : di.parent.shortName ));
         
         serviceController.start(di.deployedObject);
         
         // Done
         log.debug( "Deployed: " + di.url );
         
         // Register deployment. Use the application name in the hashtable
         //this is obsolete!!
         deployments.put(di.url, di);
      }
      catch (Exception e)
      {
         stop(di);
         destroy(di);
         
         throw new DeploymentException( "Could not deploy " + di.url, e );
      }
   }
   
   public void stop(DeploymentInfo di) 
      throws DeploymentException
   {
      try 
      {
         serviceController.stop(di.deployedObject);
      }
      catch (Exception e)
      {
         throw new DeploymentException("problem stopping ejb module: " + di.url, e);
      }
   }

   public void destroy(DeploymentInfo di) 
      throws DeploymentException
   {
      deployments.remove(di.url);
      
      try 
      {
         serviceController.destroy(di.deployedObject);
         serviceController.remove(di.deployedObject);
      }
      catch (Exception e)
      {
         throw new DeploymentException("problem destroying ejb module: " + di.url, e);
      }
   }
   
   /**
    * Is the aplication with this url deployed.
    *
    * @param url
    *
    * @throws MalformedURLException
    */
   public boolean isDeployed(String url) throws MalformedURLException
   {
      return isDeployed(new URL(url));
   }
   
   /**
   * Check if the application with this url is deployed.
   *
   * @param url
   * @return       true if deployed
   */
   public boolean isDeployed(URL url)
   {
      return deployments.get(url) != null;
   }
}

