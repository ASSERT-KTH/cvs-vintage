/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.keygenerator.hilo;

/**
 * MBean interface.
 */
public interface HiLoKeyGeneratorFactoryMBean extends org.jboss.system.ServiceMBean {

   //default object name
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory.create("jboss.system:service=KeyGeneratorFactory,type=HiLo");

  void setFactoryName(java.lang.String factoryName) ;

  java.lang.String getFactoryName() ;

  void setDataSource(javax.management.ObjectName dataSource) throws java.lang.Exception;

  javax.management.ObjectName getDataSource() ;

  java.lang.String getTableName() ;

  void setTableName(java.lang.String tableName) throws java.lang.Exception;

  java.lang.String getSequenceColumn() ;

  void setSequenceColumn(java.lang.String sequenceColumn) ;

  java.lang.String getSequenceName() ;

  void setSequenceName(java.lang.String sequenceName) ;

  java.lang.String getIdColumnName() ;

  void setIdColumnName(java.lang.String idColumnName) ;

  java.lang.String getCreateTableDdl() ;

  void setCreateTableDdl(java.lang.String createTableDdl) ;

  long getBlockSize() ;

  void setBlockSize(long blockSize) ;

}
