/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

import java.util.Enumeration;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Category;

/**
 * A simple mbean that dumps out info like the system properties, etc.
 *      
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
 * @author <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>.
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.15 $
 */
public class Info
   implements InfoMBean, MBeanRegistration
{
   // Constants -----------------------------------------------------

   public static final String OBJECT_NAME= ":service=Info";

   // Attributes ----------------------------------------------------

   /** Instance logger. */
   private Category log = Category.getInstance(this.getClass());

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws Exception
   {
      // Dump out basic info as INFO priority msgs
      log.info("Java version: " +
               System.getProperty("java.version") + "," +
               System.getProperty("java.vendor"));
      log.info("Java VM: " +
               System.getProperty("java.vm.name") + " " +
               System.getProperty("java.vm.version") + "," +
               System.getProperty("java.vm.vendor"));
      log.info("System: " +
               System.getProperty("os.name") + " " +
               System.getProperty("os.version") + "," +
               System.getProperty("os.arch"));

      // dump out the entire system properties if debug is enabled
      if (log.isDebugEnabled())
      {
         log.debug("+++ Full System Properties Dump");
         Enumeration names= System.getProperties().propertyNames();
         while (names.hasMoreElements())
         {
            String pname= (String) names.nextElement();
            log.debug(pname + ": " + System.getProperty(pname));
         }
      }
                
      // MF TODO: say everything that needs to be said here:
      // copyright, included libs and TM, contributor and (C) jboss org 2000
      name = name == null ? new ObjectName(OBJECT_NAME) : name;
      return name;
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

   public String getThreadGroupInfo(ThreadGroup group)
   {
      StringBuffer rc= new StringBuffer();

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

   public String runGarbageCollector()
   {
      StringBuffer buff = new StringBuffer();
      buff.append("<h3>Before</h3>");
      buff.append(listMemoryUsage());
      buff.append("<h3>After</h3>");

      log.info("hinting the VM to run the garbage collector");
      System.gc();
      
      buff.append(listMemoryUsage());
      return buff.toString();
   }
   
   public String listMemoryUsage()
   {
      String rc= "<P><B>Total Memory: </B>" +
         (Runtime.getRuntime().totalMemory()) +
         " </P>" + "<P><B>Free Memory: </B>" +
         (Runtime.getRuntime().freeMemory()) + " </P>";
      return rc;
   }

   public String listSystemInfo()
   {
      // Dump out basic info as INFO priority msgs
      StringBuffer rc= new StringBuffer();
      rc.append("<pre>");
      rc.append("Java version: " +
                System.getProperty("java.version") + "," +
                System.getProperty("java.vendor"));
      rc.append("\n");
      rc.append("Java VM: " +
                System.getProperty("java.vm.name") + " " +
                System.getProperty("java.vm.version") + "," +
                System.getProperty("java.vm.vendor"));
      rc.append("\n");
      rc.append("System: " +
                System.getProperty("os.name") + " " +
                System.getProperty("os.version") + "," +
                System.getProperty("os.arch"));
      rc.append("\n");
      rc.append("</pre>");

      // HRC: Should we also do a full system properties dump??

      return rc.toString();
   }

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
         return "<h2>Package: "+pkgName+" Not Found!</h2>\n";

      StringBuffer info = new StringBuffer("<h2>Package: "+pkgName+"</h2>\n");
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

   /**
    * Enable or disable tracing method calls at the Runtime level.
    */
   public void traceMethodCalls(final boolean flag)
   {
      Runtime.getRuntime().traceMethodCalls(flag);
   }
   
   /**
    * Enable or disable tracing instructions the Runtime level.
    */
   public void traceInstructions(final boolean flag)
   {
      Runtime.getRuntime().traceInstructions(flag);
   }
}
