/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc2.schema;

import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCEntityMetaData;
import org.jboss.ejb.plugins.cmp.jdbc2.bridge.JDBCEntityBridge2;
import org.jboss.ejb.plugins.cmp.jdbc2.bridge.JDBCCMRFieldBridge2;
import org.jboss.deployment.DeploymentException;
import org.jboss.tm.TransactionLocal;

import javax.ejb.EJBException;
import javax.transaction.Synchronization;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Status;
import javax.transaction.Transaction;
import java.sql.SQLException;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.2 $</tt>
 */
public class Schema
{
   private Table[] tables;

   private TransactionLocal localViews = new TransactionLocal()
   {
      protected Object initialValue()
      {
         Transaction tx = getTransaction();
         Views views = new Views(tx, new Table.View[tables.length]);
         Synchronization sync = new SchemaSynchronization(views);

         try
         {
            tx.registerSynchronization(sync);
         }
         catch(RollbackException e)
         {
            e.printStackTrace();
            throw new EJBException("Transaction already marked to roll back: " + e.getMessage());
         }
         catch(SystemException e)
         {
            e.printStackTrace();
            throw new IllegalStateException("Failed to register transaction synchronization: " + e.getMessage());
         }

         return views;
      }
   };

   public EntityTable createTable(JDBCEntityMetaData metadata, JDBCEntityBridge2 entity)
      throws DeploymentException
   {
      if(tables == null)
      {
         tables = new Table[1];
      }
      else
      {
         Table[] tmp = tables;
         tables = new Table[tmp.length + 1];
         System.arraycopy(tmp, 0, tables, 0, tmp.length);
      }

      EntityTable table = new EntityTable(metadata, entity, this, tables.length - 1);
      tables[tables.length - 1] = table;
      return table;
   }

   public RelationTable createRelationTable(JDBCCMRFieldBridge2 leftField, JDBCCMRFieldBridge2 rightField)
      throws DeploymentException
   {
      if(tables == null)
      {
         tables = new EntityTable[1];
      }
      else
      {
         Table[] tmp = tables;
         tables = new Table[tmp.length + 1];
         System.arraycopy(tmp, 0, tables, 0, tmp.length);
      }

      RelationTable table = new RelationTable(leftField, rightField, this, tables.length - 1);
      tables[tables.length - 1] = table;
      return table;
   }

   public Table.View getView(Table table)
   {
      Views views = (Views) localViews.get();
      Table.View view = views.views[table.getTableId()];
      if(view == null)
      {
         view = table.createView(views.tx);
         views.views[table.getTableId()] = view;
      }
      return view;
   }

   public void flush()
   {
      Views views = (Views) localViews.get();
      for(int i = 0; i < views.views.length; ++i)
      {
         Table.View view = views.views[i];
         if(view != null)
         {
            try
            {
               view.flush();
            }
            catch(SQLException e)
            {
               throw new EJBException("Failed to synchronize data: " + e.getMessage(), e);
            }
         }
      }
   }

   // Inner

   private class Views
   {
      public final Transaction tx;
      public final Table.View[] views;

      public Views(Transaction tx, Table.View[] views)
      {
         this.tx = tx;
         this.views = views;
      }
   }

   private class SchemaSynchronization implements Synchronization
   {
      private final Views views;

      public SchemaSynchronization(Views views)
      {
         this.views = views;
      }

      public void beforeCompletion()
      {
         flush();

         for(int i = 0; i < views.views.length; ++i)
         {
            Table.View view = views.views[i];
            if(view != null)
            {
               view.beforeCompletion();
            }
         }
      }

      public void afterCompletion(int status)
      {
         if(status != Status.STATUS_MARKED_ROLLBACK)
         {
            for(int i = 0; i < views.views.length; ++i)
            {
               Table.View view = views.views[i];
               if(view != null)
               {
                  view.committed();
               }
            }
         }
         else
         {
            for(int i = 0; i < views.views.length; ++i)
            {
               Table.View view = views.views[i];
               if(view != null)
               {
                  view.rolledback();
               }
            }
         }
      }
   }
}
