package org.jboss.web;

import java.net.URL;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.DeploymentException;
import org.jboss.system.ServiceMBeanSupport;

/** A container service used to introduce war dependencies. This service is
 created by the AbstractWebContainer during the create(DeploymentInfo) call
 and registered under the name "jboss.web.deployment:war="+di.shortName
 This name is stored in the di.context under the key AbstractWebContainer.WEB_MODULE

 When the jboss-web.xml dependencies are satisfied, this service is started
 and this triggers the AbstractWebDeployer.start. Likewise, a stop on this
 service triggers the AbstractWebDeployer.stop.
 
 @see AbstractWebContainer
 
 @author Scott.Stark@jboss.org
 @version $Revison:$
 */
public class WebModule extends ServiceMBeanSupport
   implements WebModuleMBean
{
   private DeploymentInfo di;
   private AbstractWebContainer container;
   private AbstractWebDeployer deployer;

   public WebModule(DeploymentInfo di, AbstractWebContainer container,
      AbstractWebDeployer deployer)
   {
      this.di = di;
      this.container = container;
      this.deployer = deployer;
   }

   protected void startService() throws Exception
   {
      startModule();
   }

   protected void stopService() throws Exception
   {
      stopModule();
   }

   protected void destroyService()
   {
      this.di = null;
      this.container = null;
      this.deployer = null;      
   }

   /** Invokes the deployer start
    */
   public synchronized void startModule()
      throws DeploymentException
   {
      // Get the war URL
      URL warURL = di.localUrl != null ? di.localUrl : di.url;
      WebApplication webApp = deployer.start(di);
      di.context.put(AbstractWebContainer.WEB_APP, webApp);
      container.addDeployedApp(warURL, webApp);
   }

   /** Invokes the deployer stop
    */
   public synchronized void stopModule()
      throws DeploymentException
   {
      URL warURL = di.localUrl != null ? di.localUrl : di.url;
      String warUrl = warURL.toString();
      try
      {
         WebApplication webApp = container.removeDeployedApp(warURL);
         if( deployer != null && webApp != null )
         {
            deployer.stop(di);
         }
         else
         {
            log.debug("Failed to find deployer/deployment for war: "+warUrl);
         }
      }
      catch (DeploymentException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new DeploymentException("Error during stop", e);
      }
   }

}
