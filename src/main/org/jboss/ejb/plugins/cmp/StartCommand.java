/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp;

/**
 * StartCommand informs the store that the container is ready to 
 * start sending other messages to the store.
 *      
 * Life-cycle:
 *		Tied to CMPStoreManager.
 *    
 * Multiplicity:	
 *		One per CMPStoreManager.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.1 $
 */
public interface StartCommand
{
   // Public --------------------------------------------------------
   
   public void execute() throws Exception;
}
