/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.deployment;


import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jboss.system.Service;
import org.jboss.system.ServiceLibraries;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.URLClassLoader;

import org.jboss.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
* Service Deployment Info .
*
* Every deployment (even the J2EE ones) should be seen at some point as 
* Service Deployment info
*
* @see       org.jboss.system.Service
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @author <a href="mailto:David.Maplesden@orion.co.nz">David Maplesden</a>
* @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
* @version   $Revision: 1.1 $ <p>
*
*      <b>20011211 marc fleury:</b>
*      <ul>
*        <li>initial import based on d-jenck deployement info inner class in DeploymentMBeanSupport   
*      </ul>
*
*/


public class ServiceDeploymentInfo 
{
   
   // Variables ------------------------------------------------------------
   
   //STATE
   public static final int EMPTY = 0;
   public static final int INSTALLED = 1;
   public static final int CLASSESLOADED = 2;
   public static final int MBEANSLOADED = 3;
   public static final int SUSPENDED = 4;
   public static final int GHOST = 5; //undeployed but packages are suspended on us.
   
   // TYPE, mostly un-used for now, revisit when we unify deployment and CL
   public static final int NONE = 999; // Undifined
   public static final int SAR = 1000; // Provides classes
   public static final int JAR = 1001; // Provides classes
   public static final int RAR = 1002; // Provides classes
   public static final int WAR = 1003; // Provides classes
   public static final int DIR = 1004; // Provides classes
   public static final int XML = 1005;
   public static final int EAR = 1006;
   
   /** the URL identifing this SDI **/
   public URL key;
   
   public List dirs = new ArrayList();
   
   public List classUrls = new ArrayList();
   
   public List xmlUrls = new ArrayList();
   
   public URLClassLoader cl;
   
   /** The classpath declared by this xml descriptor, needs <classpath> entry **/
   Collection classpath = new ArrayList();
   
   // The mbeans deployed from this SAR
   List mbeans = new ArrayList();
   
   // The XML document service.xml
   Document dd;
   
   // State
   int state = EMPTY;
   int type =  NONE;
   
   public ServiceDeploymentInfo(URL url)
   {
      this.key = url;
      
      if (url.toString().endsWith("SAR")) type = SAR;
         
      else if(url.toString().endsWith("JAR")) type = JAR;
      else if(url.toString().endsWith("RAR")) type = RAR;
      else if(url.toString().endsWith("WAR")) type = WAR;
      else if(url.toString().endsWith("DIR")) type = DIR;
      else if(url.toString().endsWith("XML")) type = XML;
      else if(url.toString().endsWith("EAR")) type = EAR;
   }
   
   public void addDir(File dir) {dirs.add(dir);}
   
   public void addClassUrl(URL url){classUrls.add(url);}
   public List getClassUrls(){ return classUrls;}
   
   public void addXmlUrl(URL url){xmlUrls.add(url);}
   public List getXmlUrls(){return xmlUrls;}
   
   
   
   public URLClassLoader createClassLoader()
   {
      URL[] urlArray = (URL[])classUrls.toArray(new URL[classUrls.size()]);
      cl = new URLClassLoader(urlArray, key);
      return cl;
   }
   
   public URLClassLoader removeClassLoader()
   {
      URLClassLoader localcl = cl;
      cl = null;
      return localcl;
   }
   
   public void cleanup(Logger log)
   {
      classUrls.clear();
      xmlUrls.clear();
      for (Iterator i = dirs.iterator(); i.hasNext(); )
      {
         File dir = (File)i.next();
         if (!recursiveDelete(dir))
         {
            log.info("could not delete directory " + dir + ". Will be removed on server shutdown or restart");
         } // end of if ()
         ;
      } // end of for ()
      dirs.clear();
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

}


