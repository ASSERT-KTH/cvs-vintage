/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp.jdbc;

import java.rmi.RemoteException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.ActivateEntityCommand;

import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;

/**
 * JDBCActivateEntityCommand initializes the entity persistence context.
 * For cmp 1.x it creates a place to store original values for dirty checking.
 * In CMP2.x it creates a place to store the actual value. See the code
 * in JDBCEntityBridge.
 *    
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.4 $
 */
 
public class JDBCActivateEntityCommand implements ActivateEntityCommand {
   // Constructors --------------------------------------------------
   JDBCEntityBridge entity;
   
   public JDBCActivateEntityCommand(JDBCStoreManager manager) {
      entity = manager.getEntityBridge();
   }
   
   // ActivateEntityCommand implementation -----------------------
   
   public void execute(EntityEnterpriseContext ctx) throws RemoteException {
      entity.initPersistenceContext(ctx);
   }
}
