/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.mail;

/**
 * MBean interface for the mail service.
 * 
 * @see
 * @author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 * @version $Revision: 1.5 $
 */
public interface MailServiceMBean
	extends org.jboss.system.ServiceMBean
{
	// Constants -----------------------------------------------------
	public static final String OBJECT_NAME = "jboss:service=Mail";
	
	// Public --------------------------------------------------------
	/**
	 * User id used to connect to a mail server
	 * @see #setPassword
	 */
	public void setUser(String user);
	/**
	 * Password used to connect to a mail server
	 * @see #setUser
	 */
	public void setPassword(String password);
	/**
	 * File name of the configuration mail file used by JavaMail to send mail.
	 * This file normally reside in the configuration directory of JBoss, and
	 * contains name-value pairs (such as "mail.transport.protocol = smtp") as
	 * specified in the JavaMail specification.
	 */
	public void setConfigurationFile(String file);
	/**
	 * The JNDI name under the java:/ namespace to which javax.mail.Session objects are
	 * bound.
	 */
	public void setJNDIName(String name);
}
