package org.jboss.cmp.schema;

import org.jboss.cmp.query.Query;

public interface CommandFactory
{
   public QueryCommand createQueryCommand(Query query);

   public UpdateCommand createUpdateCommand(Object update);

   public CallCommand createCallCommand(Object call);
}
