package org.jboss.cmp.schema;

import java.util.List;

public interface UpdateCommand
{
   public void executeUpdate(List params) throws PersistenceException;
}
