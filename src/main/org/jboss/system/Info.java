/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.system;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanRegistration;

import org.jboss.logging.Logger;

/**
 * An MBean that provides a rich view of system information for the JBoss
 * server in which it is deployed.
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.10 $
 */
public class Info
   implements InfoMBean, MBeanRegistration
{
   /** Class logger. */
   private static final Logger log = Logger.getLogger(Info.class);
   
   /** The cached host name for the server. */
   private String hostName;
   
   /** The cached host address for the server. */
   private String hostAddress;
   
   
   ///////////////////////////////////////////////////////////////////////////
   //                               JMX Hooks                               //
   ///////////////////////////////////////////////////////////////////////////
   
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws Exception
   {
      // Dump out basic JVM & OS info as INFO priority msgs
      log.info("Java version: " +
      System.getProperty("java.version") + "," +
      System.getProperty("java.vendor"));
      
      log.info("Java VM: " +
      System.getProperty("java.vm.name") + " " +
      System.getProperty("java.vm.version") + "," +
      System.getProperty("java.vm.vendor"));
      
      log.info("OS-System: " +
      System.getProperty("os.name") + " " +
      System.getProperty("os.version") + "," +
      System.getProperty("os.arch"));
      
      // Dump out the entire system properties if debug is enabled
      if (log.isDebugEnabled())
      {
         log.debug("Full System Properties Dump");
         Enumeration names = System.getProperties().propertyNames();
         while (names.hasMoreElements())
         {
            String pname = (String)names.nextElement();
            log.debug("    " + pname + ": " + System.getProperty(pname));
         }
      }
      
      return name == null ? OBJECT_NAME : name;
   }
   
   public void postRegister(Boolean registrationDone)
   {
      // empty
   }
   
   public void preDeregister() throws Exception
   {
      // empty
   }
   
   public void postDeregister()
   {
      // empty
   }
   
   
   ///////////////////////////////////////////////////////////////////////////
   //                            Server Information                         //
   ///////////////////////////////////////////////////////////////////////////
   
   public String getHostName()
   {
      if (hostName == null)
      {
         try
         {
            hostName = java.net.InetAddress.getLocalHost().getHostName();
         }
         catch (java.net.UnknownHostException e)
         {
            log.error("Error looking up local hostname", e);
            hostName = "<unknown>";
         }
      }
      
      return hostName;
   }
   
   public String getHostAddress()
   {
      if (hostAddress == null)
      {
         try
         {
            hostAddress = java.net.InetAddress.getLocalHost().getHostAddress();
         }
         catch (java.net.UnknownHostException e)
         {
            log.error("Error looking up local address", e);
            hostAddress = "<unknown>";
         }
      }
      
      return hostAddress;
   }

   /** Return the total memory and free memory from Runtime
    */
   public String listMemoryUsage()
   {
      String rc= "<P><B>Total Memory: </B>" +
      (Runtime.getRuntime().totalMemory()) +
      " </P>" + "<P><B>Free Memory: </B>" +
      (Runtime.getRuntime().freeMemory()) + " </P>";
      return rc;
   }

   /** Return a listing of the active threads and thread groups.
    */
   public String listThreadDump()
   {
      // Get the root thread group
      ThreadGroup root= Thread.currentThread().getThreadGroup();
      while (root.getParent() != null)
      {
         root = root.getParent();
      }
      
      // I'm not sure why what gets reported is off by +1,
      // but I'm adjusting so that it is consistent with the display
      int activeThreads = root.activeCount()-1;
      // I'm not sure why what gets reported is off by -1
      // but I'm adjusting so that it is consistent with the display
      int activeGroups = root.activeGroupCount()+1;
      
      String rc=
      "<b>Total Threads:</b> "+activeThreads+"<br>"+
      "<b>Total Thread Groups:</b> "+activeGroups+"<br>"+
      getThreadGroupInfo(root) ;
      return rc;
   }

   /** Display the java.lang.Package info for the pkgName  */
   public String displayPackageInfo(String pkgName)
   {
      Package pkg = Package.getPackage(pkgName);
      if( pkg == null )
         return "<h2>Package:"+pkgName+" Not Found!</h2>";

      StringBuffer info = new StringBuffer("<h2>Package: "+pkgName+"</h2>");
      info.append("<pre>\n");
      info.append("SpecificationTitle: "+pkg.getSpecificationTitle());
      info.append("\nSpecificationVersion: "+pkg.getSpecificationVersion());
      info.append("\nSpecificationVendor: "+pkg.getSpecificationVendor());
      info.append("\nImplementationTitle: "+pkg.getImplementationTitle());
      info.append("\nImplementationVersion: "+pkg.getImplementationVersion());
      info.append("\nImplementationVendor: "+pkg.getImplementationVendor());
      info.append("\nisSealed: "+pkg.isSealed());
      info.append("</pre>\n");
      return info.toString();
   }

   /** Return a Map of System.getProperties() with a toString implementation
    *that provides an html table of the key/value pairs
    */
   public Map showProperties()
   {
      return new HashMap(System.getProperties())
      {
         public String toString()
         {
            StringBuffer buff = new StringBuffer();
            buff.append("<table>");
            Iterator iter = keySet().iterator();
            while (iter.hasNext())
            {
               String key = (String)iter.next();
               buff.append("<tr><td align=\"left\">")
               .append(key)
               .append("</td><td align=\"left\">")
               .append(get(key))
               .append("</td></tr>\n\r");
            }
            buff.append("</table>");
            
            return buff.toString();
         }
      };
   }

   private String getThreadGroupInfo(ThreadGroup group)
   {
      StringBuffer rc = new StringBuffer();
      
      rc.append("<BR><B>");
      rc.append("Thread Group: " + group.getName());
      rc.append("</B> : ");
      rc.append("max priority:" + group.getMaxPriority() +
      ", demon:" + group.isDaemon());
      
      rc.append("<blockquote>");
      Thread threads[]= new Thread[group.activeCount()];
      group.enumerate(threads, false);
      for (int i= 0; i < threads.length && threads[i] != null; i++)
      {
         rc.append("<B>");
         rc.append("Thread: " + threads[i].getName());
         rc.append("</B> : ");
         rc.append("priority:" + threads[i].getPriority() +
         ", demon:" + threads[i].isDaemon());
         rc.append("<BR>");
      }
      
      ThreadGroup groups[]= new ThreadGroup[group.activeGroupCount()];
      group.enumerate(groups, false);
      for (int i= 0; i < groups.length && groups[i] != null; i++)
      {
         rc.append(getThreadGroupInfo(groups[i]));
      }
      rc.append("</blockquote>");
      
      return rc.toString();
   }
}
