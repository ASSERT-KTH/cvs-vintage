/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.bridge;

import org.jboss.ejb.plugins.cmp.bridge.EntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCEntityPersistenceStore;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCEntityMetaData;

import javax.sql.DataSource;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.3 $</tt>
 */
public interface JDBCAbstractEntityBridge
   extends EntityBridge
{
   JDBCFieldBridge[] getPrimaryKeyFields();

   JDBCAbstractCMRFieldBridge[] getCMRFields();

   JDBCFieldBridge[] getTableFields();

   JDBCEntityPersistenceStore getManager();

   String getTableName();

   String getQualifiedTableName();

   DataSource getDataSource();

   boolean[] getLoadGroupMask(String eagerLoadGroupName);

   JDBCEntityMetaData getMetaData();
}
