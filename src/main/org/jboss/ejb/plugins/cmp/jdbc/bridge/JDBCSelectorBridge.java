/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc.bridge;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.transaction.TransactionManager;
import javax.transaction.Transaction;

import org.jboss.ejb.plugins.cmp.bridge.SelectorBridge;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCQueryCommand;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCStoreManager;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;

/**
 * JDBCSelectorBridge represents one ejbSelect method.
 *
 * Life-cycle:
 *      Tied to the EntityBridge.
 *
 * Multiplicity:
 *      One for each entity bean ejbSelect method.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.9 $
 */
public class JDBCSelectorBridge implements SelectorBridge
{
   private final JDBCQueryMetaData queryMetaData;
   private final JDBCStoreManager manager;
   private TransactionManager tm;
   private boolean syncBeforeSelect;

   public JDBCSelectorBridge(JDBCStoreManager manager, JDBCQueryMetaData queryMetaData)
   {
      this.queryMetaData = queryMetaData;
      this.manager = manager;

      EntityContainer container = manager.getContainer();
      tm = container.getTransactionManager();
      syncBeforeSelect = !container.getBeanMetaData().getContainerConfiguration().getSyncOnCommitOnly();
   }

   // BridgeInvoker implementation

   public Object invoke(EntityEnterpriseContext ctx, Method method, Object[] args)
      throws Exception
   {
      Transaction tx = (ctx != null ? ctx.getTransaction() : tm.getTransaction());

      if(syncBeforeSelect)
      {
         EntityContainer.synchronizeEntitiesWithinTransaction(tx);
      }

      return execute(args);
   }

   // SelectorBridge implementation

   public String getSelectorName()
   {
      return queryMetaData.getMethod().getName();
   }

   public Method getMethod()
   {
      return queryMetaData.getMethod();
   }

   private Class getReturnType()
   {
      return queryMetaData.getMethod().getReturnType();
   }

   public Object execute(Object[] args) throws FinderException
   {
      Collection retVal;
      try
      {
         JDBCQueryCommand query = manager.getQueryManager().getQueryCommand(getMethod());
         retVal = query.execute(getMethod(), args, null);
      }
      catch(FinderException e)
      {
         throw e;
      }
      catch(EJBException e)
      {
         throw e;
      }
      catch(Exception e)
      {
         throw new EJBException("Error in " + getSelectorName(), e);
      }

      if(!Collection.class.isAssignableFrom(getReturnType()))
      {
         // single object
         if(retVal.size() == 0)
         {
            throw new ObjectNotFoundException();
         }
         if(retVal.size() > 1)
         {
            throw new FinderException(getSelectorName() +
               " returned " + retVal.size() + " objects");
         }
         return retVal.iterator().next();
      }
      else
      {
         // collection or set
         if(Set.class.isAssignableFrom(getReturnType()))
         {
            return new HashSet(retVal);
         }
         else
         {
            return new ArrayList(retVal);
         }
      }
   }
}
