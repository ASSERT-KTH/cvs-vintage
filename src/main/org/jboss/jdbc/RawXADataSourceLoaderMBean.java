/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jdbc;

import org.jboss.util.ServiceMBean;

public interface RawXADataSourceLoaderMBean extends ServiceMBean
{
   // Constants -----------------------------------------------------
   String OBJECT_NAME = ":service=RawXADataSource";

   // Public --------------------------------------------------------
   void setPoolName(String name);
   String getPoolName();
   void setDataSourceClass(String clazz);
   String getDataSourceClass();
   void setProperties(String properties);
   String getProperties();
   void setLoggingEnabled(boolean enabled);
   boolean getLoggingEnabled();
}
