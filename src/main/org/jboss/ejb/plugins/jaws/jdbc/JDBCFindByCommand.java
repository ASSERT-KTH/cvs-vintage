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

import org.jboss.ejb.plugins.jaws.deployment.Finder;
import org.jboss.ejb.plugins.jaws.CMPFieldInfo;
import org.jboss.ejb.plugins.jaws.deployment.JawsCMPField;

/**
 * JAWSPersistenceManager JDBCFindByCommand
 *
 * @see <related>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.1 $
 */
public class JDBCFindByCommand extends JDBCFinderCommand
{
   // Attributes ----------------------------------------------------
   
   // The meta-info for the field we are finding by
   private CMPFieldInfo fieldInfo;
   
   // The arguments for this finder invocation
   private Object[] argsArgument;
   
   // Constructors --------------------------------------------------
   
   public JDBCFindByCommand(JDBCCommandFactory factory, Method finderMethod)
      throws IllegalArgumentException
   {
      super(factory, finderMethod.getName());
      
      String cmpFieldName = finderMethod.getName().substring(6).toLowerCase();
      System.out.println("Finder:"+cmpFieldName);
      
      // Find the meta-info for the field we want to find by
      
      fieldInfo = null;
      Iterator iter = metaInfo.getCMPFieldInfos();
      
      while (fieldInfo == null && iter.hasNext())
      {
         CMPFieldInfo fi = (CMPFieldInfo)iter.next();
         
         if (cmpFieldName.equals(fi.getName().toLowerCase()))
         {
            fieldInfo = fi;
         }
      }
      
      if (fieldInfo == null)
      {
         throw new IllegalArgumentException(
            "No finder for this method: " + finderMethod.getName());
      }
      
      // Compute SQL
      
      String sql = "SELECT " + getPkColumnList() +
                   " FROM "+metaInfo.getTableName()+ " WHERE ";
      
      if (fieldInfo.isEJBReference())
      {
         JawsCMPField[] cmpFields = fieldInfo.getForeignKeyCMPFields();
         for (int j = 0; j < cmpFields.length; j++)
         {
            sql += (j==0?"":" AND ") + 
               fieldInfo.getColumnName() + "_" + cmpFields[j].getColumnName() + "=?";
         }
      } else
      {
         sql += fieldInfo.getColumnName() + "=?";
      }
      
      setSQL(sql);
   }
   
   // JDBCFinderCommand overrides -----------------------------------
   
   protected void setParameters(PreparedStatement stmt) throws Exception
   {
      if (fieldInfo != null)
      {
         if (fieldInfo.isEJBReference())
         {
            setForeignKey(stmt, 1, fieldInfo, argsArgument[0]);
         } else
         {
            setParameter(stmt, 1, fieldInfo.getJDBCType(), argsArgument[0]);
         }
      }
   }
}
