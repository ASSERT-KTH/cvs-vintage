package org.jboss.cmp.sql.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import org.jboss.cmp.query.Expression;
import org.jboss.cmp.query.Query;
import org.jboss.cmp.schema.QueryCommand;
import org.jboss.cmp.schema.PersistenceException;
import org.jboss.cmp.sql.SQL92Generator;
import org.jboss.cmp.sql.SQLDataType;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;

public class JDBCQueryCommand implements QueryCommand
{
   private final DataSource ds;
   private final String sql;
   private final SQLDataType[] paramTypes;
   private final SQLDataType[] resultTypes;

   public JDBCQueryCommand(DataSource ds, Query query)
   {
      this.ds = ds;
      SQL92Generator generator = new SQL92Generator();
      sql = generator.generate(query);
      paramTypes = (SQLDataType[]) query.getParameters();
      List projections = query.getProjection().getChildren();
      resultTypes = new SQLDataType[projections.size()];
      for (int i = 0; i < projections.size(); i++)
      {
         Expression expr = (Expression) projections.get(i);
         resultTypes[i] = (SQLDataType)expr.getType();
      }
   }

   public JDBCQueryCommand(DataSource ds, String sql, SQLDataType[] resultTypes, SQLDataType[] paramTypes)
   {
      this.ds = ds;
      this.sql = sql;
      this.paramTypes = paramTypes;
      this.resultTypes = resultTypes;
   }

   public List executeQuery(List params) throws PersistenceException
   {
      Connection c = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      List results = new ArrayList();
      try
      {
         c = ds.getConnection();
         ps = c.prepareStatement(sql);
         for (int i=0; i < paramTypes.length; i++)
         {
            paramTypes[i].setValue(ps, i+1, params.get(i));
         }
         rs = ps.executeQuery();
         while (rs.next())
         {
            List row = new ArrayList(resultTypes.length);
            for (int i=0; i < resultTypes.length; i++)
            {
               row.add(resultTypes[i].getValue(rs, i+1));
            }
            results.add(row);
         }
         return results;
      }
      catch (SQLException e)
      {
         JDBCUtil.safeClose(rs);
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(c);
         throw new PersistenceException(e);
      }
   }
}
