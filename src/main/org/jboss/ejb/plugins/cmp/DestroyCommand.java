/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp;

/**
 * DestroyCommand informs the store that the container is exiting. 
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
public interface DestroyCommand
{
   // Public --------------------------------------------------------
   
   public void execute();
}
