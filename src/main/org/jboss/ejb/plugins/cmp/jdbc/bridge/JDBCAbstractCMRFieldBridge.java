/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.bridge;

import org.jboss.ejb.plugins.cmp.bridge.CMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCRelationshipRoleMetaData;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.2 $</tt>
 */
public abstract class JDBCAbstractCMRFieldBridge
   implements JDBCFieldBridge, CMRFieldBridge
{
   public abstract JDBCRelationshipRoleMetaData getMetaData();

   public abstract JDBCFieldBridge[] getForeignKeyFields();

   public abstract JDBCFieldBridge[] getTableKeyFields();

   public abstract boolean hasForeignKey();

   public abstract JDBCAbstractCMRFieldBridge getRelatedCMRField();

   public abstract JDBCAbstractEntityBridge getEntity();

   public abstract String getTableName();

   public Object getPrimaryKeyValue(Object o)
   {
      throw new UnsupportedOperationException();
   }
}
