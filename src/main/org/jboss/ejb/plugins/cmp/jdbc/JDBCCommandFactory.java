/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;

/**
 * JDBCCommandFactory creates all required CMP command and some JDBC 
 * specific commands. This class should not store any data, which 
 * should be put in the store manager.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="danch@nvisia.com">danch (Dan Christopherson</a>
 * @version $Revision: 1.15 $
 */
public class JDBCCommandFactory {
   private JDBCStoreManager manager;
   
   public JDBCCommandFactory(JDBCStoreManager manager) throws Exception {
      this.manager = manager;      
   }
   
   public JDBCQueryCommand createFindByPrimaryKeyQuery(JDBCQueryMetaData q) {
      return new JDBCFindByPrimaryKeyQuery(manager, q);
   }
   
   public JDBCQueryCommand createFindAllQuery(JDBCQueryMetaData q) {
      return new JDBCFindAllQuery(manager, q);
   }
   
   public JDBCQueryCommand createDeclaredSQLQuery(JDBCQueryMetaData q) 
         throws DeploymentException {
      return new JDBCDeclaredSQLQuery(manager, q);
   }
   
   public JDBCQueryCommand createEJBQLQuery(JDBCQueryMetaData q) 
         throws DeploymentException {
      return new JDBCEJBQLQuery(manager, q);
   }

   public JDBCQueryCommand createDynamicQLQuery(JDBCQueryMetaData q) 
         throws DeploymentException {
      return new JDBCDynamicQLQuery(manager, q);
   }

   public JDBCQueryCommand createJBossQLQuery(JDBCQueryMetaData q) 
         throws DeploymentException {
      return new JDBCJBossQLQuery(manager, q);
   }

   public JDBCQueryCommand createFindByQuery(JDBCQueryMetaData q)
         throws IllegalArgumentException {
      return new JDBCFindByQuery(manager, q);
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

   // lifecycle commands
   
   public JDBCInitCommand createInitCommand() {
      return new JDBCInitCommand(manager);
   }
   
   public JDBCStartCommand createStartCommand() {
      return new JDBCStartCommand(manager);
   }
   
   public JDBCStopCommand createStopCommand() {
      return new JDBCStopCommand(manager);
   }
   
   public JDBCDestroyCommand createDestroyCommand() {
      return new JDBCDestroyCommand(manager);
   }
   
   // entity life cycle commands

   public JDBCCreateBeanClassInstanceCommand 
               createCreateBeanClassInstanceCommand() throws Exception {

      return new JDBCCreateBeanClassInstanceCommand(manager);
   }
   
   public JDBCInitEntityCommand createInitEntityCommand() {
      return new JDBCInitEntityCommand(manager);
   }
   
   public JDBCFindEntityCommand createFindEntityCommand() {
      return new JDBCFindEntityCommand(manager);
   }
   
   public JDBCFindEntitiesCommand createFindEntitiesCommand() {
      return new JDBCFindEntitiesCommand(manager);
   }
   
   public JDBCCreateEntityCommand createCreateEntityCommand() {
      return new JDBCCreateEntityCommand(manager);
   }
   
   public JDBCRemoveEntityCommand createRemoveEntityCommand() {
      return new JDBCRemoveEntityCommand(manager);
   }
   
   public JDBCLoadEntityCommand createLoadEntityCommand() {
      return new JDBCLoadEntityCommand(manager);
   }
   
   public JDBCIsModifiedCommand createIsModifiedCommand() {
      return new JDBCIsModifiedCommand(manager);
   }
   
   public JDBCStoreEntityCommand createStoreEntityCommand() {
      return new JDBCStoreEntityCommand(manager);
   }
   
   // entity activation and passivation commands
   public JDBCActivateEntityCommand createActivateEntityCommand() {
      return new JDBCActivateEntityCommand(manager);
   }
   
   public JDBCPassivateEntityCommand createPassivateEntityCommand() {
      return new JDBCPassivateEntityCommand(manager);
   }
}
