/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp;

/**
 * InitCommand at the end of the CMPStoreManager's init method.
 * The store can use this command to initialize the store for
 * the specific entity bean.
 *      
 * Life-cycle:
 *		Tied to CMPStoreManager.
 *    
 * Multiplicity:	
 *		One per CMPStoreManager.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.2 $
 */
public interface InitCommand
{
   // Public --------------------------------------------------------
   
   public void execute() throws Exception;
}
