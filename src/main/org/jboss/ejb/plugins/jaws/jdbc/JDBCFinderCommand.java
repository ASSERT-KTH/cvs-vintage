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
import org.jboss.ejb.plugins.jaws.metadata.PkFieldMetaData;

/**
 * Abstract superclass of finder commands that return collections.
 * Provides the handleResult() implementation that these all need.
 * @see <related>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.9 $
 */
public abstract class JDBCFinderCommand
   extends JDBCQueryCommand
   implements JPMFindEntitiesCommand
{
   // Constructors --------------------------------------------------

   public JDBCFinderCommand(JDBCCommandFactory factory, String name)
   {
      super(factory, name);
   }

   // JPMFindEntitiesCommand implementation -------------------------

   public Collection execute(Method finderMethod,
                             Object[] args,
                             EntityEnterpriseContext ctx)
      throws RemoteException, FinderException
   {
      Collection result = null;

      try
      {
         result = (Collection)jdbcExecute(args);
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
