/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.deployment;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.Iterator;
import java.util.Vector;
import java.util.Date;

import java.util.jar.Manifest;

/** Represents a J2EE application or module (EJB.jar, Web.war or App.ear). <br>
*
*  @author <a href="mailto:daniel.schulze@telkel.com">Daniel Schulze</a>
*  @version $Revision: 1.9 $
*/
public class Deployment
   implements java.io.Serializable
{
	/** the apploications name */
   protected String name;

	/** the date this deployment was made */
	protected Date date;
   
   /** the local position of the apps root directory */
   protected URL localUrl;
   
   /** the position from which this deployment is installed */
   protected URL sourceUrl;

   /** the content of the <code>commonLibs</code> directory as 
   URL Collection */
   protected Vector commonUrls;
   
   /** the EJB Modules */
   protected Vector ejbModules;
   
   /** the WEB Modules */
   protected Vector webModules;

   /** the manifest entry of the deployment (if any)
    *  manifest is not serializable ... is only needed
    *  at deployment time, so we mark it transient 
    *  @author cgjung
    */
   protected transient Manifest manifest;
   
   /** Creates a new Deployment object. */
   protected Deployment ()
   {
	   date = new Date();

      ejbModules = new Vector ();
      webModules = new Vector ();
      
      commonUrls = new Vector ();
   }

   /** returns a new instance of the Module innerclass */
   public Module newModule ()
   {
   	return new Module ();
   }
      
   /** returns the name of this deployment
    */
   public String getName() {
    return name;
   }
   
   /** returns the source url that points to from where this deployment has been
    *  downloaded 
    *  @author cgjung
    */
   public URL getSourceUrl() {
    return sourceUrl;
   }

   /** returns the local url that points to the place where this deployment
    *  has been downloaded 
    *  @author cgjung
    */
   public URL getLocalUrl() {
    return localUrl;
   }

   /** returns the common urls 
    *  @author cgjung
    */
   public Vector getCommonUrls() {
    return commonUrls;
   }

   /** returns the ejbModules 
    *  @author cgjung
    */
   public Vector getEjbModules() {
    return ejbModules;
   }

   /** returns the webModules 
    *  @author cgjung
    */
   public Vector getWebModules() {
    return webModules;
   }

   /** returns the manifest entry of the deployment 
    *  @autor cgjung
    */
   public Manifest getManifest() {
       return manifest;
   }
   
   /** returns all files (URLs) that are needed to run this deployment properly */
	public Vector getAllFiles()
	{
		// the common libs
		Vector result = new Vector();
		Iterator it = commonUrls.iterator();
		while (it.hasNext())
		{
			String s = ((URL)it.next()).getFile();
			result.add(s.substring(s.lastIndexOf("/")+1));
		}
		
		// the ejb packages
	    it = ejbModules.iterator();
		while (it.hasNext())
		{			
			String s = ((URL)((Module)it.next()).localUrls.firstElement()).getFile();
			result.add(s.substring(s.lastIndexOf("/")+1));
		}
			
		// the web packages
	    it = webModules.iterator();
		while (it.hasNext())
		{
			String s = ((URL)((Module)it.next()).localUrls.firstElement()).getFile();
			result.add(s.substring(s.lastIndexOf("/")+1));
		}

		// and the config file
		result.add(J2eeDeployer.CONFIG);

		/*
		it = result.iterator();
		while (it.hasNext())
			System.out.println ("contained file: "+it.next());
		*/

		return result;
	}


   /** Represents a J2ee module. */
   public class Module
      implements java.io.Serializable
   {
   	/** a short name for the module */
   	String name;

      /** a collection of urls that make this module. <br>
      actually there is only one url for the modules jar file or 
      in case of web the modules root directory needed. But to be able 
      to allow alternative descriptors, the base directories of this alternative
      descriptors can be put here before the real module url, so that these where 
      found first */
   	Vector localUrls;
   	
      /** the web root context in case of war file */
   	String webContext;
      
      Module ()
      {
         localUrls = new Vector ();
      }
      
     /** 
       * returns the local urls
       * @author cgjung
       */
      public Vector getLocalUrls() {
        return localUrls;
      }
     
      /** 
       * returns the name
       * @author cgjung
       */
      public String getName() {
        return name;
      }
     
       /** 
       * returns the web context
       * @author cgjung
       */
      public String getWebContext() {
        return webContext;
      }
     
   }
}
