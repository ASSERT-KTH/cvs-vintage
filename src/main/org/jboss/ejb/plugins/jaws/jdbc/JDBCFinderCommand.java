/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import java.rmi.RemoteException;
import java.rmi.ServerException;

import java.sql.ResultSet;

import javax.ejb.FinderException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.jaws.JPMFindEntitiesCommand;
import org.jboss.ejb.plugins.jaws.metadata.FinderMetaData;
import org.jboss.ejb.plugins.jaws.metadata.PkFieldMetaData;
import org.jboss.util.FinderResults;

/**
 * Abstract superclass of finder commands that return collections.
 * Provides the handleResult() implementation that these all need.
 * @see <related>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1.12 $
 *
 * Revisions:
 * 20010621 Bill Burke: added constructor to facilitate re-use. Removed extra login for setting up FinderResults since this class is really not used anymore.
 */
public abstract class JDBCFinderCommand
   extends JDBCQueryCommand
   implements JPMFindEntitiesCommand
{
   protected FinderMetaData finderMetaData = null;
   // Constructors --------------------------------------------------

   public JDBCFinderCommand(JDBCCommandFactory factory, String name)
   {
      super(factory, name);
   }
   public JDBCFinderCommand(JDBCCommandFactory factory, FinderMetaData f)
   {
      super(factory, f.getName());
      
      finderMetaData = f;
   }
   
   public FinderMetaData getFinderMetaData() {
      return finderMetaData;
   }


   /** This method must be overridden to return the where clause used in 
    *  this query. This must start with the keyword 'WHERE' and include all 
    *  conditions needed to execute the query properly. 
    */
   public abstract String getWhereClause();

   /** This method must be ovverridden to return the full table list for 
    *  the query, including any join statements. This must start with the 
    *  keyword 'FROM' and include all tables needed to execute the query properly.
    */   
   public abstract String getFromClause();
   
   /** This method must be ovverridded to return the full order by clause for 
    *  the query, including the 'ORDER BY' keyword.
    */
   public abstract String getOrderByClause();
   
   // JPMFindEntitiesCommand implementation -------------------------

   public FinderResults execute(Method finderMethod,
                             Object[] args,
                             EntityEnterpriseContext ctx)
      throws RemoteException, FinderException
   {
      FinderResults result = null;

      try
      {
         Collection keys = (Collection)jdbcExecute(args);
         result = new FinderResults(keys, null, null, null);
      } catch (Exception e)
      {
         log.debug(e);
         throw new FinderException("Find failed");
      }

      return result;
   }

   // JDBCQueryCommand overrides ------------------------------------

   protected Object handleResult(ResultSet rs, Object argOrArgs) throws Exception
   {
      Collection result = new ArrayList();
      
      if (jawsEntity.hasCompositeKey())
      {
         // Compound key
         try
         {
            while (rs.next())
            {
               Object pk = jawsEntity.getPrimaryKeyClass().newInstance();
               int i = 1;   // parameter index
               Iterator it = jawsEntity.getPkFields();
               
               while (it.hasNext())
               {
                  PkFieldMetaData pkFieldMetaData = (PkFieldMetaData)it.next();
                  Field pkField = pkFieldMetaData.getPkField();
                  pkField.set(pk, getResultObject(rs, 
                                                  i++, 
                                                  pkField.getType()));
               }
               result.add(pk);
            }
         } catch (Exception e)
         {
            throw new ServerException("Finder failed",e);
         }
      } else
      {
         // Primitive key
         Iterator it = jawsEntity.getPkFields();
         PkFieldMetaData pkFieldMetaData = (PkFieldMetaData)it.next();
         
         while (rs.next())
         {
            result.add(getResultObject(rs, 1, pkFieldMetaData.getCMPField().getType()));
         }
      }

      return result;
   }
}
