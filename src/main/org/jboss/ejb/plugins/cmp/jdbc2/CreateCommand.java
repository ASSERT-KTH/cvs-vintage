/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc2;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.deployment.DeploymentException;

import javax.ejb.CreateException;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.2 $</tt>
 */
public interface CreateCommand
{
   void init(JDBCStoreManager2 manager) throws DeploymentException;

   Object execute(Method m, Object[] args, EntityEnterpriseContext ctx) throws CreateException;
}
