/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

import java.io.*;
import java.net.*;

import javax.management.*;
import javax.management.loading.MLet;

import org.jboss.logging.Log;
import org.jboss.util.ServiceMBeanSupport;

/**
 *   Add URL's to the MLet classloader
 *      
 *   @see <related>
 *   @author Rickard �berg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.10 $
 */
public class ClassPathExtension
   implements ClassPathExtensionMBean, MBeanRegistration
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=ClassPathExtension";
    
   // Attributes ----------------------------------------------------
   String url;
   String name;
   
   Log log = Log.createLog("Classpath extension");
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public ClassPathExtension(String url)
   {
      this(url, url);
   }
   
   public ClassPathExtension(String url, String name)
   {
      this.name = name;
      this.url = url;
   }
   
   // MBeanRegistration implementation ------------------------------
   public ObjectName preRegister(MBeanServer server,
                              ObjectName objName)
                       throws java.lang.Exception
   {
      return objName == null ? new ObjectName(OBJECT_NAME+",name="+this.name) : objName;
   }
   
   public void postRegister(java.lang.Boolean registrationDone)
   {
      String separator = System.getProperty("path.separator");
      String classPath = System.getProperty("java.class.path");
   
      MLet mlet = (MLet)Thread.currentThread().getContextClassLoader();
      
      URL u = null;
      
      if (url.endsWith("/"))
      {
         // Add all libs in directory
         File dir;
         try
         {
//            URL u = new URL(getClass().getProtectionDomain().getCodeSource().getLocation(),url);
			u = new URL(getClass().getProtectionDomain().getCodeSource().getLocation(),url);
            dir = new File(u.getFile());
         } catch (MalformedURLException e)
         {
            dir = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile(), url);
         }
      
         try
         {
            String[] files = dir.list();
            int found = 0;
            for (int i = 0; i < files.length; i++)
            {
               if (files[i].endsWith(".jar") || files[i].endsWith(".zip"))
               {
                  URL file = new File(dir, files[i]).getCanonicalFile().toURL();
                  log.debug("Added library:"+file);
                  mlet.addURL(file);
               
                  // Add to java.class.path
                  classPath += separator + file.getFile();
               
                  found++;
               }
            }
         
            if (found == 0)
            {
               // Add dir
               try
               {
//                  URL u = new URL(getClass().getProtectionDomain().getCodeSource().getLocation(),url);
                  u = new URL(getClass().getProtectionDomain().getCodeSource().getLocation(),url);
                  mlet.addURL(u);
               
                  // Add to java.class.path
                  classPath += separator + u.getFile();
               
                  log.debug("Added directory:"+u);
               } catch (MalformedURLException e)
               {
//                  URL u = new File(url).toURL();
                  u = new File(url).toURL();
                  mlet.addURL(u);
               
                  // Add to java.class.path
                  classPath += separator + u.getFile();
               
                  log.debug("Added directory:"+url);
               }
            }
         } catch (Throwable ex)
         {
         	log.warning("Classpath extension "+u+" is invalid.");
            ex.printStackTrace();
         }
      } else
      {
         try
         {
//            URL u = new URL(getClass().getProtectionDomain().getCodeSource().getLocation(),url);
            u = new URL(getClass().getProtectionDomain().getCodeSource().getLocation(),url);
            mlet.addURL(u);
         
            // Add to java.class.path
            classPath += separator + u.getFile();
         
            log.debug("Added library:"+u);
         } catch (MalformedURLException e)
         {
            try
            {
//               URL u = new File(url).toURL();
               u = new File(url).toURL();
               mlet.addURL(u);
         
               // Add to java.class.path
               classPath += separator + u.getFile();
         
               log.debug("Added library:"+url);
            } catch (MalformedURLException ex)
            {
               log.warning("Classpath extension "+u+" is invalid.");
               log.exception(ex);
            }
         }
      }
   
      // Set java.class.path
      System.setProperty("java.class.path", classPath);
   }
   
   public void preDeregister()
      throws java.lang.Exception
   {
      
   }
   
   public void postDeregister()
   {
      
   }
}

