/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.util.ArrayList;
import java.util.StringTokenizer;

import java.sql.PreparedStatement;

import org.jboss.ejb.plugins.jaws.deployment.Finder;

/**
 * JAWSPersistenceManager JDBCDefinedFinderCommand
 *
 * @see <related>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.1 $
 */
public class JDBCDefinedFinderCommand extends JDBCFinderCommand
{
   // Attributes ----------------------------------------------------
   
   private int[] parameterArray;
   
   // Constructors --------------------------------------------------
   
   public JDBCDefinedFinderCommand(JDBCCommandFactory factory, Finder f)
   {
      super(factory, f.getName());
      
      // Replace placeholders with ?
      String query = "";
      StringTokenizer finderQuery = new StringTokenizer(f.getQuery(),"{}", true);
      ArrayList parameters = new ArrayList();
      
      while (finderQuery.hasMoreTokens())
      {
         String t = finderQuery.nextToken();
         if (t.equals("{"))
         {
            query += "?";
            String idx = finderQuery.nextToken(); // Remove number
            parameters.add(new Integer(idx));
            finderQuery.nextToken(); // Remove }
         } else
            query += t;
      }
      
      // Copy index numbers to parameterArray
      parameterArray = new int[parameters.size()];
      for (int i = 0; i < parameterArray.length; i++)
         parameterArray[i] = ((Integer)parameters.get(i)).intValue();
      
      // Construct SQL
      String sql = "SELECT " + getPkColumnList() +
         (f.getOrder().equals("") ? "" : ","+f.getOrder()) + 
         " FROM " + metaInfo.getTableName() + " WHERE " + query;
      if (!f.getOrder().equals(""))
      {
         sql += " ORDER BY "+f.getOrder();
      }
      
      setSQL(sql);
   }
   
   // JDBCFinderCommand overrides ------------------------------------
   
   protected void setParameters(PreparedStatement stmt) throws Exception
   {
      for (int i = 0; i < parameterArray.length; i++)
      {
         stmt.setObject(i+1, argsArgument[parameterArray[i]]);
      }
   }
}
