/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb;

import java.io.File;
import java.net.URL;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.log4j.NDC;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.SubDeployerSupport;
import org.jboss.deployment.DeploymentException;

import org.jboss.logging.Logger;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.util.jmx.MBeanProxy;
import org.jboss.util.jmx.ObjectNameConverter;
import org.jboss.verifier.BeanVerifier;
import org.jboss.verifier.event.VerificationEvent;
import org.jboss.verifier.event.VerificationListener;
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.XmlFileLoader;

/**
 * A EJBDeployer is used to deploy EJB applications. It can be given a
 * URL to an EJB-jar or EJB-JAR XML file, which will be used to instantiate
 * containers and make them available for invocation. In case of
 * EJB2.1 deployments containing webservices.xml meta-data, EJBDeployer 
 * can delegate to a web-service for j2ee deployer.
 *
 * @jmx:mbean
 *      name="jboss.ejb:service=EJBDeployer"
 *      extends="org.jboss.deployment.SubDeployerMBean"
 *
 * @see Container
 *
 * @version <tt>$Revision: 1.34 $</tt>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:christoph.jung@infor.de">Christoph G. Jung</a>
 */
public class EJBDeployer
   extends SubDeployerSupport
   implements EJBDeployerMBean
{
   private ServiceControllerMBean serviceController;

   /** Verify EJB-jar contents on deployments */
   private boolean verifyDeployments;

   /** Enable verbose verification. */
   private boolean verifierVerbose;

   /** Enable strict verification: deploy JAR only if Verifier reports
    * no problems */
   private boolean strictVerifier;

   /** Enable metrics interceptor */
   private boolean metricsEnabled;

   /** A flag indicating if deployment descriptors should be validated */
   private boolean validateDTDs;

   /** objectname of a ws4ee deployer */
   private ObjectName ws4eeDeployer;
   
   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws MalformedObjectNameException
   {
      return name == null ? OBJECT_NAME : name;
   }

   /**
    * Get a reference to the ServiceController
    */
   protected void startService() throws Exception
   {
      serviceController = (ServiceControllerMBean)
      MBeanProxy.create(ServiceControllerMBean.class,
         ServiceControllerMBean.OBJECT_NAME, server);

      // register with MainDeployer
      super.startService();
   }


   /**
    * Enables/disables the application bean verification upon deployment.
    *
    * @jmx:managed-attribute
    *
    * @param   verify  true to enable; false to disable
    */
   public void setVerifyDeployments( boolean verify )
   {
      verifyDeployments = verify;
   }

   /**
    * Returns the state of bean verifier (on/off)
    *
    * @jmx:managed-attribute
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
    * @jmx:managed-attribute
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
    * @jmx:managed-attribute
    *
    * @return true if enabled; false otherwise
    */
   public boolean getVerifierVerbose()
   {
      return verifierVerbose;
   }

   /**
    * Enables/disables the strict mode on the verifier.
    *
    * @jmx:managed-attribute
    *
    * @param strictVerifier <code>true</code> to enable; <code>false</code>
    *   to disable
    */
   public void setStrictVerifier( boolean strictVerifier )
   {
      this.strictVerifier = strictVerifier;
   }

   /**
    * Returns the mode of the bean verifier (strict/non-strict mode)
    *
    * @jmx:managed-attribute
    *
    * @return <code>true</code> if the Verifier is in strict mode,
    *   <code>false</code> otherwise
    */
   public boolean getStrictVerifier()
   {
      return strictVerifier;
   }


   /**
    * Enables/disables the metrics interceptor for containers.
    *
    * @jmx:managed-attribute
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
    * @jmx:managed-attribute
    *
    * @return   true if metrics are enabled; false otherwise
    */
   public boolean isMetricsEnabled()
   {
      return metricsEnabled;
   }

   /**
    * Get the flag indicating that ejb-jar.dtd, jboss.dtd &amp;
    * jboss-web.dtd conforming documents should be validated
    * against the DTD.
    *
    * @jmx:managed-attribute
    */
   public boolean getValidateDTDs()
   {
      return validateDTDs;
   }

   /**
    * Set the flag indicating that ejb-jar.dtd, jboss.dtd &amp;
    * jboss-web.dtd conforming documents should be validated
    * against the DTD.
    *
    * @jmx:managed-attribute
    */
   public void setValidateDTDs(boolean validate)
   {
      this.validateDTDs = validate;
   }

   /** 
    * returns the currently registered web service deployer
    * @jmx:managed-attribute
    */
   public ObjectName getWS4EEDeployer() {
      return ws4eeDeployer;
   }
   
   /**
    * Set the web service deployer to delegate to
    *
    * @jmx:managed-attribute
    */
   public void setWS4EEDeployer(ObjectName deployer)
   {
      this.ws4eeDeployer = deployer;
   }

   public boolean accepts(DeploymentInfo di)
   {
      // To be accepted the deployment's root name must end in jar
      String urlStr = di.url.getFile();
      if( !urlStr.endsWith("jar") && !urlStr.endsWith("jar/") )
      {
         return false;
      }

      // However the jar must also contain at least one ejb-jar.xml
      boolean accepts = false;
      try
      {
         URL dd = di.localCl.findResource("META-INF/ejb-jar.xml");
         if (dd == null)
         {
            return false;
         }

         // If the DD url is not a subset of the urlStr then this is coming
         // from a jar referenced by the deployment jar manifest and the
         // this deployment jar it should not be treated as an ejb-jar
         if( di.localUrl != null )
         {
            urlStr = di.localUrl.toString();
         }

         String ddStr = dd.toString();
         if ( ddStr.indexOf(urlStr) >= 0 )
         {
            accepts = true;
         }
      }
      catch( Exception ignore )
      {
      }

      return accepts;
   }

   public boolean init(DeploymentInfo di)
      throws DeploymentException
   {
      try
      {
         if( di.url.getProtocol().equalsIgnoreCase("file") )
         {
            File file = new File(di.url.getFile());

            if( !file.isDirectory() )
            {
               // If not directory we watch the package
               di.watch = di.url;
            }
            else
            {
               // If directory we watch the xml files
               di.watch = new URL(di.url, "META-INF/ejb-jar.xml");
            }
         }
         else
         {
            // We watch the top only, no directory support
            di.watch = di.url;
         }
      }
      catch (Exception e)
      {
         if (e instanceof DeploymentException)
            throw (DeploymentException)e;
         throw new DeploymentException( "failed to initialize", e );
      }

      // invoke super-class initialization
      if(super.init(di)) {
         // if ok, and we have got a ws4ee deployer attached
         if(ws4eeDeployer!=null) {
            // try to find webservices info
            URL webservicesUrl = di.localCl.getResource("META-INF/webservices.xml");
            if(webservicesUrl!=null) {
               // if found, we delegate that part
               try{
                  return ((Boolean) server.invoke(ws4eeDeployer,"init",new Object[] {di},new String[] {di.getClass().getName()})).booleanValue();
               } catch(Exception e) {
                  // need to convert better
                  throw new DeploymentException("failed to delegate ws4ee initialization.",e);
               }
            }
         }
         // no ws4ee found
         return true;
      } else {
         return false;
      }
      
   }

   public synchronized void create(DeploymentInfo di)
      throws DeploymentException
   {
      try
      {
         // Create a file loader with which to load the files
         XmlFileLoader efm = new XmlFileLoader(validateDTDs);
         efm.setClassLoader(di.localCl);

         // Load XML
         di.metaData = efm.load();
      }
      catch (Exception e)
      {
         if (e instanceof DeploymentException)
            throw (DeploymentException)e;
         throw new DeploymentException( "Failed to load metadata", e );
      }

      if( verifyDeployments )
      {
         // Check validity
         NDC.push("Verifier");

         // we have a positive attitude
         boolean allOK = true;

         // wrapping this into a try - catch block to prevent errors in
         // verifier from stopping the deployment
         try
         {
            BeanVerifier verifier = new BeanVerifier();

            // add a listener so we can log the results
            verifier.addVerificationListener(new VerificationListener()
               {
                  Logger log = Logger.getLogger(EJBDeployer.class,
                     "verifier" );

                  public void beanChecked(VerificationEvent event)
                  {
                     log.debug( "Bean checked: " + event.getMessage() );
                  }

                  public void specViolation(VerificationEvent event)
                  {
                     log.warn( "EJB spec violation: " +
                        (verifierVerbose ? event.getVerbose() : event.getMessage()));
                  }
               });

            log.debug("Verifying " + di.url);
            verifier.verify( di.url, (ApplicationMetaData) di.metaData,
               di.ucl );

            allOK = verifier.getSuccess();
         }
         catch (Throwable t)
         {
            log.warn("Verify failed; continuing", t );
         }
         finally
         {
            // unset verifier context
            NDC.pop();
            NDC.remove();
         }

         // If the verifier is in strict mode and an error/warning
         // was found in the Verification process, throw a Deployment
         // Exception
         if( strictVerifier && !allOK )
         {
            throw new DeploymentException( "Verification of Enterprise " +
               "Beans failed, see above for error messages." );
         }

      }

      // Create application

      try
      {
         ApplicationMetaData metadata = (ApplicationMetaData) di.metaData;
         EjbModule ejbModule = new EjbModule(di);
         String name = metadata.getJmxName();
         if( name == null )
         {
            name = EjbModule.BASE_EJB_MODULE_NAME + ",module=" + di.shortName; 
         }
         // Build an escaped JMX name including deployment shortname
         ObjectName ejbModuleName = ObjectNameConverter.convert(name);
         // Check that the name is not registered
         if( server.isRegistered(ejbModuleName) == true )
         {
            log.debug("The EJBModule name: "+ejbModuleName
               +"is already registered, adding uid="+System.identityHashCode(ejbModule));
            name = name + ",uid="+System.identityHashCode(ejbModule);
            ejbModuleName = ObjectNameConverter.convert(name);
         }

         server.registerMBean(ejbModule, ejbModuleName);
         di.deployedObject = ejbModuleName;

         log.debug( "Deploying: " + di.url );

         // Init application
         serviceController.create(ejbModuleName);
         super.create(di);
      }
      catch (Exception e)
      {
         throw new DeploymentException( "error in create of EjbModule: "
            + di.url, e );
      }
      
      // since ejb deployer does not use the dom4j bit, we
      // can use this as a flag
      if(ws4eeDeployer!=null && di.getDocument()!=null) {
         try{
            server.invoke(ws4eeDeployer,"create",new Object[] {di},new String[] {di.getClass().getName()});
         } catch(Exception e) {
            // need to convert better
            throw new DeploymentException("could not delegate ws4ee creation.",e);
         }
      }
   }

   public synchronized void start(DeploymentInfo di)
      throws DeploymentException
   {
      try
      {
         // Start application
         log.debug( "start application, deploymentInfo: " + di +
                    ", short name: " + di.shortName +
                    ", parent short name: " +
                    (di.parent == null ? "null" : di.parent.shortName) );

         serviceController.start(di.deployedObject);
         super.start(di);
         
      }
      catch (Exception e)
      {
         throw new DeploymentException( "Could not deploy " + di.url, e );
      }
      
      // since ejb deployer does not use the dom4j bit, we
      // can use this as a flag
      if(ws4eeDeployer!=null && di.getDocument()!=null) {
         try{
            server.invoke(ws4eeDeployer,"start",new Object[] {di},new String[] {di.getClass().getName()});
         } catch(Exception e) {
            // need to convert better
            throw new DeploymentException("could not delegate ws42ee startup.",e);
         }
      }

      log.debug( "Deployed: " + di.url );
   }

   public void stop(DeploymentInfo di)
      throws DeploymentException
   {
      // since ejb deployer does not use the dom4j bit, we
      // can use this as a flag
      if(ws4eeDeployer!=null && di.getDocument()!=null) {
         try{
            server.invoke(ws4eeDeployer,"stop",new Object[] {di},new String[] {di.getClass().getName()});
         } catch(Exception e) {
            log.error("could not delegate ws4ee stopping.",e);
         }
      }

      try
      {
         serviceController.stop(di.deployedObject);
         super.stop(di);
      }
      catch (Exception e)
      {
         throw new DeploymentException( "problem stopping ejb module: " +
            di.url, e );
      }
      

   }

   public void destroy(DeploymentInfo di) 
      throws DeploymentException
   {
      // since ejb deployer does not use the dom4j bit, we
      // can use this as a flag
      if(ws4eeDeployer!=null && di.getDocument()!=null) {
         try{
            server.invoke(ws4eeDeployer,"destroy",new Object[] {di},new String[] {di.getClass().getName()});
         } catch(Exception e) {
            log.error("could not delegate ws4ee destruction.",e);
         }
      }

      try
      {
         serviceController.destroy( di.deployedObject );
         serviceController.remove( di.deployedObject );
         super.destroy(di);
      }
      catch (Exception e)
      {
         throw new DeploymentException( "problem destroying ejb module: " +
            di.url, e );
      }
   }
}
/*
vim:ts=3:sw=3:et
*/
