/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp.jdbc;

import java.rmi.RemoteException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.CMPStoreManager;
import org.jboss.ejb.plugins.cmp.PassivateEntityCommand;

import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;

/**
 * JDBCPassivateEntityCommand deletes the entity persistence context,
 * where data about the instence is keeps.
 *    
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.3 $
 */
 
public class JDBCPassivateEntityCommand implements PassivateEntityCommand {
   // Constructors --------------------------------------------------
   JDBCEntityBridge entity;

   public JDBCPassivateEntityCommand(JDBCStoreManager manager) {
      entity = manager.getEntityBridge();
   }
   
   // PassivateEntityCommand implementation ----------------------
   
   public void execute(EntityEnterpriseContext ctx) throws RemoteException {
      entity.destroyPersistenceContext(ctx);
   }
}
