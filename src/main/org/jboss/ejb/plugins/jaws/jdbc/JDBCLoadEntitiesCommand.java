/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.Map;
import java.util.Iterator;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.ServerException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.jaws.JAWSPersistenceManager;
import org.jboss.ejb.plugins.jaws.JPMLoadEntitiesCommand;
import org.jboss.ejb.plugins.jaws.metadata.CMPFieldMetaData;
import org.jboss.ejb.plugins.jaws.metadata.FinderMetaData;
import org.jboss.ejb.plugins.jaws.metadata.JawsEntityMetaData;
import org.jboss.ejb.plugins.jaws.metadata.PkFieldMetaData;
import org.jboss.util.FinderResults;

/**
 * Implementation of the LoadEntitiesCommand added in JBoss 2.3. This preloads
 * data for all entities whose keys were retrieved by a finder.
 *
 * @see <related>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="mailto:dirk@jboss.de">Dirk Zimmermann</a>
 * @author <a href="mailto:danch@nvisia.com">danch (Dan Christopherson)</a>
 * @version $Revision: 1.2 $
 */
public class JDBCLoadEntitiesCommand
   extends JDBCLoadEntityCommand
   implements JPMLoadEntitiesCommand
{
   String selectClause;
   // Constructors --------------------------------------------------

   public JDBCLoadEntitiesCommand(JDBCCommandFactory factory)
   {
      super(factory);

      selectClause = super.createSelectClause();
   }

   // JPMLoadEntitiesCommand implementation.
   
   public void execute(FinderResults keys)
      throws RemoteException
   {
      JDBCFinderCommand finder = (JDBCFinderCommand)keys.getFinder();
      FinderMetaData metaData = null;
      if (finder != null)
         metaData = finder.getFinderMetaData();
      if (metaData != null && metaData.hasReadAhead() && keys.getQueryData() != null)
      {
         try
         {
            Object[] args = {keys};
            jdbcExecute(args);
         } catch (Exception e)
         {
            throw new ServerException("Load failed", e);
         }
      }
   }
   
   // JDBCQueryCommand overrides ------------------------------------

   protected Object handleResult(ResultSet rs, Object argOrArgs) throws Exception
   {
      FinderResults keys = (FinderResults)((Object[])argOrArgs)[0];
      Map instances = keys.getEntityMap();
      while (rs.next())
      {
         Object key = createKey(rs);
         preloadOneEntity(rs, key);
      }
      return null;
   }
   
   protected void preloadOneEntity(ResultSet rs, Object key) {
//log.debug("PRELOAD: preloading entity "+key);   
      int idx = 1;
      // skip the PK fields at the beginning of the select.
      Iterator keyIt = jawsEntity.getPkFields();
      while (keyIt.hasNext()) {
         keyIt.next();
         idx++;
      }

      int fieldCount = 0;
      Object[] allValues = new Object[jawsEntity.getNumberOfCMPFields()];
      Iterator iter = jawsEntity.getCMPFields();
      try {
         while (iter.hasNext())
         {
            CMPFieldMetaData cmpField = (CMPFieldMetaData)iter.next();
            
            Object value = getResultObject(rs, cmpFieldPositionInSelect[fieldCount], cmpField);
            allValues[fieldCount] = value;
            fieldCount++;
         }
         factory.addPreloadData(key, allValues);
      } catch (SQLException sqle) {
         log.warning("SQL Error preloading data for key "+key);
      }
   }
   
   protected void setParameters(PreparedStatement stmt, Object argOrArgs)
      throws Exception
   {
      FinderResults keys = (FinderResults)((Object[])argOrArgs)[0];
      JDBCFinderCommand finder = (JDBCFinderCommand)keys.getFinder();
      Object[] args = keys.getQueryArgs();
      finder.setParameters(stmt, args);
   }
      
   // JDBCommand ovverrides -----------------------------------------
   protected String getSQL(Object argOrArgs) throws Exception
   {
      FinderResults keys = (FinderResults)((Object[])argOrArgs)[0];
      JDBCFinderCommand finder = (JDBCFinderCommand)keys.getFinder();
      return selectClause + " " + finder.getFromClause() + " " + finder.getWhereClause() + " " + finder.getOrderByClause();
   }
   
   // protected -----------------------------------------------------
   
   protected Object createKey(ResultSet rs) throws Exception {
   
      if (jawsEntity.hasCompositeKey())
      {
         // Compound key
         try
         {
            Object pk = jawsEntity.getPrimaryKeyClass().newInstance();
            int i = 1;   // parameter index
            Iterator it = jawsEntity.getPkFields();
            
            while (it.hasNext())
            {
               PkFieldMetaData pkFieldMetaData = (PkFieldMetaData)it.next();
               Field pkField = pkFieldMetaData.getPkField();
               String colName = pkFieldMetaData.getColumnName();
               pkField.set(pk, getResultObject(rs, 
                                               i++, 
                                               pkField.getType()));
            }
            return pk;
         } catch (Exception e)
         {
            return null;
         }
      } else
      {
         // Primitive key
         Iterator it = jawsEntity.getPkFields();
         PkFieldMetaData pkFieldMetaData = (PkFieldMetaData)it.next();
         return getResultObject(rs, 1, pkFieldMetaData.getCMPField().getType());
      }
   }
   
}
