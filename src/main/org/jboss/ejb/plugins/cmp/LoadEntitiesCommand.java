/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp;

import org.jboss.util.FinderResults;

/**
 * LoadEntityCommand invoked after FindEntitiesCommand.
 * This command should try to load the entites idetified
 * in the finder results.
 *      
 * Life-cycle:
 *      Tied to CMPStoreManager.
 *    
 * Multiplicity:   
 *      One per CMPStoreManager.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:danch@nvisia.com">danch (Dan Christopherson)</a>
 * @version $Revision: 1.5 $
 */
public interface LoadEntitiesCommand
{
   // Public --------------------------------------------------------
   
   public void execute(FinderResults keys);
}

