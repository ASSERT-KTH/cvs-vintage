/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.deployment;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.Iterator;
import java.util.Vector;

/** Represents a J2EE application or module (EJB.jar, Web.war or App.ear). <br>
*
*  @author <a href="mailto:daniel.schulze@telkel.com">Daniel Schulze</a>
*  @version $Revision: 1.4 $
*/
public class Deployment
   implements java.io.Serializable
{
	/** the apploications name */
   protected String name;
   
   /** the local position of the apps root directory */
   protected URL localUrl;
   
   /** the content of the <code>commonLibs</code> directory as 
   URL Collection */
   protected Vector commonUrls;
   
   /** the EJB Modules */
   protected Vector ejbModules;
   
   /** the WEB Modules */
   protected Vector webModules;

   /** Creates a new Deployment object.  */
   Deployment ()
   {
      ejbModules = new Vector ();
      webModules = new Vector ();
      
      commonUrls = new Vector ();
   }

   /** returns a new instance of the Module innerclass */
   public Module newModule ()
   {
   	return new Module ();
   }
   

   /** Represents a J2ee module. */
   class Module
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
   }
}
