/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc2;


import org.jboss.ejb.plugins.cmp.jdbc2.bridge.JDBCEntityBridge2;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCJBossQLQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQlQueryMetaData;
import org.jboss.deployment.DeploymentException;

import javax.ejb.FinderException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.2 $</tt>
 */
public class QueryFactory
{
   private final Map queriesByMethod = new HashMap();
   private final JDBCEntityBridge2 entity;

   public QueryFactory(JDBCEntityBridge2 entity)
   {
      this.entity = entity;
   }

   public QueryCommand getQueryCommand(Method queryMethod) throws FinderException
   {
      QueryCommand queryCommand = (QueryCommand)queriesByMethod.get(queryMethod.getName());
      if(queryCommand == null)
      {
         throw new FinderException("Unknown query method: " + queryMethod);
      }
      return queryCommand;
   }

   public void init() throws DeploymentException
   {
      Class home = entity.getHomeClass();
      if(home != null)
      {
         try
         {
            home.getMethod("findByPrimaryKey", new Class[]{entity.getPrimaryKeyClass()});
         }
         catch(NoSuchMethodException e)
         {
            throw new DeploymentException("Home interface " + home.getClass().getName() +
               " does not contain findByPrimaryKey(" + entity.getPrimaryKeyClass().getName() + ")");
         }
      }

      Class local = entity.getLocalHomeClass();
      if(local != null)
      {
         try
         {
            local.getMethod("findByPrimaryKey", new Class[]{entity.getPrimaryKeyClass()});
         }
         catch(NoSuchMethodException e)
         {
            throw new DeploymentException("Local home interface " + home.getClass().getName() +
               " does not contain findByPrimaryKey(" + entity.getPrimaryKeyClass().getName() + ")");
         }
      }

      FindByPrimaryKeyCommand findByPk = new FindByPrimaryKeyCommand(entity);
      queriesByMethod.put("findByPrimaryKey", findByPk);

      //
      // Defined finders - Overrides automatic finders.
      //
      Iterator definedFinders = entity.getMetaData().getQueries().iterator();
      while(definedFinders.hasNext())
      {
         JDBCQueryMetaData q = (JDBCQueryMetaData) definedFinders.next();

         if(!queriesByMethod.containsKey(q.getMethod()))
         {
            if(q instanceof JDBCJBossQLQueryMetaData)
            {
               QueryCommand queryCommand = new JBossQLQueryCommand(entity, (JDBCJBossQLQueryMetaData)q);
               queriesByMethod.put(q.getMethod().getName(), queryCommand);
            }
            else if(q instanceof JDBCQlQueryMetaData)
            {
               QueryCommand queryCommand = new EJBQLQueryCommand(entity, (JDBCQlQueryMetaData)q);
               queriesByMethod.put(q.getMethod().getName(), queryCommand);
            }
            else
            {
               throw new DeploymentException("Unsupported query metadata: method=" + q.getMethod().getName() +
                  ", metadata=" + q);
            }
         }
      }
   }
}
