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
public interface DataSourceMetaDataMBean extends org.jboss.system.ServiceMBean {

  java.lang.String getTypeMapping() ;

  void setTypeMapping(java.lang.String typeMapping) ;

  javax.management.ObjectName getMetadataLibrary() ;

  void setMetadataLibrary(javax.management.ObjectName metadataLibrary) ;

  org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCTypeMappingMetaData getTypeMappingMetaData() throws java.lang.Exception;

}
