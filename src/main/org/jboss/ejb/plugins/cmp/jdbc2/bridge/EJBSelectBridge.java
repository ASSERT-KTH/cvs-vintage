/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc2.bridge;

import org.jboss.ejb.plugins.cmp.bridge.SelectorBridge;
import org.jboss.ejb.plugins.cmp.jdbc2.QueryCommand;
import org.jboss.ejb.plugins.cmp.jdbc2.JDBCStoreManager2;
import org.jboss.ejb.plugins.cmp.jdbc2.schema.Schema;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.GenericEntityObjectFactory;

import javax.ejb.FinderException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.3 $</tt>
 */
public class EJBSelectBridge
   implements SelectorBridge
{
   private final static byte SINGLE = 0;
   private final static byte COLLECTION = 2;

   private final JDBCQueryMetaData metadata;
   private final QueryCommand command;
   private final byte returnType;
   private final Schema schema;
   private boolean syncBeforeSelect;
   private TransactionManager tm;

   public EJBSelectBridge(EntityContainer container, Schema schema, JDBCQueryMetaData metadata, QueryCommand command)
   {
      this.schema = schema;
      this.metadata = metadata;
      this.command = command;

      Class type = metadata.getMethod().getReturnType();
      if(Collection.class.isAssignableFrom(type))
      {
         returnType = COLLECTION;
      }
      else
      {
         returnType = SINGLE;
      }

      tm = container.getTransactionManager();
      syncBeforeSelect = !container.getBeanMetaData().getContainerConfiguration().getSyncOnCommitOnly();
   }

   // BridgeInvoker implementation

   public Object invoke(EntityEnterpriseContext instance, Method method, Object[] args)
      throws Exception
   {
      Transaction tx = (instance != null ? instance.getTransaction() : tm.getTransaction());

      if(syncBeforeSelect)
      {
         EntityContainer.synchronizeEntitiesWithinTransaction(tx);
      }

      return execute(args);
   }

   // SelectorBridge implementation

   public String getSelectorName()
   {
      return metadata.getMethod().getName();
   }

   public Method getMethod()
   {
      return metadata.getMethod();
   }

   public Object execute(Object[] args) throws FinderException
   {
      JDBCStoreManager2 manager = command.getStoreManager();
      GenericEntityObjectFactory factory = (metadata.isResultTypeMappingLocal() ?
         (GenericEntityObjectFactory)manager.getContainer().getLocalProxyFactory() : manager.getContainer().getProxyFactory());

      Object result;
      switch(returnType)
      {
         case SINGLE:
            result = command.fetchOne(schema, factory, args);
            break;
         case COLLECTION:
            result = command.fetchCollection(schema, factory, args);
            break;
         default:
            throw new IllegalStateException("Unexpected return type: " + returnType);
      }
      return result;
   }
}
