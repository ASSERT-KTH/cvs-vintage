/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc;

import org.jboss.ejb.EntityPersistenceStore;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCEntityMetaData;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.2 $</tt>
 */
public interface JDBCEntityPersistenceStore
   extends EntityPersistenceStore
{
   JDBCEntityMetaData getMetaData();

   JDBCTypeFactory getJDBCTypeFactory();
}
