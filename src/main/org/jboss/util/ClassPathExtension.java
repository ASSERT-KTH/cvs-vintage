/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

import java.io.*;
import java.net.*;

import javax.management.*;
import javax.management.loading.MLet;

import org.jboss.logging.Logger;
import org.jboss.util.ServiceMBeanSupport;

/**
 *   Add URL's to the MLet classloader
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.7 $
 */
public class ClassPathExtension
   extends ServiceMBeanSupport
   implements ClassPathExtensionMBean
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=ClassPathExtension";
    
   // Attributes ----------------------------------------------------
   String url;
   String name;
   
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
   
   // Public --------------------------------------------------------
   public ObjectName getObjectName(MBeanServer server, ObjectName ojbName)
      throws javax.management.MalformedObjectNameException
   {
      return ojbName == null ? new ObjectName(OBJECT_NAME+",name="+this.name) : ojbName;
   }
   
   public String getName()
   {
      return "Classpath extension";
   }
   
   public void initService()
      throws java.lang.Exception
   {
      String separator = System.getProperty("path.separator");
      String classPath = System.getProperty("java.class.path");
   
      MLet mlet = (MLet)Thread.currentThread().getContextClassLoader();
      
      if (url.endsWith("/"))
      {
         // Add all libs in directory
         File dir;
         try
         {
            URL u = new URL(getClass().getProtectionDomain().getCodeSource().getLocation(),url);
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
                  Logger.debug("Added library:"+file);
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
                  URL u = new URL(getClass().getProtectionDomain().getCodeSource().getLocation(),url);
                  mlet.addURL(u);
                  
                  // Add to java.class.path
                  classPath += separator + u.getFile();
                  
                  Logger.debug("Added directory:"+u);
               } catch (MalformedURLException e)
               {
                  URL u = new File(url).toURL();
                  mlet.addURL(u);
                  
                  // Add to java.class.path
                  classPath += separator + u.getFile();
                  
                  Logger.debug("Added directory:"+url);
               }
            }
         } catch (Throwable ex)
         {
            ex.printStackTrace();
         }
      } else
      {
         try
         {
            URL u = new URL(getClass().getProtectionDomain().getCodeSource().getLocation(),url);
            mlet.addURL(u);
            
            // Add to java.class.path
            classPath += separator + u.getFile();
            
            Logger.debug("Added library:"+u);
         } catch (MalformedURLException e)
         {
            URL u = new File(url).toURL();
            mlet.addURL(u);
            
            // Add to java.class.path
            classPath += separator + u.getFile();
            
            Logger.debug("Added library:"+url);
         }
      }
      
      // Set java.class.path
      System.setProperty("java.class.path", classPath);
   }
   // Protected -----------------------------------------------------
}

