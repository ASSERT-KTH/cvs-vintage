/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins.keygenerator.hilo;

import org.jboss.system.ServiceMBean;


/**
 * MBean interface for HiLoKeyGeneratorFactory
 *
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 *
 * @version $Revision: 1.1 $
 */
public interface HiLoKeyGeneratorFactoryMBean
   extends ServiceMBean
{
   // Public ------------------------------------------
   /**
    * @return factory's JNDI name
    */
   public String getFactoryName();

   /**
    * @param factoryJNDI  factory's JNDI name.
    */
   public void setFactoryName(String factoryJNDI);

   /**
    * @return table name
    */
   public String getTableName();

   /**
    * @param tableName  Table name
    */
   public void setTableName(String tableName);

   /**
    * @return column name
    */
   public String getColumnName();

   /**
    * @param columnName  column name.
    */
   public void setColumnName(String columnName);

   /**
    * @return column sql type
    */
   public String getColumnSQLType();

   /**
    * @param columnSQLType  column sql type.
    */
   public void setColumnSQLType(String columnSQLType);

   /**
    * @return datasource JNDI name.
    */
   public String getDataSource();

   /**
    * @param dataSource  datasource JNDI name.
    */
   public void setDataSource(String dataSource);

   /**
    * @return transaction manager JNDI name.
    */
   public String getTransactionManager();

   /**
    * @param transactionManager  Transaction manager JNDI name.
    */
   public void setTransactionManager(String transactionManager);

   /**
    * @return block size
    */
   public int getBlockSize();

   /**
    * @param blockSize  Block size.
    */
   public void setBlockSize(int blockSize);
}
