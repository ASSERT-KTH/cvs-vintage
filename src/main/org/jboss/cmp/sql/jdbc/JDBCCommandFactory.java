package org.jboss.cmp.sql.jdbc;

import javax.sql.DataSource;

import org.jboss.cmp.query.Query;
import org.jboss.cmp.query.CommandNode;
import org.jboss.cmp.schema.CallCommand;
import org.jboss.cmp.schema.CommandFactory;
import org.jboss.cmp.schema.QueryCommand;
import org.jboss.cmp.schema.UpdateCommand;

public class JDBCCommandFactory implements CommandFactory
{
   private final DataSource ds;

   public JDBCCommandFactory(DataSource ds)
   {
      this.ds = ds;
   }

   public QueryCommand createQueryCommand(Query query)
   {
      return new JDBCQueryCommand(ds, query);
   }

   public UpdateCommand createUpdateCommand(CommandNode update)
   {
      return new JDBCUpdateCommand(ds, update);
   }

   public CallCommand createCallCommand(CommandNode call)
   {
      return null;
   }
}
