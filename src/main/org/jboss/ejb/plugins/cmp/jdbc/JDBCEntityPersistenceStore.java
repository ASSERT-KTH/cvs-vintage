/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc;

import org.jboss.ejb.EntityPersistenceStore;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCEntityMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCAbstractEntityBridge;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.3 $</tt>
 */
public interface JDBCEntityPersistenceStore
   extends EntityPersistenceStore
{
   JDBCAbstractEntityBridge getEntityBridge();

   JDBCEntityMetaData getMetaData();

   JDBCTypeFactory getJDBCTypeFactory();

   Object getApplicationData(Object key);

   void putApplicationData(Object key, Object value);

   EntityContainer getContainer();
}
