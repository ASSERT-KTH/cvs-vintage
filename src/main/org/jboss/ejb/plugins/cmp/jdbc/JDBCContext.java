/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc;

import java.util.HashMap;
import java.util.List;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCReadAheadMetaData;

/** 
 * This is the persistence context data associated with the 
 * EntityEnterpriseContext for each entity instance.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.2 $
 */
public final class JDBCContext 
{
   private static final Object LOAD_KEYS = new Object();
   private static final Object READ_AHEAD = new Object();

   public static Object get(EntityEnterpriseContext ctx, Object key)
   {
      return ((JDBCContext)ctx.getPersistenceContext()).get(key);
   }

   public static void put(EntityEnterpriseContext ctx, Object key, Object value)
   {
      ((JDBCContext)ctx.getPersistenceContext()).put(key, value);
   }

   public static JDBCReadAheadMetaData getReadAheadMetaData(
         EntityEnterpriseContext ctx)
   {
      return (JDBCReadAheadMetaData) get(ctx, READ_AHEAD);
   }

   public static void setReadAheadMetaData(
         EntityEnterpriseContext ctx, 
         JDBCReadAheadMetaData readAheadMetaData)
   {
      put(ctx, READ_AHEAD, readAheadMetaData);
   }

   public static List getLoadKeys(EntityEnterpriseContext ctx)
   {
      return (List) get(ctx, LOAD_KEYS);
   }

   public static void setLoadKeys(EntityEnterpriseContext ctx, List loadKeys)
   {
      put(ctx, LOAD_KEYS, loadKeys);
   }

   private final HashMap data = new HashMap();

   public Object get(Object key) 
   {
      return data.get(key);
   }

   public void put(Object key, Object value) 
   {
      data.put(key, value);
   }

   public void remove(Object key) 
   {
      data.remove(key);
   }
}
