/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.keygenerator.hilo;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.ejb.plugins.keygenerator.KeyGeneratorFactory;
import org.jboss.ejb.plugins.keygenerator.KeyGenerator;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;
import org.jboss.ejb.plugins.cmp.jdbc.SQLUtil;
import org.jboss.naming.Util;
import org.jboss.deployment.DeploymentException;

import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.5 $</tt>
 * @jmx.mbean name="jboss.system:service=KeyGeneratorFactory,type=HiLo"
 * extends="org.jboss.system.ServiceMBean"
 */
public class HiLoKeyGeneratorFactory
   extends ServiceMBeanSupport
   implements KeyGeneratorFactory, HiLoKeyGeneratorFactoryMBean, Serializable
{
   private ObjectName dataSource;
   private DataSource ds;

   private String jndiName;
   private String tableName;
   private String sequenceColumn;
   private String sequenceName;
   private String idColumnName;
   private String createTableDdl;
   private long blockSize;

   private TransactionManager tm;

   /**
    * @jmx.managed-attribute
    */
   public void setFactoryName(String factoryName)
   {
      this.jndiName = factoryName;
   }

   /**
    * @jmx.managed-attribute
    */
   public String getFactoryName()
   {
      return jndiName;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setDataSource(ObjectName dataSource) throws Exception
   {
      if(getState() == STARTED && !dataSource.equals(this.dataSource))
      {
         ds = lookupDataSource(dataSource);
      }
      this.dataSource = dataSource;
   }

   /**
    * @jmx.managed-attribute
    */
   public ObjectName getDataSource()
   {
      return dataSource;
   }

   /**
    * @jmx.managed-operation
    */
   public String getTableName()
   {
      return tableName;
   }

   /**
    * @jmx.managed-operation
    */
   public void setTableName(String tableName)
      throws Exception
   {
      if(getState() == STARTED && !tableName.equals(this.tableName))
      {
         //dropTableIfExists(this.tableName);
         initSequence(tableName, sequenceColumn, sequenceName, idColumnName);
      }
      this.tableName = tableName;
   }

   /**
    * @jmx.managed-attribute
    */
   public String getSequenceColumn()
   {
      return sequenceColumn;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setSequenceColumn(String sequenceColumn)
   {
      this.sequenceColumn = sequenceColumn;
   }

   /**
    * @jmx.managed-attribute
    */
   public String getSequenceName()
   {
      return sequenceName;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setSequenceName(String sequenceName)
   {
      this.sequenceName = sequenceName;
   }

   /**
    * @jmx.managed-attribute
    */
   public String getIdColumnName()
   {
      return idColumnName;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setIdColumnName(String idColumnName)
   {
      this.idColumnName = idColumnName;
   }

   /**
    * @jmx.managed-attribute
    */
   public String getCreateTableDdl()
   {
      return createTableDdl;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setCreateTableDdl(String createTableDdl)
   {
      this.createTableDdl = createTableDdl;
   }

   /**
    * @jmx.managed-attribute
    */
   public long getBlockSize()
   {
      return blockSize;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setBlockSize(long blockSize)
   {
      this.blockSize = blockSize;
   }

   // KeyGeneratorFactory implementation

   public KeyGenerator getKeyGenerator() throws Exception
   {
      return new HiLoKeyGenerator(ds, tableName, sequenceColumn, sequenceName, idColumnName, blockSize, tm);
   }

   // ServiceMBeanSupport overrides

   public void startService()
      throws Exception
   {
      Context ctx = new InitialContext();

      // bind the factory
      Util.rebind(ctx, getFactoryName(), this);

      tm = (TransactionManager)ctx.lookup("java:/TransactionManager");

      ds = lookupDataSource(dataSource);
      dropTableIfExists(tableName);
      initSequence(tableName, sequenceColumn, sequenceName, idColumnName);
   }

   public void stopService()
      throws Exception
   {
      // unbind the factory
      Context ctx = new InitialContext();
      Util.unbind(ctx, getFactoryName());
   }

   // Private

   private void initSequence(String tableName, String sequenceColumn, String sequenceName, String idColumnName)
      throws SQLException, DeploymentException
   {
      createTableIfNotExists(tableName);

      Connection con = null;
      Statement st = null;
      ResultSet rs = null;
      try
      {
         String sql = "select * from " + tableName + " where " + sequenceColumn + "='" + sequenceName + "'";
         log.debug("Executing SQL: " + sql);

         con = ds.getConnection();
         st = con.createStatement();
         rs = st.executeQuery(sql);
         if(!rs.next())
         {
            sql = "insert into " +
               tableName +
               "(" +
               sequenceColumn +
               ", " +
               idColumnName +
               ") values ('" + sequenceName + "', 0)";
            log.debug("Executing SQL: " + sql);

            final Statement insertSt = con.createStatement();
            try
            {
               final int i = insertSt.executeUpdate(sql);
               if(i != 1)
               {
                  throw new SQLException("Expected one updated row but got: " + i);
               }
            }
            finally
            {
               JDBCUtil.safeClose(insertSt);
            }
         }
      }
      finally
      {
         JDBCUtil.safeClose(rs);
         JDBCUtil.safeClose(st);
         JDBCUtil.safeClose(con);
      }
   }

   private void createTableIfNotExists(String tableName)
      throws SQLException, DeploymentException
   {
      Connection con = null;
      Statement st = null;
      try
      {
         if(!SQLUtil.tableExists(tableName, ds))
         {
            log.debug("Executing DDL: " + createTableDdl);

            con = ds.getConnection();
            st = con.createStatement();
            st.executeUpdate(createTableDdl);
         }
      }
      finally
      {
         JDBCUtil.safeClose(st);
         JDBCUtil.safeClose(con);
      }
   }

   private void dropTableIfExists(String tableName)
      throws SQLException, DeploymentException
   {
      Connection con = null;
      Statement st = null;
      try
      {
         if(SQLUtil.tableExists(tableName, ds))
         {
            final String ddl = "drop table " + tableName;
            log.debug("Executing DDL: " + ddl);

            con = ds.getConnection();
            st = con.createStatement();
            st.executeUpdate(ddl);
         }
      }
      finally
      {
         JDBCUtil.safeClose(st);
         JDBCUtil.safeClose(con);
      }
   }

   private DataSource lookupDataSource(ObjectName dataSource)
      throws Exception
   {
      try
      {
         String dsJndi = (String) server.getAttribute(dataSource, "BindName");
         return (DataSource)new InitialContext().lookup(dsJndi);
      }
      catch(NamingException e)
      {
         throw new Exception("Failed to lookup data source: " + dataSource);
      }
   }
}
