/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp.jdbc;

import org.jboss.ejb.plugins.cmp.StopCommand;

/**
 * JDBCStopCommand does nothing.
 *    
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.2 $
 */
public class JDBCStopCommand implements StopCommand
{
   // Constructors --------------------------------------------------
   
   public JDBCStopCommand(JDBCStoreManager manager)
   {
   }
   
   // StopCommand implementation ---------------------------------
   
   public void execute()
   {
   }
}
