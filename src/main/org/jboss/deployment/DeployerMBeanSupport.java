/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.deployment;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jboss.deployment.DeploymentException;
import org.jboss.system.ServiceMBeanSupport;

/**
 * An abstract base class for deployer service implementations.
 *
 * @author <a href="mailto:toby.allsopp@peace.com">Toby Allsopp</a>
 * @version $Revision: 1.5 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>20010725 Toby Allsopp (patch from David Jencks)</b>
 * <ul><li>Added <code>getDeployments</code> method so that subclasses
 * can find out what has been deployed.</li></ul>
 */
public abstract class DeployerMBeanSupport
   extends ServiceMBeanSupport
   implements DeployerMBean
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------

   private Map deployments = new HashMap();
    
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   // DeployerMBean implementation ----------------------------------

   public void deploy (String url)
      throws MalformedURLException, IOException, DeploymentException
   {
      URL u = new URL(url);
      synchronized (deployments)
      {
         if (deployments.containsKey(u))
         {
            Object info = deployments.get(u);
            try
            {
               undeploy(u, info);
            }
            catch (Throwable t)
            {
               log.exception(t);
               if (t instanceof Exception)
               {
                  if (t instanceof IOException) throw (IOException) t;
                  if (t instanceof DeploymentException)
                     throw (DeploymentException) t;
                  throw (RuntimeException) t;
               }
               throw (Error) t;
            }
         }
         try
         {
            Object info = deploy(u);
            deployments.put(u, info);
         }
         catch (Throwable t)
         {
            log.exception(t);
            if (t instanceof Exception)
            {
               if (t instanceof IOException) throw (IOException) t;
               if (t instanceof DeploymentException)
                  throw (DeploymentException) t;
               throw (RuntimeException) t;
            }
            throw (Error) t;
         }
      }
   }

   public void undeploy (String url)
      throws MalformedURLException, IOException, DeploymentException
   {
      URL u = new URL(url);
      synchronized (deployments)
      {
         if (deployments.containsKey(u))
         {
            Object info = deployments.remove(u);
            undeploy(u, info);
         }
      }
   }

   public boolean isDeployed (String url)
      throws MalformedURLException, DeploymentException
   {
      URL u = new URL(url);
      synchronized (deployments)
      {
         return deployments.containsKey(u);
      }
   }
    
   // ServiceMBeanSupport overrides ---------------------------------
   
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------

   /**
    * Retrieves the object associated with a deployment. This
    * association is made during deployment using the object returned
    * from <code>deploy(URL)</code>. If there is no such deployment,
    * null is returned. Note that this is distinguishable from the
    * case of a deployment with an null information object only using
    * <code>isDeployed(URL)</code>.
    *
    * @param url the deployment for which information is required
    * @return an object, possibly null
    */
   protected Object getInfo(URL url)
   {
      synchronized (deployments)
      {
         return deployments.get(url);
      }
   }

   /**
    * Subclasses override to perform actual deployment.
    *
    * @param url the location to be deployed
    * @return an object, possibly null, that will be passed back to
    *         <code>undeploy</code> and can be obtained using
    *         <code>getInfo(URL)</code>
    */
   protected abstract Object deploy(URL url)
      throws IOException, DeploymentException;

   /**
    * Subclasses override to perform any actions neccessary for
    * undeployment.
    *
    * @param url the location to be undeployed
    * @param info the object that was returned by the corresponding
    *             <code>deploy</code>
    */
   protected abstract void undeploy(URL url, Object info)
      throws IOException, DeploymentException;

   /**
    * Returns the deployments that have been deployed by this
    * deployer.  The <code>Map</code> returned from this method is a
    * snapshot of the deployments at the time the method is called and
    * will not reflect any subsequent deployments or undeployments.
    *
    * @return a mapping from <code>URL</code> to
    *         <code>DeploymentInfo</code>
    */
   protected Map getDeployments()
   {
      Map ret = new HashMap();
      synchronized (deployments)
      {
         ret.putAll(deployments);
      }
      return ret;
   }
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
