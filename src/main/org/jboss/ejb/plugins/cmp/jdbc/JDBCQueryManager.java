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

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCAutomaticQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCDeclaredQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQlQueryMetaData;
import org.jboss.logging.Logger;

/**
 * Maintains a map from a query method to query command.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.3 $
 */
public class JDBCQueryManager {
   private final Map knownQueries = new HashMap();
   private final JDBCStoreManager manager;
   
   public JDBCQueryManager(JDBCStoreManager manager) {
      this.manager = manager;
   }
   
   public JDBCQueryCommand getQueryCommand(Method queryMethod) 
         throws FinderException {

      JDBCQueryCommand queryCommand =
            (JDBCQueryCommand)knownQueries.get(queryMethod);
   
      if(queryCommand == null) {
         throw new FinderException("Unknown query: " + queryMethod);
      }
      return queryCommand;
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
      // findByPrimaryKey
      //
      JDBCEntityBridge entity = manager.getEntityBridge();
      if(homeClass != null) {
         try {
            // try to get the finder method on the home interface
            Method method = homeClass.getMethod(
                  "findByPrimaryKey", 
                  new Class[] {entity.getPrimaryKeyClass()});
            
            // got it add it to known finders
            JDBCQueryMetaData q = new JDBCAutomaticQueryMetaData(method);
            knownQueries.put(
                  method, 
                  factory.createFindByPrimaryKeyQuery(q));

            log.debug("Added findByPrimaryKey query command for " +
                  "home interface");
         } catch(NoSuchMethodException e) {
            throw new DeploymentException("Home interface does not " +
                  "have a findByPrimaryKey method");
         }
      }
         
      if(localHomeClass != null) {
         try {
            // try to get the finder method on the local home interface
            Method method = localHomeClass.getMethod(
                  "findByPrimaryKey", 
                  new Class[] {entity.getPrimaryKeyClass()});
            
            // got it add it to known finders
            JDBCQueryMetaData q = new JDBCAutomaticQueryMetaData(method);
            knownQueries.put(
                  method, 
                  factory.createFindByPrimaryKeyQuery(q));

            log.debug("Added findByPrimaryKey query command for " +
                  "local home interface");
         } catch(NoSuchMethodException e) {
            throw new DeploymentException("Local home interface does " +
                  "not have a findByPrimaryKey method");
         }
      }
 
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
                  knownQueries.put(
                        interfaceMethod, 
                        new JDBCCustomFinderQuery(manager, m));

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
                  knownQueries.put(
                        interfaceMethod, 
                        new JDBCCustomFinderQuery(manager, m));

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

         if(!knownQueries.containsKey(q.getMethod()) ) {
            if(q instanceof JDBCDeclaredQueryMetaData) {
               knownQueries.put(
                     q.getMethod(), factory.createDeclaredSQLQuery(q));
                  
            } else if(q instanceof JDBCQlQueryMetaData) {
               knownQueries.put(
                     q.getMethod(), factory.createEJBQLQuery(q));
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
   
   private void addAutomaticFinders(
         JDBCStoreManager manager,
         Method[] homeMethods,
         Logger log) {

      JDBCCommandFactory factory = manager.getCommandFactory();      
      for (int i = 0; i < homeMethods.length; i++) {
         Method m = homeMethods[i];
         
         if(!knownQueries.containsKey(m)) {
            String name = m.getName();
            if(name.equals("findAll")) {
               JDBCQueryMetaData q = new JDBCAutomaticQueryMetaData(m);
               knownQueries.put(m, factory.createFindAllQuery(q));
            } else if(name.startsWith("findBy") &&
                  !name.equals("findByPrimaryKey")) {
               
               try {
                  JDBCQueryMetaData q = new JDBCAutomaticQueryMetaData(m);
                  knownQueries.put(m, factory.createFindByQuery(q));
               } catch (IllegalArgumentException e) {
                  log.debug("Could not create the finder " + name +
                        ", because no matching CMP field was found.");
               }
            }
         }
      }
   }
}
