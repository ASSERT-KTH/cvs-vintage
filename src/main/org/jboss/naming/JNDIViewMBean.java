/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.naming;

import java.io.IOException;
import javax.naming.NamingException;

import javax.management.ObjectName;

import org.jboss.util.jmx.ObjectNameFactory;

/**
 * The JMX management interface for the {@link JNDIView} MBean.
 * 
 * @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
 * @version $Revision: 1.10 $
 */
public interface JNDIViewMBean
   extends org.jboss.system.ServiceMBean
{
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss:service=JNDIView");
    
   /**
    * List the JBoss JNDI namespace.
    * @param verbose, 
    */
   String list(boolean verbose);
   
   /**
    * List the JBoss JNDI namespace in XML Format
    */
   String listXML();
}
