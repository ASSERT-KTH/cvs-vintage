/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.resource;

import org.jboss.util.ServiceMBean;

/**
 *   Binds a <code>ConnectionManagerFactory</code> instance into JNDI
 *   so that <code>ConnectionFactoryLoader</code>s can get at it.
 *
 *   @see 
 *   @author Toby Allsopp (toby.allsopp@peace.com)
 *   @version $Revision: 1.1 $
 */
public interface ConnectionManagerFactoryLoaderMBean
   extends ServiceMBean
{
   // Constants -----------------------------------------------------

   String OBJECT_NAME = "JCA:service=ConnectionManagerFactoryLoader";

   // Public --------------------------------------------------------

   String getFactoryName();
   void setFactoryName(String name);

   String getProperties();
   void setProperties(String p);

   String getTransactionManagerName();
   void setTransactionManagerName(String n);

   String getFactoryClass();
   void setFactoryClass(String c);
}
