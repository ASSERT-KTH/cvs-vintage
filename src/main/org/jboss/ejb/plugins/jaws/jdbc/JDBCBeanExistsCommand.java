/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.util.Iterator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.jaws.MetaInfo;
import org.jboss.ejb.plugins.jaws.PkFieldInfo;
import org.jboss.logging.Log;

/**
 * JDBCBeanExistsCommand
 *
 * @see <related>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.1 $
 */
public class JDBCBeanExistsCommand extends JDBCQueryCommand
{
   // Attributes ----------------------------------------------------
   
   private Object idArgument;    // the id given to execute()
   
   private boolean result;       // the result to be returned by execute()
   
   // Constructors --------------------------------------------------
   
   public JDBCBeanExistsCommand(JDBCCommandFactory factory)
   {
      super(factory, "Exists");
      String sql = "SELECT COUNT(*) AS Total FROM " + metaInfo.getTableName() +
                   " WHERE " + getPkColumnWhereList();
      setSQL(sql);
   }
   
   // Public --------------------------------------------------------
   
   // Checks whether the database already holds the entity
   
   public boolean execute(Object id)
   {
      // Save argument so setParameters() can access it
      idArgument = id;
      
      // Assume bean doesn't exist; handleResult() can change this
      result = false;
      
      try
      {
         jdbcExecute();
      } catch (Exception e)
      {
         log.exception(e);
      }
      
      return result;
   }
   
   // JDBCQueryCommand overrides ------------------------------------
   
   protected void setParameters(PreparedStatement stmt) 
      throws Exception
   {
      // Primary key in WHERE-clause
      Iterator it = metaInfo.getPkFieldInfos();
      int i = 1;   // parameter index
      
      if (metaInfo.hasCompositeKey())
      {
         // Compound key
         while (it.hasNext())
         {
            PkFieldInfo pkFieldInfo = (PkFieldInfo)it.next();
            int jdbcType = pkFieldInfo.getJDBCType();
            Object value = getPkFieldValue(idArgument, pkFieldInfo);
            
            // Set this field of the key
            setParameter(stmt, i++, jdbcType, value);
         }
      } else
      {
         // We have a Field key
         // So just set that field
         PkFieldInfo pkFieldInfo = (PkFieldInfo)it.next();
         int jdbcType = pkFieldInfo.getJDBCType();
         setParameter(stmt, i, jdbcType, idArgument);
      }
   }
   
   protected void handleResult(ResultSet rs) throws Exception
   {
      if ( !rs.next() )
      {
         throw new SQLException("Unable to check for EJB in database");
      }
      int total = rs.getInt("Total");
      result = (total >= 1);
   }
}
