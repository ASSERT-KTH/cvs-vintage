package org.jboss.cmp.sql.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

import org.jboss.cmp.query.Query;
import org.jboss.cmp.query.CommandNode;
import org.jboss.cmp.schema.PersistenceException;
import org.jboss.cmp.schema.UpdateCommand;
import org.jboss.cmp.sql.SQL92Generator;
import org.jboss.cmp.sql.SQLDataType;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;

public class JDBCUpdateCommand implements UpdateCommand
{
   private final DataSource ds;
   private final String sql;
   private final SQLDataType[] paramTypes;

   public JDBCUpdateCommand(DataSource ds, CommandNode update)
   {
      this.ds = ds;
      SQL92Generator generator = new SQL92Generator();
      sql = generator.generate(update);
      paramTypes = (SQLDataType[]) update.getParameters();
   }

   public JDBCUpdateCommand(DataSource ds, String sql, SQLDataType[] paramTypes)
   {
      this.ds = ds;
      this.sql = sql;
      this.paramTypes = paramTypes;
   }

   public void executeUpdate(List params) throws PersistenceException
   {
      Connection c = null;
      PreparedStatement ps = null;
      try
      {
         c = ds.getConnection();
         ps = c.prepareStatement(sql);
         for (int i=0; i < paramTypes.length; i++)
         {
            paramTypes[i].setValue(ps, i+1, params.get(i));
         }
         ps.executeUpdate();
      }
      catch (SQLException e)
      {
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(c);
         throw new PersistenceException(e);
      }
   }
}
