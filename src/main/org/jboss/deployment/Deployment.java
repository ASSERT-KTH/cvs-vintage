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
*  @version $Revision: 1.2 $
*/
public class Deployment
   implements java.io.Serializable
{
	// the applications name
   protected String name;
   // the url from which this app is downloaded and under which it is deployed 
   //protected URL downloadUrl;
   // the local position of the apps root directory
   protected URL localUrl;
   
   // the Modules for jBoss and Tomcat
   protected Vector ejbModules;
   protected Vector webModules;
   

   /** Creates a new Deployment object.
   */
   Deployment ()
   {
      ejbModules = new Vector ();
      webModules = new Vector ();

   }

   public Module newModule ()
   {
   	return new Module ();
   }
   

   /** the name of the application.
   *   @param the download URL
   *   @return the app context as filename
   */      
   public static String getAppName (URL _url)
   {
    	String s = _url.getFile ();
    	// if it is a jar:<something>!/path/file url
    	if (_url.getProtocol ().equals ("jar"))
    	{
    		int p = s.lastIndexOf ("!");
    		if (p != -1)
       		s = s.substring (0, p);
    	}
    	
    	return s.substring (Math.max (0, Math.min (s.length (), s.lastIndexOf ("/") + 1))); 
   }

   /** returns app name with relative path in case of url is jar url. jar:<something>!/
   *   @param the download URL
   *   @return  wether file:|http:<bla>/<RETURNVALUE> or jar:<something>!/<RETURNVALUE>
   */      
   public static String getFileName (URL _url)
   {
    	String s = _url.getFile ();
 		int p = s.lastIndexOf ("!");
 		if (p != -1)
 			// jar url...
       	s = s.substring (Math.min (p + 2, s.length ()));
    	else
    		s = s.substring (Math.max (0, Math.min (s.length (), s.lastIndexOf ("/") + 1))); 
    	
    	return s;
   }

   /** tries to compose a webcontext for WAR files from the given war file url */
   public static String getWebContext (URL _downloadUrl)
   {
   	String s = getFileName (_downloadUrl);
   	
   	// truncate the file extension
   	int p = s.lastIndexOf (".");
      if (p != -1)
         s = s.substring (0, p);

      return "/" + s.replace ('.', '/');
   }	  

   /** Represents a J2ee module.
   *   the downloadURL (in case of ear something like: jar:applicationURL!/packagename)
   *   the localURL where the downloaded file is placed
   *   the downloadURL for an alternative dd
   *   in case of a web package the optional web root context
   */
   class Module
      implements java.io.Serializable
   {
   	// a short name for the module
   	String name;
   	// the url from which it is downloaded
   	// (in case of ear module like: jar:<app.downloadUrl>!/<module>
   	//URL downloadUrl;
   	// the local url under which it is deployed by the special deployer
   	URL localUrl;
   	// the web root context in case of war file
   	String webContext; 
   }
}
