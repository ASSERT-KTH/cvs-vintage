/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp.jdbc;

import org.jboss.ejb.plugins.cmp.StartCommand;

/**
 * JDBCStartCommand does nothing.
 *    
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.1 $
 */
public class JDBCStartCommand implements StartCommand
{
   // Constructors --------------------------------------------------
   
   public JDBCStartCommand(JDBCStoreManager manager)
   {
   }
   
   // StartCommand implementation --------------------------------
   
   public void execute() throws Exception
   {
   }
}
