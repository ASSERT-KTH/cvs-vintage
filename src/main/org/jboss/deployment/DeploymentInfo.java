/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.deployment;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.management.ObjectName;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.jboss.logging.Logger;
import org.jboss.system.ServiceLibraries;
import org.jboss.system.UnifiedClassLoader;

/**
 * Service Deployment Info .
 *
 * Every deployment (even the J2EE ones) should be seen at some point as 
 * Service Deployment info
 *
 * @see org.jboss.system.Service
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:David.Maplesden@orion.co.nz">David Maplesden</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:daniel.schulze@telkel.com">Daniel Schulze</a>
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @version   $Revision: 1.11 $ <p>
 *
 * <b>20011211 marc fleury:</b>
 * <ul>
 *    <li>initial import based on d-jenck deployement info inner class in DeploymentMBeanSupport   
 * </ul>
 *
 * <b>20011225 marc fleury:</b>
 * <ul>
 *    <li>Unification of deployers and merge with Jung/Schulze's Deployment.java   
 * </ul>
 */
public class DeploymentInfo 
{   
   // Variables ------------------------------------------------------------
   
   public static HashMap deployments = new HashMap();
   
   /** when **/
   public Date date = new Date();
   
   /** the URL identifing this SDI **/
   public URL url;
   
   public URL localUrl;
   
   public URL watch;
   
   public String shortName;
   
   public long lastDeployed = 0;

   public long lastModified = 0; //use for "should we redeploy failed"
   
   // A free form status for the "state" can be Deployed/failed etc etc
   public String status;
   
   //   public List dirs = new ArrayList();
   
   public DeployerMBean deployer;
   
   /** Unified CL is a global scope class loader **/
   public ClassLoader ucl;
   
   /** local Cl is a CL that is used for metadata loading, if ejb-jar.xml is left in the parent CL 
   through old deployments, this makes ensures that we use the local version **/
   public ClassLoader localCl;
   
   /** The classpath declared by this xml descriptor, needs <classpath> entry **/
   final public Collection classpath = new ArrayList();
   
   // The mbeans deployed
   final public List mbeans = new ArrayList();
   
   // Anyone can have subdeployments
   final public Set subDeployments = new HashSet();
   
   // And the subDeployments have a parent
   public DeploymentInfo parent = null;
   
   /** the web root context in case of war file */
   public String webContext;
   
   /** the manifest entry of the deployment (if any)
   *  manifest is not serializable ... is only needed
   *  at deployment time, so we mark it transient
   */
   public Manifest manifest;
   
   // Each Deployment is really mapping one to one to a XML document, here in its parsed form
   public Document document;
   
   // We can hold "typed" metadata, really an interpretation of the bare XML document
   public Object metaData;
   
   public boolean isXML;
   public boolean isDirectory;

   /**
    * The variable <code>deployedObject</code> can contain the MBean that
    * is created through the deployment.  for instance, deploying an ejb-jar
    * results in an EjbModule mbean, which is stored here.
    *
    */
   public ObjectName deployedObject;
   
   public DeploymentInfo(URL url, DeploymentInfo parent)
      throws DeploymentException
   { 
      // The key url the deployment comes from 
      this.url = url;
      
      // this may be changed by deployers in case of directory and xml file following
      this.watch =url;
      
      // Whether we are part of a subdeployment or not
      this.parent = parent;
      
      // Is it a directory?
      if (url.getProtocol().startsWith("file") && new File(url.getFile()).isDirectory()) 
         this.isDirectory = true;
      
      // Does it even exist?
      if (!isDirectory) 
      {
         try { url.openStream().close(); }
            catch (Exception e) {throw new DeploymentException("url "+url+" could not be opened, does it exist?");}
      }
      
      // marcf FIXME FIXME 
      // DO the same for the URL based deployments
      
      if (parent != null) parent.subDeployments.add(this);
         
      // The "short name" for the deployment http://myserver/mybean.ear should yield "mybean.ear"
      shortName = url.getFile();
      
      if (shortName.endsWith("/")) shortName = shortName.substring(0, shortName.length() - 1);
         
      shortName = shortName.substring(shortName.lastIndexOf("/") + 1);
      
      // Do we have an XML descriptor only deployment?
      isXML = shortName.toLowerCase().endsWith("xml");
   
   }
   
   public void createClassLoaders()
   {
      if (isXML)
      {
         // All the class loading will happen from the unified classloader on the context
         localCl = Thread.currentThread().getContextClassLoader();
         ucl = localCl;
      }
      
      else 
      {
         // create a local classloader for local files, don't go with the UCL for ejb-jar.xml 
         localCl = new URLClassLoader(new URL[] {localUrl});
         
         // the classes are passed to a UCL that will share the classes with the whole base 
         ucl = new UnifiedClassLoader(localUrl); 
      }
   }
   
   
   /**
    * getManifest returns (if present) the deployment's manifest
    * it is lazy loaded to work from the localURL
    */
   public Manifest getManifest() 
   {
      try 
      {
         if (manifest == null)
         {
            File file = new File(localUrl.getFile());
            
            if (file.isDirectory()) 
               manifest= new Manifest(new FileInputStream(new File(file, "META-INF/MANIFEST.MF")));
            
            else // a jar
               manifest = new JarFile(file).getManifest();
         
         }
         
         return manifest;
      }
      // It is ok to barf at any time in the above, means no manifest
      catch (Exception ignored) { return null;}
   }
   
   public void cleanup(Logger log)
   {
      if (localUrl == null || localUrl.equals(url)) 
      {
         log.info("not deleting localUrl, it is null or not a copy: " + localUrl);
      } // end of if ()
      else if (recursiveDelete(new File(localUrl.getFile())))
      {
         log.info("Cleaned Deployment "+url);
      }
      else 
      {
         log.info("could not delete directory " + localUrl.toString()+" restart will delete it");
      }
      
      if (!isXML)
         ServiceLibraries.getLibraries().removeClassLoader((UnifiedClassLoader) ucl);
      
      subDeployments.clear();
      mbeans.clear();
   }

   private boolean recursiveDelete(File f)
   {
      if (f.isDirectory())
      {
         File[] files = f.listFiles();
         for (int i = 0; i < files.length; ++i)
         {
            if (!recursiveDelete(files[i]))
            {
               return false;
            }
         }
      }
      return f.delete();
   }

   public int hashCode() 
   {
      return url.hashCode();
   }
   
   public boolean equals(Object other) 
   {
      if (other instanceof DeploymentInfo) 
      {
         return ((DeploymentInfo) other).url.equals(this.url);
      }
      return false;
   }

   public String toString()
   {
      return super.toString() + "{ url=" + url + " }";
   }
}
