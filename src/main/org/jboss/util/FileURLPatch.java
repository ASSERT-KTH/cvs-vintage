/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

import java.util.Enumeration;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import java.net.URLStreamHandlerFactory;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLConnection;

import org.jboss.logging.Logger;
import org.jboss.util.jmx.ObjectNameFactory;

/** A MBean that patches the file URL handing implementation so that JBoss
 * can be run in directories with a space in it.  Has the weird side-effect that
 * all file based URLs when externalized with have spaces replaced with pluses.
 *      
 *   @author <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>.
 *   @version $Revision: 1.6 $
 */
public class FileURLPatch implements FileURLPatchMBean, MBeanRegistration {

   ObjectName OBJECT_NAME = ObjectNameFactory.create(":service=FileURLPatch");
   Logger log = Logger.getLogger(FileURLPatch.class);
   private CustomURLStreamHandlerFactory customURLStreamHandlerFactory= new CustomURLStreamHandlerFactory();
   private boolean enabled= false;
   private FileHandler fileHander= new FileHandler();

   /** 
    * This class is used to hook into the URL protocol parsing sysytem
    */
   private class CustomURLStreamHandlerFactory implements URLStreamHandlerFactory {
	  public URLStreamHandler createURLStreamHandler(String protocol) {
		 if (protocol.equals("file"))
			return fileHander;
		 return null;
	  }
   }

   /** 
    * This class will override how the file handler is implemented.
	*/
   private class FileHandler extends sun.net.www.protocol.file.Handler {

	  /**
	   * When we externalize the URL we want to make all the spaces in the file name
	   * a '+' character
	   */
	  protected String toExternalForm(URL u) {
		 if (enabled) {
			String s= super.toExternalForm(u);
			return s.replace(' ', '+');
		 }
		 return super.toExternalForm(u);
	  }

	  /**
	   * When we load a URL in we want to convert all the '+' characters in the file name
	   * into spaces.
	   */
	  protected void parseURL(URL u, String spec, int start, int limit) {
		 super.parseURL(u, spec, start, limit);
		 if (enabled) {
		     //			setURL(u, u.getProtocol(), u.getHost(), u.getPort(), u.getFile().replace('+', ' '), u.getRef());
		     setURL(u, u.getProtocol(), u.getHost(), u.getPort(), u.getAuthority(), u.getUserInfo(),
			    u.getPath().replace('+',' '), u.getQuery().replace('+',' '), u.getRef());
		 }
	  }
   }

   /**
    * This patch can be enabled and disabled at runtime by calling this method.
	*/ 
   public void setEnabled(boolean enable) {
	  this.enabled= enable;
	  if (enabled)
		 log.info("The file URL patch has been enabled.");
	  else
		 log.info("The file URL patch has been disabled.");
   }   
   
   public ObjectName preRegister(MBeanServer server, ObjectName name) throws java.lang.Exception {
	  URL.setURLStreamHandlerFactory(customURLStreamHandlerFactory);
	  return OBJECT_NAME;
   }   

   public void postRegister(java.lang.Boolean registrationDone) {
   }   

   public void preDeregister() throws java.lang.Exception {
   }   

   public void postDeregister() {
   }   
   
}
