/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.lang.reflect.Method;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import javax.ejb.FinderException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.DeploymentException;
import org.jboss.ejb.plugins.cmp.FindEntitiesCommand;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCAutomaticQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCDeclaredQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQlQueryMetaData;
import org.jboss.ejb.plugins.cmp.bmp.CustomFindByEntitiesCommand;
import org.jboss.logging.Logger;
import org.jboss.util.FinderResults;

/**
 * Keeps a map from finder name to specific finder command, and
 * delegates to the relevant specific finder command.
 *
 * @see org.jboss.ejb.plugins.cmp.FindEntitiesCommand
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.12 $
 */
public class JDBCFindEntitiesCommand implements FindEntitiesCommand {
   private final Map knownFinderCommands = new HashMap();
   private final JDBCStoreManager manager;
   
   public JDBCFindEntitiesCommand(JDBCStoreManager manager) {
      this.manager = manager;
   }
   
   public void start() throws DeploymentException {
      Logger log = Logger.getLogger(
            this.getClass().getName() + 
            "." + 
            manager.getMetaData().getName());
      
      JDBCCommandFactory factory = manager.getCommandFactory();      
      
      Class homeClass = manager.getContainer().getHomeClass();
      Class localHomeClass = manager.getContainer().getLocalHomeClass();

      //
      // Custom finders - Overrides defined and automatic finders.
      //
      Class ejbClass = manager.getMetaData().getEntityClass();

      Method[] customMethods = ejbClass.getMethods();         
      for (int i = 0; i < customMethods.length; i++) {
         Method m = customMethods[i];
         String  methodName = m.getName();
         if(methodName.startsWith("ejbFind")) {
            String interfaceName = "f" +  methodName.substring(4);

            if(homeClass != null) {
               try {
                  // try to get the finder method on the home interface
                  Method interfaceMethod = homeClass.getMethod(
                        interfaceName, 
                        m.getParameterTypes());
                  
                  // got it add it to known finders
                  knownFinderCommands.put(
                        interfaceMethod, 
                        new CustomFindByEntitiesCommand(m));

                  log.debug("Added custom finder " + methodName +
                        " on home interface");
               } catch(NoSuchMethodException e) {
                  // this is ok method may not be defined on this interface
               }
            }
               
            if(localHomeClass != null) {
               try {
                  // try to get the finder method on the local home interface
                  Method interfaceMethod = localHomeClass.getMethod(
                        interfaceName, 
                        m.getParameterTypes());
                  
                  // got it add it to known finders
                  knownFinderCommands.put(
                        interfaceMethod, 
                        new CustomFindByEntitiesCommand(m));

                  log.debug("Added custom finder " + methodName +
                        " on local home interface");
               } catch(NoSuchMethodException e) {
                  // this is ok method may not be defined on this interface
               }
            }
         }
      }

      //
      // Defined finders - Overrides automatic finders.
      //
      Iterator definedFinders = manager.getMetaData().getQueries().iterator();
      while(definedFinders.hasNext()) {
         JDBCQueryMetaData q = (JDBCQueryMetaData)definedFinders.next();

         if(!knownFinderCommands.containsKey(q.getMethod()) ) {
            if(q instanceof JDBCDeclaredQueryMetaData) {
               knownFinderCommands.put(
                     q.getMethod(), factory.createDefinedFinderCommand(q));
                  
            } else if(q instanceof JDBCQlQueryMetaData) {
               knownFinderCommands.put(
                     q.getMethod(), factory.createEJBQLFinderCommand(q));
            }
         }
      }
      
      //
      // Automatic finders - The last resort
      //
      if(homeClass != null) {
         addAutomaticFinders(manager, homeClass.getMethods(), log);
      }
      
      if(localHomeClass != null) {
         addAutomaticFinders(manager, localHomeClass.getMethods(), log);
      }
   }
   
   protected void addAutomaticFinders(
         JDBCStoreManager manager,
         Method[] homeMethods,
         Logger log) {

      for (int i = 0; i < homeMethods.length; i++) {
         Method m = homeMethods[i];
         
         if(!knownFinderCommands.containsKey(m)) {
            String name = m.getName();
            if(name.equals("findAll")) {
               JDBCQueryMetaData q = new JDBCAutomaticQueryMetaData(m);
               knownFinderCommands.put(
                     m, manager.getCommandFactory().createFindAllCommand(q));
            } else if(name.startsWith("findBy") &&
                  !name.equals("findByPrimaryKey")) {
               
               try {
                  JDBCQueryMetaData q = new JDBCAutomaticQueryMetaData(m);
                  knownFinderCommands.put(
                        m, manager.getCommandFactory().createFindByCommand(q));
               } catch (IllegalArgumentException e) {
                  log.debug("Could not create the finder " + name +
                        ", because no matching CMP field was found.");
               }
            }
         }
      }
   }
   
   public FinderResults execute(Method finderMethod,
         Object[] args,
         EntityEnterpriseContext ctx) throws Exception {   

      FindEntitiesCommand finderCommand =
            (FindEntitiesCommand)knownFinderCommands.get(finderMethod);
   
      if(finderCommand == null) {
         throw new FinderException("Unknown finder: " + finderMethod);
      }
      return finderCommand.execute(finderMethod, args, ctx);
   }
}
