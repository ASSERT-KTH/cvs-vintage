/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc2;

import org.jboss.ejb.plugins.cmp.jdbc2.bridge.JDBCEntityBridge2;
import org.jboss.ejb.plugins.cmp.jdbc2.bridge.JDBCCMPFieldBridge2;
import org.jboss.ejb.plugins.cmp.jdbc2.schema.Schema;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;
import org.jboss.ejb.plugins.cmp.jdbc.QueryParameter;
import org.jboss.ejb.plugins.cmp.ejbql.SelectFunction;
import org.jboss.ejb.GenericEntityObjectFactory;
import org.jboss.logging.Logger;

import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.3 $</tt>
 */
public abstract class AbstractQueryCommand implements QueryCommand
{
   private static final CollectionFactory COLLECTION_FACTORY = new CollectionFactory()
   {
      public Collection newCollection()
      {
         return new ArrayList();
      }
   };

   private static final CollectionFactory SET_FACTORY = new CollectionFactory()
   {
      public Collection newCollection()
      {
         return new HashSet();
      }
   };

   protected String sql;
   protected Logger log;
   protected JDBCEntityBridge2 entity;
   protected QueryParameter[] params = null;
   private CollectionFactory collectionFactory;
   private CollectionStrategy collectionStrategy = new EagerCollectionStrategy();
   private ResultReader resultReader;

   // Protected

   protected void setResultType(Class clazz)
   {
      if(Set.class.isAssignableFrom(clazz))
      {
         collectionFactory = SET_FACTORY;
      }
      else if(Collection.class.isAssignableFrom(clazz))
      {
         collectionFactory = COLLECTION_FACTORY;
      }
   }

   protected void setFieldReader(JDBCCMPFieldBridge2 field)
   {
      this.resultReader = new FieldReader(field);
   }

   protected void setFunctionReader(SelectFunction func)
   {
      this.resultReader = new FunctionReader(func);
   }

   protected void setEntityReader(JDBCEntityBridge2 entity)
   {
      this.entity = entity;
      this.resultReader = new EntityReader(entity);
   }

   // QueryCommand implementation

   public JDBCStoreManager2 getStoreManager()
   {
      return (JDBCStoreManager2) entity.getManager();
   }

   public Collection fetchCollection(Schema schema, GenericEntityObjectFactory factory, Object[] args)
      throws FinderException
   {
      schema.flush();

      Collection result;

      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      try
      {
         if(log.isDebugEnabled())
         {
            log.debug("executing: " + sql);
         }

         con = entity.getDataSource().getConnection();
         ps = con.prepareStatement(sql);

         if(params != null)
         {
            for(int i = 0; i < params.length; i++)
            {
               params[i].set(log, ps, i + 1, args);
            }
         }

         rs = ps.executeQuery();
      }
      catch(Exception e)
      {
         JDBCUtil.safeClose(rs);
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);

         log.error("Finder failed: " + e.getMessage(), e);
         throw new FinderException(e.getMessage());
      }

      result = collectionStrategy.readResultSet(con, ps, rs, factory);

      return result;
   }

   public Object fetchOne(Schema schema, GenericEntityObjectFactory factory, Object[] args) throws FinderException
   {
      schema.flush();
      return executeFetchOne(args, factory);
   }

   // Protected

   protected Object executeFetchOne(Object[] args, GenericEntityObjectFactory factory) throws FinderException
   {
      Object pk;
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      try
      {
         if(log.isDebugEnabled())
         {
            log.debug("executing: " + sql);
         }

         con = entity.getDataSource().getConnection();
         ps = con.prepareStatement(sql);

         if(params != null)
         {
            for(int i = 0; i < params.length; i++)
            {
               params[i].set(log, ps, i + 1, args);
            }
         }

         rs = ps.executeQuery();
         if(rs.next())
         {
            pk = resultReader.readRow(rs, factory);
            if(rs.next())
            {
               List list = new ArrayList();
               list.add(pk);
               list.add(resultReader.readRow(rs, factory));
               while(rs.next())
               {
                  list.add(resultReader.readRow(rs, factory));
               }
               throw new FinderException("More than one instance matches the single-object finder criteria: " + list);
            }
         }
         else
         {
            throw new ObjectNotFoundException();
         }
      }
      catch(FinderException e)
      {
         throw e;
      }
      catch(Exception e)
      {
         throw new FinderException(e.getMessage());
      }
      finally
      {
         JDBCUtil.safeClose(rs);
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);
      }

      return pk;
   }

   protected void setParameters(List p)
   {
      if(p.size() > 0)
      {
         params = new QueryParameter[p.size()];
         for(int i = 0; i < p.size(); i++)
         {
            Object pi = p.get(i);
            if(!(pi instanceof QueryParameter))
            {
               throw new IllegalArgumentException("Element "
                  +
                  i
                  +
                  " of list is not an instance of QueryParameter, but " +
                  p.get(i).getClass().getName());
            }
            params[i] = (QueryParameter) pi;
         }
      }
   }

   // Inner

   private static interface CollectionFactory
   {
      Collection newCollection();
   }

   private static interface ResultReader
   {
      Object readRow(ResultSet rs, GenericEntityObjectFactory factory) throws SQLException;
   }

   private static class EntityReader implements ResultReader
   {
      private final JDBCEntityBridge2 entity;

      public EntityReader(JDBCEntityBridge2 entity)
      {
         this.entity = entity;
      }

      public Object readRow(ResultSet rs, GenericEntityObjectFactory factory)
      {
         final Object pk = entity.getTable().loadRow(rs);
         return pk == null ? null : factory.getEntityEJBObject(pk);
      }
   };

   private static class FieldReader implements ResultReader
   {
      private final JDBCCMPFieldBridge2 field;

      public FieldReader(JDBCCMPFieldBridge2 field)
      {
         this.field = field;
      }

      public Object readRow(ResultSet rs, GenericEntityObjectFactory factory) throws SQLException
      {
         return field.loadArgumentResults(rs, 1);
      }
   }

   private static class FunctionReader implements ResultReader
   {
      private final SelectFunction function;

      public FunctionReader(SelectFunction function)
      {
         this.function = function;
      }

      public Object readRow(ResultSet rs, GenericEntityObjectFactory factory) throws SQLException
      {
         return function.readResult(rs);
      }
   }

   private interface CollectionStrategy
   {
      Collection readResultSet(Connection con, PreparedStatement ps, ResultSet rs, GenericEntityObjectFactory factory)
         throws FinderException;
   }

   private class EagerCollectionStrategy
      implements CollectionStrategy
   {
      public Collection readResultSet(Connection con,
                                      PreparedStatement ps,
                                      ResultSet rs,
                                      GenericEntityObjectFactory factory)
         throws FinderException
      {
         Collection result;
         try
         {
            if(rs.next())
            {
               result = collectionFactory.newCollection();
               Object instance = resultReader.readRow(rs, factory);
               result.add(instance);
               while(rs.next())
               {
                  instance = resultReader.readRow(rs, factory);
                  result.add(instance);
               }
            }
            else
            {
               result = Collections.EMPTY_SET;
            }
         }
         catch(Exception e)
         {
            log.error("Finder failed: " + e.getMessage(), e);
            throw new FinderException(e.getMessage());
         }
         finally
         {
            JDBCUtil.safeClose(rs);
            JDBCUtil.safeClose(ps);
            JDBCUtil.safeClose(con);
         }
         return result;
      }
   }
}
