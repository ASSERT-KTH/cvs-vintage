/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.system;

import java.lang.Object;

/**
 * The management interface for the Info bean.
 *      
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 * @author <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>.
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:marc.fleurY@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.1 $
 */
public interface InfoMBean
{
	
	public static final String OBJECT_NAME= "JBOSS-SYSTEM:service=Info";

   String listMemoryUsage();
   String listSystemInfo();
   String listThreadDump();
   String runGarbageCollector();
   void traceMethodCalls(boolean flag);
   void traceInstructions(boolean flag);
	
	/* getJBossSystemHome
	* return the filesystem directory where JBoss is running on this node
   */
	String getLocalJBossSystemHomeDirectory();
	/*
	* installation URL
	* can be local in a file: or remote with http:
	* tells where the JBOSS-SYSTEM was loaded from
	* By default it is the systemHome.  In case of an http install the system will be the 
	* local file system where the run is and the install will be an http based install with 
	* centralized configuration for the farms
	*/
	String getInstallationURL();
	
	/*
	* configurationDirectory()
	* relative to the installation you can reference different install directories so you 
	* can configure farms from an http base with different profiles.  The webserver will 
	* need the subdirectory at the installationURL
	* ex: a install refering to -n http://www.jboss.org/world -c acme-inc will download 
	* a profile from www.jboss.org that is acme-inc only on the www.jboss.org 
	* we need to have conf/acme-inc under the base directory of JBoss installs
	*/
	String getConfigurationDirectoryURL();
	
	/*
	* Lib directory
	* Where to find basic libraries in this system. 
	* This is an absoluteURL
	*
	*/
	public String getLibraryDirectoryURL();
	
	/** 
	 * Various System getters
	 * 
	 */
	public String getJavaVersion();
	public String getJavaVendor();
	public String getJavaVMName();
	public String getJavaVMVersion();
	public String getJavaVMVendor();
	public String getOSName();
	public String getOSVersion();
	public String getOSArch() ;
	public String getJBossVersion();
	
}
