/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

/**
 * MBean interface.
 */
public interface MetaDataLibraryMBean extends org.jboss.system.ServiceMBean {

   //default object name
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory.create("jboss.jdbc:service=metadata");

  org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCTypeMappingMetaData findTypeMappingMetaData(java.lang.String name) ;

  java.util.Set getTypeMappingNames() ;

}
