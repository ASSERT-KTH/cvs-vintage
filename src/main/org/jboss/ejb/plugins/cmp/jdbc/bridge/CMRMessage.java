/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.bridge;

import org.jboss.ejb.EntityEnterpriseContext;

/**
 * The methods of this interface are passed as messages to related containers.
 * There are no implementations of this interface.  The method object is passed
 * through the invocation interceptor chain and caught by the 
 * JDBCRelationInterceptor.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.4 $
 */
public interface CMRMessage extends NonentrantMessage {
   public Object getRelatedId(EntityEnterpriseContext myCtx, JDBCCMRFieldBridge cmrField);
   public void addRelation(EntityEnterpriseContext myCtx, Object relatedId);
   public void removeRelation(EntityEnterpriseContext myCtx, Object relatedId);
}
