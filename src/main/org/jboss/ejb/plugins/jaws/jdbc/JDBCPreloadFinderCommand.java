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
import java.util.ArrayList;
import java.util.Collection;

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
import org.jboss.ejb.plugins.jaws.metadata.TypeMappingMetaData;
import org.jboss.util.FinderResults;

/**
 * Preloads data for all entities in where clause
 *
 * @see <related>
 * @author <a href="mailto:danch@nvisia.com">danch (Dan Christopherson)</a>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1.2 $
 * 
 * Revision:
 * 20010621 danch: abstracted the finders further so that this class can be 
 *    used against the findAll and FindBy types of finders as well as the 
 *    defined finders.
 */
public class JDBCPreloadFinderCommand
   extends JDBCFinderCommand
{
   /** The finder we delegate to for setParameters and to get our SQL */
   protected JDBCFinderCommand finderDelegate;
   /** The load command we delegate to for our column list */
   protected JDBCLoadEntityCommand loadCommand;

   // Constructors --------------------------------------------------

   public JDBCPreloadFinderCommand(JDBCCommandFactory factory, String name)
   {
      super(factory, name);
      loadCommand = new JDBCLoadEntityCommand(factory);
   }
   public JDBCPreloadFinderCommand(JDBCCommandFactory factory, FinderMetaData f)
   {
      super(factory, "PreloadFinder " + f.getName());
      loadCommand = new JDBCLoadEntityCommand(factory);
      finderDelegate = new JDBCDefinedFinderCommand(factory, f);
      buildSQL();
   }
   public JDBCPreloadFinderCommand(JDBCCommandFactory factory, JDBCFinderCommand finder) {
      super(factory, "PreloadFinder "+finder.getName());
      loadCommand = new JDBCLoadEntityCommand(factory);
      finderDelegate = finder;
      buildSQL();
   }

   public String getWhereClause() {
      return finderDelegate.getWhereClause();
   }
   public String getFromClause() {
      return finderDelegate.getFromClause();
   }
   public String getOrderByClause() {
      return finderDelegate.getOrderByClause();
   }

   /** Helper method called by the constructors */
   protected void buildSQL() {
      String sql = loadCommand.createSelectClause() + " "
         + finderDelegate.getFromClause() + " "
         + finderDelegate.getWhereClause() + " "
         + finderDelegate.getOrderByClause();
      if (jawsEntity.hasSelectForUpdate())
      {
         sql += " FOR UPDATE";
      }
      setSQL(sql);
   }
   
   protected Object handleResult(ResultSet rs, Object argOrArgs) throws Exception
   {
      Collection result = new ArrayList();
      while (rs.next())
      {
         Object key = createKey(rs);
         result.add(key);
         preloadOneEntity(rs, key);
      }
      return result;
   }
   
   protected void preloadOneEntity(ResultSet rs, Object key) {
      if (debug)
         log.debug("PRELOAD: preloading entity "+key);   
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
            
            Object value = getResultObject(rs, loadCommand.cmpFieldPositionInSelect[fieldCount], cmpField);
            allValues[fieldCount] = value;
            fieldCount++;
         }
         if (debug)
            log.debug("adding to preload data: " + key.toString());
         factory.addPreloadData(key, allValues);
      } catch (Exception sqle) {
         log.warning("SQL Error preloading data for key "+key);
      }
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
   
   protected void setParameters(PreparedStatement stmt, Object argOrArgs)
      throws Exception
   {
      finderDelegate.setParameters(stmt, argOrArgs);
/*      
      Object[] args = (Object[])argOrArgs;

      TypeMappingMetaData typeMapping = jawsEntity.getJawsApplication().getTypeMapping();
      for (int i = 0; i < defined.getParameterArray().length; i++)
      {
         Object arg = args[defined.getParameterArray()[i]];
         int jdbcType = typeMapping.getJdbcTypeForJavaType(arg.getClass());
         setParameter(stmt,i+1,jdbcType,arg);
      }
*/
   }
}
