/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc;

import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCReadAheadMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCAbstractEntityBridge;
import org.jboss.ejb.plugins.cmp.ejbql.SelectFunction;
import org.jboss.ejb.plugins.cmp.bridge.EntityBridge;
import org.jboss.ejb.EntityPersistenceStore;

import java.util.List;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.2 $</tt>
 */
public interface QLCompiler
{
   void compileEJBQL(
      String ejbql,
      Class returnType,
      Class[] parameterTypes,
      JDBCReadAheadMetaData readAhead
      ) throws Exception;

   void compileJBossQL(
      String ejbql,
      Class returnType,
      Class[] parameterTypes,
      JDBCReadAheadMetaData readAhead
      )
      throws Exception;

   String getSQL();

   int getOffsetValue();

   int getOffsetParam();

   int getLimitValue();

   int getLimitParam();

   boolean isSelectEntity();

   JDBCAbstractEntityBridge getSelectEntity();

   boolean isSelectField();

   JDBCFieldBridge getSelectField();

   SelectFunction getSelectFunction();

   EntityPersistenceStore getStoreManager();

   List getInputParameters();

   List getLeftJoinCMRList();
}
