/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.system;

import java.util.Enumeration;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanRegistration;

import org.jboss.logging.Logger;

/**
 * A simple mbean that dumps out info like the system properties, etc.
 *      
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.6 $
 */
public class Info
   implements InfoMBean, MBeanRegistration
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /** Class logger. */
   private static Logger log = Logger.getLogger("org.jboss.system.GPA");

   //
   // System information
   //
   
   protected String javaVersion;
   protected String javaVendor;
   protected String javaVMName;
   protected String javaVMVersion;
   protected String libraryDirectory;
   protected String javaVMVendor;
   protected String osName;
   protected String osVersion;
   protected String osArch;
   protected String jbossLocalHomeDirectory;
   protected String installationURL;
   protected String configurationDirectory;
   protected String patchDirectory;
   protected String jbossVersion;

   /** When this instance was started. */
   protected String jbossStarted;

   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
	
   // Public --------------------------------------------------------
	
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws Exception
   {
      // VM stuff
      javaVersion = System.getProperty("java.version");
      javaVendor = System.getProperty("java.vendor");
      javaVMName = System.getProperty("java.vm.name");
      javaVMVersion = System.getProperty("java.vm.version");
      javaVMVendor =  System.getProperty("java.vm.vendor");
		
      // OS stuff
      osName = System.getProperty("os.name");
      osVersion = System.getProperty("os.version");
      osArch = System.getProperty("os.arch");
		
      // JBoss stuff
      jbossVersion = System.getProperty("jboss.system.version");
      jbossStarted = System.getProperty("jboss.system.started");
      jbossLocalHomeDirectory = System.getProperty("jboss.system.home");
      installationURL = System.getProperty("jboss.system.installationURL");
      configurationDirectory = System.getProperty("jboss.system.configurationDirectory");
      libraryDirectory = System.getProperty("jboss.system.libraryDirectory");
      patchDirectory = System.getProperty("jboss.system.patchDirectory");
		
      log.info("General Purpose Architecture (GPA)");
		
      // Dump out basic info as INFO priority msgs
      log.info("Java version: " +
              javaVersion + "," +
              javaVendor);
      
      log.info("Java VM: " +
              javaVMName + " " +
              javaVMVersion + "," +
              javaVMVendor);
      
      log.info("OS-System: " +
              osName + " " +
              osVersion + "," +
              osArch);
      
      log.info("JBoss Version: " + jbossVersion);
      log.info("JBoss start time: " + jbossStarted);
      log.info("Local Home Dir: " + jbossLocalHomeDirectory);
      log.info("Installation URL: " + installationURL);
      log.info("Configuration Dir: " + configurationDirectory);
      log.info("Library Dir: " + libraryDirectory);
      log.info("Local Patch Directory: " + patchDirectory);
      log.info("Oh, and remember we love you");
		
      // dump out the entire system properties if debug is enabled
      if (log.isDebugEnabled()) {
         log.debug("+++ Full System Properties Dump");
         Enumeration names = System.getProperties().propertyNames();
         while (names.hasMoreElements())
         {
            String pname = (String)names.nextElement();
            log.debug(pname + ": " + System.getProperty(pname));
         }
      }
		
      return new ObjectName(OBJECT_NAME);
   }
	
   public void postRegister(Boolean registrationDone) {
      // empty
   }
	
   public void preDeregister() throws Exception {
      // empty
   }
	
   public void postDeregister() {
      // empty
   }
	
   public String getJavaVersion() { return javaVersion; } 
   public String getJavaVendor() { return javaVendor; }
   public String getJavaVMName() { return javaVMName; }
   public String getJavaVMVersion() { return javaVMVersion; }
   public String getJavaVMVendor() { return javaVMVendor; }
   public String getOSName() { return osName; }
   public String getOSVersion() { return osVersion; }
   public String getOSArch() { return osArch; }
   public String getLocalJBossSystemHomeDirectory() { return jbossLocalHomeDirectory; }
   public String getLocalInstallationURL() { return installationURL; }
   public String getConfigurationDirectoryURL() { return configurationDirectory; }
   public String getLocalPatchDirectory() { return patchDirectory; }
   public String getJBossVersion() { return jbossVersion; }
   public String getLibraryDirectoryURL() { return libraryDirectory; }
   public String getInstallationURL() { return installationURL; }

   /**
    * Return the time which this server instance was started.
    */
   public String getStartTime() {
      return jbossStarted;
   }
   
   public String getThreadGroupInfo(ThreadGroup group) {
      StringBuffer rc = new StringBuffer();
		
      rc.append("<BR><B>");
      rc.append("Thread Group: " + group.getName());
      rc.append("</B> : ");
      rc.append("max priority:" + group.getMaxPriority() +
                ", demon:" + group.isDaemon());
		
      rc.append("<blockquote>");
      Thread threads[]= new Thread[group.activeCount()];
      group.enumerate(threads, false);
      for (int i= 0; i < threads.length && threads[i] != null; i++) {
         rc.append("<B>");
         rc.append("Thread: " + threads[i].getName());
         rc.append("</B> : ");
         rc.append("priority:" + threads[i].getPriority() +
                   ", demon:" + threads[i].isDaemon());
         rc.append("<BR>");
      }
		
      ThreadGroup groups[]= new ThreadGroup[group.activeGroupCount()];
      group.enumerate(groups, false);
      for (int i= 0; i < groups.length && groups[i] != null; i++) {
         rc.append(getThreadGroupInfo(groups[i]));
      }
      rc.append("</blockquote>");
      return rc.toString();
   }
	
   public String runGarbageCollector() {
      StringBuffer buff = new StringBuffer();
      buff.append("<h3>Before</h3>");
      buff.append(listMemoryUsage());
      buff.append("<h3>After</h3>");
		
      log.debug("hinting the VM to run the garbage collector");
      System.gc();
		
      buff.append(listMemoryUsage());
      return buff.toString();
   }
	
   public String listMemoryUsage() {
      String rc= "<P><B>Total Memory: </B>" +
         (Runtime.getRuntime().totalMemory()) +
         " </P>" + "<P><B>Free Memory: </B>" +
         (Runtime.getRuntime().freeMemory()) + " </P>";
      return rc;
   }
	
   public String listSystemInfo() {
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
	
   public String listThreadDump() {
      // Get the root thread group
      ThreadGroup root= Thread.currentThread().getThreadGroup();
      while (root.getParent() != null) {
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
	
   /**
    * Enable or disable tracing method calls at the Runtime level.
    */
   public void traceMethodCalls(final boolean flag) {
      Runtime.getRuntime().traceMethodCalls(flag);
   }
	
   /**
    * Enable or disable tracing instructions the Runtime level.
    */
   public void traceInstructions(final boolean flag) {
      Runtime.getRuntime().traceInstructions(flag);
   }
}
