/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc2.schema;

import javax.transaction.Transaction;
import java.sql.SQLException;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.2 $</tt>
 */
public interface Table
{
   public int getTableId();

   public String getTableName();
   
   View createView(Transaction tx);

   public interface View
   {
      void flush() throws SQLException;

      void beforeCompletion();
      
      void committed();

      void rolledback();
   }
}
