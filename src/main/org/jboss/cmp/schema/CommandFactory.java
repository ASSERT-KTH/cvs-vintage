package org.jboss.cmp.schema;

import org.jboss.cmp.query.Query;
import org.jboss.cmp.query.CommandNode;

public interface CommandFactory
{
   public QueryCommand createQueryCommand(Query query);

   public UpdateCommand createUpdateCommand(CommandNode update);

   public CallCommand createCallCommand(CommandNode call);
}
