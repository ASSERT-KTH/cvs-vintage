/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.lang.reflect.Method;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.DeploymentException;
 
import org.jboss.ejb.plugins.cmp.CommandFactory;
import org.jboss.ejb.plugins.cmp.InitCommand;
import org.jboss.ejb.plugins.cmp.StartCommand;
import org.jboss.ejb.plugins.cmp.StopCommand;
import org.jboss.ejb.plugins.cmp.DestroyCommand;
import org.jboss.ejb.plugins.cmp.InitEntityCommand;
import org.jboss.ejb.plugins.cmp.FindEntityCommand;
import org.jboss.ejb.plugins.cmp.FindEntitiesCommand;
import org.jboss.ejb.plugins.cmp.CreateEntityCommand;
import org.jboss.ejb.plugins.cmp.RemoveEntityCommand;
import org.jboss.ejb.plugins.cmp.LoadEntityCommand;
import org.jboss.ejb.plugins.cmp.LoadEntitiesCommand;
import org.jboss.ejb.plugins.cmp.StoreEntityCommand;
import org.jboss.ejb.plugins.cmp.ActivateEntityCommand;
import org.jboss.ejb.plugins.cmp.PassivateEntityCommand;

import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;


/**
 * JDBCCommandFactory creates all required CMP command and some JDBC 
 * specific commands. This class should not store any data, which 
 * should be put in the store manager.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="danch@nvisia.com">danch (Dan Christopherson</a>
 * @version $Revision: 1.8 $
 */
public class JDBCCommandFactory implements CommandFactory
{
   // Attributes ----------------------------------------------------

   // These support singletons (within the scope of this factory)
   private JDBCBeanExistsCommand beanExistsCommand;
   private FindEntitiesCommand findEntitiesCommand;
   
   private JDBCStoreManager manager;
   
   // Constructors --------------------------------------------------
   
   public JDBCCommandFactory(JDBCStoreManager manager) throws Exception {
      this.manager = manager;      
   }
   
   // Public --------------------------------------------------------
   

   // Additional Command creation
   public JDBCLoadFieldCommand createLoadFieldCommand() {
      return new JDBCLoadFieldCommand(manager);
   }   
   
   /**
    * Singleton: multiple callers get references to the 
    * same command instance.
    */
   public JDBCBeanExistsCommand createBeanExistsCommand()
   {
      if (beanExistsCommand == null)
      {
         beanExistsCommand = new JDBCBeanExistsCommand(manager);
      }
      
      return beanExistsCommand;
   }
   
   public FindEntitiesCommand createFindAllCommand(JDBCQueryMetaData q)
   {
      return new JDBCFindAllCommand(manager, q);
   }
   
   public FindEntitiesCommand createDefinedFinderCommand(JDBCQueryMetaData q) 
      throws DeploymentException
   {
      return new JDBCDefinedFinderCommand(manager, q);
   }
   
   public FindEntitiesCommand createEJBQLFinderCommand(JDBCQueryMetaData q) 
      throws DeploymentException
   {
      return new JDBCEJBQLFinderCommand(manager, q);
   }

   public FindEntitiesCommand createFindByCommand(JDBCQueryMetaData q)
      throws IllegalArgumentException
   {
      return new JDBCFindByCommand(manager, q);
   }

   public JDBCFindByForeignKeyCommand createFindByForeignKeyCommand() {
      return new JDBCFindByForeignKeyCommand(manager);
   }

   public JDBCLoadRelationCommand createLoadRelationCommand() {
      return new JDBCLoadRelationCommand(manager);
   }

   public JDBCDeleteRelationsCommand createDeleteRelationsCommand() {
      return new JDBCDeleteRelationsCommand(manager);
   }

   public JDBCInsertRelationsCommand createInsertRelationsCommand() {
      return new JDBCInsertRelationsCommand(manager);
   }

   public JDBCReadAheadCommand createReadAheadCommand() {
      return new JDBCReadAheadCommand(manager);
   }

   // CommandFactory implementation ------------------------------

   // lifecycle commands
   
   public InitCommand createInitCommand() {
      return new JDBCInitCommand(manager);
   }
   
   public StartCommand createStartCommand() {
      return new JDBCStartCommand(manager);
   }
   
   public StopCommand createStopCommand() {
      return new JDBCStopCommand(manager);
   }
   
   public DestroyCommand createDestroyCommand() {
      return new JDBCDestroyCommand(manager);
   }
   
   public InitEntityCommand createInitEntityCommand() {
      return new JDBCInitEntityCommand(manager);
   }
   
   // entity persistence-related commands
   
   public FindEntityCommand createFindEntityCommand() {
      return new JDBCFindEntityCommand(manager);
   }
   
   /**
    * Singleton: multiple callers get references to the 
    * same command instance.
    */
   public FindEntitiesCommand createFindEntitiesCommand()
   {
      if(findEntitiesCommand == null) {
         findEntitiesCommand = new JDBCFindEntitiesCommand(manager);
      }
      
      return findEntitiesCommand;
   }
   
   public CreateEntityCommand createCreateEntityCommand()
   {                   
      return new JDBCCreateEntityCommand(manager);
   }
   
   public RemoveEntityCommand createRemoveEntityCommand()
   {
      return new JDBCRemoveEntityCommand(manager);
   }
   
   public LoadEntityCommand createLoadEntityCommand()
   {
      return new JDBCLoadEntityCommand(manager);
   }
   
   public LoadEntitiesCommand createLoadEntitiesCommand()
   {
      return new JDBCLoadEntitiesCommand(manager);
   }
   
   public StoreEntityCommand createStoreEntityCommand()
   {
      return new JDBCStoreEntityCommand(manager);
   }
   
   // entity activation and passivation commands
   
   public ActivateEntityCommand createActivateEntityCommand() {
      return new JDBCActivateEntityCommand(manager);
   }
   
   public PassivateEntityCommand createPassivateEntityCommand() {
      return new JDBCPassivateEntityCommand(manager);
   }
   
   // Private -------------------------------------------------------
}
