/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc2.schema;

import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCEntityMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;
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
import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.DatabaseMetaData;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.5 $</tt>
 */
public class Schema
{
   private EntityTable[] entityTables;
   private RelationTable[] relationTables;

   private TransactionLocal localViews = new TransactionLocal()
   {
      protected Object initialValue()
      {
         Transaction tx = getTransaction();
         Views views = new Views(tx);
         Synchronization sync = new SchemaSynchronization(views);

         try
         {
            tx.registerSynchronization(sync);
         }
         catch(RollbackException e)
         {
            throw new EJBException("Transaction already marked to roll back: " + e.getMessage(), e);
         }
         catch(SystemException e)
         {
            e.printStackTrace();
            throw new IllegalStateException("Failed to register transaction synchronization: " + e.getMessage());
         }

         return views;
      }
   };

   public EntityTable createEntityTable(JDBCEntityMetaData metadata, JDBCEntityBridge2 entity)
      throws DeploymentException
   {
      if(entityTables == null)
      {
         entityTables = new EntityTable[1];
      }
      else
      {
         EntityTable[] tmp = entityTables;
         entityTables = new EntityTable[tmp.length + 1];
         System.arraycopy(tmp, 0, entityTables, 0, tmp.length);
      }

      EntityTable table = new EntityTable(metadata, entity, this, entityTables.length - 1);
      entityTables[entityTables.length - 1] = table;
      return table;
   }

   public RelationTable createRelationTable(JDBCCMRFieldBridge2 leftField, JDBCCMRFieldBridge2 rightField)
      throws DeploymentException
   {
      if(relationTables == null)
      {
         relationTables = new RelationTable[1];
      }
      else
      {
         RelationTable[] tmp = relationTables;
         relationTables = new RelationTable[tmp.length + 1];
         System.arraycopy(tmp, 0, relationTables, 0, tmp.length);
      }

      RelationTable table = new RelationTable(leftField, rightField, this, relationTables.length - 1);
      relationTables[relationTables.length - 1] = table;
      return table;
   }

   public Table.View getView(EntityTable table)
   {
      Views views = (Views) localViews.get();
      Table.View view = views.entityViews[table.getTableId()];
      if(view == null)
      {
         view = table.createView(views.tx);
         views.entityViews[table.getTableId()] = view;
      }
      return view;
   }

   public Table.View getView(RelationTable table)
   {
      Views views = (Views) localViews.get();
      Table.View view = views.relationViews[table.getTableId()];
      if(view == null)
      {
         view = table.createView(views.tx);
         views.relationViews[table.getTableId()] = view;
      }
      return view;
   }

   public void flush()
   {
      Views views = (Views) localViews.get();

      Table.View[] relationViews = views.relationViews;
      if(relationViews != null)
      {
         for(int i = 0; i < relationViews.length; ++i)
         {
            final Table.View view = relationViews[i];
            if(view != null)
            {
               try
               {
                  view.flushDeleted(views);
               }
               catch(SQLException e)
               {
                  throw new EJBException("Failed to delete many-to-many relationships: " + e.getMessage(), e);
               }
            }
         }
      }

      final Table.View[] entityViews = views.entityViews;
      for(int i = 0; i < entityViews.length; ++i)
      {
         Table.View view = entityViews[i];
         if(view != null)
         {
            try
            {
               view.flushDeleted(views);
            }
            catch(SQLException e)
            {
               throw new EJBException("Failed to delete instances: " + e.getMessage(), e);
            }
         }
      }

      for(int i = 0; i < entityViews.length; ++i)
      {
         Table.View view = entityViews[i];
         if(view != null)
         {
            try
            {
               view.flushCreated(views);
            }
            catch(SQLException e)
            {
               throw new EJBException("Failed to create instances: " + e.getMessage(), e);
            }
         }
      }

      for(int i = 0; i < entityViews.length; ++i)
      {
         Table.View view = entityViews[i];
         if(view != null)
         {
            try
            {
               view.flushUpdated();
            }
            catch(SQLException e)
            {
               throw new EJBException("Failed to update instances: " + e.getMessage(), e);
            }
         }
      }

      if(relationViews != null)
      {
         for(int i = 0; i < relationViews.length; ++i)
         {
            final Table.View view = relationViews[i];
            if(view != null)
            {
               try
               {
                  view.flushCreated(views);
               }
               catch(SQLException e)
               {
                  throw new EJBException("Failed to create many-to-many relationships: " + e.getMessage(), e);
               }
            }
         }
      }
   }

   // Inner

   public class Views
   {
      public final Transaction tx;
      public final Table.View[] entityViews;
      public final Table.View[] relationViews;

      public Views(Transaction tx)
      {
         this.tx = tx;
         this.entityViews = new Table.View[entityTables.length];
         this.relationViews = relationTables == null ? null : new Table.View[relationTables.length];
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

         for(int i = 0; i < views.entityViews.length; ++i)
         {
            Table.View view = views.entityViews[i];
            if(view != null)
            {
               view.beforeCompletion();
            }
         }
      }

      public void afterCompletion(int status)
      {
         if(status == Status.STATUS_MARKED_ROLLBACK ||
            status == Status.STATUS_ROLLEDBACK ||
            status == Status.STATUS_ROLLING_BACK)
         {
            for(int i = 0; i < views.entityViews.length; ++i)
            {
               Table.View view = views.entityViews[i];
               if(view != null)
               {
                  view.rolledback();
               }
            }
         }
         else
         {
            for(int i = 0; i < views.entityViews.length; ++i)
            {
               Table.View view = views.entityViews[i];
               if(view != null)
               {
                  view.committed();
               }
            }
         }
      }
   }
}
