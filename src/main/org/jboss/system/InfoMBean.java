/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.system;

import java.util.Map;

import javax.management.ObjectName;

import org.jboss.util.jmx.ObjectNameFactory;

/**
 * The management interface for the Info bean.
 *      
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:marc.fleurY@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.7 $
 */
public interface InfoMBean
{
   /** The default JMX object name for this MBean. */
   ObjectName OBJECT_NAME =
      ObjectNameFactory.create("jboss.system", "service", "Info");

   /**
    * InetAddress.getLocalHost().getHostName();
    */
   String getHostName();
   
   /**
    * Returns InetAddress.getLocalHost().getHostAddress();
    */
   String getHostAddress();
   
   /**
    * Return the total memory and free memory from Runtime
    */
   String listMemoryUsage();
   
   /**
    * Return a listing of the active threads and thread groups.
    */
   String listThreadDump();
   
   /**
    * Display the java.lang.Package info for the pkgName
    */
   String displayPackageInfo(String pkgName);
   
   /**
    * Return a Map of System.getProperties() with a toString implementation
    * that provides an html table of the key/value pairs
    */
   Map showProperties();   
}
