/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.lang.reflect.Method;

import java.sql.PreparedStatement;

import java.util.Iterator;

import org.jboss.logging.Logger;

import org.jboss.ejb.plugins.jaws.metadata.CMPFieldMetaData;

/**
 * JAWSPersistenceManager JDBCFindByCommand
 *
 * @see <related>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.5 $
 */
public class JDBCFindByCommand extends JDBCFinderCommand
{
   // Attributes ----------------------------------------------------
   
   // The meta-info for the field we are finding by
   private CMPFieldMetaData cmpField;
   
   // Constructors --------------------------------------------------
   
   public JDBCFindByCommand(JDBCCommandFactory factory, Method finderMethod)
      throws IllegalArgumentException
   {
      super(factory, finderMethod.getName());
      
      String cmpFieldName = finderMethod.getName().substring(6).toLowerCase();
      Logger.log("Finder:"+cmpFieldName);
      
      // Find the meta-info for the field we want to find by
      
      cmpField = null;
      Iterator iter = jawsEntity.getCMPFields();
      
      while (cmpField == null && iter.hasNext())
      {
         CMPFieldMetaData fi = (CMPFieldMetaData)iter.next();
         
         if (cmpFieldName.equals(fi.getName().toLowerCase()))
         {
            cmpField = fi;
         }
      }
      
      if (cmpField == null)
      {
         throw new IllegalArgumentException(
            "No finder for this method: " + finderMethod.getName());
      }
      
      // Compute SQL
      
      String sql = "SELECT " + getPkColumnList() +
                   " FROM "+jawsEntity.getTableName()+ " WHERE ";
      
      sql += cmpField.getColumnName() + "=?";
      
      setSQL(sql);
   }
   
   // JDBCFinderCommand overrides -----------------------------------
   
   protected void setParameters(PreparedStatement stmt, Object argOrArgs) 
      throws Exception
   {
      Object[] args = (Object[])argOrArgs;
      
      if (cmpField != null)
      {
         setParameter(stmt, 1, cmpField.getJDBCType(), args[0]);
      }
   }
}
