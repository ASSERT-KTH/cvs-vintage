/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc2;

import org.jboss.ejb.plugins.cmp.jdbc2.bridge.JDBCEntityBridge2;
import org.jboss.ejb.plugins.cmp.jdbc2.bridge.JDBCCMRFieldBridge2;
import org.jboss.ejb.plugins.cmp.jdbc2.schema.EntityTable;
import org.jboss.ejb.plugins.cmp.jdbc2.schema.Cache;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCAbstractCMRFieldBridge;

import javax.ejb.DuplicateKeyException;
import java.sql.SQLException;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.2 $</tt>
 */
public class PersistentContext
{
   private final EntityTable.Row row;
   private final JDBCCMRFieldBridge2.FieldState[] cmrStates;

   public PersistentContext(JDBCEntityBridge2 entity, EntityTable.Row row)
   {
      this.row = row;

      JDBCAbstractCMRFieldBridge[] cmrFields = entity.getCMRFields();
      if(cmrFields != null)
      {
         cmrStates = new JDBCCMRFieldBridge2.FieldState[cmrFields.length];
      }
      else
      {
         cmrStates = null;
      }
   }

   public Object getFieldValue(int rowIndex)
   {
      return row.getFieldValue(rowIndex);
   }

   public void setFieldValue(int rowIndex, Object value)
   {
      row.setFieldValue(rowIndex, value);
   }

   public void setPk(Object pk) throws DuplicateKeyException
   {
      if(pk == null)
      {
         throw new IllegalStateException("Primary key is null!");
      }

      row.insert(pk);
   }

   public boolean isDirty()
   {
      return row.isDirty();
   }

   public void setDirty()
   {
      row.setDirty();
   }

   public void remove()
   {
      row.delete();
   }

   public JDBCCMRFieldBridge2.FieldState getCMRState(int cmrIndex)
   {
      return cmrStates[cmrIndex];
   }

   public void setCMRState(int cmrIndex, JDBCCMRFieldBridge2.FieldState state)
   {
      cmrStates[cmrIndex] = state;
   }

   public void loadCachedRelations(int cmrIndex, Cache.CacheLoader loader)
   {
      row.loadCachedRelations(cmrIndex, loader);
   }

   public void cacheRelations(int cmrIndex, Cache.CacheLoader loader)
   {
      row.cacheRelations(cmrIndex, loader);
   }

   public void flush() throws SQLException, DuplicateKeyException
   {
      row.flush();
   }
}
