/**
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins.cmp.jdbc;

import java.util.List;
import org.jboss.ejb.CacheKey;
import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceStore;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.invocation.Invocation;
import org.jboss.ejb.ListCacheKey;
import org.jboss.ejb.plugins.AbstractInterceptor;
import org.jboss.ejb.plugins.CMPPersistenceManager;
import org.jboss.proxy.ejb.ReadAheadResult;
import org.jboss.util.CachePolicy;
import org.jboss.util.FinderResults;

/**
 *
 * This interceptor extracts ids of entities to read ahead, loads them,
 * then invokes the given method on the container for all of them, gathers results and returns them.
 *
 * @author <a href="mailto:on@ibis.odessa.ua">Oleg Nitz</a>
 * @version $Revision: 1.3 $
 */
public class JDBCReadAheadInterceptor extends AbstractInterceptor {
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /**
    *  The container of this interceptor.
    */
   protected EntityContainer container;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------
   public void setContainer(Container container) {
      this.container = (EntityContainer)container;
   }

   public Container getContainer() {
      return container;
   }

   // Interceptor implementation --------------------------------------

   public Object invoke(Invocation mi) throws Exception {
      Object result = null;

      if ((mi.getId() instanceof ListCacheKey) && (container instanceof EntityContainer) &&
            (((EntityContainer) container).getPersistenceManager() instanceof CMPPersistenceManager)) {
         ListCacheKey key = (ListCacheKey) mi.getId();
         CMPPersistenceManager manager = (CMPPersistenceManager) (((EntityContainer) container).getPersistenceManager());
         EntityPersistenceStore store = manager.getPersistenceStore();

         if (store instanceof JDBCStoreManager) {
            EntityEnterpriseContext ctx = (EntityEnterpriseContext) mi.getEnterpriseContext();
            JDBCStoreManager storeManager = (JDBCStoreManager) store;
            CachePolicy cache = storeManager.getReadAheadCache();
            FinderResults results = (FinderResults) cache.get(new Long(key.getListId()));
            // Overwrite CacheKey with ListCacheKey
            ctx.setCacheKey(key);
            if ((results != null) && (results.getAllKeys() instanceof List)) {
               int from;
               int to;
               ReadAheadResult rar = new ReadAheadResult();
               List ids = (List) results.getAllKeys();

               from = key.getIndex() + 1;
               to = Math.min(results.size(), key.getIndex() + storeManager.getMetaData().getReadAhead().getLimit());
               //storeManager.loadEntities(results, from, to);
               rar.setMainResult(getNext().invoke(mi));
               for (int i = from; i < to; i++) {
                  rar.addAheadResult(container.invoke(new Invocation(
                        new CacheKey(ids.get(i)), mi.getMethod(), mi.getArguments(),
                        mi.getTransaction(), mi.getPrincipal(), mi.getCredential())));
               }
               result = rar;
            }
         }
      }
      if (result == null) {
         result = getNext().invoke(mi);
      }
      return result;
   }
}

