/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp;

/**
 * CommandFactory follows an Abstract Factory pattern [Gamma et. al, 1995]
 *
 * Life-cycle:
 *      Tied to CMPStoreManager.
 *    
 * Multiplicity:   
 *      One per CMPStore.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.3 $
 */
public interface CommandFactory
{
   // Public --------------------------------------------------------
   
   // lifecycle commands
   
   public InitCommand createInitCommand();
   
   public StartCommand createStartCommand();
   
   public StopCommand createStopCommand();
   
   public DestroyCommand createDestroyCommand();
   
   // entity persistence-related commands
   
   public InitEntityCommand createInitEntityCommand();

   public FindEntityCommand createFindEntityCommand();
   
   public FindEntitiesCommand createFindEntitiesCommand();
   
   public CreateEntityCommand createCreateEntityCommand();
   
   public RemoveEntityCommand createRemoveEntityCommand();
   
   public LoadEntityCommand createLoadEntityCommand();
   
   public LoadEntitiesCommand createLoadEntitiesCommand();
   
   public StoreEntityCommand createStoreEntityCommand();
   
   // entity activation and passivation commands
   
   public ActivateEntityCommand createActivateEntityCommand();
   
   public PassivateEntityCommand createPassivateEntityCommand();
}
