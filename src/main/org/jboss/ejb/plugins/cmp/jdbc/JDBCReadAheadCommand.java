/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;


import java.util.List;

import java.rmi.RemoteException;
import java.rmi.ServerException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.jboss.util.FinderResults;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;

/**
 * JDBCReadAheadCommand
 *
 * @author <a href="mailto:on@ibis.odessa.ua">Oleg Nitz</a>
 * @version $Revision: 1.2 $
 */
public class JDBCReadAheadCommand
   extends JDBCQueryCommand
{
   private JDBCCMPFieldBridge[] lastLoadFields;
   private String lastFullClause;
   private int lastKeysCount = -1;
   private JDBCCMPFieldBridge[] pkFields;

   // Constructors --------------------------------------------------

   public JDBCReadAheadCommand(JDBCStoreManager manager) {
      super(manager, "ReadAhead");
      pkFields = entity.getJDBCPrimaryKeyFields();
   }

   // LoadEntitiesCommand implementation.

   public void execute(JDBCCMPFieldBridge[] loadFields, FinderResults keys, int from, int to)
      throws RemoteException
   {
      List allKeys;
      Object[] args;

      if ((loadFields.length == 0) || !(keys.getAllKeys() instanceof List)) {
         return;
      }
      allKeys = (List) keys.getAllKeys();
      args = new Object[] {loadFields, allKeys.subList(from, to).toArray()};

      try {
         jdbcExecute(args);
      } catch (Exception e) {
         throw new ServerException("Load failed", e);
      }
   }

   // JDBCQueryCommand overrides ------------------------------------

   protected Object handleResult(ResultSet rs, Object argOrArgs) throws Exception
   {
      Object[] pkRef = new Object[1];
      Object pk;
      Object[] argumentRef = new Object[1];
      int parameterIndex;
      Object[] args2 = (Object[]) argOrArgs;
      JDBCCMPFieldBridge[] loadFields = (JDBCCMPFieldBridge[]) (args2[0]);

      while (rs.next()) {
         try {
            parameterIndex = 1;
            for (int i = 0; i < pkFields.length; i++) {
               parameterIndex = pkFields[i].loadPrimaryKeyResults(rs, parameterIndex, pkRef);
            }
            pk = pkRef[0];
            for (int i = 0; i < loadFields.length; i++) {
               parameterIndex = loadFields[i].loadArgumentResults(rs, parameterIndex, argumentRef);
               manager.addPreloadData(pk, loadFields[i], argumentRef[0]);
            }
         } catch (Exception e) {
            throw new ServerException("Load failed", e);
         }
      }
      return null;
   }

   protected void setParameters(PreparedStatement ps, Object args) throws Exception {
      Object[] args2 = (Object[]) args;
      Object[] keys = (Object[]) (args2[1]);

      for (int i = 0; i < keys.length; i++) {
         entity.setPrimaryKeyParameters(ps, i + 1, keys[i]);
      }
   }

   // JDBCommand ovverrides -----------------------------------------
   protected String getSQL(Object args) throws Exception {
      Object[] args2 = (Object[]) args;
      JDBCCMPFieldBridge[] loadFields = (JDBCCMPFieldBridge[]) (args2[0]);
      int keysCount = ((Object[]) (args2[1])).length;
      JDBCCMPFieldBridge[] allFields;
      StringBuffer sb;
      boolean canUseLast;

      canUseLast = (keysCount == lastKeysCount && loadFields.length == lastLoadFields.length);
      if (canUseLast) {
         for (int i = 0; i < loadFields.length; i++) {
            if (!loadFields[i].getMetaData().equals(lastLoadFields[i].getMetaData())) {
               canUseLast = false;
               break;
            }
         }
      }
      if (!canUseLast) {
         // SELECT pkFields, loadFields FROM table WHERE pk1=? AND pk2=? OR pk1=? AND pk2=? ..."
         sb = new StringBuffer(1024);
         allFields = new JDBCCMPFieldBridge[pkFields.length + loadFields.length];
         System.arraycopy(pkFields, 0, allFields, 0, pkFields.length);
         System.arraycopy(loadFields, 0, allFields, pkFields.length, loadFields.length);
         StringBuffer sql = new StringBuffer();
         sb.append("SELECT ").append(SQLUtil.getColumnNamesClause(allFields));
         sb.append(" FROM ").append(entityMetaData.getTableName());
         sb.append(" WHERE ");
         for (int i = 0; i < keysCount; i++) {
            if (i > 0) {
               sb.append(" OR ");
            }
            sb.append(SQLUtil.getWhereClause(entity.getJDBCPrimaryKeyFields()));
         }
         lastKeysCount = keysCount;
         lastLoadFields = loadFields;
         lastFullClause = sb.toString();
      }
      return lastFullClause;
   }

   // protected -----------------------------------------------------
}
