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

import org.jboss.logging.Log;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.3 $
 */
public class ClassPathExtension
   implements ClassPathExtensionMBean, MBeanRegistration
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=ClassPathExtension";
    
   // Attributes ----------------------------------------------------
   String url;
   
   Log log = new Log("Classpath");
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public ClassPathExtension(String url)
   {
      this.url = url;
   }
   
   // Public --------------------------------------------------------
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws java.lang.Exception
   {
      MLet mlet = (MLet)getClass().getClassLoader();
      
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
                  log.log("Added library:"+file);
                  mlet.addURL(file);
                  
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
                  log.log("Added directory:"+u);
               } catch (MalformedURLException e)
               {
                  mlet.addURL(new File(url).toURL());
                  log.log("Added directory:"+url);
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
            log.log("Added library:"+u);
         } catch (MalformedURLException e)
         {
            mlet.addURL(new File(url).toURL());
            log.log("Added library:"+url);
         }
      }
      
      return new ObjectName(OBJECT_NAME+",url="+url);
   }
   
   public void postRegister(java.lang.Boolean registrationDone)
   {
   }
   
   public void preDeregister()
      throws java.lang.Exception
   {
   }
   
   public void postDeregister()
   {
   }
   // Protected -----------------------------------------------------
}

