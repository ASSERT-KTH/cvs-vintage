/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.bridge;

import org.jboss.ejb.plugins.cmp.bridge.EntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCEntityPersistenceStore;

import javax.sql.DataSource;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.1 $</tt>
 */
public interface JDBCAbstractEntityBridge
   extends EntityBridge
{
   public abstract JDBCFieldBridge[] getPrimaryKeyFields();

   public abstract JDBCFieldBridge[] getTableFields();

   public abstract JDBCEntityPersistenceStore getManager();

   public abstract String getTableName();

   public abstract DataSource getDataSource();

   public abstract boolean[] getLoadGroupMask(String eagerLoadGroupName);
}
