/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins.keygenerator.hilo;

import org.jboss.ejb.plugins.keygenerator.KeyGeneratorFactory;
import org.jboss.ejb.plugins.keygenerator.KeyGenerator;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.naming.Util;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.Serializable;

/**
 * This is the factory for HiLo key generator
 *
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 *
 * @version $Revision: 1.1 $
 */
public class HiLoKeyGeneratorFactory
   extends ServiceMBeanSupport
   implements KeyGeneratorFactory, HiLoKeyGeneratorFactoryMBean, Serializable
{
   // Attributes ---------------------------------------------------
   /** my JNDI name */
   private String factoryName;

   /** table name */
   private String tableName;

   /** column name */
   private String columnName;

   /** column name */
   private String columnSQLType;

   /** datasource JNDI name*/
   private String dataSource;

   /** transaction manager JNDI name */
   private String transactionManager;

   /** block size */
   private int blockSize;

   // HiLoKeyGeneratorFactoryMBean implementation ------------------
   /**
    * @jmx.managed-attribute
    * @return factory's JNDI name
    */
   public String getFactoryName()
   {
      return factoryName;
   }

   /**
    * @jmx.managed-attribute
    * @param factoryName  factory's JNDI name.
    */
   public void setFactoryName(String factoryName)
   {
      this.factoryName = factoryName;
   }

   /**
    * @jmx.managed-attribute
    * @return table name
    */
   public String getTableName()
   {
      return tableName;
   }

   /**
    * @jmx.managed-attribute
    * @param tableName  Table name
    */
   public void setTableName(String tableName)
   {
      this.tableName = tableName;
   }

   /**
    * @jmx.managed-attribute
    * @return column name
    */
   public String getColumnName()
   {
      return columnName;
   }

   /**
    * @jmx.managed-attribute
    * @param columnName  column name.
    */
   public void setColumnName(String columnName)
   {
      this.columnName = columnName;
   }

   /**
    * @jmx.managed-attribute
    * @return column SQL type.
    */
   public String getColumnSQLType()
   {
      return columnSQLType;
   }

   /**
    * @jmx.managed-attribute
    * @param columnSQLType  Column SQL type.
    */
   public void setColumnSQLType(String columnSQLType)
   {
      this.columnSQLType = columnSQLType;
   }

   /**
    * @jmx.managed-attribute
    * @return datasource JNDI name.
    */
   public String getDataSource()
   {
      return dataSource;
   }

   /**
    * @jmx.managed-attribute
    * @param dataSource  datasource JNDI name.
    */
   public void setDataSource(String dataSource)
   {
      this.dataSource = dataSource;
   }

   /**
    * @jmx.managed-attribute
    * @return transaction manager JNDI name.
    */
   public String getTransactionManager()
   {
      return transactionManager;
   }

   /**
    * @jmx.managed-attribute
    * @param transactionManager  Transaction manager JNDI name.
    */
   public void setTransactionManager(String transactionManager)
   {
      this.transactionManager = transactionManager;
   }

   /**
    * @jmx.managed-attribute
    * @return block size.
    */
   public int getBlockSize()
   {
      return blockSize;
   }

   /**
    * @jmx.managed-attribute
    * @param blockSize  Block size.
    */
   public void setBlockSize(int blockSize)
   {
      this.blockSize = blockSize;
   }

   // KeyGeneratorFactory implementation ---------------------------
   /**
    * Returns a newly constructed key generator
    */
   public KeyGenerator getKeyGenerator()
      throws Exception
   {
      return new HiLoKeyGenerator(
         dataSource, transactionManager, tableName, columnName, columnSQLType, blockSize
      );
   }

   // ServiceMBeanSupport overridding ------------------------------
   public void startService()
   {
      // bind the factory
      try
      {
         Context ctx = new InitialContext();
         Util.rebind(ctx, factoryName, this);
      }
      catch(Exception e)
      {
         log.error("Caught exception during startService()", e);
      }
   }

   public void stopService()
   {
      // unbind the factory
      try
      {
         Context ctx = new InitialContext();
         Util.unbind(ctx, factoryName);
      }
      catch(Exception e)
      {
         log.error("Caught exception during stopService()", e);
      }
   }
}
