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

import org.apache.log4j.Category;

/** A simple mbean that dumps out info like the system properties, etc.
 *      
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>.
 *   @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
 *   @author <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>.
 *   @version $Revision: 1.8 $
 */
public class Info implements InfoMBean, MBeanRegistration {
	// Constants -----------------------------------------------------
	public static final String OBJECT_NAME= ":service=Info";

	// Attributes ----------------------------------------------------
	Category log= Category.getInstance(Info.class);

	// Static --------------------------------------------------------

	// Constructors --------------------------------------------------

	// Public --------------------------------------------------------
	public ObjectName preRegister(MBeanServer server, ObjectName name) throws java.lang.Exception {
		// Dump out basic info as INFO priority msgs
		log.info("Java version: " + System.getProperty("java.version") + "," + System.getProperty("java.vendor"));
		log.info("Java VM: " + System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version") + "," + System.getProperty("java.vm.vendor"));
		log.info("System: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + "," + System.getProperty("os.arch"));
		// Now dump out the entire System properties as DEBUG priority msgs
		log.debug("+++ Full System Properties Dump");
		Enumeration names= System.getProperties().propertyNames();
		while (names.hasMoreElements()) {
			String pname= (String) names.nextElement();
			log.debug(pname + ": " + System.getProperty(pname));
		}

		// MF TODO: say everything that needs to be said here: copyright, included libs and TM, contributor and (C) jboss org 2000
		return new ObjectName(OBJECT_NAME);
	}

	public void postRegister(java.lang.Boolean registrationDone) {
	}

	public void preDeregister() throws java.lang.Exception {
	}

	public void postDeregister() {
	}

	public String getThreadGroupInfo(ThreadGroup group) {

		StringBuffer rc= new StringBuffer();

		rc.append("<B>");
		rc.append("Thread Group: " + group.getName());
		rc.append("</B> : ");
		rc.append("max priority:" + group.getMaxPriority() + ", demon:" + group.isDaemon());

		rc.append("<blockquote>");
		Thread threads[]= new Thread[group.activeCount()];
		group.enumerate(threads, false);
		for (int i= 0; i < threads.length && threads[i] != null; i++) {
			rc.append("<B>");
			rc.append("Thread: " + threads[i].getName());
			rc.append("</B> : ");
			rc.append("priority:" + threads[i].getPriority() + ", demon:" + threads[i].isDaemon());
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

	public String listMemoryUsage() {

		String rc= "<P><B>Total Memory: </B>" + (Runtime.getRuntime().totalMemory()) + " </P>" + "<P><B>Free Memory: </B>" + (Runtime.getRuntime().freeMemory()) + " </P>";

		return rc;
	}

	public String listSystemInfo() {

		// Dump out basic info as INFO priority msgs
		StringBuffer rc= new StringBuffer();
		rc.append("<pre>");
		rc.append("Java version: " + System.getProperty("java.version") + "," + System.getProperty("java.vendor"));
		rc.append("\n");
		rc.append("Java VM: " + System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version") + "," + System.getProperty("java.vm.vendor"));
		rc.append("\n");
		rc.append("System: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + "," + System.getProperty("os.arch"));
		rc.append("\n");
		rc.append("</pre>");

		// HRC: Should we also do a full system properties dump??

		return rc.toString();
	}

	public String listThreadDump() {

		// Get the root thread group
		ThreadGroup root= Thread.currentThread().getThreadGroup();
		while (root.getParent() != null) {
			root= root.getParent();
		}

		String rc= getThreadGroupInfo(root);
		return rc;
	}
}