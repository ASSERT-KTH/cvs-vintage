package org.jboss.cmp.sql.jdbc;

import org.jboss.cmp.schema.CommandFactory;
import org.jboss.cmp.schema.QueryCommand;
import org.jboss.cmp.schema.UpdateCommand;
import org.jboss.cmp.schema.CallCommand;
import org.jboss.cmp.query.Query;

public class JDBCCommandFactory implements CommandFactory
{
   public QueryCommand createQueryCommand(Query query)
   {
      return null;
   }

   public UpdateCommand createUpdateCommand(Object update)
   {
      return null;
   }

   public CallCommand createCallCommand(Object call)
   {
      return null;
   }
}
