/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.mail;

import javax.management.ObjectName;
import org.jboss.util.ObjectNameFactory;

/**
 * MBean interface for the mail service.
 * 
 * @author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 * @version $Revision: 1.6 $
 */
public interface MailServiceMBean
   extends org.jboss.system.ServiceMBean
{
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss:service=Mail");
	
   /**
    * User id used to connect to a mail server
    * 
    * @see #setPassword
    */
   void setUser(String user);
   
   /**
    * Password used to connect to a mail server
    * 
    * @see #setUser
    */
   void setPassword(String password);
   
   /**
    * File name of the configuration mail file used by JavaMail to send mail.
    * This file normally reside in the configuration directory of JBoss, and
    * contains name-value pairs (such as "mail.transport.protocol = smtp") as
    * specified in the JavaMail specification.
    */
   void setConfigurationFile(String file);
   
   /**
    * The JNDI name under the java:/ namespace to which javax.mail.Session objects are
    * bound.
    */
   void setJNDIName(String name);
}
