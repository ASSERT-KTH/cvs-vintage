/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc2;


import org.jboss.ejb.plugins.cmp.jdbc2.schema.Schema;
import org.jboss.ejb.GenericEntityObjectFactory;

import javax.ejb.FinderException;
import java.util.Collection;


/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.2 $</tt>
 */
public interface QueryCommand
{
   public JDBCStoreManager2 getStoreManager();

   public Collection fetchCollection(Schema schema, GenericEntityObjectFactory factory, Object[] args)
      throws FinderException;

   public Object fetchOne(Schema schema, Object[] args)
      throws FinderException;
}
