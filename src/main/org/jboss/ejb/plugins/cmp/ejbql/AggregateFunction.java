/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.ejbql;

import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCResultSetReader;
import org.jboss.logging.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.3 $</tt>
 */
public abstract class AggregateFunction
   extends SimpleNode
   implements SelectFunction
{
   private final Logger log;
   private JDBCResultSetReader resultReader;
   private Class resultType;

   public String distinct = "";

   public AggregateFunction(int i)
   {
      super(i);
      log = Logger.getLogger(getClass());
   }

   public void setResultType(Class type)
   {
      if(Collection.class.isAssignableFrom(type))
      {
         resultType = getDefaultResultType();
      }
      else
      {
         this.resultType = type;
      }
      this.resultReader = JDBCUtil.getResultReaderByType(resultType);
   }

   protected Class getDefaultResultType()
   {
      return Double.class;
   }

   // SelectFunction implementation

   public Object readResult(ResultSet rs) throws SQLException
   {
      return resultReader.get(rs, 1, resultType, log);
   }
}
